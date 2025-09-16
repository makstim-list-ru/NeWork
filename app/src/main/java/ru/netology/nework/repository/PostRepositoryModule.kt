package ru.netology.nework.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module

@Suppress("UNUSED_PARAMETER")
interface PostRepositoryModule {
    @Binds
    fun bindsPostRepositorySuspend(postRepositoryOnServer: PostRepositoryOnServer): PostRepositorySuspend
}