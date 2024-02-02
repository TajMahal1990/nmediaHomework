package ru.netology.nmedia.api

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object FirebaseMessagingModule {

    @Provides
    @Singleton
    fun provideFM():FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }

//    @Provides
//    @Singleton
//    fun provideFirebaseApp():FirebaseApp {
//        return FirebaseApp.getInstance()
//    }
}