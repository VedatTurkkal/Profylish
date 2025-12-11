package com.profylish.profylish

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.profylish.profylish.ui.theme.ProfylishTheme // Teman burada (paket ismine dikkat)
import com.profylish.onboarding.OnboardingGraph // Feature modülünden geliyor
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
        startDestination = "onboarding_flow",
        modifier = modifier
    ) {

        composable("onboarding_flow") {
            OnboardingGraph(
                onOnboardingFinished = {
                    navController.navigate("dashboard") {
                        popUpTo("onboarding_flow") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            Text(text = "🎉 Login Successful! You are on the Home Page.")
        }
    }
}