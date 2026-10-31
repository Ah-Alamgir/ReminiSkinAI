package com.example.reminiskinai.ui.main

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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FaceRetouchingNatural
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
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
import com.example.reminiskinai.theme.AuraOnPrimary
import com.example.reminiskinai.theme.AuraOnPrimaryContainer
import com.example.reminiskinai.theme.AuraOnSecondary
import com.example.reminiskinai.theme.AuraOnSecondaryContainer
import com.example.reminiskinai.theme.AuraOnSurface
import com.example.reminiskinai.theme.AuraOnSurfaceVariant
import com.example.reminiskinai.theme.AuraOnTertiary
import com.example.reminiskinai.theme.AuraOnTertiaryContainer
import com.example.reminiskinai.theme.AuraOutline
import com.example.reminiskinai.theme.AuraOutlineVariant
import com.example.reminiskinai.theme.AuraPrimary
import com.example.reminiskinai.theme.AuraPrimaryContainer
import com.example.reminiskinai.theme.AuraSecondary
import com.example.reminiskinai.theme.AuraSecondaryContainer
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
import com.example.reminiskinai.theme.ReminiBlack
import com.example.reminiskinai.theme.ReminiCard
import com.example.reminiskinai.theme.ReminiCrimson
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
import kotlin.math.min
import kotlin.math.roundToInt

enum class EditorScreenMode {
    HOME,
    PROCESSING,
    RESULT,
    SAVED_SHARE
}

enum class AuraBottomNavTab {
    HOME,
    STUDIO,
    RESTORE,
    PROJECTS
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
    var showFineTuneSliders by remember { mutableStateOf(false) }
    var showModelArchitectureSheet by remember { mutableStateOf(false) }
    var showCreditsDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAllToolsDialog by remember { mutableStateOf(false) }
    var showProjectsDialog by remember { mutableStateOf(false) }

    var isSaving by remember { mutableStateOf(false) }
    var savedFileName by remember { mutableStateOf("") }
    var telemetry by remember { mutableStateOf(SkinAnalysisTelemetry()) }

