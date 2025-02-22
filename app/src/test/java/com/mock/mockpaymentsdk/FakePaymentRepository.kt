package com.mock.mockpaymentsdk

import com.mock.mockpaymentsdk.models.PaymentRequest
import com.mock.mockpaymentsdk.models.PaymentResponse
import com.mock.mockpaymentsdk.network.FakePaymentApi
import com.mock.mockpaymentsdk.repositories.PaymentRepository
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class FakePaymentRepository : PaymentRepository(api = FakePaymentApi()) {
    private val lock = ReentrantLock()
    var lastRequest: PaymentRequest? = null
    var shouldFail = false
    private val transactionHistory = mutableListOf<PaymentResponse>()

    override fun processPayment(
        request: PaymentRequest,
        callback: (Result<PaymentResponse>) -> Unit
    ) {
        lock.withLock {
            lastRequest = request

            Thread.sleep(300)

            if (shouldFail) {
                callback(Result.failure(Exception("Simulated payment failure")))
            } else {
                val transactionId = "txn-${System.currentTimeMillis()}"
                val response = PaymentResponse("Success", transactionId)
                transactionHistory.add(response)
                callback(Result.success(response))
            }
        }
    }

    fun getTransactionHistory(): List<PaymentResponse> = transactionHistory.toList()
}