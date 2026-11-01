package com.example.reminiskinai.ui.passport

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reminiskinai.model.CoatGenderCategory
import com.example.reminiskinai.model.CoatTemplateItem
import com.example.reminiskinai.model.CountryPassportStandard
import com.example.reminiskinai.model.ExportLayoutMode
import com.example.reminiskinai.model.PassportBgColor
import com.example.reminiskinai.model.PassportCoatCatalog
import com.example.reminiskinai.model.PassportEditParams
import com.example.reminiskinai.model.PipelineStageProgress
import com.example.reminiskinai.model.SamplePhotoItem
import com.example.reminiskinai.platform.PlatformActions
import com.example.reminiskinai.platform.PlatformBackHandler
import com.example.reminiskinai.platform.rememberPhotoPickerLauncher
import com.example.reminiskinai.theme.ReminiBlack
import com.example.reminiskinai.theme.ReminiCard
import com.example.reminiskinai.theme.ReminiGold
import com.example.reminiskinai.theme.ReminiRose
import com.example.reminiskinai.theme.ReminiSurface
import com.example.reminiskinai.theme.ReminiTextPrimary
import com.example.reminiskinai.theme.ReminiTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import reminiskinai.shared.generated.resources.Res
import reminiskinai.shared.generated.resources.sample_1_before
import reminiskinai.shared.generated.resources.sample_2_before
import reminiskinai.shared.generated.resources.sample_3_before
import kotlin.math.roundToInt

enum class PassportScreenMode {
    UPLOAD_HOME,
    PROCESSING,
    STUDIO_EDITOR,
    SAVED_EXPORT
}

enum class PassportStudioTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    COATS("10 Coats", Icons.Default.Checkroom),
    CROP_ALIGN("Crop & Fit", Icons.Default.Crop),
    BACKGROUND("Background", Icons.Default.Palette),
    COUNTRY_SIZE("Country Size", Icons.Default.Public)
}

