package com.actito

import org.junit.Assert.assertTrue
import org.junit.Test

public class ActitoServicesInfoTests {
    @Test
    public fun testRegexValidation() {
        val servicesInfo = ActitoServicesInfo(
            applicationKey = "testKey",
            applicationSecret = "testSecret",
            hosts = ActitoServicesInfo.Hosts(
                restApi = "example.com",
                appLinks = "example.com",
                shortLinks = "example.com",
            ),
        )

        servicesInfo.validate()
        assertTrue(true)
    }

    @Test
    public fun testHttpsLinkRegexValidation() {
        val servicesInfo = ActitoServicesInfo(
            applicationKey = "testKey",
            applicationSecret = "testSecret",
            hosts = ActitoServicesInfo.Hosts(
                restApi = "https://api.example.com",
                appLinks = "https://api.example.com",
                shortLinks = "https://api.example.com",
            ),
        )

        servicesInfo.validate()
        assertTrue(true)
    }

    @Test
    public fun testHttpLinkRegexValidation() {
        val servicesInfo = ActitoServicesInfo(
            applicationKey = "testKey",
            applicationSecret = "testSecret",
            hosts = ActitoServicesInfo.Hosts(
                restApi = "http://api.example.com",
                appLinks = "http://api.example.com",
                shortLinks = "http://api.example.com",
            ),
        )

        servicesInfo.validate()
        assertTrue(true)
    }

    @Test
    public fun testHttpLinkWithPortRegexValidation() {
        val servicesInfo = ActitoServicesInfo(
            applicationKey = "testKey",
            applicationSecret = "testSecret",
            hosts = ActitoServicesInfo.Hosts(
                restApi = "http://localhost:3000",
                appLinks = "http://localhost:3000",
                shortLinks = "http://localhost:3000",
            ),
        )

        servicesInfo.validate()
        assertTrue(true)
    }
}
