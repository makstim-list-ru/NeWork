package ru.netology.nework.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.ui.retrofit.Attachment
import ru.netology.nework.ui.retrofit.AttachmentType
import ru.netology.nework.ui.retrofit.MediaUploadResponse
import ru.netology.nework.ui.retrofit.Post
import ru.netology.nework.ui.retrofit.PostsRetrofitSuspendInterface
import ru.netology.nework.ui.retrofit.User
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryOnServer @Inject constructor(
    private val postsRetrofitSuspendInterface: PostsRetrofitSuspendInterface,
) : PostRepositorySuspend {

    @OptIn(ExperimentalPagingApi::class)
    private val dataFlow: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = true),
        pagingSourceFactory = { PostPagingSource(postsRetrofitSuspendInterface) },
    ).flow

    private val usersDataFlow: Flow<PagingData<User>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = true),
        pagingSourceFactory = { UserPagingSource(postsRetrofitSuspendInterface) },
    ).flow

    private val _dbFlag = MutableLiveData<DbFlagsList>(DbFlagsList.NONE)
    override val dbFlag: LiveData<DbFlagsList>
        get() = _dbFlag


    private val dataLive: LiveData<PagingData<Post>> = dataFlow.asLiveData(Dispatchers.Default)

    @Volatile
    private var flagLoad = false

    override fun getData(): LiveData<PagingData<Post>> = dataLive

    override fun getDataFlow(): Flow<PagingData<Post>> = dataFlow
    override fun getUsersDataFlow(): Flow<PagingData<User>> = usersDataFlow

    override suspend fun shareByID(id: Long) = Unit //Nothing to do in this release

    override suspend fun removeByID(id: Long) {

        try {
            postsRetrofitSuspendInterface.removeById(id)
        } catch (_: Exception) {
            Timber.i("INFO error on network - removeByID")
            _dbFlag.value = DbFlagsList.ERROR_NETWORK
            _dbFlag.value = DbFlagsList.NONE
        }
    }

    override suspend fun saveNewOrOld(post: Post, uploadFile: File?) {

        var responseUpload: MediaUploadResponse? = null

        if (uploadFile != null)
            try {
                responseUpload = uploadMedia(uploadFile) ?: let {
                    Timber.i("INFO save(post: Post, file: File)->FAULT upload file failure")
                    return
                }
            } catch (e: Exception) {
                Timber.i("INFO save(post: Post)->retrofitService.save(myPost) ERROR => $e")
                return
            }

        val post =
            post.copy(attachment = responseUpload?.let { Attachment(it.url, AttachmentType.IMAGE) })

        try {
            postsRetrofitSuspendInterface.save(post)
            _dbFlag.value = DbFlagsList.REFRESH_REQUEST
            _dbFlag.value = DbFlagsList.NONE
        } catch (_: Exception) {
            Timber.i("INFO error on network - saveNewOrOld")
            _dbFlag.value = DbFlagsList.ERROR_NETWORK
            _dbFlag.value = DbFlagsList.NONE
        }
    }

    override suspend fun likeByID(id: Long) {
        try {
            val post = postsRetrofitSuspendInterface.getById(id)
            if (post.likedByMe) postsRetrofitSuspendInterface.dislikeById(id)
            else postsRetrofitSuspendInterface.likeById(id)
            _dbFlag.value = DbFlagsList.REFRESH_REQUEST
            _dbFlag.value = DbFlagsList.NONE
        } catch (_: Exception) {
            Timber.i("INFO error on network - likeByID")
            _dbFlag.value = DbFlagsList.ERROR_NETWORK
            _dbFlag.value = DbFlagsList.NONE
        }
    }

    override suspend fun uploadMedia(file: File): MediaUploadResponse? {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", file.name, file.asRequestBody()
            )

            val response = postsRetrofitSuspendInterface.upload(media)
            if (!response.isSuccessful) {
                Timber.i("INFO upload->response.isSuccessful ERROR: if-else")
                return null
            }

            return response.body()
        } catch (e: Exception) {
            Timber.i("INFO upload->CATCH ERROR => $e")
            return null
        }
    }

    enum class DbFlagsList {
        REFRESH_REQUEST, ERROR_NETWORK, NONE
    }

}