@Composable
fun PassportPhotoScreen(
    onBackToMain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    val samplePhotos = remember {
        listOf(
            SamplePhotoItem("sample_2", "Male Studio Portrait", "Frontal face, natural posture", Res.drawable.sample_2_before),
            SamplePhotoItem("sample_1", "Female Portrait", "Neutral expression, clear facial landmarks", Res.drawable.sample_1_before),
            SamplePhotoItem("sample_3", "Outdoor Portrait", "Daylight capture", Res.drawable.sample_3_before)
        )
    }

    val coatTemplates = remember { PassportCoatCatalog.templates }

    var mode by remember { mutableStateOf(PassportScreenMode.UPLOAD_HOME) }
    var activeTab by remember { mutableStateOf(PassportStudioTab.COATS) }
    var activePortraitDrawable by remember { mutableStateOf(samplePhotos[0].beforeDrawable) }
    var customPickedBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var editParams by remember { mutableStateOf(PassportEditParams()) }
    var stageProgress by remember { mutableStateOf<PipelineStageProgress?>(null) }
    var showAiInfoDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var savedFileName by remember { mutableStateOf("") }

    fun startPassportProcessing(drawable: DrawableResource) {
        customPickedBitmap = null
        activePortraitDrawable = drawable
        mode = PassportScreenMode.PROCESSING

        coroutineScope.launch {
            val stages = listOf(
                PipelineStageProgress(1, 4, "Hivision MODNet AI Matte", "Isolating subject from background with alpha feathering...", 0.25f),
                PipelineStageProgress(2, 4, "BiSeNet 19-Class Biometrics", "Extracting head center, chin contour, and eye line...", 0.50f),
                PipelineStageProgress(3, 4, "MediaPipe Multi-Class Mask", "Detecting collar alignment anchor points...", 0.75f),
                PipelineStageProgress(4, 4, "Biometric Standard Composer", "Fitting ICAO frame & studio lighting...", 1.00f)
            )

            for (stage in stages) {
                stageProgress = stage
                delay(280)
            }
            delay(120)
            stageProgress = null
            mode = PassportScreenMode.STUDIO_EDITOR
        }
    }

    val photoPicker = rememberPhotoPickerLauncher { picked ->
        if (picked != null) {
            customPickedBitmap = picked
            mode = PassportScreenMode.PROCESSING
            coroutineScope.launch {
                val stages = listOf(
                    PipelineStageProgress(1, 4, "Hivision MODNet AI Matte", "Isolating subject from background with alpha feathering...", 0.25f),
                    PipelineStageProgress(2, 4, "BiSeNet 19-Class Biometrics", "Extracting head center, chin contour, and eye line...", 0.50f),
                    PipelineStageProgress(3, 4, "MediaPipe Multi-Class Mask", "Detecting collar alignment anchor points...", 0.75f),
                    PipelineStageProgress(4, 4, "Biometric Standard Composer", "Fitting ICAO frame & studio lighting...", 1.00f)
                )

                for (stage in stages) {
                    stageProgress = stage
                    delay(280)
                }
                delay(120)
                stageProgress = null
                mode = PassportScreenMode.STUDIO_EDITOR
            }
        }
    }

    PlatformBackHandler(enabled = true) {
        when (mode) {
            PassportScreenMode.UPLOAD_HOME -> onBackToMain()
            PassportScreenMode.PROCESSING -> mode = PassportScreenMode.UPLOAD_HOME
            PassportScreenMode.STUDIO_EDITOR -> mode = PassportScreenMode.UPLOAD_HOME
            PassportScreenMode.SAVED_EXPORT -> mode = PassportScreenMode.STUDIO_EDITOR
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ReminiBlack)
    ) {
        when (mode) {
            PassportScreenMode.UPLOAD_HOME -> {
                PassportUploadHome(
                    samplePhotos = samplePhotos,
                    onSelectSample = { sample -> startPassportProcessing(sample.beforeDrawable) },
                    onPickPhoto = { photoPicker() },
                    onBack = onBackToMain,
                    onOpenInfo = { showAiInfoDialog = true }
                )
            }
            PassportScreenMode.PROCESSING -> {
                PassportProcessingView(stageProgress = stageProgress, onCancel = { mode = PassportScreenMode.UPLOAD_HOME })
            }
            PassportScreenMode.STUDIO_EDITOR -> {
                PassportStudioEditor(
                    portraitDrawable = activePortraitDrawable,
                    customPickedBitmap = customPickedBitmap,
                    coatTemplates = coatTemplates,
                    editParams = editParams,
                    activeTab = activeTab,
                    onTabSelected = { activeTab = it },
                    onUpdateParams = { editParams = it },
                    onBack = { mode = PassportScreenMode.UPLOAD_HOME },
                    onExport = {
                        isSaving = true
                        PlatformActions.saveImageToGallery(image = customPickedBitmap, fileName = "Passport_${editParams.countryStandard.name}.png") { _, fileName ->
                            isSaving = false
                            savedFileName = fileName ?: "Passport_Photo_300DPI.png"
                            mode = PassportScreenMode.SAVED_EXPORT
                        }
                    },
                    onShare = {
                        PlatformActions.shareImage("Official ${editParams.countryStandard.countryName} Passport Photo generated with ReminiSkinAI Passport Studio!")
                    },
                    onOpenInfo = { showAiInfoDialog = true }
                )
            }
            PassportScreenMode.SAVED_EXPORT -> {
                PassportSavedExport(
                    portraitDrawable = activePortraitDrawable,
                    customPickedBitmap = customPickedBitmap,
                    coatTemplates = coatTemplates,
                    editParams = editParams,
                    fileName = savedFileName,
                    onBackToEditor = { mode = PassportScreenMode.STUDIO_EDITOR },
                    onBackToHome = { mode = PassportScreenMode.UPLOAD_HOME },
                    onShare = {
                        PlatformActions.shareImage("Official ${editParams.countryStandard.countryName} Passport Photo generated with ReminiSkinAI Passport Studio!")
                    }
                )
            }
        }

        if (showAiInfoDialog) {
            PassportAiModelsDialog(onDismiss = { showAiInfoDialog = false })
        }
    }
}

