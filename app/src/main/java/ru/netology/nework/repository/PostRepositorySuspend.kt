package ru.netology.nework.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.repository.PostRepositoryOnServer.DbFlagsList
import ru.netology.nework.ui.retrofit.MediaUploadResponse
import ru.netology.nework.ui.retrofit.Post
import ru.netology.nework.ui.retrofit.User
import java.io.File

interface PostRepositorySuspend {
    val dbFlag: LiveData<DbFlagsList>
    suspend fun likeByID(id: Long)
    suspend fun shareByID(id: Long)
    suspend fun removeByID(id: Long)
    suspend fun saveNewOrOld(post: Post, uploadFile: File? = null)
    suspend fun uploadMedia(file: File): MediaUploadResponse?
    fun getData(): LiveData<PagingData<Post>>
    fun getDataFlow(): Flow<PagingData<Post>>
    fun getUsersDataFlow(): Flow<PagingData<User>>

}