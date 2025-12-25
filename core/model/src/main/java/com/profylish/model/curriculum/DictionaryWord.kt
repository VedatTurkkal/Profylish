package com.profylish.model.curriculum

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DictionaryWord(
    val id: String,

    val word: String,

    @SerialName("english_definition")
    val definition: String,

    @SerialName("english_example")
    val exampleSentence: String?,

    val type: String
)