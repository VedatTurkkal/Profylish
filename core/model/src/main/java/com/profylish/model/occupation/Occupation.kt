package com.profylish.model.occupation

data class Occupation(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val iconUrl: String? = null,
    val learnerCount: Int = 0,
    val difficultyLevel: String = "Beginner",

    val codes: OccupationCode? = null,

    val source: OccupationSource = OccupationSource.UNKNOWN
)