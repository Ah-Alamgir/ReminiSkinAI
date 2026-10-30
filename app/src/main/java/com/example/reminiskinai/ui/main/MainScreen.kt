package com.example.reminiskinai.ui.main

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Close
import com.example.reminiskinai.PassportStudio
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.reminiskinai.ml.AestheticPreset
import com.example.reminiskinai.ml.HairShadePreset
import com.example.reminiskinai.ml.LipShadePreset
import com.example.reminiskinai.ml.SkinAnalysisTelemetry
import com.example.reminiskinai.ml.SkinEditParams
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
import kotlin.math.roundToInt

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: MainScreenViewModel = viewModel {
        MainScreenViewModel(context.applicationContext)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onGalleryPhotoPicked(uri)
        }
    }

    if (state.showModelArchitectureSheet) {
        ModelArchitectureDialog(
            onDismiss = { viewModel.toggleModelArchitectureSheet(false) }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ReminiBlack)
    ) {
        when (state.mode) {
            EditorScreenMode.HOME -> {
                ReminiHomeScreen(
                    state = state,
                    onPickGallery = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onSelectSample = { viewModel.selectSampleForPreview(it) },
                    onClosePreview = { viewModel.closePreviewModal() },
                    onStartEnhance = { viewModel.startEnhancementFromPreview() },
                    onOpenModelInfo = { viewModel.toggleModelArchitectureSheet(true) },
                    onOpenPassportStudio = { onItemClick(PassportStudio) }
                )
            }

            EditorScreenMode.PROCESSING -> {
                BackHandler { viewModel.backToHome() }
                ReminiProcessingScreen(
                    state = state,
                    onCancel = { viewModel.backToHome() }
                )
            }

            EditorScreenMode.RESULT -> {
                BackHandler { viewModel.backToHome() }
                ReminiResultScreen(
                    state = state,
                    onClose = { viewModel.backToHome() },
                    onDownload = { viewModel.saveCurrentResultToGallery() },
                    onUpdateParams = { transform -> viewModel.updateEditParams(transform) },
                    onSelectPreset = { preset -> viewModel.selectPreset(preset) },
                    onSelectLipShade = { shade -> viewModel.selectLipShade(shade) },
                    onSelectHairShade = { shade -> viewModel.selectHairShade(shade) },
                    onSaveDefaultSettings = { viewModel.saveUserDefaultSettings() },
                    onToggleSliders = { viewModel.toggleFineTuneSliders() },
                    onOpenModelInfo = { viewModel.toggleModelArchitectureSheet(true) }
                )
            }

            EditorScreenMode.SAVED_SHARE -> {
                BackHandler { viewModel.backToResultFromShare() }
                ReminiShareScreen(
                    state = state,
                    onBackToResult = { viewModel.backToResultFromShare() },
                    onDoneToHome = { viewModel.backToHome() },
                    onShare = { viewModel.shareSavedImage(context) }
                )
            }
        }
    }
}

