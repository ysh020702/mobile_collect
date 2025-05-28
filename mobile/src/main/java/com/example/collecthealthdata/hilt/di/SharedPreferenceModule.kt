package com.example.collecthealthdata.hilt.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@InstallIn(SingletonComponent::class)
@Module
object SharedPreferencesModule {

    // user_prefs 제공
    @Provides
    @Named("user_prefs")
    fun provideUserSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    }

    // login_prefs 제공
    @Provides
    @Named("login_prefs")
    fun provideLoginSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    }
}
