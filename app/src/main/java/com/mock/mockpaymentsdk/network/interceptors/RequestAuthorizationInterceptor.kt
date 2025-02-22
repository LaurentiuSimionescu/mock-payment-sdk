package com.mock.mockpaymentsdk.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response

class RequestAuthorizationInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val newRequest = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        return chain.proceed(newRequest)
    }
}