// ============================================================================
// 1. REMINI HOME SCREEN + PHOTO PREVIEW BOTTOM MODAL
// ============================================================================
@Composable
private fun ReminiHomeScreen(
    state: EditorUiState,
    onPickGallery: () -> Unit,
    onSelectSample: (SampleThumb) -> Unit,
    onClosePreview: () -> Unit,
    onStartEnhance: () -> Unit,
    onOpenModelInfo: () -> Unit,
    onOpenPassportStudio: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Header ("Remini" + PRO badge + Passport ID button + Offline 5-Model AI badge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Remini",
                    color = ReminiTextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(ReminiCrimson)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "SKIN AI",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF2563EB))
                        .clickable { onOpenPassportStudio() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = "Passport Photo Studio",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Passport ID",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(ReminiCard)
                        .border(1.dp, ReminiDivider, RoundedCornerShape(50))
                        .clickable { onOpenModelInfo() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "5-Model Info",
                            tint = ReminiGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "5 Models",
                            color = ReminiTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Search / Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(50))
                    .background(ReminiSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = ReminiTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Skin-only natural & aesthetic color balance (0% API)",
                    color = ReminiTextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Photo Grid + Model Cards
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = if (state.selectedPreviewBitmap != null) 340.dp else 90.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pipeline Banner
                item(span = { GridItemSpan(3) }) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Combined GitHub AI Pipeline",
                                color = ReminiTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Details >",
                                color = ReminiRose,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { onOpenModelInfo() }
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PipelineMiniCard(
                                step = "Model 1",
                                title = "MediaPipe 6-Class",
                                desc = "Subject & Skin Mask",
                                accent = ReminiRose
                            )
                            PipelineMiniCard(
                                step = "Model 2",
                                title = "Real-ESRGAN x4v3",
                                desc = "1080p Neural Upscaler",
                                accent = Color(0xFF38BDF8)
                            )
                            PipelineMiniCard(
                                step = "Model 3",
                                title = "BiSeNet 19-Class",
                                desc = "512×512 Facial Anatomy",
                                accent = ReminiGold
                            )
                            PipelineMiniCard(
                                step = "Model 4",
                                title = "GPEN-BFR GAN",
                                desc = "Face Restoration Prior",
                                accent = Color(0xFFA855F7)
                            )
                            PipelineMiniCard(
                                step = "Model 5",
                                title = "Dermis Polish",
                                desc = "Aesthetic Skin Balancer",
                                accent = Color(0xFF34D399)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        // NEW: Passport Photo Maker Studio Banner Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF1E3A8A), Color(0xFF1E1B4B))
                                    )
                                )
                                .border(1.dp, Color(0xFF60A5FA).copy(alpha = 0.55f), RoundedCornerShape(20.dp))
                                .clickable { onOpenPassportStudio() }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Checkroom,
                                    contentDescription = "Passport Coat Studio",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Passport Size Photo Maker",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(ReminiGold)
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "NEW",
                                            color = ReminiBlack,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "10 Nano Banana 2 Coats (Men & Women) · White/Color BG · Crop/Rotate/Scale · 10 Country Sizes",
                                    color = Color.White.copy(alpha = 0.82f),
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Open Passport Studio",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // "Enhance ✨" section title + Gallery Picker button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Enhance",
                                    color = ReminiTextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = ReminiGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Button(
                                onClick = onPickGallery,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = ReminiBlack
                                ),
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Open Gallery",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap any photo below from your aiapp folder or pick from device gallery:",
                            color = ReminiTextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Sample photos from c:\Users\abuha\Pictures\Screenshots\aiapp
                items(state.sampleThumbs, key = { it.item.id }) { thumb ->
                    val isSelected = state.selectedTitle == thumb.item.title
                    Box(
                        modifier = Modifier
                            .aspectRatio(0.75f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) ReminiRose else ReminiDivider,
                                shape = RoundedCornerShape(16.dp)
                            )
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
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                    )
                                )
                                .padding(8.dp)
                        ) {
                            Text(
                                text = thumb.item.title,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Remini-style Bottom Sheet Modal for Selected Photo (matches IMG_2535.webp & IMG_2542.webp)
        val previewBmp = state.selectedPreviewBitmap
        if (previewBmp != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(370.dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(ReminiSurface)
                    .border(
                        width = 1.dp,
                        color = ReminiDivider,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    )
            ) {
                Image(
                    bitmap = remember(previewBmp) { previewBmp.asImageBitmap() },
                    contentDescription = state.selectedTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top-left Close 'X' button (matches IMG_2535.webp)
                IconButton(
                    onClick = onClosePreview,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close preview",
                        tint = Color.White
                    )
                }

                // Selected title pill top-right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.58f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${state.selectedTitle} · Skin Only",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Bottom large "Enhance >" pill button (matches IMG_2535.webp & IMG_2542.webp)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .height(58.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF101014).copy(alpha = 0.94f))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(50))
                        .clickable { onStartEnhance() }
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Enhance",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PipelineMiniCard(
    step: String,
    title: String,
    desc: String,
    accent: Color
) {
    Column(
        modifier = Modifier
            .width(152.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ReminiCard)
            .border(1.dp, ReminiDivider, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Text(
            text = step.uppercase(),
            color = accent,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = ReminiTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = desc,
            color = ReminiTextSecondary,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

// ============================================================================
// 2. REMINI PROCESSING SCREEN (Matches IMG_2544.webp)
// ============================================================================
@Composable
private fun ReminiProcessingScreen(
    state: EditorUiState,
    onCancel: () -> Unit
) {
    val bmp = state.beforeBitmap
    val progress = state.stageProgress

    // Desaturated grayscale tint matrix matching IMG_2544.webp
    val desaturateMatrix = remember {
        ColorMatrix().apply { setToSaturation(0.15f) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReminiBlack)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(ReminiSurface)
        ) {
            if (bmp != null) {
                Image(
                    bitmap = remember(bmp) { bmp.asImageBitmap() },
                    contentDescription = "Processing photo",
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.colorMatrix(desaturateMatrix),
                    alpha = 0.42f,
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
                    color = Color.White,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = progress?.modelName ?: "Running On-Device AI...",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = progress?.stageDescription ?: "Balancing skin color...",
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { progress?.progressFraction ?: 0.3f },
                    color = ReminiRose,
                    trackColor = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier
                        .width(220.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Step ${progress?.stepIndex ?: 1} of 4 · 100% Offline TFLite",
                    color = ReminiGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Bottom white banner matching IMG_2544.webp
        Surface(
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Enhancing only skin color & facial detail with 4 trained models.\nBackground environment remains 100% untouched.",
                color = ReminiBlack,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp)
            )
        }
    }
}

// ============================================================================
// 3. REMINI RESULT SCREEN (Matches IMG_2536.webp & IMG_2545.webp)
// ============================================================================
@Composable
private fun ReminiResultScreen(
    state: EditorUiState,
    onClose: () -> Unit,
    onDownload: () -> Unit,
    onUpdateParams: ((SkinEditParams) -> SkinEditParams) -> Unit,
    onSelectPreset: (AestheticPreset) -> Unit,
    onSelectLipShade: (LipShadePreset) -> Unit,
    onSelectHairShade: (HairShadePreset) -> Unit,
    onSaveDefaultSettings: () -> Unit,
    onToggleSliders: () -> Unit,
    onOpenModelInfo: () -> Unit
) {
    val beforeBmp = state.beforeBitmap ?: return
    val afterBmp = state.afterBitmap ?: beforeBmp
    val params = state.editParams
    val telemetry = state.telemetry
    val hasReminiRef = state.selectedReminiRefBitmap != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReminiBlack)
    ) {
        // Top App Bar: Close X | "Result" | Tune | White Download Circle (matches IMG_2536.webp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Result",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "1080p HD · Regional Skin & Natural Lip AI",
                    color = ReminiGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onToggleSliders) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Fine-tune sliders",
                    tint = if (state.showFineTuneSliders) ReminiRose else Color.White
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // White circular Download button (matches IMG_2536.webp & IMG_2545.webp)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(enabled = !state.isSaving) { onDownload() },
                contentAlignment = Alignment.Center
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        color = ReminiBlack,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Save to Gallery",
                        tint = ReminiBlack,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Quantitative Skin Color Telemetry Strip
        if (telemetry != null) {
            SkinTelemetryBar(
                telemetry = telemetry,
                hasReminiRef = hasReminiRef,
                comparingRef = params.compareReminiRef,
                onToggleRef = {
                    onUpdateParams { it.copy(compareReminiRef = !it.compareReminiRef) }
                },
                onOpenModelInfo = onOpenModelInfo
            )
        }

        // Main Interactive Before/After Split Comparison View
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            BeforeAfterSlider(
                beforeBitmap = beforeBmp,
                afterBitmap = afterBmp,
                afterLabel = when {
                    params.compareReminiRef -> "Remini Ref"
                    params.showSkinMaskOverlay -> "AI Skin Mask"
                    else -> "After (Skin AI)"
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Always-Visible Skin Smoothness & Natural Lip Color Slider Card + "Save Setting" Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 3.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ReminiSurface)
                .border(1.dp, ReminiDivider, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Skin, Lip Shade & Hair Color",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = params.preset.subtitle,
                        color = ReminiTextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (state.settingsSavedConfirmation) Color(0xFF10B981) else ReminiRose
                        )
                        .clickable { onSaveDefaultSettings() }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (state.settingsSavedConfirmation) Icons.Default.CheckCircle else Icons.Default.Check,
                            contentDescription = "Save Setting",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (state.settingsSavedConfirmation) "Saved for Next Time ✓" else "Save Setting",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            CompactSliderRow(
                title = "Skin Smoothness (Auto-Saved Setting)",
                value = params.skinSmoothness,
                valueRange = 0f..1.0f,
                displayPercent = (params.skinSmoothness * 100).roundToInt(),
                onValueChange = { v -> onUpdateParams { it.copy(skinSmoothness = v, compareReminiRef = false) } }
            )
            CompactSliderRow(
                title = "Lip Color Intensity (${params.lipShade.title})",
                value = params.lipColorIntensity,
                valueRange = 0f..1.5f,
                displayPercent = (params.lipColorIntensity * 100).roundToInt(),
                onValueChange = { v -> onUpdateParams { it.copy(lipColorIntensity = v, compareReminiRef = false) } }
            )

            // Lip Shade swatches
            ShadeSwatchRow(
                label = "Lips:",
                items = LipShadePreset.entries.map { Triple(it.title, Color(it.swatchHex), params.lipShade == it) },
                onSelect = { idx -> onSelectLipShade(LipShadePreset.entries[idx]) }
            )

            // Hair Color Shade swatches
            ShadeSwatchRow(
                label = "Hair:",
                items = HairShadePreset.entries.map { Triple(it.title, Color(it.swatchHex), params.hairShade == it) },
                onSelect = { idx -> onSelectHairShade(HairShadePreset.entries[idx]) }
            )

            if (params.hairShade != HairShadePreset.ORIGINAL) {
                CompactSliderRow(
                    title = "Hair Color Intensity (${params.hairShade.title})",
                    value = params.hairColorIntensity,
                    valueRange = 0f..1.2f,
                    displayPercent = (params.hairColorIntensity * 100).roundToInt(),
                    onValueChange = { v -> onUpdateParams { it.copy(hairColorIntensity = v, compareReminiRef = false) } }
                )
            }
        }

        // Expandable Fine-Tuning Sliders Drawer (Radiance, Warmth, Clarity)
        AnimatedVisibility(visible = state.showFineTuneSliders) {
            FineTuneSlidersPanel(
                params = params,
                onUpdateParams = onUpdateParams
            )
        }

        // Regional Skin Tone Preset Chips (Remini Natural, Bangladeshi, Korean, American, African)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Region:",
                color = ReminiGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            AestheticPreset.entries.forEach { preset ->
                val selected = !params.compareReminiRef && params.preset == preset
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) Color.White else ReminiCard)
                        .border(
                            width = 1.dp,
                            color = if (selected) Color.White else ReminiDivider,
                            shape = RoundedCornerShape(50)
                        )
                        .clickable { onSelectPreset(preset) }
                        .padding(horizontal = 13.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = preset.title,
                        color = if (selected) ReminiBlack else ReminiTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Bottom Feature Toggle Row (Matches IMG_2536.webp & IMG_2545.webp with checkmarks!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReminiFeatureToggleItem(
                label = "Skin\nColors",
                icon = Icons.Default.ColorLens,
                enabled = params.skinColorEnabled,
                onClick = { onUpdateParams { it.copy(skinColorEnabled = !it.skinColorEnabled, compareReminiRef = false) } }
            )
            ReminiFeatureToggleItem(
                label = "Face\nRetouch",
                icon = Icons.Default.FaceRetouchingNatural,
                enabled = params.faceRetouchEnabled,
                onClick = { onUpdateParams { it.copy(faceRetouchEnabled = !it.faceRetouchEnabled, compareReminiRef = false) } }
            )
            ReminiFeatureToggleItem(
                label = "Face\nEnhancer",
                icon = Icons.Default.AutoFixHigh,
                enabled = params.faceEnhancerEnabled,
                onClick = { onUpdateParams { it.copy(faceEnhancerEnabled = !it.faceEnhancerEnabled, compareReminiRef = false) } }
            )
            ReminiFeatureToggleItem(
                label = "Body\nSkin Sync",
                icon = Icons.Default.PanToolAlt,
                enabled = params.bodySkinSyncEnabled,
                onClick = { onUpdateParams { it.copy(bodySkinSyncEnabled = !it.bodySkinSyncEnabled, compareReminiRef = false) } }
            )
            ReminiFeatureToggleItem(
                label = "AI Skin\nMask",
                icon = Icons.Default.Layers,
                enabled = params.showSkinMaskOverlay,
                onClick = { onUpdateParams { it.copy(showSkinMaskOverlay = !it.showSkinMaskOverlay, compareReminiRef = false) } }
            )
        }
    }
}

@Composable
private fun SkinTelemetryBar(
    telemetry: SkinAnalysisTelemetry,
    hasReminiRef: Boolean,
    comparingRef: Boolean,
    onToggleRef: () -> Unit,
    onOpenModelInfo: () -> Unit
) {
    val bRgb = telemetry.beforeSkinRgb
    val aRgb = telemetry.afterSkinRgb
    val beforeColor = Color(bRgb[0].coerceIn(0, 255), bRgb[1].coerceIn(0, 255), bRgb[2].coerceIn(0, 255))
    val afterColor = Color(aRgb[0].coerceIn(0, 255), aRgb[1].coerceIn(0, 255), aRgb[2].coerceIn(0, 255))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ReminiSurface)
            .border(1.dp, ReminiDivider, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Before -> After Skin Swatches
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(beforeColor)
                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
        )
        Text(
            text = " → ",
            color = ReminiTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(afterColor)
                .border(1.dp, Color.White, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Skin RGB [${bRgb[0]},${bRgb[1]},${bRgb[2]}] → [${aRgb[0]},${aRgb[1]},${aRgb[2]}]",
                color = ReminiTextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = "${telemetry.outputWidth}×${telemetry.outputHeight} Full HD · Skin ${(telemetry.faceSkinPercent + telemetry.bodySkinPercent).roundToInt()}% · Env 0% · ${telemetry.inferenceTimeMs}ms",
                color = ReminiTextSecondary,
                fontSize = 10.sp,
                maxLines = 1
            )
        }

        if (hasReminiRef) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (comparingRef) ReminiCrimson else ReminiCard)
                    .border(1.dp, if (comparingRef) ReminiRose else ReminiDivider, RoundedCornerShape(50))
                    .clickable { onToggleRef() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Compare,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (comparingRef) "Remini Ref" else "vs Remini",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            IconButton(
                onClick = onOpenModelInfo,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Models Info",
                    tint = ReminiGold,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun FineTuneSlidersPanel(
    params: SkinEditParams,
    onUpdateParams: ((SkinEditParams) -> SkinEditParams) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(ReminiSurface)
            .border(1.dp, ReminiDivider, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        CompactSliderRow(
            title = "Skin Radiance (Zero-DCE Curve)",
            value = params.skinRadiance,
            valueRange = 0f..1.5f,
            displayPercent = (params.skinRadiance * 100).roundToInt(),
            onValueChange = { v -> onUpdateParams { it.copy(skinRadiance = v, compareReminiRef = false) } }
        )
        CompactSliderRow(
            title = "Skin Warmth & Cast Balance",
            value = params.skinWarmth,
            valueRange = 0.5f..1.5f,
            displayPercent = (params.skinWarmth * 100).roundToInt(),
            onValueChange = { v -> onUpdateParams { it.copy(skinWarmth = v, compareReminiRef = false) } }
        )
        CompactSliderRow(
            title = "Dermis Smoothness (Retouch)",
            value = params.skinSmoothness,
            valueRange = 0f..1.0f,
            displayPercent = (params.skinSmoothness * 100).roundToInt(),
            onValueChange = { v -> onUpdateParams { it.copy(skinSmoothness = v, compareReminiRef = false) } }
        )
        CompactSliderRow(
            title = "Facial Clarity & Feature Detail",
            value = params.facialClarity,
            valueRange = 0f..1.2f,
            displayPercent = (params.facialClarity * 100).roundToInt(),
            onValueChange = { v -> onUpdateParams { it.copy(facialClarity = v, compareReminiRef = false) } }
        )
    }
}

@Composable
private fun ShadeSwatchRow(
    label: String,
    items: List<Triple<String, Color, Boolean>>,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = ReminiGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(34.dp)
        )
        items.forEachIndexed { idx, (title, swatch, selected) ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) Color.White else ReminiCard)
                    .border(
                        width = if (selected) 1.5.dp else 1.dp,
                        color = if (selected) Color.White else ReminiDivider,
                        shape = RoundedCornerShape(50)
                    )
                    .clickable { onSelect(idx) }
                    .padding(start = 4.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(swatch)
                        .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = title,
                    color = if (selected) ReminiBlack else ReminiTextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CompactSliderRow(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    displayPercent: Int,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = ReminiTextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$displayPercent%",
                color = ReminiGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = ReminiRose,
                inactiveTrackColor = ReminiDivider
            ),
            modifier = Modifier.height(26.dp)
        )
    }
}

@Composable
private fun ReminiFeatureToggleItem(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Box(modifier = Modifier.size(38.dp), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) Color.White else ReminiTextSecondary.copy(alpha = 0.45f),
                modifier = Modifier.size(26.dp)
            )
            if (enabled) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = ReminiBlack,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (enabled) Color.White else ReminiTextSecondary.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = if (enabled) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp
        )
    }
}

