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

fun NavController.navigateToLesson(levelId: String, profession: String) {
    this.navigate("$lessonRoute/$levelId/$profession")
}

fun NavGraphBuilder.lessonScreen(
    onBackClick: () -> Unit,
    onNavigateToAuth: () -> Unit // <-- ADDED THIS
) {
    composable(
        route = "$lessonRoute/{$levelIdArg}/{$professionArg}",
        arguments = listOf(
            navArgument(levelIdArg) { type = NavType.StringType },
            navArgument(professionArg) { type = NavType.StringType }
        )
    ) {
        QuizScreen(
            onBackClick = onBackClick,
            onNavigateToAuth = onNavigateToAuth // <-- PASSED HERE
        )
    }
}