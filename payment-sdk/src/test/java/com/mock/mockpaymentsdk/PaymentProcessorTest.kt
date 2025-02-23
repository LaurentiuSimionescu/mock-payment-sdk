package com.mock.mockpaymentsdk

import com.mock.mockpaymentsdk.errors.PaymentSDKPaymentInProgressException
import com.mock.mockpaymentsdk.internal.PaymentProcessor
import com.mock.mockpaymentsdk.models.PaymentResponse
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Collections
import java.util.concurrent.Executors

class PaymentProcessorTest {

    private lateinit var paymentProcessor: PaymentProcessor
    private lateinit var fakeRepository: FakePaymentRepository

    @Before
    fun setUp() {
        fakeRepository = FakePaymentRepository()
        paymentProcessor = PaymentProcessor(fakeRepository)
    }

    @Test
    fun `should generate a unique transaction ID for each payment`() = runBlocking {
        val result = paymentProcessor.processPayment(100, "USD", "user123")

        assertTrue(result.isSuccess)
        assertEquals("Success", result.getOrNull()?.status)
        assertNotNull(result.getOrNull()?.transactionId)
    }

    @Test
    fun `should prevent concurrent payments`() = runBlocking {
        val firstPayment = async { paymentProcessor.processPayment(100, "USD", "user123") }
        val secondPayment = async { paymentProcessor.processPayment(50, "EUR", "user456") }

        val firstResult = firstPayment.await()
        val secondResult = secondPayment.await()

        assertTrue(firstResult.isSuccess)
        assertTrue(secondResult.isFailure)
        assertTrue(secondResult.exceptionOrNull() is PaymentSDKPaymentInProgressException)
    }

    @Test
    fun `should call repository with correct parameters`() = runBlocking {
        val result = paymentProcessor.processPayment(100, "USD", "user123")

        assertTrue(result.isSuccess)

        val storedRequest = fakeRepository.lastRequest
        assertNotNull(storedRequest)
        assertEquals("100", storedRequest?.amount)
        assertEquals("USD", storedRequest?.currency)
        assertEquals("user123", storedRequest?.recipient)
    }

    @Test
    fun `should reset payment state after completion`() = runBlocking {
        val firstPayment = async { paymentProcessor.processPayment(100, "USD", "user123") }
        assertTrue(firstPayment.await().isSuccess)

        val secondPayment = async { paymentProcessor.processPayment(50, "EUR", "user456") }
        assertTrue(secondPayment.await().isSuccess)
    }

    @Test
    fun `should allow new payments after a failed transaction`() = runBlocking {
        fakeRepository.shouldFail = true
        val firstResult = paymentProcessor.processPayment(100, "USD", "user123")

        assertFalse(firstResult.isSuccess)

        fakeRepository.shouldFail = false
        val secondResult = paymentProcessor.processPayment(200, "EUR", "user456")

        assertTrue(secondResult.isSuccess)
    }

    @Test
    fun `should ensure thread safety when handling multiple payments`() = runBlocking {
        val executor = Executors.newFixedThreadPool(5)
        val transactionIds = Collections.synchronizedSet(mutableSetOf<String>())

        val jobs = mutableListOf<Deferred<Result<PaymentResponse>>>()

        repeat(5) {
            jobs.add(async { paymentProcessor.processPayment(100, "USD", "user123") })
        }

        jobs.awaitAll().forEach { result ->
            result.getOrNull()?.transactionId?.let { transactionIds.add(it) }
        }

        executor.shutdown()

        assertEquals(1, transactionIds.size)
    }
}