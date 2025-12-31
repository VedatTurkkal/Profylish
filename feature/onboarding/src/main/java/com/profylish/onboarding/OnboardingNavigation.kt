package com.profylish.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.profylish.onboarding.personalization.LevelSelectionScreen
import com.profylish.onboarding.search.OccupationSearchScreen
import com.profylish.onboarding.welcome.WelcomeScreen

@Composable
fun OnboardingGraph(
    onOnboardingFinished: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "welcome"

    val progress by animateFloatAsState(
        targetValue = when {
            currentRoute == "welcome" -> 0.1f
            currentRoute == "search" -> 0.5f
            currentRoute.startsWith("personalization") -> 0.9f
            else -> 0.0f
        }, label = "progress"
    )

    Scaffold(
        topBar = {
            if (currentRoute != "welcome") {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "welcome",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("welcome") {
                WelcomeScreen(
                    onGetStarted = { navController.navigate("search") },
                    onLoginClicked = onNavigateToLogin // Parametreyi buraya bağladık
                )
            }

            composable("search") {
                OccupationSearchScreen(
                    onOccupationSelected = { occupationId, occupationGroup ->
                        navController.navigate("personalization/$occupationId/$occupationGroup")
                    }
                )
            }

            composable(
                route = "personalization/{occupationId}/{occupationGroup}",
                arguments = listOf(
                    navArgument("occupationId") { type = NavType.StringType },
                    navArgument("occupationGroup") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val occupationId = backStackEntry.arguments?.getString("occupationId") ?: ""
                val occupationGroup = backStackEntry.arguments?.getString("occupationGroup") ?: ""

                LevelSelectionScreen(
                    occupationId = occupationId,
                    occupationGroup = occupationGroup,
                    onOnboardingFinished = onOnboardingFinished
                )
            }
        }
    }
}