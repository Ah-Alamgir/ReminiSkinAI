package com.example.reminiskinai.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.BrandingWatermark
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RawOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reminiskinai.theme.AuraExportButtonGradient
import com.example.reminiskinai.theme.AuraOnSurface
import com.example.reminiskinai.theme.AuraOnSurfaceVariant
import com.example.reminiskinai.theme.AuraOutline
import com.example.reminiskinai.theme.AuraOutlineVariant
import com.example.reminiskinai.theme.AuraPrimary
import com.example.reminiskinai.theme.AuraPrimaryContainer
import com.example.reminiskinai.theme.AuraPrimaryElectric
import com.example.reminiskinai.theme.AuraSecondary
import com.example.reminiskinai.theme.AuraSurface
import com.example.reminiskinai.theme.AuraSurfaceBright
import com.example.reminiskinai.theme.AuraSurfaceContainer
import com.example.reminiskinai.theme.AuraSurfaceContainerHigh
import com.example.reminiskinai.theme.AuraSurfaceLowest
import com.example.reminiskinai.theme.AuraTertiaryHotMagenta

enum class ExportResolution(
    val title: String,
    val dimensions: String,
    val badge: String,
    val fileSize: String,
    val creditCost: Int
) {
    ORIGINAL_1080P(
        title = "Original 1080p",
        dimensions = "1080 × 1440 px",
        badge = "SDR",
        fileSize = "2.4 MB",
        creditCost = 0
    ),
    ULTRA_4K(
        title = "Ultra 4K",
        dimensions = "3840 × 5120 px",
        badge = "4× UHD",
        fileSize = "16.4 MB",
        creditCost = 1
    ),
    MASTER_PRINT_8K(
        title = "Master Print 8K",
        dimensions = "7680 × 10240 px",
        badge = "8× Raw",
        fileSize = "64.8 MB",
        creditCost = 2
    )
}

/**
 * 4K Export and Upscale Modal Bottom Sheet.
 * Direct translation of `export_4k_upscale_modal/code.html` to Compose Multiplatform.
 */
