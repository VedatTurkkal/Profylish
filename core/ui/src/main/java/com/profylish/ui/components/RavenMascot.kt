package com.profylish.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.profylish.ui.R

enum class RavenState(@DrawableRes val iconRes: Int) {
    WELCOME(R.drawable.raven_welcome),
    ROADMAP(R.drawable.raven_roadmap_tree),
    SHOP(R.drawable.raven_shop),
    STATS(R.drawable.raven_stats_presentation),
    LEVEL_UP(R.drawable.raven_level_up),

    LISTENING(R.drawable.raven_listening),
    TYPING(R.drawable.raven_typing),
    TIMER(R.drawable.raven_timer),

    WORKING_CORRECT(R.drawable.raven_correct),
    CORRECT(R.drawable.raven_correct),

    CONFUSED(R.drawable.raven_thinking),
    THINKING(R.drawable.raven_thinking),

    MOTIVATION(R.drawable.raven_motivate),

    TEACHER(R.drawable.raven_teacher),

    QUEST_COMPLETE(R.drawable.raven_lesson_finish),
    LESSON_FINISH(R.drawable.raven_lesson_finish),

    GRADUATE(R.drawable.raven_achievement),

    ACHIEVEMENT(R.drawable.raven_achievement_flag),

    WINNER(R.drawable.raven_winner_book),
    PROMOTION(R.drawable.raven_promotion),

    PRACTICE(R.drawable.raven_shop)
}

@Composable
fun RavenMascot(
    state: RavenState,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = state.iconRes),
        contentDescription = "Mascot State: ${state.name}",
        modifier = modifier.size(200.dp)
    )
}