package com.actito

import com.actito.internal.ActitoLaunchState
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ActitoConfigureTest {
    private val context = RuntimeEnvironment.getApplication()

    @Test
    fun `configure with missing credentials`() {
        assertThrows(IllegalArgumentException::class.java) {
            Actito.configure(
                context = context,
                applicationKey = "key",
                applicationSecret = "",
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            Actito.configure(
                context = context,
                applicationKey = "",
                applicationSecret = "secret",
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            Actito.configure(
                context = context,
                applicationKey = "",
                applicationSecret = "",
            )
        }
    }

    @Test
    fun `configure with invalid hosts`() {
        val servicesInfo = ActitoServicesInfo(
            applicationKey = "key",
            applicationSecret = "secret",
            hosts = ActitoServicesInfo.Hosts(),
        )

        assertThrows(IllegalStateException::class.java) {
            Actito.configure(context, servicesInfo.copy(hosts = servicesInfo.hosts.copy(restApi = "htttps://")))
        }

        assertThrows(IllegalStateException::class.java) {
            Actito.configure(context, servicesInfo.copy(hosts = servicesInfo.hosts.copy(appLinks = "actito.com&")))
        }

        assertThrows(IllegalStateException::class.java) {
            Actito.configure(
                context,
                servicesInfo.copy(hosts = servicesInfo.hosts.copy(shortLinks = "actito.com/_test")),
            )
        }
    }

    @Test
    fun `configure ensure properties are initiated`() {
        Actito.configure(
            context = context,
            applicationKey = "key",
            applicationSecret = "secret",
        )

        Actito.database
        Actito.sharedPreferences

        assert(Actito.options != null)
        assert(Actito.context != null)
        assert(Actito.state == ActitoLaunchState.CONFIGURED)
    }
}
