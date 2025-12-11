package com.profylish.domain.usecase.occupation

data class Occupation(
    val id: String,
    val title: String,
    val description: String,
    val iconUrl: String? = null,
    val category: String? = null,
    val learnerCount: Int = 0,
    val difficultyLevel: String = "Beginner"
)
