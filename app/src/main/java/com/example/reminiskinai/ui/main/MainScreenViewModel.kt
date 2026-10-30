package com.example.reminiskinai.ui.main

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reminiskinai.data.ImageStorageHelper
import com.example.reminiskinai.data.UserSettingsStore
import com.example.reminiskinai.ml.AestheticPreset
import com.example.reminiskinai.ml.CachedSkinSession
import com.example.reminiskinai.ml.HairShadePreset
import com.example.reminiskinai.ml.LipShadePreset
import com.example.reminiskinai.ml.MultiModelSkinEngine
import com.example.reminiskinai.ml.PipelineStageProgress
import com.example.reminiskinai.ml.SamplePhotoItem
import com.example.reminiskinai.ml.SkinAnalysisTelemetry
import com.example.reminiskinai.ml.SkinEditParams
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EditorScreenMode {
    HOME,
    PROCESSING,
    RESULT,
    SAVED_SHARE
}

data class SampleThumb(
    val item: SamplePhotoItem,
    val bitmap: Bitmap
)

data class EditorUiState(
    val mode: EditorScreenMode = EditorScreenMode.HOME,
    val sampleThumbs: List<SampleThumb> = emptyList(),
    val selectedPreviewBitmap: Bitmap? = null,
    val selectedReminiRefBitmap: Bitmap? = null,
    val selectedTitle: String = "",
    val stageProgress: PipelineStageProgress? = null,
    val beforeBitmap: Bitmap? = null,
    val afterBitmap: Bitmap? = null,
    val editParams: SkinEditParams = SkinEditParams(),
    val settingsSavedConfirmation: Boolean = false,
    val hasCustomSavedSettings: Boolean = false,
    val telemetry: SkinAnalysisTelemetry? = null,
    val showFineTuneSliders: Boolean = false,
    val showModelArchitectureSheet: Boolean = false,
    val isSaving: Boolean = false,
    val savedImageUri: Uri? = null,
    val savedFileName: String = "",
    val errorMessage: String? = null
)

