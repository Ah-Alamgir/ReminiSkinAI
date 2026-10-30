package com.example.reminiskinai.ui.passport

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled. Compare
import androidx.compose.material.icons.filled.CropRotate
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reminiskinai.ml.CoatGenderCategory
import com.example.reminiskinai.ml.CountryPassportStandard
import com.example.reminiskinai.ml.ExportLayoutMode
import com.example.reminiskinai.ml.LoadedCoatAsset
import com.example.reminiskinai.ml.PassportBgColor
import com.example.reminiskinai.ml.PassportEditParams
import com.example.reminiskinai.ml.TransformTargetMode
import com.example.reminiskinai.theme.ReminiBlack
import com.example.reminiskinai.theme.ReminiCard
import com.example.reminiskinai.theme.ReminiCrimson
import com.example.reminiskinai.theme.ReminiDivider
import com.example.reminiskinai.theme.ReminiGold
import com.example.reminiskinai.theme.ReminiRose
import com.example.reminiskinai.theme.ReminiSurface
import com.example.reminiskinai.theme.ReminiTextPrimary
import com.example.reminiskinai.theme.ReminiTextSecondary
import com.example.reminiskinai.ui.main.SampleThumb
import kotlin.math.roundToInt

@Composable
fun PassportPhotoScreen(
    onBackToMain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: PassportPhotoViewModel = viewModel {
        PassportPhotoViewModel(context.applicationContext)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onGalleryPhotoPicked(uri)
        }
    }

    if (state.showModelInfoDialog) {
        PassportAiModelsDialog(
            onDismiss = { viewModel.toggleModelInfoDialog(false) }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ReminiBlack)
    ) {
        when (state.mode) {
            PassportScreenMode.UPLOAD_HOME -> {
                BackHandler { onBackToMain() }
                PassportUploadHomeView(
                    state = state,
                    onBack = onBackToMain,
                    onPickFromGallery = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onSelectSample = { viewModel.onSamplePhotoSelected(it) },
                    onOpenModelInfo = { viewModel.toggleModelInfoDialog(true) }
                )
            }

            PassportScreenMode.PROCESSING -> {
                BackHandler { viewModel.backToUploadHome() }
                PassportProcessingView(
                    state = state,
                    onCancel = { viewModel.backToUploadHome() }
                )
            }

            PassportScreenMode.STUDIO_EDITOR -> {
                BackHandler { viewModel.backToUploadHome() }
                PassportStudioEditorView(
                    state = state,
                    onBack = { viewModel.backToUploadHome() },
                    onPickNewPhoto = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onSelectTab = { viewModel.selectStudioTab(it) },
                    onSelectCoat = { viewModel.selectCoat(it) },
                    onSelectGenderFilter = { viewModel.selectGenderFilter(it) },
                    onSelectBgColor = { viewModel.selectBgColor(it) },
                    onSelectCountry = { viewModel.selectCountryStandard(it) },
                    onSelectTransformTarget = { viewModel.selectTransformTarget(it) },
                    onSelectExportLayout = { viewModel.selectExportLayoutMode(it) },
                    onUpdateParams = { viewModel.updateParams(it) },
                    onCanvasGesture = { panX, panY, zoom, rot ->
                        viewModel.onCanvasTransformGesture(panX, panY, zoom, rot)
                    },
                    onResetPhotoAuto = { viewModel.resetPhotoAlignmentToAuto() },
                    onResetCoatAuto = { viewModel.resetCoatAlignmentToAuto() },
                    onToggleCompare = { viewModel.toggleCompareOriginal(!state.showCompareOriginal) },
                    onExport = { viewModel.exportPassportPhoto() },
                    onOpenModelInfo = { viewModel.toggleModelInfoDialog(true) }
                )
            }

            PassportScreenMode.SAVED_EXPORT -> {
                BackHandler { viewModel.backToStudioFromExport() }
                PassportSavedExportView(
                    state = state,
                    onBackToEditor = { viewModel.backToStudioFromExport() },
                    onNewPhoto = { viewModel.backToUploadHome() },
                    onShare = { viewModel.shareExportedPhoto(context) }
                )
            }
        }
    }
}

