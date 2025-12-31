package com.profylish.lesson.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.profylish.lesson.quiz.QuizScreen

const val lessonRoute = "lesson_route"
const val levelIdArg = "levelId"
const val professionArg = "profession"
const val isProgressionArg = "isProgression"

fun NavController.navigateToLesson(levelId: String, profession: String, isProgression: Boolean) {
    this.navigate("$lessonRoute/$levelId/$profession/$isProgression")
}

fun NavGraphBuilder.lessonScreen(
    onBackClick: () -> Unit,
    onNavigateToAuth: () -> Unit
) {
    composable(
        route = "$lessonRoute/{$levelIdArg}/{$professionArg}/{$isProgressionArg}",
        arguments = listOf(
            navArgument(levelIdArg) { type = NavType.StringType },
            navArgument(professionArg) { type = NavType.StringType },

            // YENİ: Boolean tipinde argüman
            navArgument(isProgressionArg) { type = NavType.BoolType }
        )
    ) {
        QuizScreen(
            onBackClick = onBackClick,
            onNavigateToAuth = onNavigateToAuth
        )
    }
}