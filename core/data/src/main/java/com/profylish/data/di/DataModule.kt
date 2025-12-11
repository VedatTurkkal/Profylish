package com.profylish.data.di

import com.profylish.data.repository.OccupationRepositoryImpl
import com.profylish.domain.repository.OccupationRepository
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
}