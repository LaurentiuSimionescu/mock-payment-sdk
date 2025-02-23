package com.mock.mockpaymentsdk.network

import com.mock.mockpaymentsdk.models.PaymentRequest
import com.mock.mockpaymentsdk.models.PaymentResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

internal interface PaymentApi {
    @POST("payment")
    suspend fun payment(@Body request: PaymentRequest): Response<PaymentResponse>
}