package com.example.reminiskinai.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reminiskinai.theme.AuraOnSurface
import com.example.reminiskinai.theme.AuraOnSurfaceVariant
import com.example.reminiskinai.theme.AuraPrimary
import com.example.reminiskinai.theme.AuraPrimaryElectric
import com.example.reminiskinai.theme.AuraSecondary
import com.example.reminiskinai.theme.AuraSecondaryContainer
import com.example.reminiskinai.theme.AuraSurfaceLowest
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun BeforeAfterSlider(
    beforeImage: ImageBitmap,
    afterImage: ImageBitmap,
    beforeLabel: String = "BEFORE (720p)",
    afterLabel: String = "AFTER (4K AI)",
    colorFilter: ColorFilter? = null,
    showTelemetry: Boolean = true,
    showLandmarks: Boolean = false,
    isHoldCompareActive: Boolean = false,
    zoomFactor: Float = 1.0f,
    onZoomToggle: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val beforePainter = remember(beforeImage) { BitmapPainter(beforeImage) }
    val afterPainter = remember(afterImage) { BitmapPainter(afterImage) }
    BeforeAfterSlider(
        beforePainter = beforePainter,
        afterPainter = afterPainter,
        beforeLabel = beforeLabel,
        afterLabel = afterLabel,
        colorFilter = colorFilter,
        showTelemetry = showTelemetry,
        showLandmarks = showLandmarks,
        isHoldCompareActive = isHoldCompareActive,
        zoomFactor = zoomFactor,
        onZoomToggle = onZoomToggle,
        modifier = modifier
    )
}

/**
 * Luminescent Glass Before/After Comparison View.
 * Matches `enhancement_comparison_studio/code.html` & `DESIGN.md`:
 * - Realtime split slider with specular hairline rail & frosted glass thumb disc
 * - Dual opposing chevron handles
 * - Dynamic HUD tags: "BEFORE (720p)" and "AFTER (4K AI)"
 * - Top telemetry chip: "1080p → 4320p (4K UHD) • Sharpness: Ultra"
 * - Facial landmark wireframe overlay when enabled
 * - Press & hold comparison support
 */
