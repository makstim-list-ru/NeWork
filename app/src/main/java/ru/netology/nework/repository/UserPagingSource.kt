package ru.netology.nework.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import okio.IOException
import retrofit2.HttpException
import ru.netology.nework.ui.retrofit.PostsRetrofitSuspendInterface
import ru.netology.nework.ui.retrofit.User

class UserPagingSource(
    private val postsRetrofitSuspendInterface: PostsRetrofitSuspendInterface
) : PagingSource<Long, User>() {
    override fun getRefreshKey(state: PagingState<Long, User>): Long? = null

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, User> {
        try {
            val response = when (params) {
                is LoadParams.Append<*> -> return LoadResult.Page(
                    emptyList(), params.key, null
                )

                is LoadParams.Prepend<*> -> return LoadResult.Page(
                    emptyList(), params.key, null
                )

                is LoadParams.Refresh<*> -> {
                    postsRetrofitSuspendInterface.getUsers()
                }
            }
            if (!response.isSuccessful) {
                throw HttpException(response)
            }

            if (response.body().isNullOrEmpty())
                if (response.body() == null)
                    println("ERR --------------- postList = response.body() == NULL")
                else
                    println("ERR --------------- postList = response.body() == EMPTY")

            val userList = response.body().orEmpty()

            return LoadResult.Page(
                data = userList,
                prevKey = params.key,
                nextKey = userList.lastOrNull()?.id
            )
        } catch (e: IOException) {
            return LoadResult.Error(e)
        }
    }
}