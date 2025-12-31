package com.profylish.profylish

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.profylish.auth.AuthScreen
import com.profylish.chat.ChatScreen
import com.profylish.home.HomeScreen
import com.profylish.leaderboard.LeaderboardScreen
// Navigasyon fonksiyonlarını import ettiğinden emin ol
import com.profylish.lesson.navigation.lessonScreen
import com.profylish.lesson.navigation.navigateToLesson
import com.profylish.onboarding.OnboardingGraph
import com.profylish.profile.ProfileScreen
import com.profylish.profylish.navigation.TopLevelDestination
import com.profylish.profylish.ui.components.ProfylishBottomBar
import com.profylish.profylish.ui.theme.ProfylishTheme
import com.profylish.settings.SettingsScreen
import com.profylish.shop.ShopScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val startRoute by mainViewModel.startDestination.collectAsStateWithLifecycle()
            val isDarkMode by mainViewModel.isDarkMode.collectAsStateWithLifecycle()

            ProfylishTheme(
                darkTheme = isDarkMode
            ) {
                if (startRoute != null) {
                    ProfylishAppContent(startDestination = startRoute!!)
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color.White))
                }
            }
        }
    }
}

@Composable
fun ProfylishAppContent(
    startDestination: String
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val showBars = currentRoute in TopLevelDestination.entries.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("onboarding_flow") {
                OnboardingGraph(
                    onOnboardingFinished = { _ ->
                        navController.navigate(TopLevelDestination.HOME.route) {
                            popUpTo("onboarding_flow") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate("auth_route")
                    }
                )
            }

            composable(TopLevelDestination.HOME.route) {
                HomeScreen(
                    // 👇 DÜZELTME BURADA YAPILDI
                    // HomeScreen artık (levelId, profession, category) döndürüyor.
                    // 'category' bir String'dir (Örn: "TERM", "IDIOM").
                    onLessonClick = { levelId, profession, category ->
                        // LessonNavigation dosyasını güncellediğimiz için artık String kabul ediyor
                        navController.navigateToLesson(levelId, profession, category)
                    },
                    onNavigateToSearch = {
                        navController.navigate("onboarding_flow")
                    },
                    onNavigateToChat = {
                        navController.navigate("chat_route")
                    }
                )
            }

            composable("chat_route") {
                ChatScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(TopLevelDestination.LEADERBOARD.route) {
                LeaderboardScreen()
            }

            composable(TopLevelDestination.SHOP.route) {
                ShopScreen()
            }

            composable(TopLevelDestination.PROFILE.route) {
                ProfileScreen(
                    onNavigateToAuth = {
                        navController.navigate("auth_route")
                    },
                    onNavigateToSettings = {
                        navController.navigate("settings_route")
                    }
                )
            }

            composable("settings_route") {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() },
                    onSignOutSuccess = {
                        navController.popBackStack()
                    }
                )
            }

            composable("auth_route") {
                AuthScreen(
                    onNavigateToHome = {
                        navController.popBackStack()
                    }
                )
            }

            // lessonScreen artık 'category' parametresini de içeriyor (LessonNavigation içinde güncelledik)
            lessonScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToAuth = {
                    navController.navigate("auth_route")
                }
            )
        }
    }
}