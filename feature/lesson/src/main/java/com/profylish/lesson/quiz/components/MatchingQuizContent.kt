package com.profylish.lesson.quiz.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MatchingQuizContent(
    pairs: List<Pair<String, String>>,
    onAllMatched: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // Listeleri ilk seferde karıştır ve hatırla
    val leftItems = remember(pairs) { pairs.map { it.first }.shuffled() }
    val rightItems = remember(pairs) { pairs.map { it.second }.shuffled() }

    val matchedItems = remember { mutableStateListOf<String>() }
    var selectedLeft by remember { mutableStateOf<String?>(null) }
    var selectedRight by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    // Eşleşme Kontrol Fonksiyonu
    fun checkMatch(left: String, right: String) {
        val isCorrect = pairs.any { it.first == left && it.second == right }

        if (isCorrect) {
            matchedItems.add(left)
            matchedItems.add(right)
            selectedLeft = null
            selectedRight = null

            // KRİTİK NOKTA: Eğer her şey bittiyse ViewModel'i çağır
            if (matchedItems.size >= pairs.size * 2) {
                onAllMatched()
            }
        } else {
            coroutineScope.launch {
                isError = true
                delay(500)
                isError = false
                selectedLeft = null
                selectedRight = null
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tap matching pairs",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // SOL SÜTUN
            Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                leftItems.forEach { term ->
                    val isMatched = matchedItems.contains(term)
                    if (!isMatched) {
                        MatchingCard(
                            text = term,
                            isSelected = selectedLeft == term,
                            isError = isError && selectedLeft == term,
                            onClick = {
                                if (isError) return@MatchingCard
                                selectedLeft = term
                                // Eğer sağ taraf zaten seçiliyse kontrol et
                                selectedRight?.let { checkMatch(term, it) }
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    } else {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }

            // SAĞ SÜTUN
            Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                rightItems.forEach { definition ->
                    val isMatched = matchedItems.contains(definition)
                    if (!isMatched) {
                        MatchingCard(
                            text = definition,
                            isSelected = selectedRight == definition,
                            isError = isError && selectedRight == definition,
                            onClick = {
                                if (isError) return@MatchingCard
                                selectedRight = definition
                                // Eğer sol taraf zaten seçiliyse kontrol et
                                selectedLeft?.let { checkMatch(it, definition) }
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    } else {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MatchingCard(
    text: String,
    isSelected: Boolean,
    isError: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by androidx.compose.animation.animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.errorContainer
            isSelected -> Color(0xFFDDF4FF)
            else -> MaterialTheme.colorScheme.surface
        }
    )

    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        isSelected -> Color(0xFF1CB0F6)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                maxLines = 3
            )
        }
    }
}