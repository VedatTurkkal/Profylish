package com.profylish.home.roadmap.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.profylish.model.roadmap.NodeStatus
import com.profylish.model.roadmap.RoadmapNode
import androidx.compose.foundation.layout.Box

@Composable
fun LevelNode(
    node: RoadmapNode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = node.status == NodeStatus.COMPLETED
    val isActive = node.status == NodeStatus.ACTIVE

    val mainColor = if (isCompleted) Color(0xFFFFC800) else MaterialTheme.colorScheme.primary
    val shadowColor = if (isCompleted) Color(0xFFC29900) else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)

    // Aktif ders animasyonu
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.05f else 1f, // Çok abartılı olmaması için 1.05
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(100.dp) // Alanı biraz geniş tutuyoruz ki yıldızlar sığsın
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Ripple efektini kapatıyoruz (Oyun tarzı butonlarda genelde olmaz)
            ) { onClick() }
    ) {
        // --- GÖLGE KATMANI ---
        Box(
            modifier = Modifier
                .size(70.dp)
                .offset(y = 6.dp)
                .clip(CircleShape)
                .background(shadowColor)
        )

        // --- ANA BUTON KATMANI ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(mainColor)
        ) {
            val icon: ImageVector = if (isCompleted) Icons.Default.Check else Icons.Default.Star

            // Eğer aktifse ve tamamlanmamışsa 'Star' ikonu, tamamlanmışsa 'Check'
            // İç ikon da hafif beyaz olsun
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        // --- YILDIZLAR (Tamamlananlar için) ---
        if (isCompleted && node.stars > 0) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 4.dp), // Butonun biraz üzerine binsin
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(node.stars) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700), // Altın sarısı
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LockedLessonNode(
    modifier: Modifier = Modifier
) {
    val lockedColor = Color(0xFFE5E5E5)
    val lockedShadow = Color(0xFFC7C7C7)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(100.dp)
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .offset(y = 4.dp)
                .clip(CircleShape)
                .background(lockedShadow)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(lockedColor)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = Color(0xFFAAAAAA),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}