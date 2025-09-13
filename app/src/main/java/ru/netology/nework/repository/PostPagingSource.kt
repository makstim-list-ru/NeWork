package ru.netology.nework.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import okio.IOException
import retrofit2.HttpException
import ru.netology.nework.ui.retrofit.Post
import ru.netology.nework.ui.retrofit.PostsRetrofitSuspendInterface

class PostPagingSource(
    private val postsRetrofitSuspendInterface: PostsRetrofitSuspendInterface
) : PagingSource<Long, Post>() {
    override fun getRefreshKey(state: PagingState<Long, Post>): Long? = null

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Post> {
        try {
            val response = when (params) {
                is LoadParams.Append<*> -> {
                    postsRetrofitSuspendInterface.getBefore(params.key, params.loadSize)
                }

                is LoadParams.Prepend<*> -> return LoadResult.Page(
                    emptyList(), params.key, null
                )

                is LoadParams.Refresh<*> -> {
                    postsRetrofitSuspendInterface.getLatest(params.loadSize)
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

            val postList = response.body().orEmpty()

            return LoadResult.Page(
                data = postList,
                prevKey = params.key,
                nextKey = postList.lastOrNull()?.id
            )
        } catch (e: IOException) {
            return LoadResult.Error(e)
        }
    }
}