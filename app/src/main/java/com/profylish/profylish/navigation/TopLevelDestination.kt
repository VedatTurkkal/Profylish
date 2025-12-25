package com.profylish.profylish.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    HOME("dashboard", Icons.Default.Home, "Home"),
    LEADERBOARD("leaderboard", Icons.Default.Leaderboard, "League"),
    SHOP("shop", Icons.Default.ShoppingBag, "Shop"),
    PROFILE("profile", Icons.Default.Person, "Profile")
}