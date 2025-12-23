package com.profylish.profylish

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.profylish.profylish.ui.theme.ProfylishTheme
import com.profylish.onboarding.OnboardingGraph
import com.profylish.home.HomeScreen // <-- BU IMPORT ÖNEMLİ
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProfylishTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "onboarding_flow", // İlk açılış onboarding
        modifier = modifier
    ) {

        // 1. Onboarding Akışı
        composable("onboarding_flow") {
            OnboardingGraph(
                onOnboardingFinished = {
                    // Onboarding bitince dashboard'a git ve geri gelinememesi için stack'i temizle
                    navController.navigate("dashboard") {
                        popUpTo("onboarding_flow") { inclusive = true }
                    }
                }
            )
        }

        // 2. Ana Ekran (Home)
        composable("dashboard") {
            HomeScreen(
                onLessonClick = { lessonId ->
                    // Buraya derse tıklandığında ne olacağını yazacaksın.
                    // Örneğin: navController.navigate("lesson_detail/$lessonId")
                    Log.d("Navigation", "Derse tıklandı: $lessonId")
                }
                // viewModel parametresini Hilt otomatik olarak enjekte eder,
                // buraya manuel vermene gerek yok.
            )
        }
    }
}