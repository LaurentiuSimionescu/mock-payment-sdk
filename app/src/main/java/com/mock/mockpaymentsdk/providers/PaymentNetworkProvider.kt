package com.mock.mockpaymentsdk.providers

import com.mock.mockpaymentsdk.BuildConfig
import com.mock.mockpaymentsdk.network.PaymentApi
import com.mock.mockpaymentsdk.network.interceptors.RequestAuthorizationInterceptor
import com.mock.mockpaymentsdk.network.interceptors.RequestRetryInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PaymentNetworkProvider {

    fun createPaymentApi(apiKey: String): PaymentApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(RequestRetryInterceptor(BuildConfig.REQUEST_MAX_RETRIES))
            .addInterceptor(RequestAuthorizationInterceptor(apiKey))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(PaymentApi::class.java)
    }
}