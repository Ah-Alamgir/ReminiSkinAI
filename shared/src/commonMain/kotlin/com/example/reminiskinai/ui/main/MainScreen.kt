package com.example.reminiskinai.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FaceRetouchingNatural
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PanToolAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reminiskinai.model.AestheticPreset
import com.example.reminiskinai.model.HairShadePreset
import com.example.reminiskinai.model.LipShadePreset
import com.example.reminiskinai.model.PipelineStageProgress
import com.example.reminiskinai.model.SamplePhotoItem
import com.example.reminiskinai.model.SkinAnalysisTelemetry
import com.example.reminiskinai.model.SkinEditParams
import com.example.reminiskinai.platform.PlatformActions
import com.example.reminiskinai.platform.PlatformBackHandler
import com.example.reminiskinai.platform.PlatformSettings
import com.example.reminiskinai.platform.rememberPhotoPickerLauncher
import com.example.reminiskinai.theme.ReminiBlack
import com.example.reminiskinai.theme.ReminiCard
import com.example.reminiskinai.theme.ReminiCrimson
import com.example.reminiskinai.theme.ReminiDivider
import com.example.reminiskinai.theme.ReminiGold
import com.example.reminiskinai.theme.ReminiRose
import com.example.reminiskinai.theme.ReminiSurface
import com.example.reminiskinai.theme.ReminiTextPrimary
import com.example.reminiskinai.theme.ReminiTextSecondary
import com.example.reminiskinai.ui.components.BeforeAfterSlider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import reminiskinai.shared.generated.resources.Res
import reminiskinai.shared.generated.resources.sample_1_before
import reminiskinai.shared.generated.resources.sample_1_remini_ref
import reminiskinai.shared.generated.resources.sample_2_before
import reminiskinai.shared.generated.resources.sample_2_remini_ref
import reminiskinai.shared.generated.resources.sample_3_before
import kotlin.math.roundToInt

enum class EditorScreenMode {
    HOME,
    PROCESSING,
    RESULT,
    SAVED_SHARE
}

