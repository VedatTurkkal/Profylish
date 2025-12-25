package com.profylish.profylish

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.profylish.home.HomeScreen
import com.profylish.lesson.navigation.lessonScreen
import com.profylish.lesson.navigation.navigateToLesson
import com.profylish.onboarding.OnboardingGraph
import com.profylish.profylish.navigation.TopLevelDestination
import com.profylish.profylish.ui.components.ProfylishBottomBar
import com.profylish.profylish.ui.theme.ProfylishTheme
import com.profylish.ui.components.GamifiedTopBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProfylishTheme {
                ProfylishAppContent()
            }
        }
    }
}

@Composable
fun ProfylishAppContent(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    // MainViewModel'daki veriyi dinliyoruz (Seçilen Meslek burada!)
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val showBars = currentRoute in TopLevelDestination.entries.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (showBars) {
                GamifiedTopBar(
                    professionName = userProfile.currentProfession,
                    gemCount = userProfile.gems,
                    streakCount = userProfile.streak,
                    heartCount = userProfile.hearts,
                    onProfessionClick = { /* ... */ }
                )
            }
        },
        bottomBar = {
            if (showBars) {
                ProfylishBottomBar(
                    destinations = TopLevelDestination.entries.toTypedArray(),
                    currentDestination = currentDestination,
                    onNavigateToDestination = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "onboarding_flow",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("onboarding_flow") {
                OnboardingGraph(
                    onOnboardingFinished = { selectedJob ->
                        viewModel.updateProfession(selectedJob)
                        navController.navigate(TopLevelDestination.HOME.route) {
                            popUpTo("onboarding_flow") { inclusive = true }
                        }
                    }
                )
            }

            composable(TopLevelDestination.HOME.route) {
                HomeScreen(
                    // DEĞİŞİKLİK BURADA:
                    // Sadece Level ID geliyor. Profession'ı userProfile'dan alıyoruz.
                    onLessonClick = { levelId ->
                        navController.navigateToLesson(
                            levelId = levelId,
                            profession = userProfile.currentProfession // <-- DİNAMİK VERİ BURADA KULLANILIYOR
                        )
                    }
                )
            }

            composable(TopLevelDestination.LEADERBOARD.route) {}
            composable(TopLevelDestination.SHOP.route) {}
            composable(TopLevelDestination.PROFILE.route) {}

            // Lesson ekranı (Quiz)
            lessonScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}