package com.example.reminiskinai.ui.passport

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reminiskinai.data.ImageStorageHelper
import com.example.reminiskinai.ml.CachedPassportSession
import com.example.reminiskinai.ml.CoatGenderCategory
import com.example.reminiskinai.ml.CountryPassportStandard
import com.example.reminiskinai.ml.ExportLayoutMode
import com.example.reminiskinai.ml.LoadedCoatAsset
import com.example.reminiskinai.ml.PassportBgColor
import com.example.reminiskinai.ml.PassportBiometricGeometry
import com.example.reminiskinai.ml.PassportEditParams
import com.example.reminiskinai.ml.PassportPhotoEngine
import com.example.reminiskinai.ml.PipelineStageProgress
import com.example.reminiskinai.ml.TransformTargetMode
import com.example.reminiskinai.ui.main.SampleThumb
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PassportScreenMode {
    UPLOAD_HOME,
    PROCESSING,
    STUDIO_EDITOR,
    SAVED_EXPORT
}

enum class PassportStudioTab(val title: String) {
    COATS("10 Coats"),
    CROP_ALIGN("Crop & Fit"),
    BACKGROUND("Background"),
    COUNTRY_SIZE("Country Size")
}

data class PassportUiState(
    val mode: PassportScreenMode = PassportScreenMode.UPLOAD_HOME,
    val activeTab: PassportStudioTab = PassportStudioTab.COATS,
    val sampleThumbs: List<SampleThumb> = emptyList(),
    val coatAssets: List<LoadedCoatAsset> = emptyList(),
    val sourceBitmap: Bitmap? = null,
    val renderedPassportBitmap: Bitmap? = null,
    val exportedBitmap: Bitmap? = null,
    val editParams: PassportEditParams = PassportEditParams(),
    val geometry: PassportBiometricGeometry? = null,
    val stageProgress: PipelineStageProgress? = null,
    val isSaving: Boolean = false,
    val savedImageUri: Uri? = null,
    val savedFileName: String = "",
    val showCompareOriginal: Boolean = false,
    val showModelInfoDialog: Boolean = false,
    val errorMessage: String? = null
)

