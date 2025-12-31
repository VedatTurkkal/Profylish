package com.profylish.leaderboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.profylish.model.leaderboard.LeaderboardEntry
import com.profylish.model.leaderboard.RankTrend

@Composable
fun LeaderboardItem(
    entry: LeaderboardEntry,
    modifier: Modifier = Modifier
) {
    val rankColor = when (entry.rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val backgroundColor = if (entry.isCurrentUser) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = if (entry.isCurrentUser) MaterialTheme.colorScheme.primary else Color.Transparent

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${entry.rank}",
                fontWeight = FontWeight.Bold,
                color = rankColor,
                fontSize = 18.sp,
                modifier = Modifier.width(30.dp)
            )

            if (entry.avatarUrl != null) {
                AsyncImage(
                    model = entry.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.LightGray, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entry.username.firstOrNull()?.toString()?.uppercase() ?: "?",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // İSİM VE TREND
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (entry.isCurrentUser) "${entry.username} (You)" else entry.username,
                    fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.Normal,
                    color = if (entry.isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )

                TrendIndicator(entry.trend)
            }

            // XP
            Text(
                text = "${entry.xp} XP",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun TrendIndicator(trend: RankTrend) {
    val (text, color) = when (trend) {
        RankTrend.UP -> "▲" to Color.Green
        RankTrend.DOWN -> "▼" to Color.Red
        RankTrend.STABLE -> "-" to Color.Gray
    }
    Text(text = text, color = color, fontSize = 10.sp)
}