    fun startProcessing(before: DrawableResource, after: DrawableResource?, presetMode: AestheticPreset? = null) {
        if (presetMode != null) {
            editParams = editParams.copy(preset = presetMode)
        }
        activeBeforeDrawable = before
        activeAfterDrawable = after ?: before
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
                delay(260)
            }
            delay(120)
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
            .background(AuraSurfaceLowest)
    ) {
        // Ambient Deep OLED Glow Meshes in Background
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Top-left primary purple blur
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(AuraPrimary.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(-40f, -40f),
                        radius = size.width * 0.9f
                    ),
                    center = Offset(-40f, -40f),
                    radius = size.width * 0.9f
                )
                // Mid-right cyan glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(AuraSecondaryContainer.copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(size.width + 60f, size.height * 0.28f),
                        radius = size.width * 0.85f
                    ),
                    center = Offset(size.width + 60f, size.height * 0.28f),
                    radius = size.width * 0.85f
                )
                // Lower center tertiary magenta glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(AuraTertiaryContainer.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.65f),
                        radius = size.width * 0.8f
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.65f),
                    radius = size.width * 0.8f
                )
            }
        }

        when (mode) {
            EditorScreenMode.HOME -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 100.dp)
                    ) {
                        // Header: TopAppBar
                        AuraTopAppBar(
                            credits = userCredits,
                            onOpenCredits = { showCreditsDialog = true },
                            onOpenSettings = { showSettingsDialog = true }
                        )

                        // Main Content Container
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(22.dp)
                        ) {
                            // Hero Headline & Subtitle with Overlapping Avatar Badges
                            AuraHeroHeader(
                                avatar1 = Res.drawable.sample_1_remini_ref,
                                avatar2 = Res.drawable.sample_2_remini_ref
                            )

                            // Hero Feature Card: AI HD Enhance & Unblur with Interactive Split Slider
                            AuraHeroFeatureCard(
                                beforeDrawable = Res.drawable.sample_1_before,
                                afterDrawable = Res.drawable.sample_1_remini_ref,
                                onOneTapEnhance = {
                                    startProcessing(
                                        Res.drawable.sample_1_before,
                                        Res.drawable.sample_1_remini_ref,
                                        AestheticPreset.REMINI_NATURAL
                                    )
                                }
                            )

                            // Precision Tools Bento Grid
                            AuraPrecisionToolsSection(
                                sampleVintageDrawable = Res.drawable.sample_2_before,
                                onViewAll = { showAllToolsDialog = true },
                                onToolSelected = { toolKey ->
                                    when (toolKey) {
                                        "portrait_4k" -> {
                                            startProcessing(
                                                samplePhotos[0].beforeDrawable,
                                                samplePhotos[0].reminiRefDrawable,
                                                AestheticPreset.REMINI_NATURAL
                                            )
                                        }
                                        "old_restore" -> {
                                            selectedSample = samplePhotos[1]
                                            startProcessing(
                                                samplePhotos[1].beforeDrawable,
                                                samplePhotos[1].reminiRefDrawable,
                                                AestheticPreset.REMINI_NATURAL
                                            )
                                        }
                                        "denoise" -> {
                                            startProcessing(
                                                samplePhotos[0].beforeDrawable,
                                                samplePhotos[0].reminiRefDrawable,
                                                AestheticPreset.REMINI_NATURAL
                                            )
                                        }
                                        "face_glow" -> {
                                            startProcessing(
                                                samplePhotos[0].beforeDrawable,
                                                samplePhotos[0].reminiRefDrawable,
                                                AestheticPreset.KOREAN
                                            )
                                        }
                                    }
                                }
                            )

                            // Recent Restorations Horizontal Scroll
                            AuraRecentRestorationsSection(
                                sample1Drawable = Res.drawable.sample_1_remini_ref,
                                sample2Drawable = Res.drawable.sample_2_remini_ref,
                                onOpenSample1 = {
                                    selectedSample = samplePhotos[0]
                                    activeBeforeDrawable = samplePhotos[0].beforeDrawable
                                    activeAfterDrawable = samplePhotos[0].reminiRefDrawable
                                    mode = EditorScreenMode.RESULT
                                },
                                onOpenSample2 = {
                                    selectedSample = samplePhotos[1]
                                    activeBeforeDrawable = samplePhotos[1].beforeDrawable
                                    activeAfterDrawable = samplePhotos[1].reminiRefDrawable
                                    mode = EditorScreenMode.RESULT
                                },
                                onBatchEnhance = {
                                    photoPickerLauncher()
                                },
                                onOpenHistory = {
                                    showProjectsDialog = true
                                }
                            )
                        }
                    }

                    // Floating Bottom Navigation Bar
                    AuraBottomNavBar(
                        selectedTab = activeBottomTab,
                        onTabSelected = { tab ->
                            activeBottomTab = tab
                            when (tab) {
                                AuraBottomNavTab.HOME -> {
                                    // already home
                                }
                                AuraBottomNavTab.STUDIO -> {
                                    onNavigateToPassport()
                                }
                                AuraBottomNavTab.RESTORE -> {
                                    photoPickerLauncher()
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

            EditorScreenMode.PROCESSING -> {
                AuraProcessingScreen(
                    stageProgress = stageProgress,
                    onCancel = { mode = EditorScreenMode.HOME }
                )
            }

            EditorScreenMode.RESULT -> {
                ReminiResultScreen(
                    beforeDrawable = activeBeforeDrawable,
                    afterDrawable = activeAfterDrawable ?: activeBeforeDrawable,
                    customPickedBitmap = null,
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
                        PlatformActions.saveImageToGallery("AuraAI_${selectedSample?.id ?: "4K"}.png") { success, fileName ->
                            isSaving = false
                            savedFileName = fileName ?: "Aura_Restored_4K.png"
                            mode = EditorScreenMode.SAVED_SHARE
                        }
                    },
                    onShare = {
                        PlatformActions.shareImage("Check out my ultra-sharp 4K restored portrait enhanced with Aura AI!")
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
                        PlatformActions.shareImage("Check out my ultra-sharp 4K restored portrait enhanced with Aura AI!")
                    }
                )
            }
        }

        // Dialogs & Sheets
        if (showCreditsDialog) {
            AuraCreditsDialog(
                credits = userCredits,
                onClaimBonus = {
                    userCredits += 10
                },
                onDismiss = { showCreditsDialog = false }
            )
        }

        if (showSettingsDialog) {
            AuraSettingsDialog(
                onOpenArchitecture = {
                    showSettingsDialog = false
                    showModelArchitectureSheet = true
                },
                onDismiss = { showSettingsDialog = false }
            )
        }

        if (showAllToolsDialog) {
            AuraAllToolsDialog(
                onSelectTool = { toolId ->
                    showAllToolsDialog = false
                    when (toolId) {
                        "passport" -> onNavigateToPassport()
                        "portrait_4k" -> startProcessing(samplePhotos[0].beforeDrawable, samplePhotos[0].reminiRefDrawable, AestheticPreset.REMINI_NATURAL)
                        "old_restore" -> startProcessing(samplePhotos[1].beforeDrawable, samplePhotos[1].reminiRefDrawable, AestheticPreset.REMINI_NATURAL)
                        "denoise" -> startProcessing(samplePhotos[0].beforeDrawable, samplePhotos[0].reminiRefDrawable, AestheticPreset.REMINI_NATURAL)
                        "beauty" -> startProcessing(samplePhotos[0].beforeDrawable, samplePhotos[0].reminiRefDrawable, AestheticPreset.KOREAN)
                        "custom" -> photoPickerLauncher()
                    }
                },
                onDismiss = { showAllToolsDialog = false }
            )
        }

        if (showProjectsDialog) {
            AuraProjectsDialog(
                onSelectProject = { index ->
                    showProjectsDialog = false
                    if (index == 0) {
                        selectedSample = samplePhotos[0]
                        activeBeforeDrawable = samplePhotos[0].beforeDrawable
                        activeAfterDrawable = samplePhotos[0].reminiRefDrawable
                        mode = EditorScreenMode.RESULT
                    } else {
                        selectedSample = samplePhotos[1]
                        activeBeforeDrawable = samplePhotos[1].beforeDrawable
                        activeAfterDrawable = samplePhotos[1].reminiRefDrawable
                        mode = EditorScreenMode.RESULT
                    }
                },
                onDismiss = { showProjectsDialog = false }
            )
        }

        if (showModelArchitectureSheet) {
            ModelArchitectureDialog(
                onDismiss = { showModelArchitectureSheet = false }
            )
        }
    }
}

