package com.mock.mockpaymentsdk

import PaymentRepository
import com.mock.mockpaymentsdk.models.PaymentRequest
import com.mock.mockpaymentsdk.models.PaymentResponse
import kotlinx.coroutines.delay
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal class FakePaymentRepository : PaymentRepository(api = FakePaymentApi()) {

    private val lock = ReentrantLock()
    private val transactionHistory = CopyOnWriteArrayList<PaymentResponse>()

    var lastRequest: PaymentRequest? = null
    var shouldFail = false

    override suspend fun processPayment(request: PaymentRequest): Result<PaymentResponse> {
        val transactionId: String?
        val shouldFailCopy: Boolean

        lock.withLock {
            lastRequest = request
            shouldFailCopy = shouldFail
        }

        delay(300)

        return if (shouldFailCopy) {
            Result.failure(Exception("Simulated payment failure"))
        } else {
            transactionId = "txn-${System.currentTimeMillis()}"
            val response = PaymentResponse("Success", transactionId)

            lock.withLock {
                transactionHistory.add(response)
            }

            Result.success(response)
        }
    }
}