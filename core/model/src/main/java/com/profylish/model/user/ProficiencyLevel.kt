package com.profylish.model.user

enum class ProficiencyLevel(
    val title: String,
    val description: String,
    val needsAssessment: Boolean,
    val targetCefrLevel: String
) {
    BEGINNER(
        title = "Starting from Scratch",
        description = "I know general English, but no professional terms.",
        needsAssessment = false,
        targetCefrLevel = "B1"
    ),
    INTERMEDIATE(
        title = "Some Knowledge",
        description = "I know basic professional terminology.",
        needsAssessment = true,
        targetCefrLevel = "B2"
    ),
    ADVANCED(
        title = "Experienced",
        description = "I communicate fluently in my profession.",
        needsAssessment = true,
        targetCefrLevel = "C1"
    )
}