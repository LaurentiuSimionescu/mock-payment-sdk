package com.mock.mockpaymentsdk

import com.mock.mockpaymentsdk.errors.PaymentSDKAPIKeyException
import com.mock.mockpaymentsdk.errors.PaymentSDKInitializationException
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PaymentSDKTest {

    @Before
    fun setUp() {
        val instanceField = PaymentSDK::class.java.getDeclaredField("instance")
        instanceField.isAccessible = true
        instanceField.set(null, null)
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
        PaymentSDK.getInstance()
    }

    @Test
    fun `should initialize with different API keys separately`() {
        val sdk1 = PaymentSDK.Builder().setApiKey("first-key").build()
        val instanceField = PaymentSDK::class.java.getDeclaredField("instance")
        instanceField.isAccessible = true
        instanceField.set(null, null)

        val sdk2 = PaymentSDK.Builder().setApiKey("second-key").build()

        assertNotNull(sdk1)
        assertNotNull(sdk2)
        assertEquals(
            "second-key",
            sdk2.javaClass.getDeclaredField("apiKey").apply { isAccessible = true }.get(sdk2)
        )
    }

    @Test
    fun `should handle case-sensitive API keys correctly`() {
        val sdk1 = PaymentSDK.Builder().setApiKey("API-KEY").build()
        val instanceField = PaymentSDK::class.java.getDeclaredField("instance")
        instanceField.isAccessible = true
        instanceField.set(null, null)

        val sdk2 = PaymentSDK.Builder().setApiKey("api-key").build()

        assertNotEquals(sdk1, sdk2)
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
        val executor = Executors.newFixedThreadPool(2)
        val latch = CountDownLatch(2)
        var sdk1: PaymentSDK? = null
        var sdk2: PaymentSDK? = null

        executor.execute {
            sdk1 = PaymentSDK.Builder().setApiKey("test-key").build()
            latch.countDown()
        }

        executor.execute {
            sdk2 = PaymentSDK.Builder().setApiKey("test-key").build()
            latch.countDown()
        }

        latch.await(3, TimeUnit.SECONDS)
        assertSame(sdk1, sdk2)
    }

    @Test
    fun `should not allow changing API key after initialization`() {
        val sdk1 = PaymentSDK.Builder().setApiKey("initial-key").build()

        val instanceField = PaymentSDK::class.java.getDeclaredField("instance")
        instanceField.isAccessible = true
        val currentInstance = instanceField.get(null)

        val sdk2 = PaymentSDK.Builder().setApiKey("new-key").build()

        assertSame(sdk1, sdk2)
        assertEquals("initial-key", currentInstance!!.javaClass.getDeclaredField("apiKey").apply {
            isAccessible = true
        }.get(currentInstance))
    }
}