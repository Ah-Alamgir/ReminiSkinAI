package com.example.reminiskinai.ui.main

import com.example.reminiskinai.processor.SkinToneProcessor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BlurOff
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.reminiskinai.theme.AuraCyanGradient
import com.example.reminiskinai.theme.AuraExportButtonGradient
import com.example.reminiskinai.theme.AuraGlassBorderFade
import com.example.reminiskinai.theme.AuraGlassBorderLight
import com.example.reminiskinai.theme.AuraGlassBorderSubtle
import com.example.reminiskinai.theme.AuraGlassCardBg
import com.example.reminiskinai.theme.AuraHeroButtonGradient
import com.example.reminiskinai.theme.AuraOnPrimary
import com.example.reminiskinai.theme.AuraOnPrimaryContainer
import com.example.reminiskinai.theme.AuraOnSecondary
import com.example.reminiskinai.theme.AuraOnSecondaryContainer
import com.example.reminiskinai.theme.AuraOnSurface
import com.example.reminiskinai.theme.AuraOnSurfaceVariant
import com.example.reminiskinai.theme.AuraOutline
import com.example.reminiskinai.theme.AuraOutlineVariant
import com.example.reminiskinai.theme.AuraPrimary
import com.example.reminiskinai.theme.AuraPrimaryContainer
import com.example.reminiskinai.theme.AuraPrimaryElectric
import com.example.reminiskinai.theme.AuraSecondary
import com.example.reminiskinai.theme.AuraSecondaryContainer
import com.example.reminiskinai.theme.AuraSecondaryLuminous
import com.example.reminiskinai.theme.AuraSurface
import com.example.reminiskinai.theme.AuraSurfaceBright
import com.example.reminiskinai.theme.AuraSurfaceContainer
import com.example.reminiskinai.theme.AuraSurfaceContainerHigh
import com.example.reminiskinai.theme.AuraSurfaceContainerHighest
import com.example.reminiskinai.theme.AuraSurfaceContainerLow
import com.example.reminiskinai.theme.AuraSurfaceLowest
import com.example.reminiskinai.theme.AuraTertiary
import com.example.reminiskinai.theme.AuraTertiaryContainer
import com.example.reminiskinai.theme.AuraTertiaryFixed
import com.example.reminiskinai.theme.AuraTertiaryHotMagenta
import com.example.reminiskinai.theme.AuraVioletCyanGradient
import com.example.reminiskinai.ui.components.BeforeAfterSlider
import com.example.reminiskinai.ui.components.Export4kModal
import com.example.reminiskinai.ui.components.ExportResolution
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

enum class EditorScreenMode {
    HOME,
    COMPARISON_STUDIO,
    RETOUCH_SUITE,
    PROCESSING,
    SAVED_SHARE
}

enum class AuraBottomNavTab {
    HOME,
    STUDIO,
    RESTORE,
    PROJECTS
}

