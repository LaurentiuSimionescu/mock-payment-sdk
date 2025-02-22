package com.mock.mockpaymentsdk.repositories

import com.mock.mockpaymentsdk.models.PaymentRequest
import com.mock.mockpaymentsdk.models.PaymentResponse
import com.mock.mockpaymentsdk.network.PaymentApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class PaymentRepository(private val api: PaymentApi) {

    open fun processPayment(
        request: PaymentRequest,
        callback: (Result<PaymentResponse>) -> Unit
    ) {
        val call = api.payment(request)

        call.enqueue(object : Callback<PaymentResponse> {
            override fun onResponse(
                call: Call<PaymentResponse>,
                response: Response<PaymentResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        callback(Result.success(it))
                    } ?: callback(Result.failure(Exception("Empty response body")))
                } else {
                    callback(Result.failure(Exception("Error: ${response.errorBody()?.string()}")))
                }
            }

            override fun onFailure(call: Call<PaymentResponse>, t: Throwable) {
                callback(Result.failure(t))
            }
        })
    }
}