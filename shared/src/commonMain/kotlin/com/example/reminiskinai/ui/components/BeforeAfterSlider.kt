package com.example.reminiskinai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.example.reminiskinai.theme.ReminiGold
import com.example.reminiskinai.theme.ReminiRose
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun BeforeAfterSlider(
    beforeImage: ImageBitmap,
    afterImage: ImageBitmap,
    afterLabel: String = "After",
    colorFilter: ColorFilter? = null,
    modifier: Modifier = Modifier
) {
    val beforePainter = remember(beforeImage) { BitmapPainter(beforeImage) }
    val afterPainter = remember(afterImage) { BitmapPainter(afterImage) }
    BeforeAfterSlider(
        beforePainter = beforePainter,
        afterPainter = afterPainter,
        afterLabel = afterLabel,
        colorFilter = colorFilter,
        modifier = modifier
    )
}

/**
 * Multiplatform Remini-style Before/After Split Comparison View with HD Zoom toggle (1x / 2x).
 * Left of the divider shows [beforePainter], right of the divider shows [afterPainter],
 * with a draggable circular `< >` handle matching Remini's Result screen.
 */
@Composable
fun BeforeAfterSlider(
    beforePainter: Painter,
    afterPainter: Painter,
    afterLabel: String = "After",
    colorFilter: ColorFilter? = null,
    modifier: Modifier = Modifier
) {
    var splitFraction by remember { mutableFloatStateOf(0.18f) }
    var zoomFactor by remember { mutableFloatStateOf(1.0f) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF121216))
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        zoomFactor = if (zoomFactor > 1.2f) 1.0f else 2.0f
                    },
                    onTap = { offset ->
                        if (size.width > 0) {
                            splitFraction = (offset.x / size.width).coerceIn(0.02f, 0.98f)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    if (size.width > 0) {
                        splitFraction = (change.position.x / size.width).coerceIn(0.02f, 0.98f)
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
            val scale = baseScale * zoomFactor
            val drawW = (imgW * scale).roundToInt()
            val drawH = (imgH * scale).roundToInt()
            val leftOff = ((canvasW - drawW) * 0.5f).roundToInt()
            val topOff = if (zoomFactor > 1.1f) {
                ((canvasH * 0.46f - drawH * 0.36f)).roundToInt()
            } else {
                ((canvasH - drawH) * 0.5f).roundToInt()
            }

            val visibleLeft = leftOff.toFloat().coerceAtLeast(0f)
            val visibleRight = (leftOff + drawW).toFloat().coerceAtMost(canvasW)
            val splitX = (visibleLeft + (visibleRight - visibleLeft) * splitFraction)
                .coerceIn(visibleLeft, visibleRight)

            val drawSize = Size(drawW.toFloat(), drawH.toFloat())

            clipRect(left = 0f, top = 0f, right = canvasW, bottom = canvasH) {
                // Draw After image on full canvas
                translate(left = leftOff.toFloat(), top = topOff.toFloat()) {
                    with(afterPainter) {
                        draw(size = drawSize, colorFilter = colorFilter)
                    }
                }

                // Clip left side [0 .. splitX] to draw Before image
                clipRect(left = 0f, top = 0f, right = splitX, bottom = canvasH) {
                    translate(left = leftOff.toFloat(), top = topOff.toFloat()) {
                        with(beforePainter) {
                            draw(size = drawSize)
                        }
                    }
                }
            }

            // Draw vertical divider line
            val lineTop = topOff.toFloat().coerceAtLeast(0f)
            val lineBottom = (topOff + drawH).toFloat().coerceAtMost(canvasH)
            drawLine(
                color = Color.Black.copy(alpha = 0.35f),
                start = Offset(splitX + 1.5f, lineTop),
                end = Offset(splitX + 1.5f, lineBottom),
                strokeWidth = 3.dp.toPx()
            )
            drawLine(
                color = Color.White,
                start = Offset(splitX, lineTop),
                end = Offset(splitX, lineBottom),
                strokeWidth = 2.dp.toPx()
            )

            // Draw circular drag handle with `< >` arrows at 72% height
            val handleY = lineTop + (lineBottom - lineTop) * 0.72f
            val handleRadius = 18.dp.toPx()

            drawCircle(
                color = Color.Black.copy(alpha = 0.45f),
                radius = handleRadius + 1.5.dp.toPx(),
                center = Offset(splitX, handleY + 1.5.dp.toPx())
            )
            drawCircle(
                color = Color.White,
                radius = handleRadius,
                center = Offset(splitX, handleY)
            )

            val arrowOffset = 5.dp.toPx()
            val arrowSize = 4.5.dp.toPx()
            val chevronStroke = Stroke(
                width = 2.2.dp.toPx(),
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

            drawPath(path = leftChevron, color = Color(0xFF141418), style = chevronStroke)
            drawPath(path = rightChevron, color = Color(0xFF141418), style = chevronStroke)
        }

        // Top-Left "Before" badge
        if (splitFraction > 0.14f) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.58f))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "Before",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Top-Right "After" badge
        if (splitFraction < 0.86f) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.58f))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = afterLabel,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Bottom-Right HD Zoom Toggle Pill
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(50))
                .background(if (zoomFactor > 1.1f) ReminiRose else Color.Black.copy(alpha = 0.68f))
                .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(50))
                .clickable {
                    zoomFactor = if (zoomFactor > 1.1f) 1.0f else 2.0f
                }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "1080p HD",
                    color = if (zoomFactor > 1.1f) Color.White else ReminiGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (zoomFactor > 1.1f) "2× Zoom (Tap 1×)" else "Tap 2× Zoom",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
