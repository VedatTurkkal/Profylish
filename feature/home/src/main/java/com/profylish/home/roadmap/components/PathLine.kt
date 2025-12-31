package com.profylish.home.roadmap.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp

@Composable
fun PathLine(
    startX: Dp,
    endX: Dp,
    itemHeight: Dp,
    color: Color
) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val startPoint = Offset(x = startX.toPx(), y = size.height / 2)

        val endPoint = Offset(x = endX.toPx(), y = (size.height / 2) - itemHeight.toPx())

        val verticalDistance = startPoint.y - endPoint.y
        val controlPoint1 = Offset(
            x = startPoint.x,
            y = startPoint.y - (verticalDistance * 0.5f)
        )
        val controlPoint2 = Offset(
            x = endPoint.x,
            y = endPoint.y + (verticalDistance * 0.5f)
        )

        val path = Path().apply {
            moveTo(startPoint.x, startPoint.y)
            cubicTo(
                x1 = controlPoint1.x, y1 = controlPoint1.y,
                x2 = controlPoint2.x, y2 = controlPoint2.y,
                x3 = endPoint.x, y3 = endPoint.y
            )
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 24f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = if (color == Color(0xFFE0E0E0))
                    PathEffect.dashPathEffect(floatArrayOf(40f, 20f))
                else null
            )
        )
    }
}