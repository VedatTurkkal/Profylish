package com.profylish.domain.repository

import com.profylish.model.curriculum.DictionaryWord

interface DictionaryRepository {
    suspend fun getWordsForLevel(profession: String, cefrLevel: String): List<DictionaryWord>
}