package com.profylish.model.curriculum

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DictionaryWord(
    val id: Int,

    val word: String,

    val type: String? = null,

    @SerialName("english_definition")
    val englishDefinition: String? = null,

    @SerialName("english_example")
    val englishExample: String? = null,

    @SerialName("source_profession")
    val sourceProfession: String? = null,

    @SerialName("source_cefr_level")
    val cefrLevel: String? = null
)