// ============================================================================
// 4. REMINI SAVED / SHARE SCREEN (Matches IMG_2547.webp)
// ============================================================================
@Composable
private fun ReminiShareScreen(
    state: EditorUiState,
    onBackToResult: () -> Unit,
    onDoneToHome: () -> Unit,
    onShare: () -> Unit
) {
    val bmp = state.afterBitmap ?: state.beforeBitmap ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReminiBlack)
    ) {
        // Top Bar: < Back | "Share" | "Done" pill (matches IMG_2547.webp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackToResult) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Share",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, ReminiDivider, RoundedCornerShape(50))
                    .clickable { onDoneToHome() }
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Done",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Main Saved Image Card with "Saved in Gallery ✓" pill & Share Floating Button
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(ReminiSurface)
        ) {
            Image(
                bitmap = remember(bmp) { bmp.asImageBitmap() },
                contentDescription = "Saved result",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            // "Saved in Gallery ✓" pill (matches IMG_2547.webp)
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.92f))
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved in Gallery",
                    color = ReminiBlack,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ReminiBlack,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Bottom-Right Floating Share Button (matches IMG_2547.webp)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onShare() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share photo",
                    tint = ReminiBlack,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Exact Saved File Location Card (for both Android Gallery & Windows PC Sync)
        val displayFileName = state.savedFileName.ifEmpty { "ReminiSkinAI_edited.jpg" }
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
                text = "Saved Photo Locations ($displayFileName):",
                color = ReminiGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "• PC Folder 1: C:\\Users\\abuha\\Pictures\\ReminiSkinAI\\$displayFileName",
                color = ReminiTextPrimary,
                fontSize = 11.sp
            )
            Text(
                text = "• PC Folder 2: C:\\Users\\abuha\\Pictures\\Screenshots\\aiapp\\Saved_From_App\\$displayFileName",
                color = ReminiTextPrimary,
                fontSize = 11.sp
            )
            Text(
                text = "• Android Gallery: Pictures/ReminiSkinAI/$displayFileName",
                color = ReminiTextSecondary,
                fontSize = 10.sp
            )
        }

        // Bottom "Enhance Another ✨" full-width white button (matches IMG_2547.webp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White)
                .clickable { onDoneToHome() }
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Enhance Another",
                color = ReminiBlack,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = ReminiBlack,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(22.dp)
            )
        }
    }
}