class MainScreenViewModel(
    private val appContext: Context
) : ViewModel() {

    private val engine = MultiModelSkinEngine(appContext)
    private var activeSession: CachedSkinSession? = null
    private var recomposeJob: Job? = null

    private val _uiState = MutableStateFlow(
        EditorUiState(
            editParams = UserSettingsStore.loadSavedParams(appContext),
            hasCustomSavedSettings = UserSettingsStore.hasCustomSavedSettings(appContext)
        )
    )
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    init {
        loadSamples()
    }

    private fun loadSamples() {
        viewModelScope.launch {
            val loaded = ImageStorageHelper.builtInSamples.mapNotNull { item ->
                val bmp = ImageStorageHelper.loadAssetBitmap(appContext, item.beforeAssetPath)
                if (bmp != null) SampleThumb(item, bmp) else null
            }
            val first = loaded.firstOrNull()
            val firstRef = first?.item?.reminiRefAssetPath?.let {
                ImageStorageHelper.loadAssetBitmap(appContext, it)
            }
            _uiState.update {
                it.copy(
                    sampleThumbs = loaded,
                    selectedPreviewBitmap = first?.bitmap,
                    selectedReminiRefBitmap = firstRef,
                    selectedTitle = first?.item?.title ?: ""
                )
            }
        }
    }

    fun selectSampleForPreview(sampleThumb: SampleThumb) {
        viewModelScope.launch {
            val refBmp = sampleThumb.item.reminiRefAssetPath?.let {
                ImageStorageHelper.loadAssetBitmap(appContext, it)
            }
            _uiState.update {
                it.copy(
                    selectedPreviewBitmap = sampleThumb.bitmap,
                    selectedReminiRefBitmap = refBmp,
                    selectedTitle = sampleThumb.item.title,
                    errorMessage = null
                )
            }
        }
    }

    fun closePreviewModal() {
        _uiState.update {
            it.copy(
                selectedPreviewBitmap = null,
                selectedReminiRefBitmap = null,
                selectedTitle = ""
            )
        }
    }

    fun onGalleryPhotoPicked(uri: Uri) {
        viewModelScope.launch {
            val bmp = ImageStorageHelper.loadUriBitmap(appContext, uri)
            if (bmp != null) {
                _uiState.update {
                    it.copy(
                        selectedPreviewBitmap = bmp,
                        selectedReminiRefBitmap = null,
                        selectedTitle = "Gallery Photo",
                        errorMessage = null
                    )
                }
                startEnhancement(bmp, null)
            } else {
                _uiState.update {
                    it.copy(errorMessage = "Could not load the selected image.")
                }
            }
        }
    }

    fun startEnhancementFromPreview() {
        val previewBmp = _uiState.value.selectedPreviewBitmap ?: return
        val refBmp = _uiState.value.selectedReminiRefBitmap
        startEnhancement(previewBmp, refBmp)
    }

    private fun startEnhancement(sourceBitmap: Bitmap, reminiRefBitmap: Bitmap?) {
        viewModelScope.launch {
            // Automatically load user's saved smoothness, lip color, and regional preset settings!
            val autoAppliedParams = UserSettingsStore.loadSavedParams(appContext)
            val hasCustom = UserSettingsStore.hasCustomSavedSettings(appContext)
            _uiState.update {
                it.copy(
                    mode = EditorScreenMode.PROCESSING,
                    beforeBitmap = sourceBitmap,
                    editParams = autoAppliedParams,
                    hasCustomSavedSettings = hasCustom,
                    settingsSavedConfirmation = hasCustom,
                    stageProgress = PipelineStageProgress(
                        stepIndex = 1,
                        totalSteps = 5,
                        modelName = "Upscaling to 1080p HD & Initializing 5 AI Models...",
                        stageDescription = "Preparing ${sourceBitmap.width}×${sourceBitmap.height} Full HD canvas...",
                        progressFraction = 0.05f
                    ),
                    errorMessage = null
                )
            }

            try {
                val session = engine.analyzePhoto(
                    sourceBitmap = sourceBitmap,
                    reminiRefBitmap = reminiRefBitmap,
                    onStageProgress = { progress ->
                        _uiState.update { state -> state.copy(stageProgress = progress) }
                    }
                )
                activeSession = session
                val (renderedBitmap, telemetry) = engine.renderSession(session, autoAppliedParams)

                _uiState.update {
                    it.copy(
                        mode = EditorScreenMode.RESULT,
                        beforeBitmap = sourceBitmap,
                        afterBitmap = renderedBitmap,
                        telemetry = telemetry,
                        stageProgress = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        mode = EditorScreenMode.HOME,
                        errorMessage = "AI Enhancement error: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateEditParams(transform: (SkinEditParams) -> SkinEditParams) {
        val session = activeSession ?: return
        val newParams = transform(_uiState.value.editParams)
        _uiState.update {
            it.copy(
                editParams = newParams,
                settingsSavedConfirmation = false
            )
        }

        recomposeJob?.cancel()
        recomposeJob = viewModelScope.launch {
            val (renderedBitmap, telemetry) = engine.renderSession(session, newParams)
            _uiState.update {
                it.copy(
                    afterBitmap = renderedBitmap,
                    telemetry = telemetry
                )
            }
        }
    }

    /**
     * Saves the current Skin Smoothness, Lip Color, Regional Skin Preset, and sliders
     * to SharedPreferences so they are automatically applied to every photo next time.
     */
    fun saveUserDefaultSettings() {
        val currentParams = _uiState.value.editParams
        UserSettingsStore.saveParams(appContext, currentParams)
        _uiState.update {
            it.copy(
                settingsSavedConfirmation = true,
                hasCustomSavedSettings = true
            )
        }
    }

    fun selectPreset(preset: AestheticPreset) {
        updateEditParams { it.copy(preset = preset, compareReminiRef = false) }
    }

    fun selectLipShade(shade: LipShadePreset) {
        updateEditParams { it.copy(lipShade = shade, compareReminiRef = false) }
    }

    fun selectHairShade(shade: HairShadePreset) {
        updateEditParams { it.copy(hairShade = shade, compareReminiRef = false) }
    }

    fun toggleFineTuneSliders() {
        _uiState.update { it.copy(showFineTuneSliders = !it.showFineTuneSliders) }
    }

    fun toggleModelArchitectureSheet(show: Boolean) {
        _uiState.update { it.copy(showModelArchitectureSheet = show) }
    }

    fun saveCurrentResultToGallery() {
        val bitmapToSave = _uiState.value.afterBitmap ?: return
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            // Also persist user's current smoothness & preset settings when saving
            UserSettingsStore.saveParams(appContext, _uiState.value.editParams)
            val savedResult = ImageStorageHelper.saveBitmapToGallery(appContext, bitmapToSave)
            _uiState.update {
                it.copy(
                    isSaving = false,
                    settingsSavedConfirmation = true,
                    hasCustomSavedSettings = true,
                    savedImageUri = savedResult?.uri,
                    savedFileName = savedResult?.fileName ?: "",
                    mode = if (savedResult != null) EditorScreenMode.SAVED_SHARE else it.mode,
                    errorMessage = if (savedResult == null) "Failed to save image to Gallery" else null
                )
            }
        }
    }

    fun shareSavedImage(context: Context) {
        val uri = _uiState.value.savedImageUri ?: return
        ImageStorageHelper.shareImageUri(context, uri)
    }

    fun backToHome() {
        _uiState.update {
            it.copy(
                mode = EditorScreenMode.HOME,
                showFineTuneSliders = false,
                savedImageUri = null
            )
        }
    }

    fun backToResultFromShare() {
        _uiState.update {
            it.copy(mode = EditorScreenMode.RESULT)
        }
    }

    override fun onCleared() {
        super.onCleared()
        engine.close()
    }
}
