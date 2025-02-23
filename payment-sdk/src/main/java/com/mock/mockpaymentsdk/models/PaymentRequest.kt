package com.mock.mockpaymentsdk.models

data class PaymentRequest(
    val amount: String,
    val currency: String,
    val recipient: String
)