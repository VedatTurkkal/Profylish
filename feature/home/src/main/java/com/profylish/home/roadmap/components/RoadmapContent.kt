package com.profylish.home.roadmap.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
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
    // 👇 YENİ EKLENEN KISIM: LİSTE BOŞSA UYARI GÖSTER
    if (nodes.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No lessons found.\n\nPossible reasons:\n1. Supabase RLS is enabled (Disable it!)\n2. No words match this career in DB.",
                color = Color.Gray, // Arka plan koyuysa açık renk, açıksa koyu renk seçilmeli
                textAlign = TextAlign.Center
            )
        }
        return // Fonksiyondan çık, gerisini çizme
    }
    // 👆 -------------------------------------------

    val listState = rememberLazyListState()

    // Her bir satırın yüksekliği (Node + Boşluk)
    val itemHeight = 120.dp

    // Zikzak sapma miktarı (Sağa/Sola ne kadar gideceği)
    val zigZagOffset = 70.dp

    // İlk açılışta Aktif olan derse otomatik scroll
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
                        LockedLessonNode(modifier = nodeModifier)
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