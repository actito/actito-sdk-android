package com.actito.utilities.networking

import org.junit.Assert.assertEquals
import org.junit.Test

public class HostTests {
    @Test
    public fun testUrlWithHttpScheme() {
        val url = "http://www.example.com"

        assertEquals(url, url.ensureScheme())
    }

    @Test
    public fun testUrlWithHttpsScheme() {
        val url = "https://www.example.com"

        assertEquals(url, url.ensureScheme())
    }

    @Test
    public fun testUrlWithNoScheme() {
        val url = "www.example.com"

        assertEquals("https://$url", url.ensureScheme())
    }
}
