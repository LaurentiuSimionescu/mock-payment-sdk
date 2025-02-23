package com.mock.mockpaymentsdk

import PaymentRepository
import androidx.annotation.VisibleForTesting
import com.mock.mockpaymentsdk.errors.PaymentSDKAPIKeyException
import com.mock.mockpaymentsdk.errors.PaymentSDKInitializationException
import com.mock.mockpaymentsdk.internal.PaymentProcessor
import com.mock.mockpaymentsdk.models.PaymentResponse
import com.mock.mockpaymentsdk.providers.PaymentNetworkProvider.createPaymentApi

class PaymentSDK private constructor(
    private val paymentProcessor: PaymentProcessor
) {

    suspend fun makePayment(
        amount: Int,
        currency: String,
        recipient: String,
    ): Result<PaymentResponse> {
        return paymentProcessor.processPayment(amount, currency, recipient)
    }

    companion object {
        @Volatile
        private var instance: PaymentSDK? = null

        fun getInstance(): PaymentSDK {
            return instance ?: throw PaymentSDKInitializationException()
        }

        fun deleteInstance() {
            synchronized(PaymentSDK::class.java) {
                instance = null
            }
        }

        @VisibleForTesting
        internal fun createForTesting(paymentProcessor: PaymentProcessor): PaymentSDK {
            return PaymentSDK(paymentProcessor)
        }
    }

    class Builder {
        private var apiKey: String? = null

        fun setApiKey(apiKey: String) = apply { this.apiKey = apiKey }

        fun build(): PaymentSDK {
            require(!apiKey.isNullOrEmpty()) { throw PaymentSDKAPIKeyException() }

            val api = createPaymentApi(apiKey!!)
            val repository = PaymentRepository(api)
            val processor = PaymentProcessor(repository)

            return instance ?: synchronized(PaymentSDK::class.java) {
                instance ?: PaymentSDK(processor).also { instance = it }
            }
        }
    }
}