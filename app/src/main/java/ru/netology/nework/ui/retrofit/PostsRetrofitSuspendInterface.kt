package ru.netology.nework.ui.retrofit

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import ru.netology.nework.ui.auth.AuthUploadResponse


interface PostsRetrofitSuspendInterface {
    @GET("posts")
    suspend fun getAll(): Response<List<Post>>

    @GET("posts/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<Post>>

    @GET("users")
    suspend fun getUsers(): Response<List<User>>

    @GET("posts/{id}")
    suspend fun getById(@Path("id") id: Long): Post

    @GET("posts/{id}/before")
    suspend fun getBefore(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/after")
    suspend fun getAfter(@Path("id") id: Long, @Query("count") count: Int): Response<List<Post>>

    @POST("posts")
    suspend fun save(@Body post: Post): Post

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long)

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Post

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Post

    @GET("posts/{id}/newer")
    suspend fun getPostsNewer(@Path("id") id: Long): Response<List<Post>>

    @Multipart
    @POST("media")
    suspend fun upload(@Part media: MultipartBody.Part): Response<MediaUploadResponse>

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun updateUser(
        @Field("login") login: String, @Field("pass") pass: String
    ): Response<AuthUploadResponse>

    @Multipart
    @POST("users/registration")
    suspend fun registerUser(
        @Part("login") login: RequestBody,
        @Part("pass") pass: RequestBody,
        @Part("name") name: RequestBody,
        @Part media: MultipartBody.Part?
    ): Response<AuthUploadResponse>
}

//object PostsRetrofitSuspend {
//    private val logging = HttpLoggingInterceptor().apply {
//        level = HttpLoggingInterceptor.Level.BODY
//    }
//
//    private val okhttp = OkHttpClient.Builder()
//        .addInterceptor(logging)
//        .addInterceptor { chain ->
//            val newRequest =
//                AppAuthorization.getInstance().authStateFlow.value.token?.let { token ->
//                    chain.request().newBuilder()
//                        .addHeader("Authorization", token)
//                        .build()
//                } ?: chain.request()
//            chain.proceed(newRequest)
//        }
//        .build()
//
//    private const val BASE_URL = "http://10.0.2.2:9999/api/slow/"
//
//    val retrofitService: PostsRetrofitSuspendInterface by lazy {
//        Retrofit.Builder()
//            .addConverterFactory(GsonConverterFactory.create())
//            .baseUrl(BASE_URL)
//            .client(okhttp)
//            .build()
//            .create(PostsRetrofitSuspendInterface::class.java)
//    }
//}