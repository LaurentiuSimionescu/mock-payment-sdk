package com.mock.mockpaymentsdk.network.interceptors

import com.mock.mockpaymentsdk.errors.PaymentRequestError
import com.mock.mockpaymentsdk.errors.PaymentRetryRequestError
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class RequestRetryInterceptor(private val maxRetries: Int = 3) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var attempts = 0
        var response: Response? = null
        var exception: PaymentRequestError? = null

        do {
            attempts++
            try {
                response = chain.proceed(chain.request())
                if (response.isSuccessful) return response
            } catch (e: IOException) {
                exception = PaymentRequestError(e)
            }
        } while (attempts < maxRetries)

        //TODO sometimes it is ok to fail, for example the user does not have funds
        // so based on a code, decide if the retry is still needed
        return response ?: throw exception ?: PaymentRetryRequestError()
    }
}