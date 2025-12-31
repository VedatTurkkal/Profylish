package com.profylish.home.roadmap.components

import android.content.Context // ✅ EKLENDİ
import android.os.Build // ✅ EKLENDİ
import android.os.VibrationEffect // ✅ EKLENDİ
import android.os.Vibrator // ✅ EKLENDİ
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.profylish.model.roadmap.NodeStatus
import com.profylish.model.roadmap.RoadmapNode

@Composable
fun RoadmapContent(
    nodes: List<RoadmapNode>,
    onNodeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current

    val itemHeight = 120.dp
    val zigZagOffset = 70.dp

    LaunchedEffect(nodes) {
        val activeIndex = nodes.indexOfFirst { it.status == NodeStatus.ACTIVE }
        if (activeIndex != -1) {
            listState.scrollToItem(activeIndex)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val centerOffset = maxWidth / 2

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp, top = 40.dp),
            reverseLayout = true
        ) {
            itemsIndexed(nodes) { index, node ->

                val currentOffset = calculateZigZagOffset(index, zigZagOffset)
                val nextOffset = if (index < nodes.lastIndex) {
                    calculateZigZagOffset(index + 1, zigZagOffset)
                } else null

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    if (nextOffset != null) {
                        val nextNode = nodes[index + 1]
                        val pathColor = if (nextNode.status == NodeStatus.LOCKED)
                            Color(0xFFE0E0E0)
                        else
                            Color(0xFFFFC800)

                        PathLine(
                            startX = centerOffset + currentOffset,
                            endX = centerOffset + nextOffset,
                            itemHeight = itemHeight,
                            color = pathColor
                        )
                    }

                    val nodeModifier = Modifier.offset(x = currentOffset)

                    if (node.status == NodeStatus.LOCKED) {
                        LockedLessonNode(
                            modifier = nodeModifier,
                            onClick = {
                                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

                                if (vibrator.hasVibrator()) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                                    } else {
                                        @Suppress("DEPRECATION")
                                        vibrator.vibrate(100)
                                    }
                                }

                                Toast.makeText(
                                    context,
                                    "Locked! Complete previous level first. 🔒",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    } else {
                        LevelNode(
                            node = node,
                            onClick = { onNodeClick(node.id) },
                            modifier = nodeModifier
                        )
                    }
                }
            }
        }
    }
}

private fun calculateZigZagOffset(index: Int, amplitude: Dp): Dp {
    return when (index % 4) {
        0 -> 0.dp
        1 -> amplitude
        2 -> 0.dp
        3 -> -amplitude
        else -> 0.dp
    }
}