// -------------------------------------------------------------------------
// Top App Bar
// -------------------------------------------------------------------------
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
        // Logo & Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(AuraPrimary.copy(alpha = 0.20f))
                    .border(1.dp, AuraPrimary.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Aura AI Logo",
                    tint = AuraPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Aura AI",
                color = AuraOnSurface,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
        }

        // Credits Pill & Settings Button
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    modifier = Modifier.size(16.dp)
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
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = AuraOnSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// Hero Headline & Avatars
// -------------------------------------------------------------------------
@Composable
private fun AuraHeroHeader(
    avatar1: DrawableResource,
    avatar2: DrawableResource
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Revive Your Memories",
                color = AuraOnSurface,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.8).sp,
                modifier = Modifier.weight(1f)
            )

            // Overlapping Avatars
            Row(
                modifier = Modifier.padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .border(2.dp, AuraSurfaceLowest, CircleShape)
                ) {
                    Image(
                        painter = painterResource(avatar1),
                        contentDescription = "Avatar 1",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .offset(x = (-10).dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .border(2.dp, AuraSurfaceLowest, CircleShape)
                ) {
                    Image(
                        painter = painterResource(avatar2),
                        contentDescription = "Avatar 2",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
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

// -------------------------------------------------------------------------
// Hero Feature Card: AI HD Enhance & Unblur
// -------------------------------------------------------------------------
@Composable
private fun AuraHeroFeatureCard(
    beforeDrawable: DrawableResource,
    afterDrawable: DrawableResource,
    onOneTapEnhance: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.28f),
                        Color.White.copy(alpha = 0.10f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Interactive Split Visual Canvas / Comparison Slider
            AuraHeroComparisonCanvas(
                beforeDrawable = beforeDrawable,
                afterDrawable = afterDrawable,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Card Information
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
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Action Button with Specular Glow Gradient
            Button(
                onClick = onOneTapEnhance,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(9999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFA078FF),
                                    Color(0xFFA078FF),
                                    Color(0xFFF751A1)
                                )
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(9999.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "One-Tap Magic Enhance",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Interactive Comparison Canvas for Hero Card
// -------------------------------------------------------------------------
@Composable
private fun AuraHeroComparisonCanvas(
    beforeDrawable: DrawableResource,
    afterDrawable: DrawableResource,
    modifier: Modifier = Modifier
) {
    val beforePainter = painterResource(beforeDrawable)
    val afterPainter = painterResource(afterDrawable)

    var splitFraction by remember { mutableFloatStateOf(0.55f) }

    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (size.width > 0) {
                        splitFraction = (offset.x / size.width).coerceIn(0.05f, 0.95f)
                    }
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    if (size.width > 0) {
                        splitFraction = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            val intrinsicSize = afterPainter.intrinsicSize
            val imgW = if (intrinsicSize.width.isFinite() && intrinsicSize.width > 0f) intrinsicSize.width else canvasW
            val imgH = if (intrinsicSize.height.isFinite() && intrinsicSize.height > 0f) intrinsicSize.height else canvasH

            val scale = maxOf(canvasW / imgW, canvasH / imgH)
            val drawW = imgW * scale
            val drawH = imgH * scale
            val leftOff = (canvasW - drawW) * 0.5f
            val topOff = (canvasH - drawH) * 0.5f
            val drawSize = Size(drawW, drawH)

            val splitX = (canvasW * splitFraction).coerceIn(0f, canvasW)

            // Draw full After image
            clipRect(0f, 0f, canvasW, canvasH) {
                translate(left = leftOff, top = topOff) {
                    with(afterPainter) {
                        draw(size = drawSize)
                    }
                }
            }

            // Draw Before image clipped to [0 .. splitX]
            clipRect(0f, 0f, splitX, canvasH) {
                translate(left = leftOff, top = topOff) {
                    with(beforePainter) {
                        draw(size = drawSize)
                    }
                }
            }

            // Gradient shadow overlays
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.40f), Color.Transparent, Color.Black.copy(alpha = 0.70f))
                )
            )

            // Divider vertical line
            drawLine(
                color = Color.White,
                start = Offset(splitX, 0f),
                end = Offset(splitX, canvasH),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Draggable Thumb Disc in Center of Split Line
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (splitFraction * 320).dp - 18.dp) // responsive fallback
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f))
                .border(1.5.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                contentDescription = "Compare handle",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        // Tag badge top-left: Face Reconstruction v4.2 with Pulsing Cyan Dot
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .clip(CircleShape)
                .background(AuraSurfaceLowest.copy(alpha = 0.75f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(AuraSecondary.copy(alpha = pulseAlpha))
            )
            Text(
                text = "Face Reconstruction v4.2",
                color = AuraOnSurface,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Floating Comparison Pills
        // Bottom-left: Original Blurry
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.65f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "ORIGINAL BLURRY",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        // Bottom-right: 4K Restored
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .clip(CircleShape)
                .background(AuraPrimaryContainer.copy(alpha = 0.85f))
                .border(1.dp, Color.White.copy(alpha = 0.30f), CircleShape)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "4K RESTORED",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// -------------------------------------------------------------------------
// Precision Tools Bento Grid
// -------------------------------------------------------------------------
@Composable
private fun AuraPrecisionToolsSection(
    sampleVintageDrawable: DrawableResource,
    onViewAll: () -> Unit,
    onToolSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
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
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onViewAll() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2x2 Bento Grid
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tool 1: AI Portrait 4K
                AuraBentoCard(
                    modifier = Modifier.weight(1f),
                    badgeText = "HD 4K",
                    badgeColor = AuraSecondary,
                    badgeBg = AuraSecondary.copy(alpha = 0.12f),
                    iconContent = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AuraPrimary.copy(alpha = 0.15f))
                                .border(1.dp, AuraPrimary.copy(alpha = 0.30f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CenterFocusStrong,
                                contentDescription = null,
                                tint = AuraPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    title = "AI Portrait 4K",
                    subtitle = "Crystal sharp face detail",
                    onClick = { onToolSelected("portrait_4k") }
                )

                // Tool 2: Old Photo Restore
                AuraBentoCard(
                    modifier = Modifier.weight(1f),
                    badgeText = "Vintage",
                    badgeColor = AuraTertiary,
                    badgeBg = AuraTertiaryContainer.copy(alpha = 0.20f),
                    iconContent = {
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
                                    .background(AuraPrimary.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    title = "Old Photo Restore",
                    subtitle = "Scratch removal & unblur",
                    onClick = { onToolSelected("old_restore") }
                )
            }

            // Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tool 3: Despeckle & Denoise
                AuraBentoCard(
                    modifier = Modifier.weight(1f),
                    badgeText = "ISO Clean",
                    badgeColor = AuraOnSurfaceVariant,
                    badgeBg = Color.White.copy(alpha = 0.06f),
                    iconContent = {
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
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    title = "Despeckle & Denoise",
                    subtitle = "Remove night noise",
                    onClick = { onToolSelected("denoise") }
                )

                // Tool 4: Face Glow & Makeup
                AuraBentoCard(
                    modifier = Modifier.weight(1f),
                    badgeText = "Beauty",
                    badgeColor = AuraTertiary,
                    badgeBg = AuraTertiaryContainer.copy(alpha = 0.20f),
                    iconContent = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AuraTertiaryContainer.copy(alpha = 0.20f))
                                .border(1.dp, AuraTertiaryContainer.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flare,
                                contentDescription = null,
                                tint = AuraTertiary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    title = "Face Glow & Makeup",
                    subtitle = "Studio radiance",
                    onClick = { onToolSelected("face_glow") }
                )
            }
        }
    }
}

@Composable
private fun AuraBentoCard(
    badgeText: String,
    badgeColor: Color,
    badgeBg: Color,
    iconContent: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.08f))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                iconContent()
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .border(1.dp, badgeColor.copy(alpha = 0.30f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                color = AuraOnSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = AuraOnSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// -------------------------------------------------------------------------
// Recent Restorations Horizontal Scroll
// -------------------------------------------------------------------------
@Composable
private fun AuraRecentRestorationsSection(
    sample1Drawable: DrawableResource,
    sample2Drawable: DrawableResource,
    onOpenSample1: () -> Unit,
    onOpenSample2: () -> Unit,
    onBatchEnhance: () -> Unit,
    onOpenHistory: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(
                text = "History",
                color = AuraPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onOpenHistory() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Portrait 4K
            AuraRecentPhotoCard(
                imageDrawable = sample1Drawable,
                topBadgeText = "4K ULTRA",
                topBadgeColor = AuraSecondary,
                pillIcon = Icons.Default.CheckCircle,
                pillText = "Enhanced",
                title = "Portrait_V2_4K.jpg",
                timestamp = "Today, 2:15 PM",
                onClick = onOpenSample1
            )

            // Card 2: Vintage Restored
            AuraRecentPhotoCard(
                imageDrawable = sample2Drawable,
                topBadgeText = "RESTORED",
                topBadgeColor = AuraPrimary,
                pillIcon = Icons.Default.AutoFixHigh,
                pillText = "Repaired",
                title = "Family_Archive_1974.png",
                timestamp = "Yesterday",
                onClick = onOpenSample2
            )

            // Card 3: Batch Enhance Trigger Card
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(210.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .border(
                        1.5.dp,
                        Color.White.copy(alpha = 0.18f),
                        RoundedCornerShape(20.dp)
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
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Batch Enhance",
                        color = AuraOnSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Up to 20 files",
                        color = AuraOnSurfaceVariant,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun AuraRecentPhotoCard(
    imageDrawable: DrawableResource,
    topBadgeText: String,
    topBadgeColor: Color,
    pillIcon: androidx.compose.ui.graphics.vector.ImageVector,
    pillText: String,
    title: String,
    timestamp: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(165.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.08f))
                ),
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                Image(
                    painter = painterResource(imageDrawable),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(1.dp, topBadgeColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = topBadgeText,
                        color = topBadgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Bottom Pill
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.60f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = pillIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = pillText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = AuraOnSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = timestamp,
                color = AuraOnSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}

// -------------------------------------------------------------------------
// Bottom Navigation Bar
// -------------------------------------------------------------------------
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
            .background(AuraSurfaceContainer.copy(alpha = 0.88f))
            .border(1.dp, AuraOutlineVariant.copy(alpha = 0.40f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Tab 0: Home
            AuraNavTabItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = selectedTab == AuraBottomNavTab.HOME,
                onClick = { onTabSelected(AuraBottomNavTab.HOME) }
            )

            // Tab 1: Studio
            AuraNavTabItem(
                icon = Icons.Default.Compare,
                label = "Studio",
                isSelected = selectedTab == AuraBottomNavTab.STUDIO,
                onClick = { onTabSelected(AuraBottomNavTab.STUDIO) }
            )

            // Tab 2: Restore
            AuraNavTabItem(
                icon = Icons.Default.AutoFixHigh,
                label = "Restore",
                isSelected = selectedTab == AuraBottomNavTab.RESTORE,
                onClick = { onTabSelected(AuraBottomNavTab.RESTORE) }
            )

            // Tab 3: Projects
            AuraNavTabItem(
                icon = Icons.Default.FolderOpen,
                label = "Projects",
                isSelected = selectedTab == AuraBottomNavTab.PROJECTS,
                onClick = { onTabSelected(AuraBottomNavTab.PROJECTS) }
            )
        }
    }
}

@Composable
private fun AuraNavTabItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    if (isSelected) {
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(AuraPrimary.copy(alpha = 0.20f))
                .clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = AuraPrimary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                color = AuraPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        Column(
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = AuraOnSurfaceVariant.copy(alpha = 0.70f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = AuraOnSurfaceVariant.copy(alpha = 0.70f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// -------------------------------------------------------------------------
// Aura Processing Screen
// -------------------------------------------------------------------------
@Composable
private fun AuraProcessingScreen(
    stageProgress: PipelineStageProgress?,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraSurfaceLowest),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = AuraPrimaryContainer,
                strokeWidth = 4.dp,
                modifier = Modifier.size(68.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Aura AI Neural Studio",
                color = AuraOnSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stageProgress?.modelName ?: "Loading 4K Neural Models...",
                color = AuraSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stageProgress?.stageDescription ?: "Synthesizing ultra-sharp facial features...",
                color = AuraOnSurfaceVariant,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            LinearProgressIndicator(
                progress = { stageProgress?.progressFraction ?: 0.25f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = AuraTertiaryContainer,
                trackColor = AuraSurfaceContainerHigh
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Step ${stageProgress?.stepIndex ?: 1} of ${stageProgress?.totalSteps ?: 5}",
                color = AuraOnSurfaceVariant,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(onClick = onCancel) {
                Text("Cancel", color = AuraOnSurfaceVariant)
            }
        }
    }
}

// -------------------------------------------------------------------------
// Dialogs: Credits, Settings, All Tools, Projects
// -------------------------------------------------------------------------
@Composable
private fun AuraCreditsDialog(
    credits: Int,
    onClaimBonus: () -> Unit,
    onDismiss: () -> Unit
) {
    var claimed by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = AuraSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Aura AI Credits", color = AuraOnSurface, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AuraSurfaceContainerHigh)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Current Balance", color = AuraOnSurfaceVariant, fontSize = 12.sp)
                        Text(
                            text = "$credits Credits",
                            color = AuraSecondary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text("Refills daily · 4K Pro Master Engine", color = AuraOnSurfaceVariant, fontSize = 11.sp)
                    }
                }

                Text("Credit Consumption:", color = AuraOnSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    CreditRow("AI Portrait 4K Ultra", "1 Credit")
                    CreditRow("Old Photo Scratch Repair", "1 Credit")
                    CreditRow("Passport Studio (Suit & BG)", "2 Credits")
                    CreditRow("Batch Enhance (Up to 20)", "5 Credits")
                }

                Button(
                    onClick = {
                        if (!claimed) {
                            onClaimBonus()
                            claimed = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (claimed) AuraSurfaceContainerHigh else AuraPrimaryContainer
                    ),
                    enabled = !claimed
                ) {
                    Text(if (claimed) "Claimed +10 Today!" else "Claim Free Daily Bonus (+10)")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AuraPrimary)
            }
        },
        containerColor = AuraSurfaceContainer
    )
}

@Composable
private fun CreditRow(feature: String, cost: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(feature, color = AuraOnSurfaceVariant, fontSize = 12.sp)
        Text(cost, color = AuraOnSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AuraSettingsDialog(
    onOpenArchitecture: () -> Unit,
    onDismiss: () -> Unit
) {
    var highPrecisionMesh by remember { mutableStateOf(true) }
    var autoSaveToPhotos by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = AuraPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Aura AI Preferences", color = AuraOnSurface, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("4K Ultra Precision Mesh", color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Dual-pass GPEN GAN high-frequency detail", color = AuraOnSurfaceVariant, fontSize = 11.sp)
                    }
                    Switch(
                        checked = highPrecisionMesh,
                        onCheckedChange = { highPrecisionMesh = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AuraPrimary, checkedTrackColor = AuraPrimaryContainer)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Save to Camera Roll", color = AuraOnSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Automatically export 1080p/4K master to Photos", color = AuraOnSurfaceVariant, fontSize = 11.sp)
                    }
                    Switch(
                        checked = autoSaveToPhotos,
                        onCheckedChange = { autoSaveToPhotos = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AuraPrimary, checkedTrackColor = AuraPrimaryContainer)
                    )
                }

                Button(
                    onClick = onOpenArchitecture,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AuraSurfaceContainerHigh)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = AuraSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Inspect 5-Model Neural Architecture", color = AuraOnSurface, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = AuraPrimary)
            }
        },
        containerColor = AuraSurfaceContainer
    )
}

@Composable
private fun AuraAllToolsDialog(
    onSelectTool: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("All Precision AI Tools", color = AuraOnSurface, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AllToolItem(
                    icon = Icons.Default.Badge,
                    title = "Passport Photo Studio",
                    desc = "Official US 2×2, Schengen, 10 Formal Coats & BG Swap",
                    badge = "Featured Studio",
                    badgeColor = AuraSecondary,
                    onClick = { onSelectTool("passport") }
                )
                AllToolItem(
                    icon = Icons.Default.CenterFocusStrong,
                    title = "AI Portrait 4K Ultra",
                    desc = "Crystal sharp iris gleam, pores and hair strands",
                    badge = "HD 4K",
                    badgeColor = AuraSecondary,
                    onClick = { onSelectTool("portrait_4k") }
                )
                AllToolItem(
                    icon = Icons.Default.History,
                    title = "Old Photo Restore",
                    desc = "Scratch removal, unblur & vintage tear repair",
                    badge = "Vintage",
                    badgeColor = AuraTertiary,
                    onClick = { onSelectTool("old_restore") }
                )
                AllToolItem(
                    icon = Icons.Default.Grain,
                    title = "Despeckle & Denoise",
                    desc = "Remove night ISO grain and low-light artifacts",
                    badge = "ISO Clean",
                    badgeColor = AuraOnSurfaceVariant,
                    onClick = { onSelectTool("denoise") }
                )
                AllToolItem(
                    icon = Icons.Default.Flare,
                    title = "Face Glow & Makeup",
                    desc = "Studio skin radiance, soft warmth & natural blush",
                    badge = "Beauty",
                    badgeColor = AuraTertiary,
                    onClick = { onSelectTool("beauty") }
                )
                AllToolItem(
                    icon = Icons.Default.PhotoLibrary,
                    title = "Pick Custom Photo from Gallery",
                    desc = "Upload any photo from your device library",
                    badge = "Library",
                    badgeColor = AuraPrimary,
                    onClick = { onSelectTool("custom") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AuraPrimary)
            }
        },
        containerColor = AuraSurfaceContainer
    )
}

@Composable
private fun AllToolItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    badge: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AuraSurfaceContainerHigh)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AuraSurfaceContainer)
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = AuraOnSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(badge, color = badgeColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(desc, color = AuraOnSurfaceVariant, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = AuraOnSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun AuraProjectsDialog(
    onSelectProject: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = AuraPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Saved Projects & Restorations", color = AuraOnSurface, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ProjectItemRow("Portrait_V2_4K.jpg", "Enhanced in 4K Ultra · Today", "4K UHD") { onSelectProject(0) }
                ProjectItemRow("Family_Archive_1974.png", "Vintage Restored & Unblurred · Yesterday", "RESTORED") { onSelectProject(1) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AuraPrimary)
            }
        },
        containerColor = AuraSurfaceContainer
    )
}

