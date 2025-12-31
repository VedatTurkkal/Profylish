package com.profylish.domain.repository

import com.profylish.model.curriculum.DictionaryWord

interface DictionaryRepository {
    suspend fun getWordsForLevel(
        profession: String,
        cefrLevel: String,
        excludedWordIds: List<Int> = emptyList()
    ): List<DictionaryWord>

    suspend fun getWordsForLesson(
        profession: String,
        cefrLevel: String,
        wordType: String
    ): List<DictionaryWord>

    suspend fun getLearnedWordIds(userId: String): List<Int>

    suspend fun markWordsAsLearned(userId: String, wordIds: List<Int>)
}