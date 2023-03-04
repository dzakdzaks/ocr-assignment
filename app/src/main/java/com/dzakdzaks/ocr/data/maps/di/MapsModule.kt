package com.dzakdzaks.ocr.data.maps.di

import com.dzakdzaks.ocr.core.dispatchers.IoDispatcher
import com.dzakdzaks.ocr.data.maps.api.repository.MapsRepository
import com.dzakdzaks.ocr.data.maps.impl.remote.api.MapsApi
import com.dzakdzaks.ocr.data.maps.impl.repository.MapsRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
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
    fun providesFireStore(): FirebaseFirestore {
        val settings = firestoreSettings {
            isPersistenceEnabled = true
            cacheSizeBytes = FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED
        }
        return Firebase.firestore.apply {
            firestoreSettings = settings
        }
    }

    @Provides
    @Singleton
    fun providesDataRepository(
        mapsApi: MapsApi,
        fireStore: FirebaseFirestore,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): MapsRepository = MapsRepositoryImpl(mapsApi, fireStore, ioDispatcher)

}
