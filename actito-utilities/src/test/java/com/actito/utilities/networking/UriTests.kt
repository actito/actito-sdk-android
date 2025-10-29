package com.actito.utilities.networking

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class UriTests {
    @Test
    public fun testRemoveExistingParameter() {
        val builder = Uri.parse("https://example.com/?foo=1&bar=2&baz=3").buildUpon()
        val result = builder.removeQueryParameter("bar").build()

        assertEquals("https://example.com/?foo=1&baz=3", result.toString())
    }

    @Test
    public fun testRemoveNonExistingParameter() {
        val builder = Uri.parse("https://example.com/?foo=1&baz=3").buildUpon()
        val result = builder.removeQueryParameter("bar").build()

        assertEquals("https://example.com/?foo=1&baz=3", result.toString())
    }

    @Test
    public fun testRemoveMultipleExistingKeys() {
        val builder = Uri.parse("https://example.com/?foo=1&foo=2&bar=3").buildUpon()
        val result = builder.removeQueryParameter("foo").build()

        assertEquals("https://example.com/?bar=3", result.toString())
    }

    @Test
    public fun testRemoveFromUriWithPathAndNoQueryParameters() {
        val builder = Uri.parse("https://example.com/path").buildUpon()
        val result = builder.removeQueryParameter("foo").build()

        assertEquals("https://example.com/path", result.toString())
    }

    @Test
    public fun testRemovesParameterFromUriWithPath() {
        val builder = Uri.parse("http://test.org/path?foo=1&bar=2").buildUpon()
        val result = builder.removeQueryParameter("foo").build()

        assertEquals("http://test.org/path?bar=2", result.toString())
        assertEquals("bar=2", result.query)
    }

    @Test
    public fun testRemovesParameterWithEncodedValue() {
        val builder = Uri.parse("https://example.com/?foo=hello%20world&bar=test").buildUpon()
        val result = builder.removeQueryParameter("foo").build()

        assertEquals("https://example.com/?bar=test", result.toString())
    }

    @Test
    public fun testRemovesKeyWithEmptyValue() {
        val builder = Uri.parse("https://example.com/?foo=&bar=123").buildUpon()
        val result = builder.removeQueryParameter("foo").build()

        assertEquals("https://example.com/?bar=123", result.toString())
    }
}
