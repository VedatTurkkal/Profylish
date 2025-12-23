package com.profylish.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
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
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White
        ),
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp), // Kenarlardan biraz boşluk
                horizontalArrangement = Arrangement.SpaceEvenly, // Eşit dağılım
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kalp (Hearts)
                StatChip(
                    icon = Icons.Rounded.Favorite, // Rounded ikonlar daha yumuşak durur
                    value = hearts.toString(),
                    mainColor = Color(0xFFFF4B4B) // Canlı Kırmızı
                )

                // Elmas (Gems)
                StatChip(
                    icon = Icons.Rounded.Diamond, // Material Extended kütüphanesi gerekir
                    value = gems.toString(),
                    mainColor = ProfylishBlue
                )

                // Seri (Streak)
                StatChip(
                    icon = Icons.Rounded.LocalFireDepartment,
                    value = streak.toString(),
                    mainColor = Color(0xFFFF9600) // Canlı Turuncu
                )
            }
        },
        modifier = modifier
    )
}

@Composable
private fun StatChip(
    icon: ImageVector,
    value: String,
    mainColor: Color
) {
    // Surface ile bir "hap" (pill) şekli oluşturuyoruz
    Surface(
        shape = RoundedCornerShape(12.dp),
        // Kenarlık rengini ana rengin biraz şeffaf hali yapıyoruz ki kibar dursun
        border = BorderStroke(2.dp, mainColor.copy(alpha = 0.2f)),
        color = Color.White, // Arka plan beyaz kalsın, temiz görünür
        modifier = Modifier.height(40.dp) // Yükseklik standardı
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