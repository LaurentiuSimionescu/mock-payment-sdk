package com.mock.mockpaymentsdk.network

import com.mock.mockpaymentsdk.models.PaymentRequest
import com.mock.mockpaymentsdk.models.PaymentResponse
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class FakePaymentApi : PaymentApi {

    private val executor = Executors.newSingleThreadScheduledExecutor()
    private val transactionHistory = mutableListOf<PaymentResponse>()

    var shouldFail = false
    var responseDelayMillis: Long = 500

    override fun payment(request: PaymentRequest): Call<PaymentResponse> {
        return object : Call<PaymentResponse> {

            override fun enqueue(callback: Callback<PaymentResponse>) {
                executor.schedule({
                    if (shouldFail) {
                        callback.onFailure(this, Exception("Simulated API Failure"))
                    } else {
                        val transactionId = "txn-${System.currentTimeMillis()}"
                        val response = PaymentResponse("Success", transactionId)
                        transactionHistory.add(response)
                        callback.onResponse(this, Response.success(response))
                    }
                }, responseDelayMillis, TimeUnit.MILLISECONDS)
            }

            override fun execute(): Response<PaymentResponse> {
                throw UnsupportedOperationException("Network calls should be asynchronous")
            }

            override fun isExecuted(): Boolean = false
            override fun cancel() {}
            override fun isCanceled(): Boolean = false
            override fun clone(): Call<PaymentResponse> = payment(request)
            override fun request(): okhttp3.Request {
                throw UnsupportedOperationException("Fake API does not support real HTTP requests")
            }

            override fun timeout(): Timeout {
                TODO("Not yet implemented")
            }
        }
    }

    fun getTransactionHistory(): List<PaymentResponse> = transactionHistory.toList()
}