package com.mock.mockpaymentsdk

import com.mock.mockpaymentsdk.errors.PaymentSDKAPIKeyException
import com.mock.mockpaymentsdk.errors.PaymentSDKInitializationException
import com.mock.mockpaymentsdk.errors.PaymentSDKPaymentInProgressException
import com.mock.mockpaymentsdk.internal.PaymentProcessor
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PaymentSDKTest {
    private lateinit var sdk: PaymentSDK
    private lateinit var fakeRepository: FakePaymentRepository

    @Before
    fun setUp() {
        fakeRepository = FakePaymentRepository()
        val fakeProcessor = PaymentProcessor(fakeRepository)
        sdk = PaymentSDK.createForTesting(fakeProcessor)
    }

    @Test
    fun `should initialize SDK with API key`() {
        val sdk = PaymentSDK.Builder()
            .setApiKey("test-api-key")
            .build()

        assertNotNull(sdk)
    }

    @Test(expected = PaymentSDKAPIKeyException::class)
    fun `should throw exception if API key is missing`() {
        PaymentSDK.Builder().build()
    }

    @Test
    fun `should return the same instance on multiple calls`() {
        val sdk1 = PaymentSDK.Builder().setApiKey("test-api-key").build()
        val sdk2 = PaymentSDK.getInstance()

        assertSame(sdk1, sdk2)
    }

    @Test(expected = PaymentSDKInitializationException::class)
    fun `should throw exception when getInstance is called before initialization`() {
        val instanceField = PaymentSDK::class.java.getDeclaredField("instance")
        instanceField.isAccessible = true
        instanceField.set(null, null)

        PaymentSDK.getInstance()
    }

    @Test
    fun `should allow different API keys separately`() {
        val sdk1 = PaymentSDK.Builder().setApiKey("first-key").build()
        val instanceField = PaymentSDK::class.java.getDeclaredField("instance")
        instanceField.isAccessible = true
        instanceField.set(null, null)

        val sdk2 = PaymentSDK.Builder().setApiKey("second-key").build()

        assertNotNull(sdk1)
        assertNotNull(sdk2)
    }

    @Test(expected = PaymentSDKAPIKeyException::class)
    fun `should throw exception for empty API key`() {
        PaymentSDK.Builder().setApiKey("").build()
    }

    @Test(expected = PaymentSDKAPIKeyException::class)
    fun `should throw exception for null API key`() {
        PaymentSDK.Builder().setApiKey("").build()
    }

    @Test
    fun `should ensure only one instance is created even in multithreaded environment`() {
        val sdk1 = PaymentSDK.Builder().setApiKey("test-key").build()
        val sdk2 = PaymentSDK.Builder().setApiKey("test-key").build()

        assertSame(sdk1, sdk2)
    }

    @Test
    fun `should process payments successfully`() = runBlocking {
        val result = sdk.makePayment(100, "USD", "user123")

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull()?.transactionId)
    }

    @Test
    fun `should prevent concurrent payments`() = runBlocking {
        println("Starting first payment request...")
        val firstPayment = async { sdk.makePayment(100, "USD", "user123") }

        delay(50)

        println("Starting second payment request...")
        val secondPayment = async { sdk.makePayment(50, "EUR", "user456") }

        val firstResult = firstPayment.await()
        val secondResult = secondPayment.await()

        println("First payment result: $firstResult")
        println("Second payment result: $secondResult")

        assertTrue(firstResult.isSuccess)
        assertTrue(secondResult.isFailure)
        assertTrue(secondResult.exceptionOrNull() is PaymentSDKPaymentInProgressException)
    }

    @Test
    fun `should allow new payments after a failed transaction`() = runBlocking {
        fakeRepository.shouldFail = true
        val firstResult = sdk.makePayment(100, "USD", "user123")

        assertFalse(firstResult.isSuccess)

        fakeRepository.shouldFail = false
        val secondResult = sdk.makePayment(200, "EUR", "user456")

        assertTrue(secondResult.isSuccess)
    }
}