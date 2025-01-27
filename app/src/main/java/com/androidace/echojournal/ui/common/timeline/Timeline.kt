package com.androidace.echojournal.ui.common.timeline

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Timeline(
    modifier: Modifier = Modifier,
    lineType: LineType = LineType.MIDDLE,
    lineStyle: LineStyle = LineStyle.solid(),
    orientation: TimelineOrientation = TimelineOrientation.Vertical,
    marker: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .drawTimeline(
                lineType = lineType,
                lineStyle = lineStyle,
                orientation = orientation
            ),
        contentAlignment = Alignment.TopCenter

    ) {
        Box(modifier = Modifier.padding(top = 8.dp)) {
            marker()
        }
    }
}

private fun Modifier.drawTimeline(
    lineType: LineType,
    lineStyle: LineStyle,
    orientation: TimelineOrientation
) = drawWithCache {
    // Get the marker size from the layout size
    val markerSize = minOf(size.width, size.height).toDp()

    onDrawWithContent {
        drawContent()

        if (lineType != LineType.SINGLE) {
            when (orientation) {
                TimelineOrientation.Vertical -> drawVerticalLines(lineType, markerSize, lineStyle)
            }
        }
    }
}

private fun DrawScope.drawVerticalLines(
    lineType: LineType,
    markerSize: Dp,
    lineStyle: LineStyle
) {
    when (lineType) {
        LineType.START -> {
            drawEndLine(markerSize, lineStyle)
        }

        LineType.END -> {
            drawStartLine(markerSize, lineStyle)
        }

        LineType.MIDDLE -> {
            drawStartLine(markerSize, lineStyle)
            drawEndLine(markerSize, lineStyle)
        }

        LineType.SINGLE -> { /* No lines */
        }
    }
}

private fun DrawScope.drawStartLine(markerSize: Dp, lineStyle: LineStyle) {
    val centerX = size.width / 2
    val startOffset = Offset(centerX, 0f)
    val endOffset = Offset(centerX, 26.0f)
    drawLine(
        color = lineStyle.color,
        start = startOffset,
        end = endOffset,
        strokeWidth = lineStyle.width.toPx(),
        pathEffect = lineStyle.pathEffect
    )
}

private fun DrawScope.drawEndLine(markerSize: Dp, lineStyle: LineStyle) {
    val centerX = size.width / 2

    val startOffset = Offset(centerX, markerSize.toPx() + 16.0f)
    val endOffset = Offset(centerX, size.height)

    Log.d("markerSize", "${markerSize.toPx()}")
    Log.d("startOffset", " ${startOffset.x} ${startOffset.y}")
    Log.d("endOffset", " ${endOffset.x} ${endOffset.y}")

    drawLine(
        color = lineStyle.color,
        start = startOffset,
        end = endOffset,
        strokeWidth = lineStyle.width.toPx(),
        pathEffect = lineStyle.pathEffect
    )
}