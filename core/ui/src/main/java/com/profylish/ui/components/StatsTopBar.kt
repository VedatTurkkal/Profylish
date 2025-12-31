package com.profylish.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.profylish.ui.theme.ProfylishBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsTopBar(
    hearts: Int,
    gems: Int,
    streak: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .width(240.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatChip(
            icon = Icons.Rounded.Favorite,
            value = hearts.toString(),
            mainColor = Color(0xFFFF4B4B)
        )

        StatChip(
            icon = Icons.Rounded.Diamond,
            value = gems.toString(),
            mainColor = ProfylishBlue
        )

        StatChip(
            icon = Icons.Rounded.LocalFireDepartment,
            value = streak.toString(),
            mainColor = Color(0xFFFF9600)
        )
    }
}

@Composable
private fun StatChip(
    icon: ImageVector,
    value: String,
    mainColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, mainColor.copy(alpha = 0.2f)),
        color = Color.White,
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = mainColor, // İkon ana renk
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = mainColor, // Yazı da ana renk
                    fontWeight = FontWeight.ExtraBold // Daha kalın yazı = Daha oyunsu
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatsTopBarPreview() {
    MaterialTheme {
        StatsTopBar(
            hearts = 5,
            gems = 120,
            streak = 7
        )
    }
}