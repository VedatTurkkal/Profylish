package com.profylish.network.model.dictionary

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkDictionaryEntry(
    val id: Int,
    val word: String,
    val type: String?, // Term, Idiom, Phrasal Verb...
    @SerialName("english_definition") val definition: String?,
    @SerialName("english_example") val example: String?,
    @SerialName("source_profession") val sourceProfession: String?,
    @SerialName("source_cefr_level") val cefrLevel: String? // B1, B2...
)