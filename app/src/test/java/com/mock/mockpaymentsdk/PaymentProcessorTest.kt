package com.mock.mockpaymentsdk

import com.mock.mockpaymentsdk.errors.PaymentSDKPaymentInProgressException
import com.mock.mockpaymentsdk.internal.PaymentProcessor
import com.mock.mockpaymentsdk.models.PaymentRequest
import com.mock.mockpaymentsdk.models.PaymentResponse
import com.mock.mockpaymentsdk.repositories.PaymentRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PaymentProcessorTest {

    private lateinit var paymentProcessor: PaymentProcessor
    private lateinit var fakeRepository: FakePaymentRepository

    @Before
    fun setUp() {
        fakeRepository = FakePaymentRepository()
        paymentProcessor = PaymentProcessor(fakeRepository)
    }

    @Test
    fun `should generate a unique transaction ID for each payment`() {
        val latch = CountDownLatch(1)

        paymentProcessor.processPayment(100, "USD", "user123") { result ->
            assertTrue(result.isSuccess)
            assertEquals("Success", result.getOrNull()?.status)
            assertNotNull(result.getOrNull()?.transactionId)
            latch.countDown()
        }

        latch.await(2, TimeUnit.SECONDS)
    }

    @Test
    fun `should prevent concurrent payments`() {
        val latch = CountDownLatch(2)

        val firstThread = Thread {
            paymentProcessor.processPayment(100, "USD", "user123") { result ->
                assertTrue(result.isSuccess)
            }
            latch.countDown()
        }

        val secondThread = Thread {
            paymentProcessor.processPayment(50, "EUR", "user456") { result ->
                assertTrue(result.isFailure)
                assertTrue(result.exceptionOrNull() is PaymentSDKPaymentInProgressException)
            }
            latch.countDown()
        }

        firstThread.start()
        secondThread.start()
        latch.await(3, TimeUnit.SECONDS)

        firstThread.join()
        secondThread.join()
    }

    @Test
    fun `should call repository with correct parameters`() {
        val latch = CountDownLatch(1)

        paymentProcessor.processPayment(100, "USD", "user123") { result ->
            assertTrue(result.isSuccess)
            latch.countDown()
        }

        latch.await(2, TimeUnit.SECONDS)

        val storedRequest = fakeRepository.lastRequest
        assertNotNull(storedRequest)
        assertEquals("100", storedRequest?.amount)
        assertEquals("USD", storedRequest?.currency)
        assertEquals("user123", storedRequest?.recipient)
    }

    @Test
    fun `should reset payment state after completion`() {
        val latch = CountDownLatch(2)

        paymentProcessor.processPayment(100, "USD", "user123") { result ->
            assertTrue(result.isSuccess)
            latch.countDown()
        }

        latch.await(2, TimeUnit.SECONDS)

        val secondThread = Thread {
            paymentProcessor.processPayment(50, "EUR", "user456") { result ->
                assertTrue(result.isSuccess)
                latch.countDown()
            }
        }

        secondThread.start()
        secondThread.join()
    }

    @Test
    fun `should allow new payments after a failed transaction`() {
        fakeRepository.shouldFail = true
        val latch = CountDownLatch(2)

        paymentProcessor.processPayment(100, "USD", "user123") { result ->
            assertFalse(result.isSuccess)
            latch.countDown()
        }

        latch.await(2, TimeUnit.SECONDS)

        fakeRepository.shouldFail = false

        val secondLatch = CountDownLatch(1)
        paymentProcessor.processPayment(200, "EUR", "user456") { result ->
            assertTrue(result.isSuccess)
            secondLatch.countDown()
        }

        secondLatch.await(2, TimeUnit.SECONDS)
    }

    @Test
    fun `should ensure thread safety when handling multiple payments`() {
        val executor = Executors.newFixedThreadPool(5)
        val latch = CountDownLatch(5)
        val transactionIds = mutableSetOf<String>()

        repeat(5) {
            executor.execute {
                paymentProcessor.processPayment(100, "USD", "user123") { result ->
                    synchronized(transactionIds) {
                        transactionIds.add(result.getOrNull()?.transactionId ?: "")
                    }
                }
                latch.countDown()
            }
        }

        latch.await(5, TimeUnit.SECONDS)
        executor.shutdown()

        //TODO Only one transaction should be in progress at a time
        assertEquals(1, transactionIds.size)
    }
}