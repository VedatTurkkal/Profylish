package com.profylish.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow // <-- BU IMPORT GEREKLİ
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GamifiedTopBar(
    professionName: String,
    gemCount: Int,
    streakCount: Int,
    heartCount: Int,
    modifier: Modifier = Modifier,
    onProfessionClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth().statusBarsPadding(), // statusBarsPadding burada kalmalı
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // --- SOL KISIM: Meslek Seçimi ---
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onProfessionClick() }
                    .padding(4.dp)
                    // KRİTİK NOKTA 1:
                    // fill = false diyerek sadece gerektiği kadar yer kaplamasını,
                    // ama weight(1f) ile de sağ tarafı sıkıştırmamasını sağlıyoruz.
                    .weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Work,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))

                // KRİTİK NOKTA 2: Metin ayarları
                Text(
                    text = professionName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = MaterialTheme.typography.titleMedium.fontWeight,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1, // Tek satırda kal
                    overflow = TextOverflow.Ellipsis // Sığmazsa "..." koy
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Ortadaki boşluk artık "esnek" değil, sabit bir boşluk olabilir
            // veya weight mantığıyla kalan alanı doldurur.
            // Sol tarafa weight(1f, fill=false) verdiğimiz için Spacer sadece arayı açar.
            Spacer(modifier = Modifier.width(8.dp))

            // --- SAĞ KISIM: İstatistikler ---
            // Burayı Row içine alıp `Arrangement.End` yaparsak sağa yaslanır
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                StatItem(
                    icon = Icons.Default.Diamond,
                    value = gemCount,
                    color = Color(0xFF29B6F6)
                )

                StatDivider()

                StatItem(
                    icon = Icons.Default.LocalFireDepartment,
                    value = streakCount,
                    color = Color(0xFFFFA726)
                )

                StatDivider()

                StatItem(
                    icon = Icons.Default.Favorite,
                    value = heartCount,
                    color = Color(0xFFEF5350)
                )
            }
        }
    }
}

// ... StatItem ve StatDivider aynı kalabilir ...
@Composable
private fun StatItem(
    icon: ImageVector,
    value: Int,
    color: Color
) {
    Row(
        modifier = Modifier.padding(horizontal = 6.dp), // Padding'i biraz kıstım sığması için
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp
            )
        )
    }
}

@Composable
private fun StatDivider() {
    Divider(
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier
            .height(20.dp)
            .width(1.dp)
    )
}