class PassportPhotoViewModel(
    private val appContext: Context
) : ViewModel() {

    private val engine = PassportPhotoEngine(appContext)
    private var activeSession: CachedPassportSession? = null
    private var renderJob: Job? = null

    private val _uiState = MutableStateFlow(PassportUiState())
    val uiState: StateFlow<PassportUiState> = _uiState.asStateFlow()

    init {
        loadInitialAssets()
    }

    private fun loadInitialAssets() {
        viewModelScope.launch {
            val samples = ImageStorageHelper.builtInSamples.mapNotNull { item ->
                val bmp = ImageStorageHelper.loadAssetBitmap(appContext, item.beforeAssetPath)
                if (bmp != null) SampleThumb(item, bmp) else null
            }
            val coats = engine.loadCoatAssets()
            _uiState.update {
                it.copy(
                    sampleThumbs = samples,
                    coatAssets = coats
                )
            }
        }
    }

    fun onGalleryPhotoPicked(uri: Uri) {
        viewModelScope.launch {
            val bmp = ImageStorageHelper.loadUriBitmap(appContext, uri)
            if (bmp != null) {
                startPassportProcessing(bmp)
            } else {
                _uiState.update {
                    it.copy(errorMessage = "Could not load selected image.")
                }
            }
        }
    }

    fun onSamplePhotoSelected(sample: SampleThumb) {
        startPassportProcessing(sample.bitmap)
    }

    fun startPassportProcessing(bitmap: Bitmap) {
        viewModelScope.launch {
            // Ensure coats are loaded if not yet cached
            if (_uiState.value.coatAssets.isEmpty()) {
                val coats = engine.loadCoatAssets()
                _uiState.update { it.copy(coatAssets = coats) }
            }

            val defaultParams = PassportEditParams(
                countryStandard = CountryPassportStandard.US_2X2,
                bgColor = PassportBgColor.PURE_WHITE,
                selectedCoatId = null // Default to 100% authentic existing clothes fit
            )

            _uiState.update {
                it.copy(
                    mode = PassportScreenMode.PROCESSING,
                    sourceBitmap = bitmap,
                    editParams = defaultParams,
                    stageProgress = PipelineStageProgress(
                        stepIndex = 1,
                        totalSteps = 4,
                        modelName = "Initializing Passport AI Studio...",
                        stageDescription = "Loading Hivision MODNet (ONNX) + BiSeNet + MediaPipe...",
                        progressFraction = 0.08f
                    ),
                    errorMessage = null
                )
            }

            try {
                val session = engine.analyzePortrait(
                    sourceBitmap = bitmap,
                    onStageProgress = { progress ->
                        _uiState.update { state -> state.copy(stageProgress = progress) }
                    }
                )
                activeSession = session
                val rendered = engine.renderPassportPhoto(session, defaultParams)

                _uiState.update {
                    it.copy(
                        mode = PassportScreenMode.STUDIO_EDITOR,
                        sourceBitmap = bitmap,
                        renderedPassportBitmap = rendered,
                        geometry = session.geometry,
                        stageProgress = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        mode = PassportScreenMode.UPLOAD_HOME,
                        errorMessage = "Passport processing failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun selectStudioTab(tab: PassportStudioTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun updateParams(transform: (PassportEditParams) -> PassportEditParams) {
        val session = activeSession ?: return
        val newParams = transform(_uiState.value.editParams)
        _uiState.update { it.copy(editParams = newParams) }

        renderJob?.cancel()
        renderJob = viewModelScope.launch {
            val rendered = engine.renderPassportPhoto(session, newParams)
            _uiState.update {
                it.copy(renderedPassportBitmap = rendered)
            }
        }
    }

    fun selectCoat(coatId: String?) {
        updateParams { it.copy(selectedCoatId = coatId) }
    }

    fun selectGenderFilter(category: CoatGenderCategory) {
        _uiState.update { state ->
            state.copy(editParams = state.editParams.copy(genderFilter = category))
        }
    }

    fun selectBgColor(color: PassportBgColor) {
        updateParams { it.copy(bgColor = color) }
    }

    fun selectCountryStandard(standard: CountryPassportStandard) {
        updateParams { it.copy(countryStandard = standard) }
    }

    fun selectTransformTarget(target: TransformTargetMode) {
        _uiState.update { state ->
            state.copy(editParams = state.editParams.copy(transformTarget = target))
        }
    }

    fun selectExportLayoutMode(layoutMode: ExportLayoutMode) {
        _uiState.update { state ->
            state.copy(editParams = state.editParams.copy(exportLayoutMode = layoutMode))
        }
    }

    /**
     * Handles interactive pinch-zoom, pan-drag, and two-finger rotation directly on the passport preview canvas.
     */
    fun onCanvasTransformGesture(
        panNormalizedX: Float,
        panNormalizedY: Float,
        zoomChange: Float,
        rotationChangeDeg: Float
    ) {
        val current = _uiState.value.editParams
        if (current.transformTarget == TransformTargetMode.PHOTO) {
            updateParams {
                it.copy(
                    photoScale = (it.photoScale * zoomChange).coerceIn(0.55f, 1.85f),
                    photoOffsetXNorm = (it.photoOffsetXNorm + panNormalizedX).coerceIn(-0.45f, 0.45f),
                    photoOffsetYNorm = (it.photoOffsetYNorm + panNormalizedY).coerceIn(-0.45f, 0.45f),
                    photoRotationDeg = (it.photoRotationDeg + rotationChangeDeg).coerceIn(-25f, 25f)
                )
            }
        } else {
            updateParams {
                it.copy(
                    coatScale = (it.coatScale * zoomChange).coerceIn(0.65f, 1.50f),
                    coatOffsetXNorm = (it.coatOffsetXNorm + panNormalizedX).coerceIn(-0.30f, 0.30f),
                    coatOffsetYNorm = (it.coatOffsetYNorm + panNormalizedY).coerceIn(-0.30f, 0.30f),
                    coatRotationDeg = (it.coatRotationDeg + rotationChangeDeg).coerceIn(-15f, 15f)
                )
            }
        }
    }

    fun resetPhotoAlignmentToAuto() {
        updateParams {
            it.copy(
                photoScale = 1.0f,
                photoRotationDeg = 0.0f,
                photoOffsetXNorm = 0.0f,
                photoOffsetYNorm = 0.0f,
                autoLevelEyesEnabled = true
            )
        }
    }

    fun resetCoatAlignmentToAuto() {
        updateParams {
            it.copy(
                coatScale = 1.0f,
                coatRotationDeg = 0.0f,
                coatOffsetXNorm = 0.0f,
                coatOffsetYNorm = 0.0f
            )
        }
    }

    fun toggleCompareOriginal(show: Boolean) {
        _uiState.update { it.copy(showCompareOriginal = show) }
    }

    fun toggleModelInfoDialog(show: Boolean) {
        _uiState.update { it.copy(showModelInfoDialog = show) }
    }

    fun exportPassportPhoto() {
        val session = activeSession ?: return
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val params = _uiState.value.editParams
            val exportBmp = engine.renderExportBitmap(session, params)
            val savedResult = ImageStorageHelper.saveBitmapToGallery(appContext, exportBmp)

            _uiState.update {
                it.copy(
                    isSaving = false,
                    exportedBitmap = exportBmp,
                    savedImageUri = savedResult?.uri,
                    savedFileName = savedResult?.fileName ?: "",
                    mode = if (savedResult != null) PassportScreenMode.SAVED_EXPORT else it.mode,
                    errorMessage = if (savedResult == null) "Failed to export passport photo" else null
                )
            }
        }
    }

    fun shareExportedPhoto(context: Context) {
        val uri = _uiState.value.savedImageUri ?: return
        ImageStorageHelper.shareImageUri(context, uri)
    }

    fun backToStudioFromExport() {
        _uiState.update { it.copy(mode = PassportScreenMode.STUDIO_EDITOR) }
    }

    fun backToUploadHome() {
        _uiState.update {
            it.copy(
                mode = PassportScreenMode.UPLOAD_HOME,
                savedImageUri = null,
                exportedBitmap = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        engine.close()
    }
}
