package ru.netology.nework.ui.posts

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.repository.PostRepositorySuspend
import ru.netology.nework.ui.auth.AuthApp
import ru.netology.nework.ui.retrofit.PhotoModel
import ru.netology.nework.ui.retrofit.Post
import ru.netology.nework.ui.retrofit.postByDefault
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PostsViewModel @Inject constructor(
    private val repository: PostRepositorySuspend,
    authApp: AuthApp
) : ViewModel() {

    val dbFlag = repository.dbFlag
    private val cached: Flow<PagingData<Post>> = repository.getDataFlow().cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<Post>> = authApp
        .authStateFlow.flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.map { post ->
                    post.copy(ownedByMe = (post.authorId == myId))
                }
            }
        }.flowOn(Dispatchers.Default)


    private val editedPostTmp = MutableLiveData(postByDefault)

    private val _photoLive = MutableLiveData<PhotoModel?>(null)
    val photoLive: LiveData<PhotoModel?>
        get() = _photoLive

    fun likeVM(id: Long) {
        viewModelScope.launch { repository.likeByID(id) }
    }

    fun shareVM(id: Long) {
        viewModelScope.launch { repository.shareByID(id) }
    }

    fun removeVM(id: Long) {
        viewModelScope.launch { repository.removeByID(id) }
    }

    fun saveVM(content: String) {

        val editedPost = requireNotNull(editedPostTmp.value)

        viewModelScope.launch {
            repository.saveNewOrOld(
                editedPost.copy(content = content),
                photoLive.value?.file
            )
        }

        cancelEditVM()
    }

    fun editVM(post: Post) {
        editedPostTmp.value = post
    }

    fun cancelEditVM() {
        editedPostTmp.value = postByDefault
        removePhotoVM()
    }

    fun loadAllPostsVM() {
//        viewModelScope.launch { repository.getPostsAllAsync() }
    }

    fun loadNewerVM() {
        viewModelScope.launch { repository.loadNewer() }
    }

    fun changePhotoVM(uri: Uri?, file: File?) {
        _photoLive.value = PhotoModel(uri, file)
    }

    fun removePhotoVM() {
        _photoLive.value = null
    }

}