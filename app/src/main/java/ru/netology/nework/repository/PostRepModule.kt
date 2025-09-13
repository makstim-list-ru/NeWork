package ru.netology.kotlin_for_android_hw_1.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nework.repository.PostRepositorySuspend

@InstallIn(SingletonComponent::class)
@Module
interface PostRepModule {

    @Binds
    fun bindsPostRepositorySuspend(postRepositoryInServerAndSQL: PostRepositoryInServerAndSQL): PostRepositorySuspend
}