@Composable
private fun PassportUploadHome(
    samplePhotos: List<SamplePhotoItem>,
    onSelectSample: (SamplePhotoItem) -> Unit,
    onPickPhoto: () -> Unit,
    onBack: () -> Unit,
    onOpenInfo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text("Passport Photo Studio", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text("ICAO & ISO Biometric Compliance", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            IconButton(onClick = onOpenInfo) {
                Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = ReminiTextSecondary)
            }
        }

        // Feature Highlights Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E293B))
                .padding(18.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0284C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("All-In-One Biometric ID Studio", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("10 Formal Suits · Pure White Background · US/EU/UK Sizes", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StudioBadgeItem("10 Formal Suits", "Men & Women")
                    StudioBadgeItem("Pure White BG", "Instant Cutout")
                    StudioBadgeItem("300 DPI Lab Print", "Single or 4×6 Grid")
                }
            }
        }

        // Upload Button
        Button(
            onClick = onPickPhoto,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp)
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
        ) {
            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(10.dp))
            Text("Select Photo from Camera Roll", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        // Sample Section
        Text(
            text = "Or Select a Portrait to Try Passport Studio",
            color = ReminiTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
        )

        samplePhotos.forEach { sample ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ReminiCard)
                    .clickable { onSelectSample(sample) }
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Image(
                            painter = painterResource(sample.beforeDrawable),
                            contentDescription = sample.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(sample.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(sample.subtitle, color = ReminiTextSecondary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Create Official ID Photo →", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioBadgeItem(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F172A))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Color(0xFF64748B), fontSize = 9.sp)
    }
}

@Composable
private fun PassportProcessingView(
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
            CircularProgressIndicator(color = Color(0xFF38BDF8), strokeWidth = 4.dp, modifier = Modifier.size(68.dp))
            Spacer(modifier = Modifier.height(28.dp))
            Text("Passport Biometric AI Studio", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(stageProgress?.modelName ?: "Initializing models...", color = Color(0xFF38BDF8), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(6.dp))
            Text(stageProgress?.stageDescription ?: "Processing biometric alignments...", color = ReminiTextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            LinearProgressIndicator(
                progress = { stageProgress?.progressFraction ?: 0.25f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF38BDF8),
                trackColor = ReminiCard
            )
            Spacer(modifier = Modifier.height(32.dp))
            TextButton(onClick = onCancel) {
                Text("Cancel", color = ReminiTextSecondary)
            }
        }
    }
}

@Composable
private fun PassportStudioEditor(
    portraitDrawable: DrawableResource,
    customPickedBitmap: ImageBitmap?,
    coatTemplates: List<CoatTemplateItem>,
    editParams: PassportEditParams,
    activeTab: PassportStudioTab,
    onTabSelected: (PassportStudioTab) -> Unit,
    onUpdateParams: (PassportEditParams) -> Unit,
    onBack: () -> Unit,
    onExport: () -> Unit,
    onShare: () -> Unit,
    onOpenInfo: () -> Unit
) {
    val selectedCoat = remember(editParams.selectedCoatId) {
        coatTemplates.firstOrNull { it.id == editParams.selectedCoatId }
    }

    val portraitPainter: Painter = if (customPickedBitmap != null) {
        remember(customPickedBitmap) { BitmapPainter(customPickedBitmap) }
    } else {
        painterResource(portraitDrawable)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReminiBlack)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${editParams.countryStandard.countryName} (${editParams.countryStandard.specLabel})",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "300 DPI Official Biometric Fit",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onShare) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                }
                IconButton(onClick = onExport) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = "Export", tint = Color(0xFF38BDF8))
                }
            }
        }

        // Live Passport Canvas View (44% of vertical space)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .padding(horizontal = 20.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(editParams.countryStandard.aspectRatio)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(editParams.bgColor.r, editParams.bgColor.g, editParams.bgColor.b))
                    .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            ) {
                // Portrait Layer
                Image(
                    painter = portraitPainter,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = (editParams.photoOffsetXNorm * 60).dp,
                            top = (editParams.photoOffsetYNorm * 60).dp
                        ),
                    contentScale = ContentScale.Crop
                )

                // Formal Coat Overlay Layer
                if (selectedCoat != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f * editParams.coatScale)
                            .align(Alignment.BottomCenter)
                            .padding(bottom = (editParams.coatOffsetYNorm * 40).dp)
                    ) {
                        Image(
                            painter = painterResource(selectedCoat.drawable),
                            contentDescription = selectedCoat.title,
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }

                // ICAO Biometric Guide Lines Overlay
                if (editParams.showBiometricGuides) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val crownY = size.height * editParams.countryStandard.targetCrownTopRatio
                        val chinY = crownY + size.height * editParams.countryStandard.targetHeadHeightRatio
                        val eyeY = crownY + (chinY - crownY) * 0.45f
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                        // Crown guide line
                        drawLine(
                            color = Color(0xFFEF4444),
                            start = Offset(0f, crownY),
                            end = Offset(size.width, crownY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = dashEffect
                        )
                        // Eye line
                        drawLine(
                            color = Color(0xFF22C55E),
                            start = Offset(0f, eyeY),
                            end = Offset(size.width, eyeY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = dashEffect
                        )
                        // Chin line
                        drawLine(
                            color = Color(0xFF38BDF8),
                            start = Offset(0f, chinY),
                            end = Offset(size.width, chinY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = dashEffect
                        )
                    }
                }
            }
        }

        // Studio Navigation Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF14141A))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            PassportStudioTab.entries.forEach { tab ->
                val isSelected = activeTab == tab
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = if (isSelected) Color(0xFF38BDF8) else ReminiTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.title,
                        color = if (isSelected) Color.White else ReminiTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Bottom Controls Container (Scrollable)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.15f)
                .background(ReminiSurface)
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
        ) {
            when (activeTab) {
                PassportStudioTab.COATS -> {
                    CoatsSelectorPanel(
                        coatTemplates = coatTemplates,
                        selectedCoatId = editParams.selectedCoatId,
                        onSelectCoat = { coatId -> onUpdateParams(editParams.copy(selectedCoatId = coatId)) }
                    )
                }
                PassportStudioTab.CROP_ALIGN -> {
                    CropFitPanel(
                        params = editParams,
                        onUpdate = onUpdateParams
                    )
                }
                PassportStudioTab.BACKGROUND -> {
                    BackgroundPanel(
                        selectedBg = editParams.bgColor,
                        onSelectBg = { bg -> onUpdateParams(editParams.copy(bgColor = bg)) }
                    )
                }
                PassportStudioTab.COUNTRY_SIZE -> {
                    CountrySizePanel(
                        currentStandard = editParams.countryStandard,
                        currentLayout = editParams.exportLayoutMode,
                        onSelectStandard = { std -> onUpdateParams(editParams.copy(countryStandard = std)) },
                        onSelectLayout = { layout -> onUpdateParams(editParams.copy(exportLayoutMode = layout)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Export Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onExport,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Passport Photo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Button(
                    onClick = onShare,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ReminiCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun CoatsSelectorPanel(
    coatTemplates: List<CoatTemplateItem>,
    selectedCoatId: String?,
    onSelectCoat: (String?) -> Unit
) {
    var genderFilter by remember { mutableStateOf(CoatGenderCategory.ALL) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("10 Formal Suits & Blazers", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CoatGenderCategory.entries.forEach { cat ->
                    val isSel = genderFilter == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isSel) Color(0xFF0284C7) else ReminiCard)
                            .clickable { genderFilter = cat }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(cat.label, color = if (isSel) Color.White else ReminiTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Coats horizontal row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // "Original Clothes" card
            val isOriginal = selectedCoatId == null
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isOriginal) Color(0xFF0284C7).copy(alpha = 0.2f) else ReminiCard)
                    .border(
                        width = if (isOriginal) 2.dp else 1.dp,
                        color = if (isOriginal) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelectCoat(null) }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Original", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Authentic", color = ReminiTextSecondary, fontSize = 10.sp)
                }
            }

            val filteredCoats = coatTemplates.filter {
                genderFilter == CoatGenderCategory.ALL || it.category == genderFilter
            }

            filteredCoats.forEach { coat ->
                val isSelected = selectedCoatId == coat.id
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) Color(0xFF0284C7).copy(alpha = 0.2f) else ReminiCard)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectCoat(coat.id) }
                        .padding(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                        ) {
                            Image(
                                painter = painterResource(coat.drawable),
                                contentDescription = coat.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(coat.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(coat.subtitle, color = ReminiTextSecondary, fontSize = 9.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun CropFitPanel(
    params: PassportEditParams,
    onUpdate: (PassportEditParams) -> Unit
) {
    Column {
        Text("Biometric Crop & Positioning", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        // ICAO Guide Lines Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ReminiCard)
                .clickable { onUpdate(params.copy(showBiometricGuides = !params.showBiometricGuides)) }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Show ICAO Biometric Guide Lines", color = Color.White, fontSize = 13.sp)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (params.showBiometricGuides) Color(0xFF22C55E) else Color(0xFF334155))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(if (params.showBiometricGuides) "ON" else "OFF", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Zoom & Pan controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onUpdate(params.copy(photoScale = (params.photoScale + 0.1f).coerceAtMost(1.8f))) },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ReminiCard)
            ) {
                Icon(imageVector = Icons.Default.ZoomIn, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Zoom In", fontSize = 12.sp)
            }

            Button(
                onClick = { onUpdate(params.copy(photoScale = (params.photoScale - 0.1f).coerceAtLeast(0.6f))) },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ReminiCard)
            ) {
                Icon(imageVector = Icons.Default.ZoomOut, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Zoom Out", fontSize = 12.sp)
            }

            Button(
                onClick = { onUpdate(params.copy(photoScale = 1.0f, photoOffsetXNorm = 0f, photoOffsetYNorm = 0f)) },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ReminiCard)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun BackgroundPanel(
    selectedBg: PassportBgColor,
    onSelectBg: (PassportBgColor) -> Unit
) {
    Column {
        Text("Official Background Color", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PassportBgColor.entries.forEach { bg ->
                val isSelected = selectedBg == bg
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ReminiCard)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectBg(bg) }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(bg.r, bg.g, bg.b))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(bg.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(bg.hexCode, color = ReminiTextSecondary, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CountrySizePanel(
    currentStandard: CountryPassportStandard,
    currentLayout: ExportLayoutMode,
    onSelectStandard: (CountryPassportStandard) -> Unit,
    onSelectLayout: (ExportLayoutMode) -> Unit
) {
    Column {
        Text("Country & Visa Standard Size", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        // Layout mode toggle: Single photo vs. 4x6 print sheet
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExportLayoutMode.entries.forEach { layout ->
                val isSel = currentLayout == layout
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) Color(0xFF0284C7) else ReminiCard)
                        .clickable { onSelectLayout(layout) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (layout == ExportLayoutMode.SINGLE_PASSPORT) Icons.Default.Verified else Icons.Default.GridOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(layout.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Country size list
        CountryPassportStandard.entries.forEach { std ->
            val isSelected = currentStandard == std
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color(0xFF0284C7).copy(alpha = 0.25f) else ReminiCard)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelectStandard(std) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(std.flagEmoji, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(std.countryName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(std.specLabel, color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }

                if (isSelected) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun PassportSavedExport(
    portraitDrawable: DrawableResource,
    customPickedBitmap: ImageBitmap?,
    coatTemplates: List<CoatTemplateItem>,
    editParams: PassportEditParams,
    fileName: String,
    onBackToEditor: () -> Unit,
    onBackToHome: () -> Unit,
    onShare: () -> Unit
) {
    val selectedCoat = remember(editParams.selectedCoatId) {
        coatTemplates.firstOrNull { it.id == editParams.selectedCoatId }
    }

    val portraitPainter: Painter = if (customPickedBitmap != null) {
        remember(customPickedBitmap) { BitmapPainter(customPickedBitmap) }
    } else {
        painterResource(portraitDrawable)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReminiBlack)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(0xFF0284C7).copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(36.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Passport Photo Saved!", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("300 DPI Biometric Lab Standard Saved to Photos", color = ReminiTextSecondary, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(220.dp)
                .aspectRatio(editParams.countryStandard.aspectRatio)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(editParams.bgColor.r, editParams.bgColor.g, editParams.bgColor.b))
                .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(12.dp))
        ) {
            Image(
                painter = portraitPainter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            if (selectedCoat != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .align(Alignment.BottomCenter)
                ) {
                    Image(
                        painter = painterResource(selectedCoat.drawable),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onShare,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
        ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share Official Passport Photo", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ReminiCard)
        ) {
            Text("Back to Main Studio", color = Color.White, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBackToEditor) {
            Text("Back to Adjustments", color = ReminiTextSecondary)
        }
    }
}

@Composable
private fun PassportAiModelsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Passport Studio AI Engines", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PassportModelItem("1. Hivision MODNet Alpha Matte", "Trimap-free deep learning portrait segmentation with sub-pixel alpha feathering.")
                PassportModelItem("2. BiSeNet 19-Class Landmark Parsing", "Locates crown, chin contour, eyes, and ear tips for ICAO-compliant cropping.")
                PassportModelItem("3. MediaPipe Collar Alignment Anchor", "Automatically detects the junction of neck and collar to anchor suit templates.")
                PassportModelItem("4. 300 DPI Lab Print Generator", "Calculates exact millimeter-to-pixel ratios for US 2×2, Schengen 35×45, and 4×6 print sheets.")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF38BDF8))
            }
        },
        containerColor = Color(0xFF1E293B)
    )
}

@Composable
private fun PassportModelItem(name: String, desc: String) {
    Column {
        Text(name, color = Color(0xFF38BDF8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(desc, color = Color(0xFF94A3B8), fontSize = 11.sp, lineHeight = 15.sp)
    }
}
