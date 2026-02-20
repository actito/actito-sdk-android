package com.actito

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.actito.common.ActitoBaseTest
import com.actito.internal.network.NetworkException
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class ActitoDynamicLinkTest : ActitoBaseTest() {
    @Test
    fun `dynamic link handle empty intent`() = runTest {
        val intent = Intent(Intent.ACTION_VIEW)

        val didHandle = Actito.handleDynamicLinkIntent(Activity(), intent)

        assert(!didHandle)
    }

    @Test
    fun `dynamic link handle intent with wrong uri host`() = runTest {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://test.com/path"),
        )

        val didHandle = Actito.handleDynamicLinkIntent(Activity(), intent)

        assert(!didHandle)
    }

    @Test
    fun `dynamic link handle intent`() = runTest {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://actito-sample-app-dev.test.ntc.re/0z4juv8466"),
        )

        val didHandle = Actito.handleDynamicLinkIntent(Activity(), intent)

        assert(didHandle)
    }

    @Test
    fun `dynamic link fetch invalid link`() {
        val uri = Uri.parse("https://test.com/path")

        Assert.assertThrows(NetworkException.ValidationException::class.java) {
            runTest {
                Actito.fetchDynamicLink(uri)
            }
        }
    }

    @Test
    fun `dynamic link fetch link`() = runTest {
        val uri = Uri.parse("https://actito-sample-app-dev.test.ntc.re/0z4juv8466")

        val actitoDynamicLink = Actito.fetchDynamicLink(uri)

        assert(actitoDynamicLink.target == "com.actito.sample.app.test.dev://actito.com/example")
    }
}
