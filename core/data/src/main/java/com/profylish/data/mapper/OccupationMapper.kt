package com.profylish.data.mapper

import com.profylish.model.occupation.Occupation
import com.profylish.model.occupation.OccupationCode
import com.profylish.model.occupation.OccupationSource
import com.profylish.network.model.NetworkOccupation
import kotlin.random.Random


fun NetworkOccupation.toDomainModel(): Occupation {
    val fakeLearnerCount = Random.nextInt(1200, 50000)

    val categories = listOf("Technology", "Arts", "Healthcare", "Business", "Science", "Engineering")
    val randomCategory = categories.random()

    val generatedDescription = "Master the skills needed to become a world-class $jobTitle."

    return Occupation(
        id = this.socCode ?: this.jobTitle,
        title = this.jobTitle,
        description = generatedDescription,
        iconUrl = null,
        category = randomCategory,
        learnerCount = fakeLearnerCount,
        difficultyLevel = "Beginner",

        codes = OccupationCode(
            soc = this.socCode,
            isco08 = null,
            onetGroup = this.onetGroup
        ),
        source = OccupationSource.fromString(this.source)
    )
}