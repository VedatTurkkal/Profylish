package com.profylish.data.mapper

import com.profylish.model.curriculum.DictionaryWord
import com.profylish.network.model.dictionary.NetworkDictionaryEntry

fun NetworkDictionaryEntry.toDomain(): DictionaryWord {
    return DictionaryWord(
        id = this.id.toString(),
        word = this.word,
        definition = this.definition ?: "Definition unavailable", // Null check
        exampleSentence = this.example,
        type = this.type ?: "Term"
    )
}