@Composable
fun Export4kModal(
    isVisible: Boolean,
    userCredits: Int,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSaveToCameraRoll: (resolution: ExportResolution, removeWatermark: Boolean, preserveExif: Boolean, p3Color: Boolean) -> Unit,
    onShareSheet: () -> Unit,
    onInstagramStory: () -> Unit,
    onRawTiff: () -> Unit,
    onGetMoreCredits: () -> Unit
) {
    if (!isVisible) return

    var selectedResolution by remember { mutableStateOf(ExportResolution.ULTRA_4K) }
    var removeWatermark by remember { mutableStateOf(true) }
    var preserveExif by remember { mutableStateOf(true) }
    var p3ColorGamut by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .pointerInput(Unit) {
                detectTapGestures { onDismiss() }
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(AuraSurfaceContainer.copy(alpha = 0.95f))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.28f), Color.White.copy(alpha = 0.05f))
                    ),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .pointerInput(Unit) {
                    detectTapGestures { /* consume taps inside sheet */ }
                }
                .navigationBarsPadding()
                .padding(bottom = 12.dp)
        ) {
            // Drag Pill Handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(AuraOutlineVariant)
                )
            }

            // Sheet Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Export",
                        tint = AuraPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Export Quality",
                        color = AuraOnSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AuraSurfaceBright.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = AuraOnSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Scrollable Options Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Resolution Presets
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RESOLUTION PRESET",
                            color = AuraOnSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = AuraSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Neural Engine 4.0",
                                color = AuraSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Preset Cards
                    ExportResolution.values().forEach { res ->
                        val isSelected = selectedResolution == res
                        val cardBorderBrush = if (isSelected) {
                            Brush.horizontalGradient(
                                listOf(AuraSecondary.copy(alpha = 0.9f), AuraPrimary.copy(alpha = 0.7f))
                            )
                        } else {
                            Brush.linearGradient(listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.04f)))
                        }

                        val cardBg = if (isSelected) {
                            Brush.horizontalGradient(
                                listOf(
                                    AuraPrimaryContainer.copy(alpha = 0.20f),
                                    AuraSurfaceContainerHigh.copy(alpha = 0.50f),
                                    AuraSecondary.copy(alpha = 0.15f)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(Color.White.copy(alpha = 0.04f), Color.White.copy(alpha = 0.02f))
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(cardBg)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    brush = cardBorderBrush,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedResolution = res }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Radio Circle
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) AuraSecondary else Color.Transparent)
                                    .border(
                                        width = if (isSelected) 0.dp else 1.5.dp,
                                        color = if (isSelected) Color.Transparent else AuraOutline,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(AuraSurfaceLowest)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Text details with weight(1f) to guarantee no wrapping or overlap
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = res.title,
                                        color = AuraOnSurface,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (isSelected) AuraSecondary.copy(alpha = 0.20f)
                                                else Color.White.copy(alpha = 0.08f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = res.badge,
                                            color = if (isSelected) AuraSecondary else AuraOnSurfaceVariant,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = res.dimensions,
                                        color = if (isSelected) AuraOnSurfaceVariant else AuraOutline,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                    Text(
                                        text = "•",
                                        color = AuraOutline.copy(alpha = 0.5f),
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = res.fileSize,
                                        color = if (isSelected) AuraSecondary else AuraOutline,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Right Pill: Credit / Free Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (res.creditCost > 0) {
                                            if (isSelected) AuraPrimaryElectric.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.06f)
                                        } else {
                                            Color.White.copy(alpha = 0.05f)
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (res.creditCost > 0 && isSelected) AuraPrimaryElectric.copy(alpha = 0.45f) else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (res.creditCost == 0) "Free" else "${res.creditCost} Credit",
                                    color = if (res.creditCost > 0) (if (isSelected) AuraPrimaryElectric else AuraPrimary) else AuraOnSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Section: Advanced Enhancement Toggles
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ENHANCEMENT & OUTPUT OPTIONS",
                        color = AuraOnSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                    ) {
                        // Toggle 1: Remove Watermark
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AuraPrimaryContainer.copy(alpha = 0.20f))
                                        .border(1.dp, AuraPrimaryContainer.copy(alpha = 0.40f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.BrandingWatermark,
                                        contentDescription = null,
                                        tint = AuraPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Remove Aura Watermark",
                                            color = AuraOnSurface,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(AuraPrimaryContainer.copy(alpha = 0.25f))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "PRO",
                                                color = AuraPrimary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Clean export without signature logo",
                                        color = AuraOutline,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Switch(
                                checked = removeWatermark,
                                onCheckedChange = { removeWatermark = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AuraPrimaryContainer,
                                    uncheckedThumbColor = AuraOutline,
                                    uncheckedTrackColor = AuraSurfaceBright.copy(alpha = 0.5f)
                                )
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.05f)))

                        // Toggle 2: Preserve EXIF Metadata
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AuraSecondary.copy(alpha = 0.15f))
                                        .border(1.dp, AuraSecondary.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        tint = AuraSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Preserve EXIF & Camera Metadata",
                                        color = AuraOnSurface,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Keep shutter speed, ISO & lens GPS tags",
                                        color = AuraOutline,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Switch(
                                checked = preserveExif,
                                onCheckedChange = { preserveExif = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AuraPrimaryContainer,
                                    uncheckedThumbColor = AuraOutline,
                                    uncheckedTrackColor = AuraSurfaceBright.copy(alpha = 0.5f)
                                )
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.05f)))

                        // Toggle 3: P3 Wide Color Gamut
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AuraSecondary.copy(alpha = 0.15f))
                                        .border(1.dp, AuraSecondary.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = AuraSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Enhance Color Gamut (P3 Wide Color)",
                                        color = AuraOnSurface,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Apple Retina 10-bit vibrant studio grading",
                                        color = AuraOutline,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Switch(
                                checked = p3ColorGamut,
                                onCheckedChange = { p3ColorGamut = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AuraPrimaryContainer,
                                    uncheckedThumbColor = AuraOutline,
                                    uncheckedTrackColor = AuraSurfaceBright.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }

                // Balance Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = AuraSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Aura Pro Balance: $userCredits Credits",
                            color = AuraOnSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "Get More",
                        color = AuraPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onGetMoreCredits() }
                    )
                }

                // Primary CTA: Save to Camera Roll
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(CircleShape)
                        .background(AuraExportButtonGradient)
                        .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                        .clickable(enabled = !isSaving) {
                            onSaveToCameraRoll(selectedResolution, removeWatermark, preserveExif, p3ColorGamut)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSaving) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Exporting in ${selectedResolution.title}...",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Save to Camera Roll",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Secondary Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Action 1: Instagram Story
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                            .clickable { onInstagramStory() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFF09433), Color(0xFFDC2743), Color(0xFFBC1888))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HistoryEdu,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "IG Story",
                                color = AuraOnSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Action 2: iOS Share Sheet
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                            .clickable { onShareSheet() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(AuraSurfaceBright.copy(alpha = 0.50f))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.IosShare,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Share Sheet",
                                color = AuraOnSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Action 3: Raw TIFF
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                            .clickable { onRawTiff() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(AuraSurfaceBright.copy(alpha = 0.50f))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RawOn,
                                    contentDescription = null,
                                    tint = AuraSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Raw TIFF",
                                color = AuraOnSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // iOS Home indicator clearance bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 120.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                    )
                }
            }
        }
    }
}