// ============================================================================
// 5. 4-MODEL ENSEMBLE ARCHITECTURE DIALOG
// ============================================================================
@Composable
private fun ModelArchitectureDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ReminiSurface,
        titleContentColor = Color.White,
        textContentColor = ReminiTextSecondary,
        title = {
            Text(
                text = "5-Model On-Device AI Pipeline",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "100% offline inference using 5 pre-trained neural networks bundled in assets/ (zero external APIs):",
                    color = ReminiTextPrimary,
                    fontSize = 13.sp
                )
                Text(
                    text = "1. MediaPipe Multiclass Segmenter (selfie_multiclass_256x256.tflite)\n" +
                        "Segments 6 classes: Background, Hair, Body Skin (neck/arms/hands), Face Skin, Clothes, Accessories. Isolates subject while protecting 100% of environment.",
                    fontSize = 12.sp
                )
                Text(
                    text = "2. Real-ESRGAN General x4v3 Neural Super-Resolution (real_esrgan_general_x4v3.tflite)\n" +
                        "Subject-Guided 1080p Full HD neural super-resolution. Delivers 4x neural reconstruction on person & hair while smoothly de-blocking backgrounds to eliminate GAN tile artifacts.",
                    fontSize = 12.sp
                )
                Text(
                    text = "3. BiSeNet 19-Class Face Parser (faceparsing.tflite)\n" +
                        "512×512 CelebAMask-HQ semantic parsing of 19 facial anatomy classes + neural FFHQ landmark extraction (eyes, nose, mouth).",
                    fontSize = 12.sp
                )
                Text(
                    text = "4. GPEN-BFR-256 Face Restoration GAN (gpen_bfr_256.onnx)\n" +
                        "GAN Prior Embedded Network for Blind Face Restoration. Reconstructs porcelain-smooth, non-distorted facial skin and crystal-clear eyes, catchlights, brows & lips.",
                    fontSize = 12.sp
                )
                Text(
                    text = "5. Multi-Scale Bilateral Dermis Polish + Remini Aesthetic Balancer\n" +
                        "Zero-DCE rational curve illumination & regional skin tone presets (Remini Natural, Porcelain, American, Korean, Bangladeshi, African) + lip & hair shade calibration.",
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got It", color = ReminiRose, fontWeight = FontWeight.Bold)
            }
        }
    )
}
