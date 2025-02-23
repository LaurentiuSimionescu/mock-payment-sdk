package com.mock.mockpaymentsdk

import com.mock.mockpaymentsdk.models.PaymentRequest
import com.mock.mockpaymentsdk.models.PaymentResponse
import com.mock.mockpaymentsdk.network.PaymentApi
import kotlinx.coroutines.delay
import retrofit2.Response

class FakePaymentApi : PaymentApi {
    private var shouldFail = false
    private var responseDelayMillis: Long = 500

    override suspend fun payment(request: PaymentRequest): Response<PaymentResponse> {
        delay(responseDelayMillis)

        return if (shouldFail) {
            throw Exception("Simulated API Failure")
        } else {
            val transactionId = "txn-${System.currentTimeMillis()}"
            val response = PaymentResponse("Success", transactionId)
            Response.success(response)
        }
    }
}