// ============================================================================
// 1. PASSPORT UPLOAD & HOME VIEW
// ============================================================================
@Composable
private fun PassportUploadHomeView(
    state: PassportUiState,
    onBack: () -> Unit,
    onPickFromGallery: () -> Unit,
    onSelectSample: (SampleThumb) -> Unit,
    onOpenModelInfo: () -> Unit
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ReminiCard)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Passport AI Studio",
                        color = Color.White,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF2563EB))
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "300 DPI",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Text(
                    text = "Hivision MODNet + BiSeNet + 10 Nano Banana 2 Coats",
                    color = ReminiTextSecondary,
                    fontSize = 11.sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(ReminiCard)
                    .border(1.dp, ReminiDivider, RoundedCornerShape(50))
                    .clickable { onOpenModelInfo() }
                    .padding(horizontal = 11.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "AI Models",
                        tint = ReminiGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Models",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Upload Hero Banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF1E1B4B))
                    )
                )
                .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.45f), RoundedCornerShape(24.dp))
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2563EB).copy(alpha = 0.22f))
                    .border(1.5.dp, Color(0xFF60A5FA), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    tint = Color(0xFF93C5FD),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Upload Photo for Passport ID",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Automatically removes background to Pure White, levels eye tilt, fits 10 formal Men's & Women's coats, and crops to official country specs.",
                color = ReminiTextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onPickFromGallery,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = ReminiBlack
                ),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Upload Photo from Gallery",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Quick Test with Built-in Sample Portraits
        Column(modifier = Modifier.padding(horizontal = 18.dp)) {
            Text(
                text = "Or Try Instant Demo with Sample Portraits",
                color = ReminiTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap any portrait below to test background removal + coat alignment:",
                color = ReminiTextSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                state.sampleThumbs.forEach { thumb ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.76f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, ReminiDivider, RoundedCornerShape(16.dp))
                            .clickable { onSelectSample(thumb) }
                    ) {
                        Image(
                            bitmap = remember(thumb.bitmap) { thumb.bitmap.asImageBitmap() },
                            contentDescription = thumb.item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                    )
                                )
                                .padding(8.dp)
                        ) {
                            Text(
                                text = thumb.item.title,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Showcase of the 10 Nano Banana 2 Men's & Women's Coats
        Column(modifier = Modifier.padding(horizontal = 18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "10 Formal Coats (Nano Banana 2)",
                    color = ReminiTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "5 Men · 5 Women",
                    color = ReminiGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.coatAssets, key = { it.item.id }) { coat ->
                Column(
                    modifier = Modifier
                        .width(114.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ReminiCard)
                        .border(1.dp, ReminiDivider, RoundedCornerShape(16.dp))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = remember(coat.bitmap) { coat.bitmap.asImageBitmap() },
                            contentDescription = coat.item.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = coat.item.title,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = coat.item.subtitle,
                        color = ReminiTextSecondary,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Supported Country Standards Preview Strip
        Column(modifier = Modifier.padding(horizontal = 18.dp)) {
            Text(
                text = "10 Official Country Sizes (300 DPI)",
                color = ReminiTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CountryPassportStandard.entries.forEach { std ->
                    Column(
                        modifier = Modifier
                            .width(148.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ReminiCard)
                            .border(1.dp, ReminiDivider, RoundedCornerShape(14.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "${std.flagEmoji} ${std.countryName}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = std.specLabel,
                            color = ReminiGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${std.widthPx300Dpi}×${std.heightPx300Dpi} px · Head ${(std.targetHeadHeightRatio * 100).roundToInt()}%",
                            color = ReminiTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 2. PASSPORT AI PROCESSING VIEW
// ============================================================================
@Composable
private fun PassportProcessingView(
    state: PassportUiState,
    onCancel: () -> Unit
) {
    val bmp = state.sourceBitmap
    val progress = state.stageProgress
    val desaturateMatrix = remember { ColorMatrix().apply { setToSaturation(0.18f) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReminiBlack)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(ReminiSurface)
        ) {
            if (bmp != null) {
                Image(
                    bitmap = remember(bmp) { bmp.asImageBitmap() },
                    contentDescription = "Processing passport photo",
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.colorMatrix(desaturateMatrix),
                    alpha = 0.38f,
                    modifier = Modifier.fillMaxSize()
                )
            }

            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF60A5FA),
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = progress?.modelName ?: "Running Passport AI...",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = progress?.stageDescription ?: "Removing background & aligning coat...",
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { progress?.progressFraction ?: 0.25f },
                    color = Color(0xFF3B82F6),
                    trackColor = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier
                        .width(220.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Step ${progress?.stepIndex ?: 1} of ${progress?.totalSteps ?: 4} · 100% On-Device ONNX + LiteRT",
                    color = ReminiGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Surface(
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Removing background to Pure White (#FFFFFF), leveling eyes,\nand aligning Nano Banana 2 formal suit collar to neck.",
                color = ReminiBlack,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
            )
        }
    }
}

// ============================================================================
// 3. INTERACTIVE PASSPORT STUDIO EDITOR VIEW
// ============================================================================
@Composable
private fun PassportStudioEditorView(
    state: PassportUiState,
    onBack: () -> Unit,
    onPickNewPhoto: () -> Unit,
    onSelectTab: (PassportStudioTab) -> Unit,
    onSelectCoat: (String?) -> Unit,
    onSelectGenderFilter: (CoatGenderCategory) -> Unit,
    onSelectBgColor: (PassportBgColor) -> Unit,
    onSelectCountry: (CountryPassportStandard) -> Unit,
    onSelectTransformTarget: (TransformTargetMode) -> Unit,
    onSelectExportLayout: (ExportLayoutMode) -> Unit,
    onUpdateParams: ((PassportEditParams) -> PassportEditParams) -> Unit,
    onCanvasGesture: (Float, Float, Float, Float) -> Unit,
    onResetPhotoAuto: () -> Unit,
    onResetCoatAuto: () -> Unit,
    onToggleCompare: () -> Unit,
    onExport: () -> Unit,
    onOpenModelInfo: () -> Unit
) {
    val params = state.editParams
    val std = params.countryStandard
    val displayBmp = if (state.showCompareOriginal) {
        state.sourceBitmap
    } else {
        state.renderedPassportBitmap ?: state.sourceBitmap
    } ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReminiBlack)
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${std.flagEmoji} ${std.countryName}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${std.specLabel} · ${std.widthPx300Dpi}×${std.heightPx300Dpi}px · Bg ${params.bgColor.title}",
                    color = ReminiGold,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Biometric Guide Grid Toggle
            IconButton(
                onClick = { onUpdateParams { it.copy(showBiometricGuides = !it.showBiometricGuides) } },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GridOn,
                    contentDescription = "Biometric Guides",
                    tint = if (params.showBiometricGuides) Color(0xFF34D399) else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Compare Original Toggle
            IconButton(
                onClick = onToggleCompare,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Compare,
                    contentDescription = "Compare Original",
                    tint = if (state.showCompareOriginal) ReminiRose else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Export Button
            Button(
                onClick = onExport,
                enabled = !state.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = ReminiBlack
                ),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.height(38.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        color = ReminiBlack,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Export Passport Photo",
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Export",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Interactive Touch Hint + Quick Mode Switch Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(ReminiSurface)
                    .border(1.dp, ReminiDivider, RoundedCornerShape(50))
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TransformTargetMode.entries.forEach { mode ->
                    val selected = params.transformTarget == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) Color(0xFF2563EB) else Color.Transparent)
                            .clickable { onSelectTransformTarget(mode) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = mode.title,
                            color = if (selected) Color.White else ReminiTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Change Photo",
                    color = Color(0xFF60A5FA),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onPickNewPhoto() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                IconButton(
                    onClick = onOpenModelInfo,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = ReminiGold,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        // Center Passport Photo Interactive Preview Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF121318))
                .border(1.dp, ReminiDivider, RoundedCornerShape(22.dp))
                .pointerInput(params.transformTarget) {
                    detectTransformGestures { _, pan, zoom, rotation ->
                        val normX = (pan.x / size.width.coerceAtLeast(1)).coerceIn(-0.15f, 0.15f)
                        val normY = (pan.y / size.height.coerceAtLeast(1)).coerceIn(-0.15f, 0.15f)
                        val safeZoom = zoom.coerceIn(0.85f, 1.18f)
                        val safeRot = rotation.coerceIn(-8f, 8f)
                        onCanvasGesture(normX, normY, safeZoom, safeRot)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Physical Passport Card Frame with drop border
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.92f)
                    .aspectRatio(std.aspectRatio)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(2.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
            ) {
                Image(
                    bitmap = remember(displayBmp) { displayBmp.asImageBitmap() },
                    contentDescription = "Passport Photo Preview",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Bottom-left badge showing active gesture mode
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.68f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (state.showCompareOriginal) {
                        "Original Uploaded Photo"
                    } else {
                        "Pinch / Drag to ${params.transformTarget.title}"
                    },
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Bottom-right dimension badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.68f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${std.widthMm.roundToInt()}×${std.heightMm.roundToInt()} mm",
                    color = ReminiGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Active Tab Control Panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(ReminiSurface)
                .border(1.dp, ReminiDivider, RoundedCornerShape(20.dp))
                .padding(12.dp)
        ) {
            when (state.activeTab) {
                PassportStudioTab.COATS -> {
                    CoatsSelectorPanel(
                        coatAssets = state.coatAssets,
                        selectedCoatId = params.selectedCoatId,
                        genderFilter = params.genderFilter,
                        onSelectGenderFilter = onSelectGenderFilter,
                        onSelectCoat = onSelectCoat,
                        onOpenCoatFitSliders = {
                            onSelectTransformTarget(TransformTargetMode.COAT)
                            onSelectTab(PassportStudioTab.CROP_ALIGN)
                        }
                    )
                }

                PassportStudioTab.CROP_ALIGN -> {
                    CropRotateScalePanel(
                        params = params,
                        autoEyeTiltDeg = state.geometry?.autoEyeTiltDeg ?: 0f,
                        onSelectTransformTarget = onSelectTransformTarget,
                        onUpdateParams = onUpdateParams,
                        onResetPhotoAuto = onResetPhotoAuto,
                        onResetCoatAuto = onResetCoatAuto
                    )
                }

                PassportStudioTab.BACKGROUND -> {
                    BackgroundColorPanel(
                        params = params,
                        onSelectBgColor = onSelectBgColor,
                        onUpdateParams = onUpdateParams
                    )
                }

                PassportStudioTab.COUNTRY_SIZE -> {
                    CountryStandardAndExportPanel(
                        params = params,
                        onSelectCountry = onSelectCountry,
                        onSelectExportLayout = onSelectExportLayout
                    )
                }
            }
        }

        // Bottom Studio Navigation Bar (4 Tabs)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudioTabButton(
                title = "10 Coats",
                icon = Icons.Default.Checkroom,
                selected = state.activeTab == PassportStudioTab.COATS,
                badge = if (params.selectedCoatId != null) "ON" else null,
                onClick = { onSelectTab(PassportStudioTab.COATS) }
            )
            StudioTabButton(
                title = "Crop & Fit",
                icon = Icons.Default.CropRotate,
                selected = state.activeTab == PassportStudioTab.CROP_ALIGN,
                onClick = { onSelectTab(PassportStudioTab.CROP_ALIGN) }
            )
            StudioTabButton(
                title = "Background",
                icon = Icons.Default.Palette,
                selected = state.activeTab == PassportStudioTab.BACKGROUND,
                onClick = { onSelectTab(PassportStudioTab.BACKGROUND) }
            )
            StudioTabButton(
                title = "Country Size",
                icon = Icons.Default.Public,
                selected = state.activeTab == PassportStudioTab.COUNTRY_SIZE,
                badge = std.flagEmoji,
                onClick = { onSelectTab(PassportStudioTab.COUNTRY_SIZE) }
            )
        }
    }
}

// ============================================================================
// TAB 1: 10 NANO BANANA 2 COATS SELECTOR (5 MEN + 5 WOMEN + NONE)
// ============================================================================
@Composable
private fun CoatsSelectorPanel(
    coatAssets: List<LoadedCoatAsset>,
    selectedCoatId: String?,
    genderFilter: CoatGenderCategory,
    onSelectGenderFilter: (CoatGenderCategory) -> Unit,
    onSelectCoat: (String?) -> Unit,
    onOpenCoatFitSliders: () -> Unit
) {
    val filteredCoats = remember(coatAssets, genderFilter) {
        when (genderFilter) {
            CoatGenderCategory.ALL -> coatAssets
            CoatGenderCategory.MEN -> coatAssets.filter { it.item.category == CoatGenderCategory.MEN }
            CoatGenderCategory.WOMEN -> coatAssets.filter { it.item.category == CoatGenderCategory.WOMEN }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CoatGenderCategory.entries.forEach { cat ->
                    val isSelected = genderFilter == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) Color.White else ReminiCard)
                            .border(1.dp, if (isSelected) Color.White else ReminiDivider, RoundedCornerShape(50))
                            .clickable { onSelectGenderFilter(cat) }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = cat.label,
                            color = if (isSelected) ReminiBlack else ReminiTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (selectedCoatId != null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF2563EB).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF60A5FA), RoundedCornerShape(50))
                        .clickable { onOpenCoatFitSliders() }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CropRotate,
                        contentDescription = null,
                        tint = Color(0xFF93C5FD),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Adjust Coat Fit",
                        color = Color(0xFF93C5FD),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            // "Original Clothes / No Coat" Card
            item(key = "no_coat") {
                val isNone = selectedCoatId == null
                Column(
                    modifier = Modifier
                        .width(92.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isNone) Color.White.copy(alpha = 0.14f) else ReminiCard)
                        .border(
                            width = if (isNone) 2.dp else 1.dp,
                            color = if (isNone) ReminiRose else ReminiDivider,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectCoat(null) }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ReminiBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "No Coat",
                            tint = ReminiTextSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "No Coat",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "Original Top",
                        color = ReminiTextSecondary,
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                }
            }

            // 10 Nano Banana 2 Men's & Women's Coats
            items(filteredCoats, key = { it.item.id }) { coat ->
                val isSelected = selectedCoatId == coat.item.id
                Column(
                    modifier = Modifier
                        .width(98.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) Color(0xFF2563EB).copy(alpha = 0.22f) else ReminiCard)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(0xFF60A5FA) else ReminiDivider,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectCoat(coat.item.id) }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = remember(coat.bitmap) { coat.bitmap.asImageBitmap() },
                            contentDescription = coat.item.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2563EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = coat.item.title,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = coat.item.subtitle,
                        color = ReminiTextSecondary,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ============================================================================
// TAB 2: CROP / ROTATE / SCALE PANEL (PHOTO & COAT ALIGNMENT)
// ============================================================================
@Composable
private fun CropRotateScalePanel(
    params: PassportEditParams,
    autoEyeTiltDeg: Float,
    onSelectTransformTarget: (TransformTargetMode) -> Unit,
    onUpdateParams: ((PassportEditParams) -> PassportEditParams) -> Unit,
    onResetPhotoAuto: () -> Unit,
    onResetCoatAuto: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Mode Switch Header + Reset Auto-Fit Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TransformTargetMode.entries.forEach { mode ->
                    val isSelected = params.transformTarget == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) Color.White else ReminiCard)
                            .border(1.dp, if (isSelected) Color.White else ReminiDivider, RoundedCornerShape(50))
                            .clickable { onSelectTransformTarget(mode) }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = mode.title,
                            color = if (isSelected) ReminiBlack else ReminiTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(ReminiCard)
                    .border(1.dp, ReminiDivider, RoundedCornerShape(50))
                    .clickable {
                        if (params.transformTarget == TransformTargetMode.PHOTO) {
                            onResetPhotoAuto()
                        } else {
                            onResetCoatAuto()
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Auto Fit",
                    tint = ReminiGold,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Auto-Fit",
                    color = ReminiGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (params.transformTarget == TransformTargetMode.PHOTO) {
            PassportSliderItem(
                label = "Photo Scale / Zoom",
                valueText = "${(params.photoScale * 100).roundToInt()}%",
                value = params.photoScale,
                valueRange = 0.55f..1.85f,
                onValueChange = { v -> onUpdateParams { it.copy(photoScale = v) } }
            )
            PassportSliderItem(
                label = "Rotate Photo (Auto Eye-Level: ${String.format("%.1f°", autoEyeTiltDeg)})",
                valueText = "${String.format("%+.1f°", params.photoRotationDeg)}",
                value = params.photoRotationDeg,
                valueRange = -25f..25f,
                onValueChange = { v -> onUpdateParams { it.copy(photoRotationDeg = v) } }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    PassportSliderItem(
                        label = "Pan Left / Right (X)",
                        valueText = "${(params.photoOffsetXNorm * 100).roundToInt()}%",
                        value = params.photoOffsetXNorm,
                        valueRange = -0.45f..0.45f,
                        onValueChange = { v -> onUpdateParams { it.copy(photoOffsetXNorm = v) } }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    PassportSliderItem(
                        label = "Pan Up / Down (Y)",
                        valueText = "${(params.photoOffsetYNorm * 100).roundToInt()}%",
                        value = params.photoOffsetYNorm,
                        valueRange = -0.45f..0.45f,
                        onValueChange = { v -> onUpdateParams { it.copy(photoOffsetYNorm = v) } }
                    )
                }
            }
        } else {
            PassportSliderItem(
                label = "Coat Scale / Shoulder Width",
                valueText = "${(params.coatScale * 100).roundToInt()}%",
                value = params.coatScale,
                valueRange = 0.65f..1.50f,
                onValueChange = { v -> onUpdateParams { it.copy(coatScale = v) } }
            )
            PassportSliderItem(
                label = "Collar Vertical Fit (Up / Down)",
                valueText = "${(params.coatOffsetYNorm * 100).roundToInt()}%",
                value = params.coatOffsetYNorm,
                valueRange = -0.30f..0.30f,
                onValueChange = { v -> onUpdateParams { it.copy(coatOffsetYNorm = v) } }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    PassportSliderItem(
                        label = "Coat Center (X)",
                        valueText = "${(params.coatOffsetXNorm * 100).roundToInt()}%",
                        value = params.coatOffsetXNorm,
                        valueRange = -0.30f..0.30f,
                        onValueChange = { v -> onUpdateParams { it.copy(coatOffsetXNorm = v) } }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    PassportSliderItem(
                        label = "Coat Rotate",
                        valueText = "${String.format("%+.1f°", params.coatRotationDeg)}",
                        value = params.coatRotationDeg,
                        valueRange = -15f..15f,
                        onValueChange = { v -> onUpdateParams { it.copy(coatRotationDeg = v) } }
                    )
                }
            }
        }
    }
}

// ============================================================================
// TAB 3: BACKGROUND COLOR & STUDIO POLISH PANEL
// ============================================================================
@Composable
private fun BackgroundColorPanel(
    params: PassportEditParams,
    onSelectBgColor: (PassportBgColor) -> Unit,
    onUpdateParams: ((PassportEditParams) -> PassportEditParams) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Passport Background Color",
                color = ReminiTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${params.bgColor.title} (${params.bgColor.hexCode})",
                color = ReminiGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Color Swatches Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PassportBgColor.entries.forEach { bg ->
                val isSelected = params.bgColor == bg
                val swatchColor = Color(bg.r, bg.g, bg.b)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectBgColor(bg) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(swatchColor)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFF3B82F6) else Color.White.copy(alpha = 0.4f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (bg.r > 180 && bg.g > 180) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = bg.title.substringBefore(" "),
                        color = if (isSelected) Color.White else ReminiTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                PassportSliderItem(
                    label = "Hair Edge Feathering",
                    valueText = "${(params.alphaEdgeFeather * 100).roundToInt()}%",
                    value = params.alphaEdgeFeather,
                    valueRange = 0f..1f,
                    onValueChange = { v -> onUpdateParams { it.copy(alphaEdgeFeather = v) } }
                )
            }

            // Studio Skin Polish Toggle Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (params.studioSkinPolish) Color(0xFF2563EB) else ReminiCard)
                    .border(1.dp, if (params.studioSkinPolish) Color(0xFF60A5FA) else ReminiDivider, RoundedCornerShape(50))
                    .clickable { onUpdateParams { it.copy(studioSkinPolish = !it.studioSkinPolish) } }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (params.studioSkinPolish) "Skin Polish ON" else "Skin Polish OFF",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ============================================================================
// TAB 4: COUNTRY STANDARD SIZE & EXPORT LAYOUT PANEL
// ============================================================================
@Composable
private fun CountryStandardAndExportPanel(
    params: PassportEditParams,
    onSelectCountry: (CountryPassportStandard) -> Unit,
    onSelectExportLayout: (ExportLayoutMode) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Export Layout Mode Switch (Single Photo vs 4x6" Print Sheet)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExportLayoutMode.entries.forEach { layoutMode ->
                val selected = params.exportLayoutMode == layoutMode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Color.White else ReminiCard)
                        .border(1.dp, if (selected) Color.White else ReminiDivider, RoundedCornerShape(12.dp))
                        .clickable { onSelectExportLayout(layoutMode) }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Column {
                        Text(
                            text = layoutMode.title,
                            color = if (selected) ReminiBlack else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = layoutMode.subtitle,
                            color = if (selected) ReminiBlack.copy(alpha = 0.7f) else ReminiTextSecondary,
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 10 Official Country Standards Horizontal Cards
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(CountryPassportStandard.entries, key = { it.name }) { std ->
                val isSelected = params.countryStandard == std
                Column(
                    modifier = Modifier
                        .width(146.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) Color(0xFF2563EB).copy(alpha = 0.24f) else ReminiCard)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(0xFF60A5FA) else ReminiDivider,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectCountry(std) }
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = std.flagEmoji,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = std.countryName,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = std.specLabel,
                        color = ReminiGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${std.widthPx300Dpi}×${std.heightPx300Dpi} px (300 DPI)",
                        color = ReminiTextSecondary,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

// ============================================================================
// 4. SAVED / EXPORTED PASSPORT PHOTO SCREEN
// ============================================================================
@Composable
private fun PassportSavedExportView(
    state: PassportUiState,
    onBackToEditor: () -> Unit,
    onNewPhoto: () -> Unit,
    onShare: () -> Unit
) {
    val bmp = state.exportedBitmap ?: state.renderedPassportBitmap ?: return
    val std = state.editParams.countryStandard
    val layout = state.editParams.exportLayoutMode

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReminiBlack)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackToEditor) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Studio",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Passport Photo Exported",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, ReminiDivider, RoundedCornerShape(50))
                    .clickable { onNewPhoto() }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "Done",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(ReminiSurface)
                .border(1.dp, ReminiDivider, RoundedCornerShape(26.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = remember(bmp) { bmp.asImageBitmap() },
                contentDescription = "Exported Passport Photo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.94f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved to Gallery (${std.flagEmoji} ${std.specLabel})",
                    color = ReminiBlack,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(18.dp)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onShare() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Passport Photo",
                    tint = ReminiBlack,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        val displayFileName = state.savedFileName.ifEmpty { "PassportPhoto.jpg" }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ReminiSurface)
                .border(1.dp, ReminiDivider, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Export Details (${layout.title}):",
                color = ReminiGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "• Country Spec: ${std.flagEmoji} ${std.countryName} (${std.specLabel}, ${std.widthPx300Dpi}×${std.heightPx300Dpi} px @ 300 DPI)",
                color = ReminiTextPrimary,
                fontSize = 11.sp
            )
            Text(
                text = "• Background: ${state.editParams.bgColor.title} (${state.editParams.bgColor.hexCode})",
                color = ReminiTextPrimary,
                fontSize = 11.sp
            )
            Text(
                text = "• Android Gallery: Pictures/ReminiSkinAI/$displayFileName",
                color = ReminiTextSecondary,
                fontSize = 10.sp
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onBackToEditor,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ReminiCard,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text(
                    text = "Keep Editing",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onNewPhoto,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = ReminiBlack
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text(
                    text = "New Passport",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun PassportSliderItem(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = ReminiTextPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = valueText,
                color = ReminiGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF3B82F6),
                inactiveTrackColor = ReminiDivider
            ),
            modifier = Modifier.height(24.dp)
        )
    }
}

@Composable
private fun StudioTabButton(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    badge: String? = null,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Color(0xFF2563EB).copy(alpha = 0.22f) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) Color(0xFF60A5FA) else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (selected) Color.White else ReminiTextSecondary,
                modifier = Modifier.size(22.dp)
            )
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(start = 22.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF2563EB))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badge,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            color = if (selected) Color.White else ReminiTextSecondary,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun PassportAiModelsDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ReminiSurface,
        titleContentColor = Color.White,
        textContentColor = ReminiTextSecondary,
        title = {
            Text(
                text = "Passport AI Models & Nano Banana 2 Coats",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Trained GitHub & Hugging Face Models + Nano Banana 2 Assets:",
                    color = ReminiTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "1. BRIA RMBG-1.4 Neural Matting (rmbg_14_quantized.onnx)\n" +
                        "1024×1024 high-resolution background removal model. Isolates hair, shoulders, and body contours for studio-clean solid white or color passport backgrounds.",
                    fontSize = 12.sp
                )
                Text(
                    text = "2. BiSeNet 19-Class Face & Neck Parser (faceparsing.tflite)\n" +
                        "512×512 CelebAMask-HQ semantic parser extracting eyes (for auto tilt-leveling), crown-to-chin biometric ratio, and exact neck/collar coordinates.",
                    fontSize = 12.sp
                )
                Text(
                    text = "3. MediaPipe 6-Class Multiclass Segmenter (selfie_multiclass_256x256.tflite)\n" +
                        "Separates casual clothes (class 4) from neck/body skin (class 2) so original t-shirts/hoodies are cleanly hidden underneath the formal coat.",
                    fontSize = 12.sp
                )
                Text(
                    text = "4. 10 Nano Banana 2 Formal Coats (5 Men + 5 Women)\n" +
                        "10 studio ghost-mannequin formal suits & blazers generated with Nano Banana 2, chroma-keyed with open V-collars for automatic neck-to-collar alignment.",
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got It", color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold)
            }
        }
    )
}
