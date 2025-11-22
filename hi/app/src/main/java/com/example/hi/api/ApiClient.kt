package com.example.hi.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit API Client
 * 
 * IMPORTANT: Replace BASE_URL with your actual backend URL
 * You need a backend server that creates Razorpay orders
 */
object ApiClient {
    
    // TODO: Replace with your backend base URL
    // Example: "https://your-backend.com/"
    private const val BASE_URL = "https://your-backend-url.com/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val razorpayApiService: RazorpayApiService = retrofit.create(RazorpayApiService::class.java)
}







