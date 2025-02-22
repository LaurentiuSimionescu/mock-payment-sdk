package com.mock.mockpaymentsdk.errors

class PaymentRequestError(exception: Exception) : Exception("Invalid payment request")
//TODO the ideea is that we dont want to show some of the errors to the user,,
// i mean the server errors, the message might be nasty or even leak some information,
// this can be decided in real life how to handle it, but for demo i will ignore it

class PaymentRetryRequestError : Exception("Retry payment request failed")