enum class RetouchSuiteTab {
    RESTORE,
    RETOUCH,
    COLORIZE,
    BACKGROUND
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
                title = "Portrait_V2_4K.jpg",
                subtitle = "Crystal sharp face detail & eye catchlights",
                beforeDrawable = Res.drawable.sample_1_before,
                reminiRefDrawable = Res.drawable.sample_1_remini_ref
            ),
            SamplePhotoItem(
                id = "sample_2",
                title = "Family_Archive_1974.png",
                subtitle = "Scratch removal, unblur & vintage repair",
                beforeDrawable = Res.drawable.sample_2_before,
                reminiRefDrawable = Res.drawable.sample_2_remini_ref
            ),
            SamplePhotoItem(
                id = "sample_3",
                title = "Outdoor_Sunlight_HD.jpg",
                subtitle = "Direct daylight, specular skin highlights",
                beforeDrawable = Res.drawable.sample_3_before,
                reminiRefDrawable = null
            )
        )
    }

    var mode by remember { mutableStateOf(EditorScreenMode.HOME) }
    var selectedSample by remember { mutableStateOf<SamplePhotoItem?>(samplePhotos.firstOrNull()) }
    var activeBeforeDrawable by remember { mutableStateOf(samplePhotos[0].beforeDrawable) }
    var activeAfterDrawable by remember { mutableStateOf<DrawableResource?>(samplePhotos[0].reminiRefDrawable ?: samplePhotos[0].beforeDrawable) }

    var userCredits by remember { mutableIntStateOf(48) }
    var activeBottomTab by remember { mutableStateOf(AuraBottomNavTab.HOME) }

    var editParams by remember { mutableStateOf(PlatformSettings.loadSavedParams()) }
    var hasCustomSavedSettings by remember { mutableStateOf(PlatformSettings.hasCustomSavedSettings()) }
    var settingsSavedConfirmation by remember { mutableStateOf(false) }

    var stageProgress by remember { mutableStateOf<PipelineStageProgress?>(null) }
    var targetModeAfterProcessing by remember { mutableStateOf(EditorScreenMode.COMPARISON_STUDIO) }

    // Dialogs & Modal Sheets
    var showExportModal by remember { mutableStateOf(false) }
    var showCreditsDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAllToolsDialog by remember { mutableStateOf(false) }
    var showProjectsDialog by remember { mutableStateOf(false) }
    var showModelArchitectureSheet by remember { mutableStateOf(false) }

    // Export Feedback
    var isSaving by remember { mutableStateOf(false) }
    var savedFileName by remember { mutableStateOf("") }
    var telemetry by remember { mutableStateOf(SkinAnalysisTelemetry()) }

    fun startProcessing(
        before: DrawableResource,
        after: DrawableResource?,
        targetMode: EditorScreenMode = EditorScreenMode.COMPARISON_STUDIO,
        presetMode: AestheticPreset? = null
    ) {
        if (presetMode != null) {
            editParams = editParams.copy(preset = presetMode)
        }
        activeBeforeDrawable = before
        activeAfterDrawable = after ?: before
        targetModeAfterProcessing = targetMode
        mode = EditorScreenMode.PROCESSING

        coroutineScope.launch {
            val stages = listOf(
                PipelineStageProgress(1, 5, "Face Reconstruction v4.2", "Upscaling facial canvas to Ultra 4K UHD...", 0.20f),
                PipelineStageProgress(2, 5, "BiSeNet 19-Class Facial Parsing", "Segmenting forehead, cheeks, eyes, and lips...", 0.45f),
                PipelineStageProgress(3, 5, "Scratch Removal & Denoise", "Eliminating film grain, scratches & night noise...", 0.68f),
                PipelineStageProgress(4, 5, "GPEN-BFR-256 GAN Texture Engine", "Reconstructing high-frequency pores, iris catchlights...", 0.88f),
                PipelineStageProgress(5, 5, "Zero-DCE + YCrCb Studio Radiance", "Synthesizing studio lighting & regional tones...", 1.00f)
            )

            for (stage in stages) {
                stageProgress = stage
                delay(220)
            }
            delay(100)
            stageProgress = null
            mode = targetModeAfterProcessing
        }
    }

    var userCustomOriginalBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var userCustomEnhancedBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var userCustomGradedBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    var pendingTargetMode by remember { mutableStateOf(EditorScreenMode.COMPARISON_STUDIO) }
    var pendingPreset by remember { mutableStateOf(AestheticPreset.REMINI_NATURAL) }

    val photoPickerLauncher = rememberPhotoPickerLauncher { picked ->
        if (picked != null) {
            coroutineScope.launch {
                val enhanced = SkinToneProcessor.enhancePhoto(picked)
                val graded = SkinToneProcessor.applySkinToneGrading(
                    source = enhanced,
                    preset = pendingPreset,
                    warmth = editParams.skinWarmth,
                    radiance = editParams.skinRadiance
                )
                userCustomOriginalBitmap = picked
                userCustomEnhancedBitmap = enhanced
                userCustomGradedBitmap = graded
                editParams = editParams.copy(preset = pendingPreset)
                startProcessing(
                    before = samplePhotos[0].beforeDrawable,
                    after = samplePhotos[0].reminiRefDrawable,
                    targetMode = pendingTargetMode,
                    presetMode = pendingPreset
                )
            }
        }
    }

    fun triggerPhotoPicker(targetMode: EditorScreenMode, preset: AestheticPreset = AestheticPreset.REMINI_NATURAL) {
        pendingTargetMode = targetMode
        pendingPreset = preset
        photoPickerLauncher()
    }

    // Dynamic Skin-Gated Tone Grading: When user tweaks preset, warmth, or radiance,
    // recalculate graded dermis tones selectively without tinting clothes, hair, or background!
    LaunchedEffect(editParams.preset, editParams.skinWarmth, editParams.skinRadiance, userCustomEnhancedBitmap) {
        val base = userCustomEnhancedBitmap
        if (base != null) {
            userCustomGradedBitmap = SkinToneProcessor.applySkinToneGrading(
                source = base,
                preset = editParams.preset,
                warmth = editParams.skinWarmth,
                radiance = editParams.skinRadiance
            )
        }
    }

    PlatformBackHandler(enabled = mode != EditorScreenMode.HOME) {
        when (mode) {
            EditorScreenMode.PROCESSING -> mode = EditorScreenMode.HOME
            EditorScreenMode.COMPARISON_STUDIO -> mode = EditorScreenMode.HOME
            EditorScreenMode.RETOUCH_SUITE -> mode = EditorScreenMode.HOME
            EditorScreenMode.SAVED_SHARE -> mode = EditorScreenMode.COMPARISON_STUDIO
            EditorScreenMode.HOME -> {}
        }
    }

    // Global color filter is null to prevent tinting non-skin areas;
    // Remini regional skin grading is applied selectively by SkinToneProcessor
    val liveColorFilter: ColorFilter? = null

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuraSurfaceLowest)
    ) {
        // Deep Atmospheric OLED Glow Meshes
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Top-left Electric Violet blur
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AuraPrimaryElectric.copy(alpha = 0.20f), Color.Transparent),
                    center = Offset(-40f, -40f),
                    radius = size.width * 0.85f
                ),
                center = Offset(-40f, -40f),
                radius = size.width * 0.85f
            )
            // Mid-right Luminous Cyan glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AuraSecondaryContainer.copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(size.width + 60f, size.height * 0.28f),
                    radius = size.width * 0.80f
                ),
                center = Offset(size.width + 60f, size.height * 0.28f),
                radius = size.width * 0.80f
            )
            // Lower Hot Magenta glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AuraTertiaryContainer.copy(alpha = 0.14f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.65f),
                    radius = size.width * 0.75f
                ),
                center = Offset(size.width * 0.5f, size.height * 0.65f),
                radius = size.width * 0.75f
            )
        }

        // Active Screen Router
        when (mode) {
            EditorScreenMode.HOME -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 100.dp)
                    ) {
                        // Main Content (Title bar removed for edge-to-edge immersive studio)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(22.dp)
                        ) {
                            // Hero Headline, Avatars, Credits & Settings
                            AuraHeroHeader(
                                avatar1 = Res.drawable.sample_1_remini_ref,
                                avatar2 = Res.drawable.sample_2_remini_ref,
                                credits = userCredits,
                                onOpenCredits = { showCreditsDialog = true },
                                onOpenSettings = { showSettingsDialog = true }
                            )

                            // Hero Feature Card: AI HD Enhance & Unblur
                            AuraHeroFeatureCard(
                                beforeDrawable = Res.drawable.sample_1_before,
                                afterDrawable = Res.drawable.sample_1_remini_ref,
                                onOneTapEnhance = {
                                    triggerPhotoPicker(EditorScreenMode.COMPARISON_STUDIO, AestheticPreset.REMINI_NATURAL)
                                }
                            )

                            // Precision Tools Bento Grid - Let user pick an image on any tool click
                            AuraPrecisionToolsSection(
                                sampleVintageDrawable = Res.drawable.sample_2_before,
                                onViewAll = { showAllToolsDialog = true },
                                onToolSelected = { toolKey ->
                                    when (toolKey) {
                                        "portrait_4k" -> {
                                            triggerPhotoPicker(EditorScreenMode.COMPARISON_STUDIO, AestheticPreset.REMINI_NATURAL)
                                        }
                                        "old_restore" -> {
                                            triggerPhotoPicker(EditorScreenMode.RETOUCH_SUITE, AestheticPreset.REMINI_NATURAL)
                                        }
                                        "denoise" -> {
                                            triggerPhotoPicker(EditorScreenMode.RETOUCH_SUITE, AestheticPreset.REMINI_NATURAL)
                                        }
                                        "face_glow" -> {
                                            triggerPhotoPicker(EditorScreenMode.RETOUCH_SUITE, AestheticPreset.KOREAN)
                                        }
                                    }
                                }
                            )

                            // Recent Restorations
                            AuraRecentRestorationsSection(
                                sample1Drawable = Res.drawable.sample_1_remini_ref,
                                sample2Drawable = Res.drawable.sample_2_remini_ref,
                                onOpenSample1 = {
                                    selectedSample = samplePhotos[0]
                                    activeBeforeDrawable = samplePhotos[0].beforeDrawable
                                    activeAfterDrawable = samplePhotos[0].reminiRefDrawable
                                    mode = EditorScreenMode.COMPARISON_STUDIO
                                },
                                onOpenSample2 = {
                                    selectedSample = samplePhotos[1]
                                    activeBeforeDrawable = samplePhotos[1].beforeDrawable
                                    activeAfterDrawable = samplePhotos[1].reminiRefDrawable
                                    mode = EditorScreenMode.COMPARISON_STUDIO
                                },
                                onBatchEnhance = {
                                    triggerPhotoPicker(EditorScreenMode.COMPARISON_STUDIO, AestheticPreset.REMINI_NATURAL)
                                },
                                onOpenHistory = {
                                    showProjectsDialog = true
                                }
                            )
                        }
                    }

                    // Floating Glass Bottom Navigation Bar
                    AuraBottomNavBar(
                        selectedTab = activeBottomTab,
                        onTabSelected = { tab ->
                            activeBottomTab = tab
                            when (tab) {
                                AuraBottomNavTab.HOME -> {
                                    mode = EditorScreenMode.HOME
                                }
                                AuraBottomNavTab.STUDIO -> {
                                    activeBeforeDrawable = samplePhotos[0].beforeDrawable
                                    activeAfterDrawable = samplePhotos[0].reminiRefDrawable
                                    mode = EditorScreenMode.COMPARISON_STUDIO
                                }
                                AuraBottomNavTab.RESTORE -> {
                                    triggerPhotoPicker(EditorScreenMode.RETOUCH_SUITE, AestheticPreset.REMINI_NATURAL)
                                }
                                AuraBottomNavTab.PROJECTS -> {
                                    showProjectsDialog = true
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                    )
                }
            }

            EditorScreenMode.COMPARISON_STUDIO -> {
                AuraComparisonStudioScreen(
                    beforeDrawable = activeBeforeDrawable,
                    afterDrawable = activeAfterDrawable ?: activeBeforeDrawable,
                    beforeImageBitmap = userCustomOriginalBitmap,
                    afterImageBitmap = userCustomGradedBitmap ?: userCustomEnhancedBitmap,
                    colorFilter = liveColorFilter,
                    editParams = editParams,
                    userCredits = userCredits,
                    onBack = { mode = EditorScreenMode.HOME },
                    onNavigateToRetouchSuite = { mode = EditorScreenMode.RETOUCH_SUITE },
                    onRegenerate = {
                        startProcessing(activeBeforeDrawable, activeAfterDrawable, EditorScreenMode.COMPARISON_STUDIO)
                    },
                    onOpenExportModal = { showExportModal = true },
                    onUpdateIntensity = { intensity ->
                        editParams = editParams.copy(facialClarity = intensity)
                    }
                )
            }

            EditorScreenMode.RETOUCH_SUITE -> {
                AuraRetouchSuiteScreen(
                    beforeDrawable = activeBeforeDrawable,
                    afterDrawable = activeAfterDrawable ?: activeBeforeDrawable,
                    beforeImageBitmap = userCustomOriginalBitmap,
                    afterImageBitmap = userCustomGradedBitmap ?: userCustomEnhancedBitmap,
                    colorFilter = liveColorFilter,
                    editParams = editParams,
                    userCredits = userCredits,
                    onBack = { mode = EditorScreenMode.HOME },
                    onNavigateToStudio = { mode = EditorScreenMode.COMPARISON_STUDIO },
                    onUpdateParams = { update ->
                        editParams = update(editParams)
                        settingsSavedConfirmation = false
                    },
                    onSelectPreset = { preset ->
                        editParams = editParams.copy(preset = preset)
                    },
                    onOpenExportModal = { showExportModal = true },
                    onOpenSettings = { showSettingsDialog = true }
                )
            }

            EditorScreenMode.PROCESSING -> {
                AuraProcessingScreen(
                    stageProgress = stageProgress,
                    onCancel = { mode = EditorScreenMode.HOME }
                )
            }

            EditorScreenMode.SAVED_SHARE -> {
                AuraSavedShareScreen(
                    savedDrawable = activeAfterDrawable ?: activeBeforeDrawable,
                    fileName = savedFileName,
                    onBackToResult = { mode = EditorScreenMode.COMPARISON_STUDIO },
                    onEnhanceAnother = { mode = EditorScreenMode.HOME },
                    onShare = {
                        PlatformActions.shareImage("Check out my ultra-sharp 4K restored portrait enhanced with Aura AI!")
                    }
                )
            }
        }

        // 4K Export Upscale Modal
        Export4kModal(
            isVisible = showExportModal,
            userCredits = userCredits,
            isSaving = isSaving,
            onDismiss = { showExportModal = false },
            onSaveToCameraRoll = { resolution, removeWatermark, preserveExif, p3Color ->
                isSaving = true
                val creditCost = resolution.creditCost
                if (userCredits >= creditCost) {
                    userCredits -= creditCost
                }
                val exportBitmap = userCustomGradedBitmap ?: userCustomEnhancedBitmap
                val fileName = "AuraAI_${resolution.title.replace(" ", "_")}"
                PlatformActions.saveImageToGallery(image = exportBitmap, fileName = fileName) { success, savedName ->
                    isSaving = false
                    showExportModal = false
                    savedFileName = savedName ?: "${fileName}.jpg"
                    mode = EditorScreenMode.SAVED_SHARE
                }
            },
            onShareSheet = {
                showExportModal = false
                PlatformActions.shareImage("Check out my ultra-sharp 4K restored portrait enhanced with Aura AI!")
            },
            onInstagramStory = {
                showExportModal = false
                PlatformActions.shareImage("My 4K portrait restored by Aura AI!")
            },
            onRawTiff = {
                isSaving = true
                val exportBitmap = userCustomGradedBitmap ?: userCustomEnhancedBitmap
                PlatformActions.saveImageToGallery(image = exportBitmap, fileName = "AuraAI_Master_8K_Raw") { success, savedName ->
                    isSaving = false
                    showExportModal = false
                    savedFileName = savedName ?: "AuraAI_Master_8K_Raw.jpg"
                    mode = EditorScreenMode.SAVED_SHARE
                }
            },
            onGetMoreCredits = {
                showExportModal = false
                showCreditsDialog = true
            }
        )

        // Refill / Credits Dialog
        if (showCreditsDialog) {
            AuraCreditsDialog(
                credits = userCredits,
                onClaimBonus = { userCredits += 10 },
                onDismiss = { showCreditsDialog = false }
            )
        }

        // Settings Dialog
        if (showSettingsDialog) {
            AuraSettingsDialog(
                onOpenArchitecture = {
                    showSettingsDialog = false
                    showModelArchitectureSheet = true
                },
                onDismiss = { showSettingsDialog = false }
            )
        }

        // Precision Tools Picker Dialog
        if (showAllToolsDialog) {
            AuraAllToolsDialog(
                onSelectTool = { toolId ->
                    showAllToolsDialog = false
                    when (toolId) {
                        "passport" -> onNavigateToPassport()
                        "portrait_4k" -> triggerPhotoPicker(EditorScreenMode.COMPARISON_STUDIO, AestheticPreset.REMINI_NATURAL)
                        "old_restore" -> triggerPhotoPicker(EditorScreenMode.RETOUCH_SUITE, AestheticPreset.REMINI_NATURAL)
                        "denoise" -> triggerPhotoPicker(EditorScreenMode.RETOUCH_SUITE, AestheticPreset.REMINI_NATURAL)
                        "beauty" -> triggerPhotoPicker(EditorScreenMode.RETOUCH_SUITE, AestheticPreset.KOREAN)
                        "custom" -> triggerPhotoPicker(EditorScreenMode.COMPARISON_STUDIO, AestheticPreset.REMINI_NATURAL)
                    }
                },
                onDismiss = { showAllToolsDialog = false }
            )
        }

        // Projects / History Dialog
        if (showProjectsDialog) {
            AuraProjectsDialog(
                onSelectProject = { index ->
                    showProjectsDialog = false
                    if (index == 0) {
                        selectedSample = samplePhotos[0]
                        activeBeforeDrawable = samplePhotos[0].beforeDrawable
                        activeAfterDrawable = samplePhotos[0].reminiRefDrawable
                        mode = EditorScreenMode.COMPARISON_STUDIO
                    } else {
                        selectedSample = samplePhotos[1]
                        activeBeforeDrawable = samplePhotos[1].beforeDrawable
                        activeAfterDrawable = samplePhotos[1].reminiRefDrawable
                        mode = EditorScreenMode.COMPARISON_STUDIO
                    }
                },
                onDismiss = { showProjectsDialog = false }
            )
        }

        // Model Architecture Sheet
        if (showModelArchitectureSheet) {
            ModelArchitectureDialog(
                onDismiss = { showModelArchitectureSheet = false }
            )
        }
    }
}

