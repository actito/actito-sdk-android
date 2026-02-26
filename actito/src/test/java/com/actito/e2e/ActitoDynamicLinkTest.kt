package com.actito.e2e

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.actito.Actito
import com.actito.e2e.common.ActitoBaseTest
import com.actito.internal.network.NetworkException
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

private const val TEST_DYNAMIC_LINK = "https://actito-sample-app-dev.test.ntc.re/0z4juv8466"
private const val TEST_INVALID_DYNAMIC_LINK = "https://test.com/path"
private const val TEST_DEEP_LINK = "com.actito.sample.app.test.dev://actito.com/example"

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
            Uri.parse(TEST_INVALID_DYNAMIC_LINK),
        )

        val didHandle = Actito.handleDynamicLinkIntent(Activity(), intent)

        assert(!didHandle)
    }

    @Test
    fun `dynamic link handle intent`() = runTest {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(TEST_DYNAMIC_LINK),
        )

        val didHandle = Actito.handleDynamicLinkIntent(Activity(), intent)

        assert(didHandle)
    }

    @Test
    fun `dynamic link fetch invalid link`() {
        val uri = Uri.parse(TEST_INVALID_DYNAMIC_LINK)

        Assert.assertThrows(NetworkException.ValidationException::class.java) {
            runTest {
                Actito.fetchDynamicLink(uri)
            }
        }
    }

    @Test
    fun `dynamic link fetch link`() = runTest {
        val uri = Uri.parse(TEST_DYNAMIC_LINK)

        val actitoDynamicLink = Actito.fetchDynamicLink(uri)

        assert(actitoDynamicLink.target == TEST_DEEP_LINK)
    }
}
