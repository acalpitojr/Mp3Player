package com.calpito.mediaplayer.di

import android.content.Context
import com.calpito.mediaplayer.MusicPlayerImpl
import com.calpito.mediaplayer.interfaces.MusicPlayerInterface
import com.calpito.mediaplayer.interfaces.RepositoryInterface
import com.calpito.mediaplayer.repository.RepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideApplicationContext(
        @ApplicationContext appContext: Context
    ): Context = appContext

    @Provides
    @Singleton
    fun provideMusicPlayer(musicPlayer: MusicPlayerImpl):MusicPlayerInterface{
        return musicPlayer
    }

    @Provides
    @Singleton
    fun provideRepository(repository: RepositoryImpl): RepositoryInterface {
        return repository
    }

}