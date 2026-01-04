package com.profylish.leaderboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profylish.leaderboard.components.LeaderboardItem
import com.profylish.leaderboard.components.LeagueHeader
import com.profylish.leaderboard.components.PromotionZone
import com.profylish.ui.components.RavenMascot
import com.profylish.ui.components.RavenState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.fetchLeaderboardData(isInitialLoad = false)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Leaderboard", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is LeaderboardUiState.Loading -> {
                    // Sadece dönen bir şey yerine, düşünen bir Raven
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RavenMascot(
                            state = RavenState.THINKING, // veya STATS
                            modifier = Modifier.size(120.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Loading ranks...", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                is LeaderboardUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Hata durumunda kafası karışık Raven
                        RavenMascot(
                            state = RavenState.CONFUSED,
                            modifier = Modifier.size(120.dp)
                        )
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = { viewModel.fetchLeaderboardData(isInitialLoad = true) }) {
                            Text("Retry")
                        }
                    }
                }

                is LeaderboardUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {

                        // Eğer liste boşsa veya çok az kişi varsa motive edici bir Raven koyulabilir
                        // Ancak burada liste dolu olduğu için Header ve Liste devam ediyor.

                        LeagueHeader(
                            tierName = state.tier.name,
                            timeLeft = state.timeRemaining
                        )

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {

                            item {
                                PromotionZone(
                                    message = "Top 3 promote to next league!",
                                    color = Color(0xFF4CAF50)
                                )
                            }

                            items(state.entries) { entry ->
                                LeaderboardItem(entry = entry)

                                when (entry.rank) {
                                    3 -> {
                                        PromotionZone(
                                            message = "Promotion Zone",
                                            color = Color.Gray
                                        )
                                    }
                                    (state.entries.size - 5).coerceAtLeast(4) -> {
                                        PromotionZone(
                                            message = "Demotion Zone",
                                            color = Color(0xFFE53935)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}