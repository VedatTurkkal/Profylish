package com.profylish.onboarding.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // ✅ Bu gerekli
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
// import com.profylish.onboarding.search.components.OccupationResultItem (Eğer kullanıyorsan)

@Composable
fun OccupationSearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    // 👇 DİKKAT: Burası artık 2 parametre almalı (ID ve Group)
    onOccupationSelected: (String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // 👇 EKSİK OLAN PARÇA BURASIYDI: Event Dinleyici
    // ViewModel'den gelen "Git" emrini burada yakalıyoruz.
    LaunchedEffect(true) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is SearchNavigationEvent.NavigateToPersonalization -> {
                    // Graph'e haber ver: "Bu ID ve Bu Grup ile ilerle"
                    onOccupationSelected(event.occupationId, event.occupationGroup)
                }
            }
        }
    }
    // 👆 -------------------------------------------

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "What would you like to learn?",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search for a job") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            Text(
                text = "Error: ${uiState.errorMessage}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxSize(),
                textAlign = TextAlign.Center
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.occupations) { occupation ->
                    Button(
                        onClick = {
                            // Tıklanınca ViewModel'e bildiriyoruz
                            viewModel.onOccupationSelected(occupation)
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.aspectRatio(1f)
                    ) {
                        Text(text = occupation.title, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}