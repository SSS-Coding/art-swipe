package com.artswipe.di

import android.content.Context
import androidx.room.Room
import com.artswipe.data.local.ArtDatabase
import com.artswipe.data.local.ArtworkDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ArtDatabase {
        return Room.databaseBuilder(
            context,
            ArtDatabase::class.java,
            "art_swipe_db"
        ).build()
    }

    @Provides
    fun provideArtworkDao(database: ArtDatabase): ArtworkDao {
        return database.artworkDao()
    }
}