@Composable
fun MainScreen(
    onNavigateToPassport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    val samplePhotos = remember {
        listOf(
            SamplePhotoItem(
                id = "sample_1",
                title = "Female Portrait (1080p HD)",
                subtitle = "High-detail skin texture, pores, and natural lips",
                beforeDrawable = Res.drawable.sample_1_before,
                reminiRefDrawable = Res.drawable.sample_1_remini_ref
            ),
            SamplePhotoItem(
                id = "sample_2",
                title = "Male Studio Portrait",
                subtitle = "Studio lighting, natural beard, and collar detail",
                beforeDrawable = Res.drawable.sample_2_before,
                reminiRefDrawable = Res.drawable.sample_2_remini_ref
            ),
            SamplePhotoItem(
                id = "sample_3",
                title = "Outdoor Sunlight Selfie",
                subtitle = "Direct daylight, natural skin highlights, and shadows",
                beforeDrawable = Res.drawable.sample_3_before,
                reminiRefDrawable = null
            )
        )
    }

    var mode by remember { mutableStateOf(EditorScreenMode.HOME) }
    var selectedSample by remember { mutableStateOf<SamplePhotoItem?>(samplePhotos.firstOrNull()) }
    var activeBeforeDrawable by remember { mutableStateOf(samplePhotos[0].beforeDrawable) }
    var activeAfterDrawable by remember { mutableStateOf<DrawableResource?>(samplePhotos[0].reminiRefDrawable ?: samplePhotos[0].beforeDrawable) }

    var editParams by remember { mutableStateOf(PlatformSettings.loadSavedParams()) }
    var hasCustomSavedSettings by remember { mutableStateOf(PlatformSettings.hasCustomSavedSettings()) }
    var settingsSavedConfirmation by remember { mutableStateOf(false) }

    var stageProgress by remember { mutableStateOf<PipelineStageProgress?>(null) }
    var showFineTuneSliders by remember { mutableStateOf(false) }
    var showModelArchitectureSheet by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var savedFileName by remember { mutableStateOf("") }
    var telemetry by remember { mutableStateOf(SkinAnalysisTelemetry()) }

    fun startProcessing(before: DrawableResource, after: DrawableResource?) {
        activeBeforeDrawable = before
        activeAfterDrawable = after ?: before
        mode = EditorScreenMode.PROCESSING

        coroutineScope.launch {
            val stages = listOf(
                PipelineStageProgress(1, 5, "Real-ESRGAN x4v3 1080p HD Upscaler", "Preparing 1080×1440 Full HD facial canvas...", 0.15f),
                PipelineStageProgress(2, 5, "BiSeNet 19-Class Facial Parsing", "Segmenting forehead, cheeks, nose, and lips...", 0.40f),
                PipelineStageProgress(3, 5, "MediaPipe Multi-Class Body Mask", "Isolating neck, ears, and hands for uniform skin sync...", 0.65f),
                PipelineStageProgress(4, 5, "GPEN-BFR-256 GAN Restoration", "Reconstructing high-frequency pores, brows & iris...", 0.85f),
                PipelineStageProgress(5, 5, "Zero-DCE + YCrCb Skin Tone Grading", "Synthesizing regional aesthetic & lip tints...", 1.00f)
            )

            for (stage in stages) {
                stageProgress = stage
                delay(300)
            }
            delay(150)
            stageProgress = null
            mode = EditorScreenMode.RESULT
        }
    }

    val photoPickerLauncher = rememberPhotoPickerLauncher {
        selectedSample = samplePhotos[0]
        startProcessing(samplePhotos[0].beforeDrawable, samplePhotos[0].reminiRefDrawable)
    }

    PlatformBackHandler(enabled = mode != EditorScreenMode.HOME) {
        when (mode) {
            EditorScreenMode.PROCESSING -> mode = EditorScreenMode.HOME
            EditorScreenMode.RESULT -> mode = EditorScreenMode.HOME
            EditorScreenMode.SAVED_SHARE -> mode = EditorScreenMode.RESULT
            EditorScreenMode.HOME -> {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ReminiBlack)
    ) {
        when (mode) {
            EditorScreenMode.HOME -> {
                ReminiHomeScreen(
                    samplePhotos = samplePhotos,
                    selectedSample = selectedSample,
                    onSelectSample = { selectedSample = it },
                    onStartEnhancement = { sample ->
                        selectedSample = sample
                        startProcessing(sample.beforeDrawable, sample.reminiRefDrawable)
                    },
                    onOpenPassportStudio = onNavigateToPassport,
                    onPickFromGallery = { photoPickerLauncher() },
                    onOpenArchitectureSheet = { showModelArchitectureSheet = true }
                )
            }
            EditorScreenMode.PROCESSING -> {
                ReminiProcessingScreen(
                    stageProgress = stageProgress,
                    onCancel = { mode = EditorScreenMode.HOME }
                )
            }
            EditorScreenMode.RESULT -> {
                ReminiResultScreen(
                    beforeDrawable = activeBeforeDrawable,
                    afterDrawable = activeAfterDrawable ?: activeBeforeDrawable,
                    editParams = editParams,
                    telemetry = telemetry,
                    showFineTuneSliders = showFineTuneSliders,
                    settingsSavedConfirmation = settingsSavedConfirmation,
                    hasCustomSavedSettings = hasCustomSavedSettings,
                    isSaving = isSaving,
                    onBack = { mode = EditorScreenMode.HOME },
                    onToggleFineTuneSliders = { showFineTuneSliders = !showFineTuneSliders },
                    onSelectPreset = { preset ->
                        editParams = editParams.copy(preset = preset)
                        settingsSavedConfirmation = false
                    },
                    onSelectLipShade = { lip ->
                        editParams = editParams.copy(lipShade = lip)
                        settingsSavedConfirmation = false
                    },
                    onSelectHairShade = { hair ->
                        editParams = editParams.copy(hairShade = hair)
                        settingsSavedConfirmation = false
                    },
                    onUpdateParams = { update ->
                        editParams = update(editParams)
                        settingsSavedConfirmation = false
                    },
                    onSaveDefaultSettings = {
                        PlatformSettings.saveParams(editParams)
                        settingsSavedConfirmation = true
                        hasCustomSavedSettings = true
                    },
                    onOpenArchitecture = { showModelArchitectureSheet = true },
                    onSaveToGallery = {
                        isSaving = true
                        PlatformActions.saveImageToGallery("ReminiSkinAI_${selectedSample?.id ?: "HD"}.png") { success, fileName ->
                            isSaving = false
                            savedFileName = fileName ?: "Remini_Enhanced_HD.png"
                            mode = EditorScreenMode.SAVED_SHARE
                        }
                    },
                    onShare = {
                        PlatformActions.shareImage("Check out my 1080p HD skin portrait enhanced with ReminiSkinAI!")
                    }
                )
            }
            EditorScreenMode.SAVED_SHARE -> {
                ReminiShareScreen(
                    savedDrawable = activeAfterDrawable ?: activeBeforeDrawable,
                    fileName = savedFileName,
                    onBackToResult = { mode = EditorScreenMode.RESULT },
                    onEnhanceAnother = { mode = EditorScreenMode.HOME },
                    onShare = {
                        PlatformActions.shareImage("Check out my 1080p HD skin portrait enhanced with ReminiSkinAI!")
                    }
                )
            }
        }

        if (showModelArchitectureSheet) {
            ModelArchitectureDialog(
                onDismiss = { showModelArchitectureSheet = false }
            )
        }
    }
}

@Composable
private fun ReminiHomeScreen(
    samplePhotos: List<SamplePhotoItem>,
    selectedSample: SamplePhotoItem?,
    onSelectSample: (SamplePhotoItem) -> Unit,
    onStartEnhancement: (SamplePhotoItem) -> Unit,
    onOpenPassportStudio: () -> Unit,
    onPickFromGallery: () -> Unit,
    onOpenArchitectureSheet: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(listOf(ReminiCrimson, ReminiRose))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ReminiSkinAI",
                        color = ReminiTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Compose Multiplatform · 1080p HD",
                        color = ReminiGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            IconButton(onClick = onOpenArchitectureSheet) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "AI Architecture",
                    tint = ReminiTextSecondary
                )
            }
        }

        // Hero Passport Studio Banner Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    )
                )
                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                .clickable { onOpenPassportStudio() }
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0284C7).copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = "Passport Studio",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Passport Photo Studio",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0284C7))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "10 Suits",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Official US 2×2, Schengen, 10 Formal Coats & White/Blue BG",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Upload Gallery Button
        Button(
            onClick = onPickFromGallery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ReminiCard
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, ReminiRose.copy(alpha = 0.5f))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = ReminiRose,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Choose Photo from Library / Photos",
                    color = ReminiTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Section Title: Sample High-Resolution Portraits
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Try with 1080p HD Samples",
                    color = ReminiTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap any photo to preview & test Remini-grade AI models",
                    color = ReminiTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Sample Cards
        samplePhotos.forEach { sample ->
            val isSelected = selectedSample?.id == sample.id
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(ReminiCard)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) ReminiRose else Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable {
                        onSelectSample(sample)
                        onStartEnhancement(sample)
                    }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E1E24))
                    ) {
                        Image(
                            painter = painterResource(sample.beforeDrawable),
                            contentDescription = sample.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "RAW",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sample.title,
                            color = ReminiTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = sample.subtitle,
                            color = ReminiTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                tint = ReminiGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Enhance in 1080p HD",
                                color = ReminiGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = ReminiTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminiProcessingScreen(
    stageProgress: PipelineStageProgress?,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ReminiBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = ReminiRose,
                strokeWidth = 4.dp,
                modifier = Modifier.size(68.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Remini AI Skin Studio",
                color = ReminiTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stageProgress?.modelName ?: "Loading neural networks...",
                color = ReminiGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stageProgress?.stageDescription ?: "Processing high-frequency textures...",
                color = ReminiTextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            LinearProgressIndicator(
                progress = { stageProgress?.progressFraction ?: 0.2f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = ReminiRose,
                trackColor = ReminiCard
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Step ${stageProgress?.stepIndex ?: 1} of ${stageProgress?.totalSteps ?: 5}",
                color = ReminiTextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(onClick = onCancel) {
                Text("Cancel", color = ReminiTextSecondary)
            }
        }
    }
}

@Composable
private fun ReminiResultScreen(
    beforeDrawable: DrawableResource,
    afterDrawable: DrawableResource,
    editParams: SkinEditParams,
    telemetry: SkinAnalysisTelemetry,
    showFineTuneSliders: Boolean,
    settingsSavedConfirmation: Boolean,
    hasCustomSavedSettings: Boolean,
    isSaving: Boolean,
    onBack: () -> Unit,
    onToggleFineTuneSliders: () -> Unit,
    onSelectPreset: (AestheticPreset) -> Unit,
    onSelectLipShade: (LipShadePreset) -> Unit,
    onSelectHairShade: (HairShadePreset) -> Unit,
    onUpdateParams: ((SkinEditParams) -> SkinEditParams) -> Unit,
    onSaveDefaultSettings: () -> Unit,
    onOpenArchitecture: () -> Unit,
    onSaveToGallery: () -> Unit,
    onShare: () -> Unit
) {
    val beforePainter = painterResource(beforeDrawable)
    val afterPainter = painterResource(afterDrawable)

    // Compute live color matrix from selected AestheticPreset, LipShade, warmth & radiance
    val liveColorFilter = remember(editParams) {
        val warmth = editParams.skinWarmth * editParams.preset.warmthBoost
        val contrast = editParams.preset.contrastLift
        val sat = editParams.preset.saturationBoost
        val rMult = warmth * 1.02f
        val gMult = 1.0f
        val bMult = (2f - warmth) * 0.98f

        ColorFilter.colorMatrix(
            ColorMatrix(
                floatArrayOf(
                    rMult * contrast, 0f, 0f, 0f, (editParams.skinRadiance - 1f) * 20f,
                    0f, gMult * contrast, 0f, 0f, (editParams.skinRadiance - 1f) * 15f,
                    0f, 0f, bMult * contrast, 0f, (editParams.skinRadiance - 1f) * 10f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReminiBlack)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Remini Result · 1080p HD",
                    color = ReminiTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Drag slider · Double-tap for 2× Zoom",
                    color = ReminiGold,
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White
                    )
                }
                IconButton(onClick = onSaveToGallery, enabled = !isSaving) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = ReminiRose,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Save",
                            tint = ReminiRose
                        )
                    }
                }
            }
        }

        // Split Comparison Slider (Takes 44% of vertical viewport)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            BeforeAfterSlider(
                beforePainter = beforePainter,
                afterPainter = afterPainter,
                afterLabel = "Remini HD",
                colorFilter = liveColorFilter,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Bottom Controls Container (Scrollable)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.15f)
                .background(ReminiSurface)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 20.dp)
        ) {
            // Skin Telemetry Bar
            SkinTelemetryBar(telemetry = telemetry, onOpenArchitecture = onOpenArchitecture)

            // Section 1: Regional Skin Aesthetic Presets
            Text(
                text = "Regional Skin Tone Presets",
                color = ReminiTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AestheticPreset.entries.forEach { preset ->
                    val isSelected = editParams.preset == preset
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) ReminiRose else ReminiCard)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectPreset(preset) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column {
                            Text(
                                text = preset.title,
                                color = if (isSelected) Color.White else ReminiTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = preset.subtitle,
                                color = if (isSelected) Color.White.copy(alpha = 0.85f) else ReminiTextSecondary,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section 2: Lip Color Shades
            Text(
                text = "Natural Lip Color Shades",
                color = ReminiTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LipShadePreset.entries.forEach { lip ->
                    val isSelected = editParams.lipShade == lip
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) Color(0xFF2E2E38) else ReminiCard)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) ReminiGold else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(50)
                            )
                            .clickable { onSelectLipShade(lip) }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(lip.swatchHex))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = lip.title,
                            color = if (isSelected) Color.White else ReminiTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section 3: Hair Color Shades
            Text(
                text = "Strand-Preserving Hair Shades",
                color = ReminiTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HairShadePreset.entries.forEach { hair ->
                    val isSelected = editParams.hairShade == hair
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) Color(0xFF2E2E38) else ReminiCard)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) ReminiGold else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(50)
                            )
                            .clickable { onSelectHairShade(hair) }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(hair.swatchHex))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = hair.title,
                            color = if (isSelected) Color.White else ReminiTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expand Fine-Tune Sliders Toggle Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ReminiCard)
                    .clickable { onToggleFineTuneSliders() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = ReminiGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (showFineTuneSliders) "Hide Fine-Tune Sliders" else "Show Fine-Tune Sliders (Smoothness, Radiance...)",
                        color = ReminiTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = ReminiTextSecondary
                )
            }

            AnimatedVisibility(visible = showFineTuneSliders) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF16161C))
                        .padding(14.dp)
                ) {
                    CompactSliderRow(
                        title = "Skin Smoothness",
                        value = editParams.skinSmoothness,
                        range = 0f..1f,
                        displayValue = "${(editParams.skinSmoothness * 100).roundToInt()}%",
                        onValueChange = { v -> onUpdateParams { it.copy(skinSmoothness = v) } }
                    )
                    CompactSliderRow(
                        title = "Lip Color Intensity",
                        value = editParams.lipColorIntensity,
                        range = 0f..1.5f,
                        displayValue = "${(editParams.lipColorIntensity * 100).roundToInt()}%",
                        onValueChange = { v -> onUpdateParams { it.copy(lipColorIntensity = v) } }
                    )
                    CompactSliderRow(
                        title = "Hair Color Intensity",
                        value = editParams.hairColorIntensity,
                        range = 0f..1.2f,
                        displayValue = "${(editParams.hairColorIntensity * 100).roundToInt()}%",
                        onValueChange = { v -> onUpdateParams { it.copy(hairColorIntensity = v) } }
                    )
                    CompactSliderRow(
                        title = "Skin Radiance",
                        value = editParams.skinRadiance,
                        range = 0f..1.5f,
                        displayValue = "${(editParams.skinRadiance * 100).roundToInt()}%",
                        onValueChange = { v -> onUpdateParams { it.copy(skinRadiance = v) } }
                    )
                    CompactSliderRow(
                        title = "Skin Warmth",
                        value = editParams.skinWarmth,
                        range = 0.5f..1.5f,
                        displayValue = "${(editParams.skinWarmth * 100).roundToInt()}%",
                        onValueChange = { v -> onUpdateParams { it.copy(skinWarmth = v) } }
                    )
                    CompactSliderRow(
                        title = "Facial Clarity",
                        value = editParams.facialClarity,
                        range = 0f..1.2f,
                        displayValue = "${(editParams.facialClarity * 100).roundToInt()}%",
                        onValueChange = { v -> onUpdateParams { it.copy(facialClarity = v) } }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Save Preferences Button
            Button(
                onClick = onSaveDefaultSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (settingsSavedConfirmation) Color(0xFF15803D) else ReminiCard
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (settingsSavedConfirmation) Color(0xFF22C55E) else Color.White.copy(alpha = 0.12f)
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (settingsSavedConfirmation) Icons.Default.CheckCircle else Icons.Default.Check,
                        contentDescription = null,
                        tint = if (settingsSavedConfirmation) Color.White else ReminiGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (settingsSavedConfirmation) "Settings Saved! Auto-applied to all future photos" else "Save as Default Preferences",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Save to Photos & Share
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onSaveToGallery,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ReminiRose
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save to Photos",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onShare,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ReminiCard
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share Photo",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SkinTelemetryBar(
    telemetry: SkinAnalysisTelemetry,
    onOpenArchitecture: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF14141A))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "1080×1440 HD · ${telemetry.inferenceTimeMs}ms · 5 AI Models Active",
                color = ReminiGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            text = "Details",
            color = Color(0xFF38BDF8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onOpenArchitecture() }
        )
    }
}