@Composable
fun BeforeAfterSlider(
    beforePainter: Painter,
    afterPainter: Painter,
    beforeLabel: String = "BEFORE (720p)",
    afterLabel: String = "AFTER (4K AI)",
    colorFilter: ColorFilter? = null,
    showTelemetry: Boolean = true,
    showLandmarks: Boolean = false,
    isHoldCompareActive: Boolean = false,
    zoomFactor: Float = 1.0f,
    onZoomToggle: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var rawSplitFraction by remember { mutableFloatStateOf(0.50f) }

    // Instant zero-lag direct tracking for gestures; smooth transition only for hold-compare toggle
    val holdSplitFraction by animateFloatAsState(
        targetValue = if (isHoldCompareActive) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 150)
    )
    val effectiveSplitFraction = if (isHoldCompareActive) holdSplitFraction else rawSplitFraction

    val animatedZoom by animateFloatAsState(
        targetValue = zoomFactor,
        animationSpec = tween(durationMillis = 200)
    )

    // Pulsing animation for telemetry & landmark nodes
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        )
    )
    val landmarkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(AuraSurfaceLowest)
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(28.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onZoomToggle?.invoke()
                    },
                    onTap = { offset ->
                        if (size.width > 0 && !isHoldCompareActive) {
                            rawSplitFraction = (offset.x / size.width).coerceIn(0.04f, 0.96f)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    if (size.width > 0 && !isHoldCompareActive) {
                        rawSplitFraction = (change.position.x / size.width).coerceIn(0.04f, 0.96f)
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

            val baseScale = min(canvasW / imgW, canvasH / imgH)
            val scale = baseScale * animatedZoom
            val drawW = (imgW * scale).roundToInt()
            val drawH = (imgH * scale).roundToInt()
            val leftOff = ((canvasW - drawW) * 0.5f).roundToInt()
            val topOff = if (animatedZoom > 1.1f) {
                ((canvasH * 0.46f - drawH * 0.38f)).roundToInt()
            } else {
                ((canvasH - drawH) * 0.5f).roundToInt()
            }

            val visibleLeft = leftOff.toFloat().coerceAtLeast(0f)
            val visibleRight = (leftOff + drawW).toFloat().coerceAtMost(canvasW)
            val splitX = (visibleLeft + (visibleRight - visibleLeft) * effectiveSplitFraction)
                .coerceIn(visibleLeft, visibleRight)

            val drawSize = Size(drawW.toFloat(), drawH.toFloat())

            // Render Full Canvas
            clipRect(left = 0f, top = 0f, right = canvasW, bottom = canvasH) {
                // 1. Draw After (4K Restored) Image
                translate(left = leftOff.toFloat(), top = topOff.toFloat()) {
                    with(afterPainter) {
                        draw(size = drawSize, colorFilter = colorFilter)
                    }
                }

                // 2. Clip Left Side to draw Before (Original / 720p) Image
                clipRect(left = 0f, top = 0f, right = splitX, bottom = canvasH) {
                    translate(left = leftOff.toFloat(), top = topOff.toFloat()) {
                        with(beforePainter) {
                            draw(size = drawSize)
                        }
                    }
                }
            }

            val lineTop = topOff.toFloat().coerceAtLeast(0f)
            val lineBottom = (topOff + drawH).toFloat().coerceAtMost(canvasH)

            // Draw Divider Line with Specular Sheen
            if (!isHoldCompareActive) {
                // Ambient cyan/white glow drop shadow
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.2f),
                            AuraSecondary.copy(alpha = 0.7f),
                            Color.White.copy(alpha = 0.2f)
                        ),
                        startY = lineTop,
                        endY = lineBottom
                    ),
                    start = Offset(splitX, lineTop),
                    end = Offset(splitX, lineBottom),
                    strokeWidth = 3.dp.toPx()
                )
                drawLine(
                    color = Color.White,
                    start = Offset(splitX, lineTop),
                    end = Offset(splitX, lineBottom),
                    strokeWidth = 1.8.dp.toPx()
                )

                // Circular Specular Glass Disc Thumb Handle (Centered at 50% height)
                val handleY = lineTop + (lineBottom - lineTop) * 0.50f
                val handleRadius = 19.dp.toPx()

                // Ambient halo glow behind disc
                drawCircle(
                    color = AuraSecondaryContainer.copy(alpha = 0.40f),
                    radius = handleRadius + 6.dp.toPx(),
                    center = Offset(splitX, handleY)
                )

                // Translucent frosted glass core
                drawCircle(
                    color = Color.White.copy(alpha = 0.28f),
                    radius = handleRadius,
                    center = Offset(splitX, handleY)
                )

                // Specular 1.5px solid white border ring
                drawCircle(
                    color = Color.White,
                    radius = handleRadius,
                    center = Offset(splitX, handleY),
                    style = Stroke(width = 1.8.dp.toPx())
                )

                // Dual Opposing Chevrons (< >)
                val arrowOffset = 5.5.dp.toPx()
                val arrowSize = 4.2.dp.toPx()
                val chevronStroke = Stroke(
                    width = 2.4.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )

                val leftChevron = Path().apply {
                    moveTo(splitX - arrowOffset + arrowSize * 0.5f, handleY - arrowSize)
                    lineTo(splitX - arrowOffset - arrowSize * 0.5f, handleY)
                    lineTo(splitX - arrowOffset + arrowSize * 0.5f, handleY + arrowSize)
                }
                val rightChevron = Path().apply {
                    moveTo(splitX + arrowOffset - arrowSize * 0.5f, handleY - arrowSize)
                    lineTo(splitX + arrowOffset + arrowSize * 0.5f, handleY)
                    lineTo(splitX + arrowOffset - arrowSize * 0.5f, handleY + arrowSize)
                }

                drawPath(path = leftChevron, color = Color.White, style = chevronStroke)
                drawPath(path = rightChevron, color = Color.White, style = chevronStroke)
            }

            // Facial Landmark Detection Synthetic Wireframe Overlay (if enabled)
            if (showLandmarks) {
                val cx = leftOff + drawW * 0.50f
                val cy = topOff + drawH * 0.38f

                // Eye focal ring
                drawOval(
                    color = AuraSecondary.copy(alpha = landmarkAlpha * 0.6f),
                    topLeft = Offset(cx - 70.dp.toPx(), cy - 25.dp.toPx()),
                    size = Size(140.dp.toPx(), 50.dp.toPx()),
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // Left eye node
                drawCircle(
                    color = AuraSecondary,
                    radius = 3.dp.toPx() * pulseScale,
                    center = Offset(cx - 36.dp.toPx(), cy - 4.dp.toPx())
                )
                // Right eye node
                drawCircle(
                    color = AuraSecondary,
                    radius = 3.dp.toPx() * pulseScale,
                    center = Offset(cx + 36.dp.toPx(), cy - 4.dp.toPx())
                )

                // Smile & Skin Texture Ring
                drawOval(
                    color = AuraPrimaryElectric.copy(alpha = landmarkAlpha * 0.5f),
                    topLeft = Offset(cx - 50.dp.toPx(), cy + 24.dp.toPx()),
                    size = Size(100.dp.toPx(), 36.dp.toPx()),
                    style = Stroke(
                        width = 1.2.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )
                )
            }
        }

        // Top Overlay Telemetry Chip
        if (showTelemetry) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF131627).copy(alpha = 0.75f))
                    .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
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
                        text = "1080p → 4320p (4K UHD) • Sharpness: Ultra",
                        color = AuraOnSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Bottom HUD Badge: Left - Before
        if (effectiveSplitFraction > 0.08f) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 14.dp, bottom = 14.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0C0E18).copy(alpha = 0.70f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BlurOn,
                        contentDescription = "Blur",
                        tint = AuraOnSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = beforeLabel,
                        color = AuraOnSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                }
            }
        }

        // Bottom HUD Badge: Right - After
        if (effectiveSplitFraction < 0.92f) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 14.dp, bottom = 14.dp)
                    .clip(CircleShape)
                    .background(AuraPrimaryElectric.copy(alpha = 0.22f))
                    .border(1.dp, AuraPrimaryElectric.copy(alpha = 0.45f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Enhanced",
                        tint = AuraPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = afterLabel,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp
                    )
                }
            }
        }

        // Zoom Indicator Pill
        if (onZoomToggle != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 12.dp)
                    .clip(CircleShape)
                    .background(if (zoomFactor > 1.2f) AuraPrimaryElectric.copy(alpha = 0.35f) else Color(0xFF131627).copy(alpha = 0.70f))
                    .border(1.dp, Color.White.copy(alpha = 0.20f), CircleShape)
                    .clickable { onZoomToggle() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom",
                        tint = if (zoomFactor > 1.2f) AuraPrimary else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (zoomFactor > 1.2f) "200%" else "100%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
