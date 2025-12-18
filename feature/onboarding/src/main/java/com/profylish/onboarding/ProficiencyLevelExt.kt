package com.profylish.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.graphics.vector.ImageVector
import com.profylish.model.user.ProficiencyLevel

val ProficiencyLevel.icon: ImageVector
    get() = when (this) {
        ProficiencyLevel.BEGINNER -> Icons.Outlined.Info
        ProficiencyLevel.INTERMEDIATE -> Icons.Outlined.Build
        ProficiencyLevel.ADVANCED -> Icons.Filled.Star
    }