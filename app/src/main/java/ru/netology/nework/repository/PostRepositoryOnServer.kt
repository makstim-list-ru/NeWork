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
import retrofit2.Response
import ru.netology.nework.ui.retrofit.Attachment
import ru.netology.nework.ui.retrofit.AttachmentType
import ru.netology.nework.ui.retrofit.MediaUploadResponse
import ru.netology.nework.ui.retrofit.Post
import ru.netology.nework.ui.retrofit.PostsRetrofitSuspendInterface
import ru.netology.nework.ui.retrofit.User
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryOnServer @Inject constructor(
    private val postsRetrofitSuspendInterface: PostsRetrofitSuspendInterface,
) : PostRepositorySuspend {


    private val servStat = MutableLiveData(FeedModel())

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
        //TODO dao.removeByID(id)
        try {
            postsRetrofitSuspendInterface.removeById(id)
        } catch (e: Exception) {
            servStat.postValue(serverStatus(ServerStatus.ERROR))
            println("removeByID(id: Long)->PostsRetrofitSuspend.retrofitService.removeById(id) ERROR: $e")
        }
    }

    override suspend fun edit(post: Post, uploadFile: File?) {

        var responseUpload: MediaUploadResponse? = null

        if (uploadFile != null) try {
            responseUpload = upload(uploadFile) ?: let {
                println("save(post: Post, file: File)->FAULT upload file failure")
                return
            }
        } catch (e: Exception) {
            println("save(post: Post)->retrofitService.save(myPost) ERROR: $e")
            return
        }

        val post =
            post.copy(attachment = responseUpload?.let { Attachment(it.id, AttachmentType.IMAGE) })

//TODO        dao.edit(PostEntity.fromPostToEntity(post))

        try {
            postsRetrofitSuspendInterface.save(post)
        } catch (e: Exception) {
            servStat.postValue(serverStatus(ServerStatus.ERROR))
            println("edit(post: Post)->retrofitService.save(postWithAtt) ERROR: $e")
        }
    }

    override suspend fun save(post: Post, uploadFile: File?) {

        var responseUpload: MediaUploadResponse? = null
        val tempId: Long

        when {
            post.id > 0L -> {
                throw Exception("ERROR in save(post: Post, uploadFile: File?)")
            }

            post.id == 0L -> {
                if (uploadFile != null) try {
                    responseUpload = upload(uploadFile) ?: let {
                        println("save(post: Post, file: File)->FAULT upload file failure")
                        return
                    }
                } catch (e: Exception) {
                    println("save(post: Post)->retrofitService.save(myPost) ERROR: $e")
                    return
                }
                //TODO tempId = dao.getMinId()?.coerceAtMost(0)?.dec() ?: -1

            }

            else -> { // post.id<0L
                tempId = post.id
            }
        }

//        ++++++++++++++++++++++
//        tempId = if (post.id == 0L) dao.getMinId()?.coerceAtMost(0)?.dec() ?: -1 else post.id
//todo

//        val tempPost = post.copy(
//            id = tempId,
//            author = "Me",
//            authorAvatar = "sber.jpg",
//            attachment = responseUpload?.let { Attachment(it.id, AttachmentType.IMAGE) })
//
//        if (post.id == 0L) dao.save(    // если сохраняется свежий пост с присвоением нового (-)id в ЛБД
//            PostEntity.fromPostToEntity(tempPost)
//        )
//
//        try {
//            val serverPost = postsRetrofitSuspendInterface.save(tempPost.copy(id = 0L))
//            dao.save(PostEntity.fromPostToEntity(serverPost))
//            dao.removeByID(tempId)
//        } catch (e: Exception) {
//            servStat.postValue(serverStatus(ServerStatus.ERROR))
//            println("save(post: Post)->retrofitService.save(myPost) ERROR: $e")
//        }
    }

    override suspend fun likeByID(id: Long) {
        try {
            val post = postsRetrofitSuspendInterface.getById(id)
            if (post.likedByMe) postsRetrofitSuspendInterface.dislikeById(id)
            else postsRetrofitSuspendInterface.likeById(id)
            _dbFlag.value = DbFlagsList.REFRESH_REQUEST
            _dbFlag.value = DbFlagsList.NONE
        } catch (e: Exception) {
            //todo dao.likeByID(id)
//            servStat.postValue(serverStatus(ServerStatus.ERROR))
//            println("likeByID(id: Long)->PostsRetrofitSuspend.retrofitService.likeById(id) ERROR: $e")
            throw e
        }
    }

    override suspend fun loadNewer() {
        println("Button <loadNewer> pressed")
        flagLoad = true

        //todo

//        try {
//            val response = postsRetrofitSuspendInterface.getPostsNewer(
//                dao.getMaxId() ?: 0L
//            )
//            if (response.isSuccessful) {
//                servStat.postValue(serverStatus(ServerStatus.OK))
//                val posts = response.body()
//                if (!posts.isNullOrEmpty()) {
//                    dao.insert(posts.map { PostEntity.fromPostToEntity(it) })
//                } else {
//                    println("loadNewer()->!posts.isNullOrEmpty() FAULT: if-else")
//                }
//            } else {
//                servStat.postValue(serverStatus(ServerStatus.ERROR))
//                println("loadNewer()->response.isSuccessful ERROR: if-else")
//            }
//        } catch (e: Exception) {
//            servStat.postValue(serverStatus(ServerStatus.ERROR))
//            println("loadNewer()->PostsRetrofitSuspend.retrofitService.getPostsNewer ERROR: $e")
//        }

        flagLoad = false
    }

