package com.profylish.onboarding.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OccupationSearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onOccupationSelected: (String) -> Unit
) {
    // ViewModel'den verileri dinliyoruz
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState() // Yazılan metni dinle

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Ne öğrenmek istersin?",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 👇 ARAMA ÇUBUĞU (SEARCH BAR) 👇
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                // Her harf girişinde ViewModel'e haber veriyoruz.
                // ViewModel içindeki "debounce" sayesinde "lazy" arama otomatik çalışacak.
                viewModel.onSearchQueryChanged(query)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Meslek ara... (örn: Engineer)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Ara") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        // 👆 -------------------------- 👆

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            // Yükleniyor animasyonu
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            // Hata mesajı
            Text(
                text = "Hata: ${uiState.errorMessage}",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // LİSTELEME
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.occupations) { occupation ->
                    Button(
                        // NOT: occupation.title kısmı senin modeline göre değişebilir (jobTitle vb.)
                        onClick = { onOccupationSelected(occupation.title) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.aspectRatio(1f)
                    ) {
                        Text(
                            text = occupation.title,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}