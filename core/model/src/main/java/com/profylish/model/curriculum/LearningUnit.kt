package com.profylish.model.curriculum

data class LearningUnit(
    val id: String,
    val title: String,       // Örn: "Unit 1: Basic Terms"
    val description: String?, // Örn: "Introduction to software concepts"
    val order: Int,          // 1, 2, 3...
    val occupationGroup: String // Hangi meslek grubuna ait olduğu
)