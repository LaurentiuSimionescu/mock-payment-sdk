package com.mock.mockpaymentsdk.network

import com.mock.mockpaymentsdk.models.PaymentRequest
import com.mock.mockpaymentsdk.models.PaymentResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call

interface PaymentApi {
    @POST("payment")
    fun payment(
        @Body request: PaymentRequest
    ): Call<PaymentResponse>
}