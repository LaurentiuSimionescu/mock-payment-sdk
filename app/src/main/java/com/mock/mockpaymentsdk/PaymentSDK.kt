package com.mock.mockpaymentsdk

import com.mock.mockpaymentsdk.errors.PaymentSDKAPIKeyException
import com.mock.mockpaymentsdk.errors.PaymentSDKInitializationException
import com.mock.mockpaymentsdk.internal.PaymentProcessor
import com.mock.mockpaymentsdk.models.PaymentResponse
import com.mock.mockpaymentsdk.providers.PaymentNetworkProvider.createPaymentApi
import com.mock.mockpaymentsdk.repositories.PaymentRepository

class PaymentSDK private constructor(
    private val apiKey: String
) {

    private val paymentApi = createPaymentApi(apiKey)
    private val paymentRepository = PaymentRepository(paymentApi)
    private val paymentProcessor = PaymentProcessor(paymentRepository)

    fun makePayment(
        amount: Int,
        currency: String,
        recipient: String,
        callback: (Result<PaymentResponse>) -> Unit
    ) {
        paymentProcessor.processPayment(amount, currency, recipient, callback)
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
    }

    class Builder {
        private var apiKey: String? = null

        fun setApiKey(apiKey: String) = apply { this.apiKey = apiKey }

        fun build(): PaymentSDK {
            require(!apiKey.isNullOrEmpty()) { throw PaymentSDKAPIKeyException() }

            return instance ?: synchronized(PaymentSDK::class.java) {
                instance ?: PaymentSDK(apiKey!!).also { instance = it }
            }
        }
    }
}