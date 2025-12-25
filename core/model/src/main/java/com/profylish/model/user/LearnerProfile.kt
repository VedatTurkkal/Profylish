package com.profylish.model.user

data class LearnerProfile(
    val uid: String = "",
    val name: String = "Guest",
    val currentProfession: String = "Profession Not Selected",
    val gems: Int = 0,
    val streak: Int = 0,
    val hearts: Int = 5
)