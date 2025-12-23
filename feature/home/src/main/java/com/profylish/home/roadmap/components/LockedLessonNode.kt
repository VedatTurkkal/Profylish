package com.profylish.home.roadmap.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LockedLessonNode(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {} // Kilitli derse basınca "titreme" animasyonu tetiklenebilir
) {
    val lockedColor = Color(0xFFE5E5E5) // Açık Gri
    val lockedShadow = Color(0xFFCECECE) // Koyu Gri

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(90.dp)
            .clickable { onClick() }
    ) {
        // 1. Katman: Gölge (Daha ince)
        Box(
            modifier = Modifier
                .size(70.dp)
                .offset(y = 4.dp)
                .clip(CircleShape)
                .background(lockedShadow)
        )

        // 2. Katman: Buton
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
                tint = Color(0xFFAAAAAA), // Silik kilit rengi
                modifier = Modifier.size(28.dp)
            )
        }
    }
}