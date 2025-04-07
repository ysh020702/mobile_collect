package com.example.collecthealthdata.di

import android.content.Context
import com.example.collecthealthdata.data.repository.*
import com.example.collecthealthdata.domain.repository.*
import com.example.collecthealthdata.domain.usecase.*
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    fun provideInsertTrackedDataUseCase(
        repository: TrackedDataRepository
    ): InsertTrackedDataUseCase {
        return InsertTrackedDataUseCase(repository)
    }

    @Provides
    fun provideGetTrackedDataUseCase(
        repository: TrackedDataRepository
    ): GetTrackedDataUseCase {
        return GetTrackedDataUseCase(repository)
    }

    @Provides
    fun provideDeleteAllTrackedDataUseCase(
        repository: TrackedDataRepository
    ): DeleteAllTrackedDataUseCase {
        return DeleteAllTrackedDataUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideApplicationCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    @Provides
    @Singleton
    fun provideCapabilityClient(@ApplicationContext context: Context): CapabilityClient {
        return Wearable.getCapabilityClient(context)
    }

    @Provides
    @Singleton
    fun provideMessageClient(@ApplicationContext context: Context): MessageClient {
        return Wearable.getMessageClient(context)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Provides
    @Singleton
    fun provideTrackingRepository(
        coroutineScope: CoroutineScope,
        healthTrackingServiceConnection: HealthTrackingServiceConnection,
        @ApplicationContext context: Context
    ): TrackingRepository {
        return TrackingRepositoryImpl(coroutineScope, healthTrackingServiceConnection, context)
    }

    @Provides
    @Singleton
    fun provideMessageRepository(messageClient: MessageClient): MessageRepository {
        return MessageRepositoryImpl(messageClient)
    }

    @Provides
    @Singleton
    fun provideCapabilitiesRepository(capabilityClient: CapabilityClient): CapabilityRepository {
        return CapabilityRepositoryImpl(capabilityClient)
    }
}


/*
폴더명: DI = Dependency Injection!!
usecase 에서 repository 의 어떤 함수를 사용하는지 정의한다
DI 없으면 repository 의 어떤 함수인지를 하나씩 다 적어야 한다!
그치만 안 써도 된다 ㅋㅋ

Hilt가 ViewModel에 UseCase를 자동 주입할 수 있도록
AppModule이라는 @Module 객체를 만들고
@Provides 함수로 각 UseCase를 등록한다!



1. ViewModel이 특정 UseCase를 쓰려면:
@HiltViewModel
class TrackedDataViewModel @Inject constructor(
    private val getUseCase: GetTrackedDataUseCase,
    private val saveUseCase: SaveTrackedDataUseCase
) : ViewModel() {
    // UseCases 사용 가능!
}
이렇게 주입하려면, Hilt가 어떻게 만들 건지 알아야 함.

2. 그래서 @Module과 @Provides로 알려줘야 함:
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideGetTrackedDataUseCase(
        repository: TrackedDataRepository
    ): GetTrackedDataUseCase {
        return GetTrackedDataUseCase(repository)
    }
}
이렇게 하면 ViewModel 생성 시
→ Hilt가 알아서 GetTrackedDataUseCase를 넣어줌!

UseCase를 쓸 때, Provide 안 쓰면
  viewModelScope.launch {
            trackedDataUseCase.insert(data)
        }
 .invoke() 에서 repository.<함수 이름>을 어떤 걸 쓰는지 다 적어줘야 함
 그렇지만, provide를 쓰면, 알아서 넣어줌!
 */