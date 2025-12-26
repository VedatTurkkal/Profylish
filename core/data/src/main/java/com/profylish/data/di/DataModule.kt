package com.profylish.data.di

import com.profylish.data.repository.AuthRepositoryImpl
import com.profylish.data.repository.CurriculumRepositoryImpl
import com.profylish.data.repository.DictionaryRepositoryImpl
import com.profylish.data.repository.OccupationRepositoryImpl // <-- EKLENDİ
import com.profylish.data.repository.UserDataRepositoryImpl
import com.profylish.domain.repository.AuthRepository
import com.profylish.domain.repository.CurriculumRepository
import com.profylish.domain.repository.DictionaryRepository
import com.profylish.domain.repository.OccupationRepository // <-- EKLENDİ
import com.profylish.domain.repository.UserDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserDataRepository(
        userDataRepositoryImpl: UserDataRepositoryImpl
    ): UserDataRepository

    @Binds
    @Singleton
    abstract fun bindCurriculumRepository(
        curriculumRepositoryImpl: CurriculumRepositoryImpl
    ): CurriculumRepository

    @Binds
    @Singleton
    abstract fun bindDictionaryRepository(
        dictionaryRepositoryImpl: DictionaryRepositoryImpl
    ): DictionaryRepository

    // --- İŞTE BU KISIM EKSİK OLDUĞU İÇİN HATA ALIYORSUNUZ ---
    @Binds
    @Singleton
    abstract fun bindOccupationRepository(
        occupationRepositoryImpl: OccupationRepositoryImpl
    ): OccupationRepository
}