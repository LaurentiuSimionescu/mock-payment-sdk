package com.mock.mockpaymentsdk.internal

import PaymentRepository
import com.mock.mockpaymentsdk.errors.PaymentSDKPaymentInProgressException
import com.mock.mockpaymentsdk.models.PaymentRequest
import com.mock.mockpaymentsdk.models.PaymentResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID

internal class PaymentProcessor(private val repository: PaymentRepository) {

    private val mutex = Mutex()
    private var isPaymentInProgress = false
    private var currentTransactionId: String? = null

    suspend fun processPayment(
        amount: Int,
        currency: String,
        recipient: String
    ): Result<PaymentResponse> {
        return withContext(Dispatchers.IO) {
            mutex.withLock {
                if (isPaymentInProgress) {
                    return@withContext Result.failure(PaymentSDKPaymentInProgressException())
                }
                isPaymentInProgress = true
            }

            try {
                currentTransactionId = UUID.randomUUID().toString()
                val request = PaymentRequest(amount.toString(), currency, recipient)

                repository.processPayment(request)
            } finally {
                mutex.withLock {
                    isPaymentInProgress = false
                    currentTransactionId = null
                }
            }
        }
    }
}