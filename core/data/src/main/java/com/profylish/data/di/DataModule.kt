package com.profylish.data.di

import com.profylish.data.repository.CurriculumRepositoryImpl
import com.profylish.data.repository.DictionaryRepositoryImpl // <-- Import this
import com.profylish.data.repository.OccupationRepositoryImpl
import com.profylish.data.repository.UserDataRepositoryImpl
import com.profylish.domain.repository.CurriculumRepository
import com.profylish.domain.repository.DictionaryRepository // <-- Import this
import com.profylish.domain.repository.OccupationRepository
import com.profylish.domain.repository.UserDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindOccupationRepository(
        occupationRepositoryImpl: OccupationRepositoryImpl
    ): OccupationRepository

    @Binds
    abstract fun bindUserDataRepository(
        userDataRepositoryImpl: UserDataRepositoryImpl
    ): UserDataRepository

    @Binds
    abstract fun bindCurriculumRepository(
        curriculumRepositoryImpl: CurriculumRepositoryImpl
    ): CurriculumRepository

    @Binds
    abstract fun bindDictionaryRepository(
        dictionaryRepositoryImpl: DictionaryRepositoryImpl
    ): DictionaryRepository
}