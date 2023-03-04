package com.dzakdzaks.ocr.data.maps.di

import com.dzakdzaks.ocr.core.dispatchers.IoDispatcher
import com.dzakdzaks.ocr.data.maps.api.repository.MapsRepository
import com.dzakdzaks.ocr.data.maps.impl.remote.api.MapsApi
import com.dzakdzaks.ocr.data.maps.impl.repository.MapsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object MapsModule {

    @Provides
    @Singleton
    fun providesDataApi(
        retrofit: Retrofit,
    ): MapsApi = retrofit.create(MapsApi::class.java)

    @Provides
    @Singleton
    fun providesDataRepository(
        mapsApi: MapsApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): MapsRepository = MapsRepositoryImpl(mapsApi, ioDispatcher)
}
