package com.example.myapp.core.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Api {
    private const val BASE_URL = "http://10.0.2.2:3000/"

    class TokenInterceptor : Interceptor {
        var token: String? = null

        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val builder = original.newBuilder()
            if (token != null) {
                builder.addHeader("Authorization", "Bearer $token")
            }
            val request = builder.build()
            return chain.proceed(request)
        }
    }

    val tokenInterceptor = TokenInterceptor()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(tokenInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

