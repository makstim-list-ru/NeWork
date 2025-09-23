package ru.netology.nework.ui.retrofit

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.netology.nework.api.ApiKey
import ru.netology.nework.ui.auth.AuthApp
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class PostRetrofitModule {

    companion object {
        private const val BASE_URL = "http://94.228.125.136:8080/api/"
    }

    @Provides
    @Singleton
    fun provideLogging(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        logging: HttpLoggingInterceptor,
        authApp: AuthApp,
        apiKey: ApiKey,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val builder = chain.request().newBuilder()
                .addHeader("Api-Key", apiKey.value)
            val token = authApp.authStateFlow.value.token
            val newRequest = (token?.let { builder.addHeader("Authorization", token) } ?: builder)
                .build()
            chain.proceed(newRequest)
        }
        .build()

    @Singleton
    @Provides
    fun provideRetrofitService(
        okHttpClient: OkHttpClient
    ): PostsRetrofitSuspendInterface =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()
            .create(PostsRetrofitSuspendInterface::class.java)


}