package com.actito

public data class ActitoServicesInfo @JvmOverloads constructor(
    val applicationKey: String,
    val applicationSecret: String,
    val hosts: Hosts = Hosts(),
) {

    internal val hasDefaultHosts: Boolean
        get() = hosts.restApi == DEFAULT_REST_API_HOST &&
            hosts.appLinks == DEFAULT_APP_LINKS_HOST &&
            hosts.shortLinks == DEFAULT_SHORT_LINKS_HOST

    internal fun validate() {
        check(HOST_REGEX.matches(hosts.restApi)) {
            "Invalid REST API host."
        }

        check(HOST_REGEX.matches(hosts.appLinks)) {
            "Invalid AppLinks host."
        }

        check(HOST_REGEX.matches(hosts.shortLinks)) {
            "Invalid short links host."
        }
    }

    public data class Hosts(
        var restApi: String,
        val appLinks: String,
        val shortLinks: String,
    ) {
        init {
            if (!restApi.startsWith("http://") && !restApi.startsWith("https://")) {
                restApi = "https://$restApi"
            }
        }

        public constructor() : this(
            restApi = DEFAULT_REST_API_HOST,
            appLinks = DEFAULT_APP_LINKS_HOST,
            shortLinks = DEFAULT_SHORT_LINKS_HOST,
        )
    }

    public companion object {
        private const val DEFAULT_REST_API_HOST = "https://push.notifica.re"
        private const val DEFAULT_SHORT_LINKS_HOST = "ntc.re"
        private const val DEFAULT_APP_LINKS_HOST = "applinks.notifica.re"

        @Suppress("detekt:MaxLineLength")
        private val HOST_REGEX = Regex(
            "^(https?://)?(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])(:[0-9]{1,5})?$",
        )
    }
}
