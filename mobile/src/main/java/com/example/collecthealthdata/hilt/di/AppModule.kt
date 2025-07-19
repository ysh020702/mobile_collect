package com.example.collecthealthdata.hilt.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.collecthealthdata.data.local.AppDatabase
import com.example.collecthealthdata.data.local.TrackedDataDao
import com.example.collecthealthdata.data.remote.firebase.FirebaseRepositoryImpl
import com.example.collecthealthdata.domain.repository.FirebaseRepository
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase{
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "tracked_data_db"
        ).build()
    }

    @Provides
    fun provideTrackedDataDao(appDatabase: AppDatabase): TrackedDataDao {
        return appDatabase.trackedDataDao()
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): DatabaseReference {
        return FirebaseDatabase.getInstance().reference
    }

    @Provides
    @Singleton
    fun provideFirebaseRepository(
        database: DatabaseReference,
        @Named("user_prefs") userPrefs: SharedPreferences
    ): FirebaseRepository {
        return FirebaseRepositoryImpl(database, userPrefs)
    }
}