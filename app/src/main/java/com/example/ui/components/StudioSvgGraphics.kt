package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Renders an XML VectorDrawable SVG asset without using Material Icon components.
 */
@Composable
fun SvgDrawableGraphic(
    @DrawableRes drawableResId: Int,
    tint: Color,
    size: Dp = 18.dp,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = drawableResId),
        contentDescription = contentDescription,
        colorFilter = ColorFilter.tint(tint),
        modifier = modifier.size(size)
    )
}

/**
 * Custom Canvas SVG graphic for Record Dot / Stop Square / Play Triangle / Arrow Next.
 */
@Composable
fun SvgRecordDotGraphic(
    color: Color,
    size: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val radius = this.size.minDimension / 2f
        drawCircle(
            color = color.copy(alpha = 0.28f),
            radius = radius
        )
        drawCircle(
            color = color,
            radius = radius * 0.58f
        )
    }
}

@Composable
fun SvgStopSquareGraphic(
    color: Color,
    size: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val dim = this.size.minDimension
        drawCircle(
            color = color.copy(alpha = 0.22f),
            radius = dim / 2f
        )
        val sq = dim * 0.44f
        val offset = (dim - sq) / 2f
        drawRoundRect(
            color = color,
            topLeft = Offset(offset, offset),
            size = Size(sq, sq),
            cornerRadius = CornerRadius(3f, 3f)
        )
    }
}

@Composable
fun SvgPlayPauseGraphic(
    isPlaying: Boolean,
    color: Color,
    size: Dp = 15.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        if (isPlaying) {
            val barW = w * 0.24f
            drawRoundRect(
                color = color,
                topLeft = Offset(w * 0.18f, h * 0.15f),
                size = Size(barW, h * 0.7f),
                cornerRadius = CornerRadius(2f, 2f)
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(w * 0.58f, h * 0.15f),
                size = Size(barW, h * 0.7f),
                cornerRadius = CornerRadius(2f, 2f)
            )
        } else {
            val path = Path().apply {
                moveTo(w * 0.22f, h * 0.14f)
                lineTo(w * 0.84f, h * 0.5f)
                lineTo(w * 0.22f, h * 0.86f)
                close()
            }
            drawPath(path = path, color = color)
        }
    }
}

@Composable
fun SvgArrowForwardGraphic(
    color: Color,
    size: Dp = 15.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = w * 0.14f
        drawLine(
            color = color,
            start = Offset(w * 0.15f, h * 0.5f),
            end = Offset(w * 0.82f, h * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        val head = Path().apply {
            moveTo(w * 0.52f, h * 0.2f)
            lineTo(w * 0.84f, h * 0.5f)
            lineTo(w * 0.52f, h * 0.8f)
        }
        drawPath(
            path = head,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
