package com.example.collecthealthdata.hilt.di

import com.example.collecthealthdata.domain.repository.TrackedDataRepository
import com.example.collecthealthdata.data.repositoryImpl.FirebaseTrackedDataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {
    @Provides
    fun provideTrackedDataRepository(): TrackedDataRepository{
        return FirebaseTrackedDataRepository()
    }
}