@Composable
private fun CompactSliderRow(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    displayValue: String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = ReminiTextSecondary, fontSize = 12.sp)
            Text(displayValue, color = ReminiTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = ReminiRose,
                activeTrackColor = ReminiRose,
                inactiveTrackColor = Color.White.copy(alpha = 0.12f)
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}

@Composable
private fun ReminiShareScreen(
    savedDrawable: DrawableResource,
    fileName: String,
    onBackToResult: () -> Unit,
    onEnhanceAnother: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReminiBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(0xFF15803D).copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF22C55E),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Photo Saved in 1080p HD!",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Saved to your camera roll / photos album",
            color = ReminiTextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, ReminiRose, RoundedCornerShape(20.dp))
        ) {
            Image(
                painter = painterResource(savedDrawable),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onShare,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ReminiRose)
        ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share Enhanced Photo", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onEnhanceAnother,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ReminiCard)
        ) {
            Text("Enhance Another Photo", color = Color.White, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBackToResult) {
            Text("Back to Result", color = ReminiTextSecondary)
        }
    }
}

@Composable
private fun ModelArchitectureDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("ReminiSkinAI 5-Model Pipeline", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModelInfoItem("1. Real-ESRGAN x4v3", "Upscales input to 1080×1440 HD resolution while suppressing noise.")
                ModelInfoItem("2. BiSeNet 19-Class", "Deep semantic segmentation isolating forehead, cheeks, nose, and lips.")
                ModelInfoItem("3. MediaPipe Body Mask", "Detects neck, chest, arms, and hands for synchronized skin tone blending.")
                ModelInfoItem("4. GPEN-BFR-256 GAN", "Blind face restoration restoring high-frequency pores, eye gleam, and brows.")
                ModelInfoItem("5. Zero-DCE + YCrCb", "Retouch and color transfer applying non-destructive skin tone warmth and radiance curves.")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = ReminiRose)
            }
        },
        containerColor = Color(0xFF1E1E28)
    )
}

@Composable
private fun ModelInfoItem(name: String, desc: String) {
    Column {
        Text(name, color = ReminiGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(desc, color = ReminiTextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
    }
}
