package com.mock.mockpaymentsdk.errors

open class PaymentSDKException(message: String) : Exception(message)

class PaymentSDKAPIKeyException : PaymentSDKException("Invalid API key")

class PaymentSDKInitializationException : PaymentSDKException("SDK not initialized")

class PaymentSDKPaymentInProgressException() :
    PaymentSDKException("A payment is already in progress, Please wait until it completes.")