// =========================================================================
// SCREEN 1: Top App Bar
// =========================================================================
@Composable
private fun AuraTopAppBar(
    credits: Int,
    onOpenCredits: () -> Unit,
    onOpenSettings: () -> Unit
) {
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
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(AuraPrimaryElectric.copy(alpha = 0.20f))
                    .border(1.dp, AuraPrimaryElectric.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Aura AI Logo",
                    tint = AuraPrimary,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Aura AI",
                color = AuraOnSurface,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.4).sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Credits Counter Pill
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
                    .clickable { onOpenCredits() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Credits",
                    tint = AuraSecondary,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "$credits Credits",
                    color = AuraOnSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Settings Button
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = AuraOnSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

// =========================================================================
// SCREEN 1: Hero Header (Edge-to-Edge with sleek status controls)
// =========================================================================
@Composable
private fun AuraHeroHeader(
    avatar1: DrawableResource,
    avatar2: DrawableResource,
    credits: Int,
    onOpenCredits: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 36.dp)
    ) {
        // Floating Top Utility Row: Credits Pill & Settings Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Credits Pill
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
                    .clickable { onOpenCredits() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Credits",
                    tint = AuraSecondary,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "$credits Credits",
                    color = AuraOnSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Settings Icon
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = AuraOnSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Revive Your Memories",
                color = AuraOnSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                lineHeight = 34.sp
            )

            // Overlapping Avatar Circles
            Row(modifier = Modifier.offset(x = 6.dp)) {
                Image(
                    painter = painterResource(avatar1),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(2.dp, AuraSurfaceLowest, CircleShape)
                )
                Image(
                    painter = painterResource(avatar2),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .offset(x = (-10).dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(2.dp, AuraSurfaceLowest, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Transform low-res, vintage, and blurred portraits into ultra-sharp 4K masterpieces.",
            color = AuraOnSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

// =========================================================================
// SCREEN 1: Hero Feature Card (Interactive Split Preview)
// =========================================================================
@Composable
private fun AuraHeroFeatureCard(
    beforeDrawable: DrawableResource,
    afterDrawable: DrawableResource,
    onOneTapEnhance: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.28f), Color.White.copy(alpha = 0.05f))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Interactive Simulated Before & After Split Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(18.dp))
        ) {
            BeforeAfterSlider(
                beforePainter = painterResource(beforeDrawable),
                afterPainter = painterResource(afterDrawable),
                beforeLabel = "ORIGINAL BLURRY",
                afterLabel = "4K RESTORED",
                showTelemetry = false,
                modifier = Modifier.fillMaxSize()
            )

            // Tag badge on top left corner
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(CircleShape)
                    .background(AuraSurfaceLowest.copy(alpha = 0.75f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(AuraSecondary)
                    )
                    Text(
                        text = "Face Reconstruction v4.2",
                        color = AuraOnSurface,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Title & Description
        Column {
            Text(
                text = "AI HD Enhance & Unblur",
                color = AuraOnSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Reconstruct skin texture, eye catchlights, and hair strands",
                color = AuraOnSurfaceVariant,
                fontSize = 13.sp
            )
        }

        // One-Tap Magic Enhance Gradient Button with Specular Glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(CircleShape)
                .background(AuraHeroButtonGradient)
                .border(1.dp, Color.White.copy(alpha = 0.40f), CircleShape)
                .clickable { onOneTapEnhance() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
                Text(
                    text = "One-Tap Magic Enhance",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// =========================================================================
// SCREEN 1: Precision Tools Bento Grid
// =========================================================================
@Composable
private fun AuraPrecisionToolsSection(
    sampleVintageDrawable: DrawableResource,
    onViewAll: () -> Unit,
    onToolSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Precision Tools",
                color = AuraOnSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "View All",
                color = AuraSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onViewAll() }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tool 1: AI Portrait 4K
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                    .clickable { onToolSelected("portrait_4k") }
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AuraPrimaryElectric.copy(alpha = 0.15f))
                                .border(1.dp, AuraPrimaryElectric.copy(alpha = 0.30f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CenterFocusStrong,
                                contentDescription = null,
                                tint = AuraPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AuraSecondary.copy(alpha = 0.12f))
                                .border(1.dp, AuraSecondary.copy(alpha = 0.30f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "HD 4K",
                                color = AuraSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "AI Portrait 4K",
                        color = AuraOnSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Crystal sharp face detail",
                        color = AuraOnSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Tool 2: Old Photo Restore
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                    .clickable { onToolSelected("old_restore") }
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                painter = painterResource(sampleVintageDrawable),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(AuraPrimaryElectric.copy(alpha = 0.20f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AuraTertiaryContainer.copy(alpha = 0.20f))
                                .border(1.dp, AuraTertiaryContainer.copy(alpha = 0.30f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Vintage",
                                color = AuraTertiaryFixed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Old Photo Restore",
                        color = AuraOnSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Scratch removal & unblur",
                        color = AuraOnSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tool 3: Despeckle & Denoise
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                    .clickable { onToolSelected("denoise") }
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AuraSecondary.copy(alpha = 0.15f))
                                .border(1.dp, AuraSecondary.copy(alpha = 0.30f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Grain,
                                contentDescription = null,
                                tint = AuraSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ISO Clean",
                                color = AuraOnSurfaceVariant,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Despeckle & Denoise",
                        color = AuraOnSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Remove night noise",
                        color = AuraOnSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Tool 4: Face Glow & Makeup
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                    .clickable { onToolSelected("face_glow") }
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AuraTertiaryContainer.copy(alpha = 0.20f))
                                .border(1.dp, AuraTertiaryContainer.copy(alpha = 0.30f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flare,
                                contentDescription = null,
                                tint = AuraTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AuraTertiaryContainer.copy(alpha = 0.20f))
                                .border(1.dp, AuraTertiaryContainer.copy(alpha = 0.30f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Beauty",
                                color = AuraTertiary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Face Glow & Makeup",
                        color = AuraOnSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Studio radiance",
                        color = AuraOnSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// =========================================================================
// SCREEN 1: Recent Restorations Horizontal Scroll
// =========================================================================
@Composable
private fun AuraRecentRestorationsSection(
    sample1Drawable: DrawableResource,
    sample2Drawable: DrawableResource,
    onOpenSample1: () -> Unit,
    onOpenSample2: () -> Unit,
    onBatchEnhance: () -> Unit,
    onOpenHistory: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Recent Restorations",
                    color = AuraOnSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AuraSurfaceContainerHighest)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "3 photos",
                        color = AuraOnSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = "History",
                color = AuraPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onOpenHistory() }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1
            Column(
                modifier = Modifier
                    .width(170.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                    .clickable { onOpenSample1() }
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    Image(
                        painter = painterResource(sample1Drawable),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Top badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .border(1.dp, AuraSecondary.copy(alpha = 0.40f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "4K ULTRA",
                            color = AuraSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    // Bottom badge
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.55f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "Enhanced",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Portrait_V2_4K.jpg",
                    color = AuraOnSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Today, 2:15 PM",
                    color = AuraOnSurfaceVariant,
                    fontSize = 10.sp
                )
            }

            // Card 2
            Column(
                modifier = Modifier
                    .width(170.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                    .clickable { onOpenSample2() }
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    Image(
                        painter = painterResource(sample2Drawable),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Top badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .border(1.dp, AuraPrimary.copy(alpha = 0.40f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "RESTORED",
                            color = AuraPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    // Bottom badge
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.55f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "Repaired",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Family_Archive_1974.png",
                    color = AuraOnSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Yesterday",
                    color = AuraOnSurfaceVariant,
                    fontSize = 10.sp
                )
            }

            // Card 3: Batch Enhance Upload Trigger Card
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(220.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable { onBatchEnhance() }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = AuraPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Batch Enhance",
                        color = AuraOnSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Up to 20 files",
                        color = AuraOnSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// =========================================================================
// SCREEN 1: Floating Glass Bottom Navigation Bar
// =========================================================================
@Composable
private fun AuraBottomNavBar(
    selectedTab: AuraBottomNavTab,
    onTabSelected: (AuraBottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(AuraSurfaceContainer.copy(alpha = 0.75f))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.28f), Color.White.copy(alpha = 0.08f))
                ),
                shape = CircleShape
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 0: Home
            AuraNavTabItem(
                label = "Home",
                icon = Icons.Default.Home,
                isSelected = selectedTab == AuraBottomNavTab.HOME,
                onClick = { onTabSelected(AuraBottomNavTab.HOME) }
            )

            // Tab 1: Studio
            AuraNavTabItem(
                label = "Studio",
                icon = Icons.Default.Compare,
                isSelected = selectedTab == AuraBottomNavTab.STUDIO,
                onClick = { onTabSelected(AuraBottomNavTab.STUDIO) }
            )

            // Tab 2: Restore
            AuraNavTabItem(
                label = "Restore",
                icon = Icons.Default.AutoFixHigh,
                isSelected = selectedTab == AuraBottomNavTab.RESTORE,
                onClick = { onTabSelected(AuraBottomNavTab.RESTORE) }
            )

            // Tab 3: Projects
            AuraNavTabItem(
                label = "Projects",
                icon = Icons.Default.FolderOpen,
                isSelected = selectedTab == AuraBottomNavTab.PROJECTS,
                onClick = { onTabSelected(AuraBottomNavTab.PROJECTS) }
            )
        }
    }
}

@Composable
private fun AuraNavTabItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) AuraPrimaryElectric.copy(alpha = 0.22f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) AuraPrimary else AuraOnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                color = if (isSelected) AuraPrimary else AuraOnSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

// =========================================================================
// SCREEN 2: Enhancement Comparison Studio
// Matches `enhancement_comparison_studio/code.html`
// =========================================================================
@Composable
private fun AuraComparisonStudioScreen(
    beforeDrawable: DrawableResource,
    afterDrawable: DrawableResource,
    beforeImageBitmap: ImageBitmap? = null,
    afterImageBitmap: ImageBitmap? = null,
    colorFilter: ColorFilter?,
    editParams: SkinEditParams,
    userCredits: Int,
    onBack: () -> Unit,
    onNavigateToRetouchSuite: () -> Unit,
    onRegenerate: () -> Unit,
    onOpenExportModal: () -> Unit,
    onUpdateIntensity: (Float) -> Unit
) {
    var isHoldCompareActive by remember { mutableStateOf(false) }
    var zoomFactor by remember { mutableFloatStateOf(1.0f) }
    var studioMode by remember { mutableStateOf("split") }
    var intensityValue by remember { mutableFloatStateOf(editParams.facialClarity) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraSurfaceLowest)
    ) {
        // Retouch Studio HUD Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Exit Button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Exit Studio",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Center Feature Badge
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AuraSurfaceContainerHigh.copy(alpha = 0.85f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = AuraSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "AI Ultra Enhance",
                        color = AuraOnSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "v4.2",
                        color = AuraSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Quick Actions: Zoom & Hold-Compare
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Zoom Toggle Button
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (zoomFactor > 1.2f) AuraPrimaryElectric.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        .clickable { zoomFactor = if (zoomFactor > 1.2f) 1.0f else 1.9f }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = null,
                            tint = if (zoomFactor > 1.2f) AuraPrimary else Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (zoomFactor > 1.2f) "100%" else "200%",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Hold to Compare Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isHoldCompareActive) AuraSecondaryContainer else Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isHoldCompareActive = true
                                    tryAwaitRelease()
                                    isHoldCompareActive = false
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Compare,
                        contentDescription = "Hold to compare",
                        tint = if (isHoldCompareActive) AuraOnSecondaryContainer else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Main Comparison Canvas Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            val beforeP = if (beforeImageBitmap != null) remember(beforeImageBitmap) { BitmapPainter(beforeImageBitmap) } else painterResource(beforeDrawable)
            val afterP = if (afterImageBitmap != null) remember(afterImageBitmap) { BitmapPainter(afterImageBitmap) } else painterResource(afterDrawable)
            BeforeAfterSlider(
                beforePainter = beforeP,
                afterPainter = afterP,
                beforeLabel = "BEFORE (720p)",
                afterLabel = "AFTER (4K AI)",
                colorFilter = colorFilter,
                showTelemetry = true,
                showLandmarks = studioMode == "magnifier",
                isHoldCompareActive = isHoldCompareActive,
                zoomFactor = zoomFactor,
                onZoomToggle = { zoomFactor = if (zoomFactor > 1.2f) 1.0f else 1.9f },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Bottom Dock, Tools & CTAs
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(AuraSurfaceContainer.copy(alpha = 0.85f))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.20f), Color.White.copy(alpha = 0.04f))
                    ),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Mode Segmented Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), CircleShape)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Split View
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(if (studioMode == "split") AuraPrimaryElectric.copy(alpha = 0.28f) else Color.Transparent)
                        .clickable { studioMode = "split" }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Compare,
                            contentDescription = null,
                            tint = if (studioMode == "split") AuraPrimary else AuraOnSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Split View",
                            color = if (studioMode == "split") Color.White else AuraOnSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Face Magnifier
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(if (studioMode == "magnifier") AuraPrimaryElectric.copy(alpha = 0.28f) else Color.Transparent)
                        .clickable {
                            studioMode = "magnifier"
                            zoomFactor = if (zoomFactor > 1.2f) 1.0f else 1.9f
                        }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CenterFocusStrong,
                            contentDescription = null,
                            tint = if (studioMode == "magnifier") AuraPrimary else AuraOnSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Magnifier",
                            color = if (studioMode == "magnifier") Color.White else AuraOnSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Pro Level (Switches to Retouch Suite)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .clickable { onNavigateToRetouchSuite() }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = AuraOnSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Pro Level",
                            color = AuraOnSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Beauty
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .clickable { onNavigateToRetouchSuite() }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flare,
                            contentDescription = null,
                            tint = AuraOnSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Beauty",
                            color = AuraOnSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Quick Tuning Intensity Slider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "INTENSITY",
                    color = AuraOutline,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Natural",
                        color = AuraOnSurfaceVariant,
                        fontSize = 11.sp
                    )

                    Slider(
                        value = intensityValue,
                        onValueChange = {
                            intensityValue = it
                            onUpdateIntensity(it)
                        },
                        valueRange = 0.2f..1.2f,
                        modifier = Modifier.width(140.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = AuraSecondary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                        )
                    )

                    Text(
                        text = "Ultra 4K",
                        color = AuraSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Primary Action Cluster
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Regenerate Button
                IconButton(
                    onClick = onRegenerate,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Regenerate",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Save & Export in 4K Button with Credit Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(AuraExportButtonGradient)
                        .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                        .clickable { onOpenExportModal() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                        Text(
                            text = "Save & Export in 4K",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.22f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⚡1 Credit",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// SCREEN 3: AI Retouch & Restoration Suite
// Matches `ai_retouch_restoration_suite/code.html`
// =========================================================================
@Composable
private fun AuraRetouchSuiteScreen(
    beforeDrawable: DrawableResource,
    afterDrawable: DrawableResource,
    beforeImageBitmap: ImageBitmap? = null,
    afterImageBitmap: ImageBitmap? = null,
    colorFilter: ColorFilter?,
    editParams: SkinEditParams,
    userCredits: Int,
    onBack: () -> Unit,
    onNavigateToStudio: () -> Unit,
    onUpdateParams: ((SkinEditParams) -> SkinEditParams) -> Unit,
    onSelectPreset: (AestheticPreset) -> Unit,
    onOpenExportModal: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var activeSuiteTab by remember { mutableStateOf(RetouchSuiteTab.RETOUCH) }
    var zoomFactor by remember { mutableFloatStateOf(1.0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraSurfaceLowest)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Aura AI",
                    color = AuraOnSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AuraSurfaceContainerHigh.copy(alpha = 0.85f))
                    .border(1.dp, AuraOutlineVariant.copy(alpha = 0.40f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AuraSecondary)
                    )
                    Text(
                        text = "RESTORATION 4K",
                        color = AuraSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                }
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = AuraOnSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        // Upper Section: Retouch Canvas Preview with Landmark Detection
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.05f)
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            val beforeP = if (beforeImageBitmap != null) remember(beforeImageBitmap) { BitmapPainter(beforeImageBitmap) } else painterResource(beforeDrawable)
            val afterP = if (afterImageBitmap != null) remember(afterImageBitmap) { BitmapPainter(afterImageBitmap) } else painterResource(afterDrawable)
            BeforeAfterSlider(
                beforePainter = beforeP,
                afterPainter = afterP,
                beforeLabel = "ORIGINAL RAW",
                afterLabel = "AURA ENHANCED",
                colorFilter = colorFilter,
                showTelemetry = false,
                showLandmarks = true,
                zoomFactor = zoomFactor,
                onZoomToggle = { zoomFactor = if (zoomFactor > 1.2f) 1.0f else 1.9f },
                modifier = Modifier.fillMaxSize()
            )

            // Live Preview Tag
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(CircleShape)
                    .background(AuraSurfaceLowest.copy(alpha = 0.75f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "LIVE PREVIEW • 60 FPS",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }

            // Top Right Switch to Studio Button
            IconButton(
                onClick = onNavigateToStudio,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(AuraSurfaceLowest.copy(alpha = 0.75f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Compare,
                    contentDescription = "Full Split View",
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        // Lower Section: Frosted Glass Adjustment Control Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.35f)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(AuraSurfaceContainer.copy(alpha = 0.90f))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.04f))
                    ),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Mode Selector Pills (Segmented Glass Control)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), CircleShape)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RetouchSuiteTab.values().forEach { tab ->
                    val isSelected = activeSuiteTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) {
                                    Brush.horizontalGradient(
                                        listOf(AuraPrimaryElectric.copy(alpha = 0.35f), AuraSecondaryContainer.copy(alpha = 0.25f))
                                    )
                                } else {
                                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                }
                            )
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) AuraSecondary.copy(alpha = 0.40f) else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { activeSuiteTab = tab }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = if (isSelected) Color.White else AuraOnSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // 2. Fine-Tuning Intensity Sliders Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = AuraPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Fine-Tuning Parameters",
                        color = AuraOnSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AuraSecondaryContainer.copy(alpha = 0.15f))
                        .border(1.dp, AuraSecondary.copy(alpha = 0.25f), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Neural Engine v4.2",
                        color = AuraSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Sliders Container
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Slider 1: AI Denoise & Unblur
                AuraTuningSliderCard(
                    title = "AI Denoise & Unblur",
                    icon = Icons.Default.BlurOff,
                    accentColor = AuraSecondary,
                    value = editParams.facialClarity,
                    valueText = "${(editParams.facialClarity * 100).toInt()}%",
                    onValueChange = { onUpdateParams { p -> p.copy(facialClarity = it) } }
                )

                // Slider 2: Skin Texture & Pores
                AuraTuningSliderCard(
                    title = "Skin Texture & Pores",
                    icon = Icons.Default.Face,
                    accentColor = AuraPrimary,
                    value = editParams.skinSmoothness,
                    valueText = "${(editParams.skinSmoothness * 100).toInt()}% (Natural)",
                    onValueChange = { onUpdateParams { p -> p.copy(skinSmoothness = it) } }
                )

                // Slider 3: Eye Iris Brightness
                AuraTuningSliderCard(
                    title = "Eye Iris Brightness",
                    icon = Icons.Default.Visibility,
                    accentColor = AuraSecondary,
                    value = editParams.skinRadiance - 0.5f,
                    valueText = "${((editParams.skinRadiance - 0.5f) * 100).toInt().coerceIn(0, 100)}%",
                    onValueChange = { onUpdateParams { p -> p.copy(skinRadiance = it + 0.5f) } }
                )

                // Slider 4: Lighting Boost & HDR
                AuraTuningSliderCard(
                    title = "Lighting Boost & HDR",
                    icon = Icons.Default.WbSunny,
                    accentColor = AuraTertiaryHotMagenta,
                    value = editParams.skinWarmth - 0.5f,
                    valueText = "${((editParams.skinWarmth - 0.5f) * 100).toInt().coerceIn(0, 100)}%",
                    onValueChange = { onUpdateParams { p -> p.copy(skinWarmth = it + 0.5f) } }
                )
            }

            // 3. AI Restyle Presets (Horizontal Scroll Bento)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Restyle Presets",
                        color = AuraOnSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap to apply",
                        color = AuraOnSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AestheticPreset.values().forEach { preset ->
                        val isSelected = editParams.preset == preset
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.linearGradient(
                                            listOf(AuraSecondary.copy(alpha = 0.18f), AuraPrimaryElectric.copy(alpha = 0.12f))
                                        )
                                    } else {
                                        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.03f)))
                                    }
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) AuraSecondary else Color.White.copy(alpha = 0.10f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { onSelectPreset(preset) }
                                .padding(10.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(AuraSurfaceContainerHigh.copy(alpha = 0.70f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (isSelected) AuraSecondary else AuraOnSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(6.dp)
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(AuraSecondary)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = preset.title,
                                    color = if (isSelected) Color.White else AuraOnSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isSelected) "Active Profile" else "Preset",
                                    color = if (isSelected) AuraSecondary else AuraOutline,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // Bottom CTA: Apply & Export in 4K
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(CircleShape)
                    .background(AuraExportButtonGradient)
                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                    .clickable { onOpenExportModal() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                    Text(
                        text = "Apply & Export in 4K",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⚡1 Credit",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuraTuningSliderCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    value: Float,
    valueText: String,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = title,
                    color = AuraOnSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = valueText,
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Slider(
            value = value.coerceIn(0f, 1f),
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = accentColor,
                inactiveTrackColor = Color.White.copy(alpha = 0.12f)
            )
        )
    }
}

// =========================================================================
// SCREEN 4: Aura Neural Processing HUD
// Matches DESIGN.md component 6
// =========================================================================
@Composable
private fun AuraProcessingScreen(
    stageProgress: PipelineStageProgress?,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraSurfaceLowest),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Radial Progress Sweep
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Track
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = size.width * 0.45f,
                        style = Stroke(width = 6.dp.toPx())
                    )
                }

                // Active Gradient Arc
                CircularProgressIndicator(
                    progress = { stageProgress?.progressFraction ?: 0.5f },
                    strokeWidth = 6.dp,
                    color = AuraPrimaryElectric,
                    trackColor = Color.Transparent,
                    modifier = Modifier.fillMaxSize()
                )

                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AuraSecondary,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Shimmering Status Text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stageProgress?.modelName ?: "Neural Enhancement Engine",
                    color = AuraOnSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stageProgress?.stageDescription ?: "Processing high-frequency textures in 4K...",
                    color = AuraOnSurfaceVariant,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Cancel Button
            TextButton(
                onClick = onCancel,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(horizontal = 14.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Cancel",
                    color = AuraOutline,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// =========================================================================
// SCREEN 5: Aura Export Success & Share Screen
// =========================================================================
@Composable
private fun AuraSavedShareScreen(
    savedDrawable: DrawableResource,
    fileName: String,
    onBackToResult: () -> Unit,
    onEnhanceAnother: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraSurfaceLowest)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackToResult,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AuraSecondary.copy(alpha = 0.15f))
                    .border(1.dp, AuraSecondary.copy(alpha = 0.35f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AuraSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "SAVED TO CAMERA ROLL",
                        color = AuraSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.size(38.dp))
        }

        // Luxury Glass Framed Preview
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.05f))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Image(
                painter = painterResource(savedDrawable),
                contentDescription = "Saved Portrait",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Details & CTAs
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = fileName,
                color = AuraOnSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Exported in Ultra 4K • Studio Grade Finish",
                color = AuraOnSurfaceVariant,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Share Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(CircleShape)
                    .background(AuraHeroButtonGradient)
                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                    .clickable { onShare() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                    Text(
                        text = "Share Masterpiece",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Enhance Another
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                    .clickable { onEnhanceAnother() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Enhance Another Photo",
                    color = AuraOnSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// =========================================================================
// Dialogs: Credits, Settings, Tools, Projects, Architecture
// =========================================================================
@Composable
private fun AuraCreditsDialog(
    credits: Int,
    onClaimBonus: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AuraPrimary)
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = AuraSecondary
                )
                Text("Aura AI Credits Balance", color = AuraOnSurface, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "You currently have $credits neural processing credits available for 4K & 8K exports.",
                    color = AuraOnSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AuraSecondary.copy(alpha = 0.12f))
                        .border(1.dp, AuraSecondary.copy(alpha = 0.30f), RoundedCornerShape(14.dp))
                        .clickable { onClaimBonus() }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡ Claim Daily 10 Free Credits",
                        color = AuraSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        },
        containerColor = AuraSurfaceContainerHigh
    )
}

@Composable
private fun AuraSettingsDialog(
    onOpenArchitecture: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = AuraPrimary)
            }
        },
        title = {
            Text("Settings & Engine", color = AuraOnSurface, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Aura AI Studio Version: 4.2.0 (Luminescent Glass Edition)",
                    color = AuraOnSurfaceVariant,
                    fontSize = 13.sp
                )
                Text(
                    text = "On-Device Neural Engine: GPEN-BFR-256 + BiSeNet + Zero-DCE",
                    color = AuraOutline,
                    fontSize = 12.sp
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .clickable { onOpenArchitecture() }
                        .padding(10.dp)
                ) {
                    Text(
                        text = "View Neural Model Pipeline Spec",
                        color = AuraSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        containerColor = AuraSurfaceContainerHigh
    )
}

@Composable
private fun AuraAllToolsDialog(
    onSelectTool: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AuraOutline)
            }
        },
        title = {
            Text("All AI Creative Tools", color = AuraOnSurface, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Triple("portrait_4k", "AI Portrait 4K Enhancer", "Reconstruct facial pores and iris catchlights"),
                    Triple("passport", "AI Passport & Visa Studio", "Biometric ID standards, suits & backgrounds"),
                    Triple("old_restore", "Old Photo Scratch Repair", "Vintage damage removal and color revive"),
                    Triple("denoise", "Despeckle & Night Denoise", "Clean ISO grain while keeping sharp edges"),
                    Triple("beauty", "Face Glow & Makeup Studio", "K-Beauty radiance and lip color tuning"),
                    Triple("custom", "Upload from Device Gallery", "Pick any custom photo from device roll")
                ).forEach { (id, title, desc) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                            .clickable { onSelectTool(id) }
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(text = title, color = AuraOnSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = desc, color = AuraOnSurfaceVariant, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        containerColor = AuraSurfaceContainerHigh
    )
}

@Composable
private fun AuraProjectsDialog(
    onSelectProject: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AuraPrimary)
            }
        },
        title = {
            Text("Saved Projects & History", color = AuraOnSurface, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    Pair("Portrait_V2_4K.jpg", "Today, 2:15 PM · 4K UHD"),
                    Pair("Family_Archive_1974.png", "Yesterday · Vintage Restored")
                ).forEachIndexed { index, (name, date) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                            .clickable { onSelectProject(index) }
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(text = name, color = AuraOnSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = date, color = AuraSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        containerColor = AuraSurfaceContainerHigh
    )
}

@Composable
private fun ModelArchitectureDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got It", color = AuraPrimary)
            }
        },
        title = {
            Text("5-Stage Neural Pipeline Spec", color = AuraOnSurface, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("1. Real-ESRGAN x4v3", "Upscales input tensor to 4K UHD boundary with bicubic interpolation."),
                    Pair("2. BiSeNet 19-Class Parser", "High-res 512x512 semantic segmentation of eyes, lips, skin dermis."),
                    Pair("3. MediaPipe Multiclass Mask", "Separates background, clothes, hair, and foreground portrait dermis."),
                    Pair("4. GPEN-BFR-256 GAN Engine", "Reconstructs crystal-clear eyes, eyelashes, teeth, and skin micro-pores."),
                    Pair("5. Zero-DCE + YCrCb Rational", "Synthesizes studio radiance and calibrated regional tone balance.")
                ).forEach { (name, desc) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(text = name, color = AuraSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = desc, color = AuraOnSurfaceVariant, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        containerColor = AuraSurfaceContainerHigh
    )
}
