package com.profylish.profylish

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import com.profylish.lesson.navigation.lessonRoute
import com.profylish.lesson.navigation.lessonScreen
import com.profylish.lesson.navigation.navigateToLesson
import com.profylish.onboarding.OnboardingGraph
import com.profylish.profile.ProfileScreen
import com.profylish.profylish.navigation.TopLevelDestination
import com.profylish.profylish.ui.components.NoInternetScreen
import com.profylish.profylish.ui.components.OfflineBanner
import com.profylish.profylish.ui.components.ProfylishBottomBar
import com.profylish.profylish.ui.theme.ProfylishTheme
import com.profylish.settings.SettingsScreen
import com.profylish.shop.ShopScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val isOnboardingFinished = sharedPref.getBoolean("onboarding_complete", false)

        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()

            // ViewModel'den gelen rota önerisi
            val vmStartRoute by mainViewModel.startDestination.collectAsStateWithLifecycle()
            val isDarkMode by mainViewModel.isDarkMode.collectAsStateWithLifecycle()
            val isOffline by mainViewModel.isOffline.collectAsStateWithLifecycle()
            val isLoading by mainViewModel.isLoading.collectAsStateWithLifecycle()

            // 2. Nihai Karar: Eğer SharedPrefs "Bitti" diyorsa, ViewModel "Onboarding" dese bile
            // onu ezip HOME'a yönlendiriyoruz. Bu kısır döngüyü kırar.
            val finalStartDestination = if (isOnboardingFinished) {
                TopLevelDestination.HOME.route
            } else {
                vmStartRoute
            }

            splashScreen.setKeepOnScreenCondition {
                isLoading
            }

            ProfylishTheme(
                darkTheme = isDarkMode
            ) {
                // Burada 'finalStartDestination != null' kontrolü sayesinde Kotlin Smart Cast yapar.
                // Yani aşağıda '!!' kullanmamıza gerek kalmaz.
                if (finalStartDestination != null && (!isLoading || isOnboardingFinished)) {
                    ProfylishAppContent(
                        startDestination = finalStartDestination,
                        isOffline = isOffline
                    )
                } else {
                    // Fallback: Çok nadir durumda boş ekran gösterir
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
                }
            }
        }
    }
}

@Composable
fun ProfylishAppContent(
    startDestination: String,
    isOffline: Boolean
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val isQuizSession = currentRoute?.startsWith(lessonRoute) == true
    val showFullScreenBlocker = isOffline && !isQuizSession
    val showOfflineBanner = isOffline && isQuizSession

    if (showFullScreenBlocker) {
        NoInternetScreen()
    } else {
        val showBars = currentRoute in TopLevelDestination.entries.map { it.route }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                AnimatedVisibility(
                    visible = showOfflineBanner,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    OfflineBanner()
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
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("onboarding_flow") {
                    OnboardingGraph(
                        // Parametre kullanılmadığı için ismini '_' yaptık (Clean Code)
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
                        onLessonClick = { levelId, profession, category ->
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
                            navController.navigate(TopLevelDestination.HOME.route) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                lessonScreen(
                    onBackClick = { navController.popBackStack() },
                    onNavigateToAuth = {
                        navController.navigate("auth_route")
                    }
                )
            }
        }
    }
}