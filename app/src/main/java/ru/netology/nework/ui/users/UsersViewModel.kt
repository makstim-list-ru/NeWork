package ru.netology.nework.ui.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.repository.PostRepositorySuspend
import ru.netology.nework.ui.retrofit.User
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val repository: PostRepositorySuspend,
) : ViewModel() {

    val data: Flow<PagingData<User>> = repository.getUsersDataFlow().cachedIn(viewModelScope)
}