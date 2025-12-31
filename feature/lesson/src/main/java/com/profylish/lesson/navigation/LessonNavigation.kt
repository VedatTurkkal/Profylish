package com.profylish.lesson.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.profylish.lesson.quiz.QuizScreen

// Route sabitleri
const val lessonRoute = "lesson_route"
const val levelIdArg = "levelId"
const val professionArg = "profession"
const val quizCategoryArg = "quizCategory" // YENİ: Kategori argümanı (isProgression yerine)

// Navigasyon Fonksiyonu: Boolean yerine String (category) alıyor
fun NavController.navigateToLesson(levelId: String, profession: String, quizCategory: String) {
    this.navigate("$lessonRoute/$levelId/$profession/$quizCategory")
}

// Nav Graph Builder
fun NavGraphBuilder.lessonScreen(
    onBackClick: () -> Unit,
    onNavigateToAuth: () -> Unit
) {
    composable(
        route = "$lessonRoute/{$levelIdArg}/{$professionArg}/{$quizCategoryArg}",
        arguments = listOf(
            navArgument(levelIdArg) { type = NavType.StringType },
            navArgument(professionArg) { type = NavType.StringType },

            // YENİ: String tipinde kategori argümanı (TERM, IDIOM, vb.)
            navArgument(quizCategoryArg) { type = NavType.StringType }
        )
    ) {
        // QuizScreen içindeki ViewModel, SavedStateHandle üzerinden bu argümanları otomatik okur.
        QuizScreen(
            onBackClick = onBackClick,
            onNavigateToAuth = onNavigateToAuth
        )
    }
}