@Composable
private fun ProjectItemRow(name: String, desc: String, badge: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AuraSurfaceContainerHigh)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = AuraOnSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(desc, color = AuraOnSurfaceVariant, fontSize = 11.sp)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(AuraPrimary.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(badge, color = AuraPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------------------
// Result & Share Screens
// -------------------------------------------------------------------------
@Composable
private fun ReminiResultScreen(
    beforeDrawable: DrawableResource,
    afterDrawable: DrawableResource,
    customPickedBitmap: ImageBitmap?,
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
    val beforePainter: Painter = if (customPickedBitmap != null) {
        remember(customPickedBitmap) { BitmapPainter(customPickedBitmap) }
    } else {
        painterResource(beforeDrawable)
    }
    val afterPainter: Painter = if (customPickedBitmap != null) {
        remember(customPickedBitmap) { BitmapPainter(customPickedBitmap) }
    } else {
        painterResource(afterDrawable)
    }

    val liveColorFilter = remember(editParams) {
        val warmth = editParams.skinWarmth * editParams.preset.warmthBoost
        val contrast = editParams.preset.contrastLift
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
            .background(AuraSurfaceLowest)
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
                    text = "Aura 4K Studio Result",
                    color = AuraOnSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Drag slider · Double-tap for 2× Zoom",
                    color = AuraSecondary,
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
                            color = AuraTertiary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Save",
                            tint = AuraSecondary
                        )
                    }
                }
            }
        }

        // Split Comparison Slider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            BeforeAfterSlider(
                beforePainter = beforePainter,
                afterPainter = afterPainter,
                afterLabel = "4K Restored",
                colorFilter = liveColorFilter,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Bottom Controls Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.15f)
                .background(AuraSurfaceContainer)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 20.dp)
        ) {
            SkinTelemetryBar(telemetry = telemetry, onOpenArchitecture = onOpenArchitecture)

            // Aesthetic Presets Row
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text(
                    text = "Aesthetic Styles",
                    color = AuraOnSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AestheticPreset.entries.forEach { preset ->
                        val isSelected = editParams.preset == preset
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) AuraPrimaryContainer else AuraSurfaceContainerHigh)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) AuraPrimary else Color.White.copy(alpha = 0.10f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectPreset(preset) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = preset.title,
                                color = if (isSelected) Color.White else AuraOnSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Fine-tune Expander Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AuraSurfaceContainerHigh)
                    .clickable { onToggleFineTuneSliders() }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = AuraSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (showFineTuneSliders) "Hide Fine-Tune Adjustments" else "Show Fine-Tune Adjustments",
                        color = AuraOnSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = AuraOnSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = showFineTuneSliders) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    CompactSliderRow(
                        title = "Skin Warmth",
                        value = editParams.skinWarmth,
                        range = 0.8f..1.2f,
                        displayValue = "${(editParams.skinWarmth * 100).roundToInt()}%",
                        onValueChange = { v -> onUpdateParams { it.copy(skinWarmth = v) } }
                    )
                    CompactSliderRow(
                        title = "Radiance & Glow",
                        value = editParams.skinRadiance,
                        range = 0.8f..1.3f,
                        displayValue = "${(editParams.skinRadiance * 100).roundToInt()}%",
                        onValueChange = { v -> onUpdateParams { it.copy(skinRadiance = v) } }
                    )
                    CompactSliderRow(
                        title = "Texture Sharpness",
                        value = editParams.facialClarity,
                        range = 0.5f..1.2f,
                        displayValue = "${(editParams.facialClarity * 100).roundToInt()}%",
                        onValueChange = { v -> onUpdateParams { it.copy(facialClarity = v) } }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
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
                    colors = ButtonDefaults.buttonColors(containerColor = AuraPrimaryContainer),
                    enabled = !isSaving
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSaving) "Saving 4K..." else "Save to Photos",
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
                    colors = ButtonDefaults.buttonColors(containerColor = AuraSurfaceContainerHigh),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
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
            .background(AuraSurfaceContainerHigh)
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
                text = "3840×2160 4K UHD · ${telemetry.inferenceTimeMs}ms · 5 AI Models Active",
                color = AuraSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            text = "Details",
            color = AuraPrimary,
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
            Text(title, color = AuraOnSurfaceVariant, fontSize = 12.sp)
            Text(displayValue, color = AuraOnSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = AuraPrimary,
                activeTrackColor = AuraPrimary,
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
            .background(AuraSurfaceLowest)
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
            text = "Photo Restored in Ultra 4K!",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Saved to your camera roll & photos album",
            color = AuraOnSurfaceVariant,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, AuraPrimary, RoundedCornerShape(20.dp))
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
            colors = ButtonDefaults.buttonColors(containerColor = AuraPrimaryContainer)
        ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share 4K Portrait", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onEnhanceAnother,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AuraSurfaceContainerHigh)
        ) {
            Text("Enhance Another Photo", color = Color.White, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBackToResult) {
            Text("Back to Result", color = AuraOnSurfaceVariant)
        }
    }
}

@Composable
private fun ModelArchitectureDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Aura AI 5-Model Neural Architecture", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModelInfoItem("1. Real-ESRGAN x4v3 Ultra", "Super-resolves image to 3840×2160 UHD while preserving authentic edges.")
                ModelInfoItem("2. BiSeNet 19-Class Semantic Parsing", "Deep parsing map separating skin, eyes, eyebrows, nose, and lips.")
                ModelInfoItem("3. MediaPipe Full Body Sync", "Detects neck, collar, ears, and hands for uniform skin aesthetic blending.")
                ModelInfoItem("4. GPEN-BFR-256 GAN Texture Synthesizer", "Blind face restoration restoring high-frequency pores, eye gleam, and hair strands.")
                ModelInfoItem("5. Zero-DCE + YCrCb Tone Grading", "Non-destructive exposure correction and studio radiance curve synthesis.")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AuraPrimary)
            }
        },
        containerColor = AuraSurfaceContainer
    )
}

@Composable
private fun ModelInfoItem(name: String, desc: String) {
    Column {
        Text(name, color = AuraSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(desc, color = AuraOnSurfaceVariant, fontSize = 11.sp, lineHeight = 15.sp)
    }
}
