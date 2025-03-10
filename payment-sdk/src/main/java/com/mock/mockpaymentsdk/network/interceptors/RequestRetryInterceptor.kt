package com.mock.mockpaymentsdk.network.interceptors

import com.mock.mockpaymentsdk.errors.PaymentRequestException
import com.mock.mockpaymentsdk.errors.PaymentRetryRequestException
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

internal class RequestRetryInterceptor(private val maxRetries: Int = 3) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var attempts = 0
        var response: Response? = null
        var exception: PaymentRequestException? = null

        do {
            attempts++
            try {
                response = chain.proceed(chain.request())
                if (response.isSuccessful) return response
            } catch (e: IOException) {
                exception = PaymentRequestException(e)
            }
        } while (attempts < maxRetries)

        //TODO sometimes it is ok to fail, for example the user does not have funds
        // so based on a code, decide if the retry is still needed
        return response ?: throw exception ?: PaymentRetryRequestException()
    }
}