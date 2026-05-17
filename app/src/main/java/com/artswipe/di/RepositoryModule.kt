package com.artswipe.di

import com.artswipe.data.repository.ArtworkRepositoryImpl
import com.artswipe.data.repository.AuthRepositoryImpl
import com.artswipe.domain.repository.ArtworkRepository
import com.artswipe.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindArtworkRepository(
        artworkRepositoryImpl: ArtworkRepositoryImpl
    ): ArtworkRepository
}
