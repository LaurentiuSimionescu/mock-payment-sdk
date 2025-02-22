package com.mock.mockpaymentsdk.internal

import com.mock.mockpaymentsdk.errors.PaymentSDKPaymentInProgressException
import com.mock.mockpaymentsdk.models.PaymentRequest
import com.mock.mockpaymentsdk.models.PaymentResponse
import com.mock.mockpaymentsdk.repositories.PaymentRepository
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

class PaymentProcessor(private val repository: PaymentRepository) {

    private val isPaymentInProgress = AtomicBoolean(false)
    private var currentTransactionId: String? = null

    fun processPayment(
        amount: Int,
        currency: String,
        recipient: String,
        callback: (Result<PaymentResponse>) -> Unit
    ) {
        synchronized(this) {
            if (isPaymentInProgress.get()) {
                callback(Result.failure(PaymentSDKPaymentInProgressException()))
                return
            }

            currentTransactionId = UUID.randomUUID().toString()
            isPaymentInProgress.set(true)
        }

        val request = PaymentRequest(amount.toString(), currency, recipient)

        repository.processPayment(request) { result ->
            synchronized(this) {
                isPaymentInProgress.set(false)
                currentTransactionId = null
            }
            callback(result)
        }
    }
}