//    private fun getPostsNewer(): Flow<Int> = flow {
//        while (true) {
//            delay(10_000)
//            val response = postsRetrofitSuspendInterface.getPostsNewer(dao.getMaxId() ?: 0L)
//            if (response.isSuccessful) {
//                servStat.postValue(serverStatus(ServerStatus.OK))
//                val posts = response.body()
//                if (!posts.isNullOrEmpty()) {
//                    if (flagLoad) {
//                        println("flagLoad is ON")
//                    } else {
//                        emit(posts.size)
//                    }
//                } else emit(0)
//            } else {
//                servStat.postValue(serverStatus(ServerStatus.ERROR))
//                println("getPostsNewer()->response.isSuccessful ERROR: if-else")
//            }
//        }
//    }.catch {
//        servStat.postValue(serverStatus(ServerStatus.ERROR))
//        println("getPostsNewer()->catch ERROR: CATCH")
//    }


    override suspend fun upload(file: File): MediaUploadResponse? {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", file.name, file.asRequestBody()
            )

            val response = postsRetrofitSuspendInterface.upload(media)
            if (!response.isSuccessful) {
                println("upload->response.isSuccessful ERROR: if-else")
                return null
            }

            return response.body()
        } catch (e: Exception) {
            println("upload->CATCH ERROR: $e")
            return null
        }
    }


    private fun <T> retrofitErrorHandler(res: Response<T>): T? {
        if (res.isSuccessful) {
            servStat.postValue(serverStatus(ServerStatus.OK))
            return res.body()
        } else {
            servStat.postValue(serverStatus(ServerStatus.ERROR))
            println("retrofitErrorHandler(res: Response<T>)->res.isSuccessful ERROR: if-else")
        }
        return null
    }

    private fun serverStatus(status: ServerStatus): FeedModel {
        return when (status) {
            ServerStatus.LOADING -> FeedModel(loading = true)
            ServerStatus.ERROR -> FeedModel(error = true)
            ServerStatus.EMPTY -> FeedModel(empty = true)
            ServerStatus.REFRESHING -> FeedModel(refreshing = true)
            else -> FeedModel()
        }
    }

    private enum class ServerStatus {
        LOADING, ERROR, EMPTY, REFRESHING, OK
    }

    enum class DbFlagsList {
        REFRESH_REQUEST, ERROR_NETWORK, NONE
    }


}

