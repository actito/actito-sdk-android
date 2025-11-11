package com.actito.internal.network.request

import com.actito.Actito
import com.actito.ActitoCallback
import com.actito.InternalActitoApi
import com.actito.internal.ktx.await
import com.actito.internal.logger
import com.actito.internal.moshi
import com.actito.internal.network.ActitoHeadersInterceptor
import com.actito.internal.network.NetworkException
import com.actito.utilities.coroutines.toCallbackFunction
import com.actito.utilities.networking.isRecoverable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import kotlin.reflect.KClass

@InternalActitoApi
public typealias DecodableForFn<T> = (responseCode: Int) -> KClass<T>?

@InternalActitoApi
public class ActitoRequest private constructor(
    private val request: Request,
    private val validStatusCodes: IntRange,
) {

    private companion object {

        private val HTTP_METHODS_REQUIRE_BODY = arrayOf("PATCH", "POST", "PUT")
        private val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
        private const val MAX_RETRIES = 5
        private const val INITIAL_DELAY_MILLISECONDS: Long = 500
        private const val BACKOFF_FACTOR = 2

        private val client = OkHttpClient.Builder()
            // .authenticator(NotificareBasicAuthenticator())
            .addInterceptor(ActitoHeadersInterceptor())
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (Actito.options?.debugLoggingEnabled == true) {
                        HttpLoggingInterceptor.Level.BASIC
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                },
            )
            .build()
    }

    public suspend fun response(closeResponse: Boolean): Response {
        try {
            val response = client.newCall(request).await()
            return handleResponse(
                response = response,
                closeResponse = closeResponse,
            )
        } catch (e: Exception) {
            if (e.isRecoverable) {
                logger.debug("Network request failed. Retrying...")
                return retryResponse(closeResponse)
            } else {
                throw e
            }
        }
    }

    private suspend fun retryResponse(closeResponse: Boolean, maxRetries: Int = MAX_RETRIES): Response {
        var attempt = 0
        var delay = INITIAL_DELAY_MILLISECONDS
        var lastException: Exception? = null

        if (maxRetries <= 0) {
            throw IllegalArgumentException("Number of retries must be 1 or larger.")
        }

        while (attempt < maxRetries) {
            try {
                val response = client.newCall(request).await()

                return handleResponse(
                    response = response,
                    closeResponse = closeResponse,
                )
            } catch (e: Exception) {
                if (!e.isRecoverable) {
                    logger.debug("Network request failed.", e)
                    throw e
                }

                lastException = e
                attempt++

                if (attempt < maxRetries) {
                    logger.debug("Network request retry attempt $attempt failed. Retrying...")

                    delay(delay)
                    delay = delay * BACKOFF_FACTOR
                }
            }
        }

        logger.debug("Network request retry attempt $attempt failed.")
        throw NetworkException.InaccessibleServiceException(attempt, lastException)
    }

    public suspend fun responseString(): String {
        val response = response(closeResponse = false)

        val body = response.body
            ?: throw IllegalArgumentException("The response contains an empty body. Cannot parse into 'String'.")

        return withContext(Dispatchers.IO) {
            try {
                body.string()
            } catch (e: Exception) {
                throw NetworkException.ParsingException(cause = e)
            } finally {
                body.close()
            }
        }
    }

    public suspend fun <T : Any> responseDecodable(klass: KClass<T>): T {
        val response = response(closeResponse = false)

        val body = response.body
            ?: throw IllegalArgumentException(
                "The response contains an empty body. Cannot parse into '${klass.simpleName}'.",
            )

        return withContext(Dispatchers.IO) {
            try {
                val adapter = Actito.moshi.adapter(klass.java)

                adapter.fromJson(body.source())
                    ?: throw NetworkException.ParsingException(message = "JSON parsing resulted in a null object.")
            } catch (e: Exception) {
                throw NetworkException.ParsingException(cause = e)
            } finally {
                body.close()
            }
        }
    }

    public suspend fun <T : Any> responseDecodable(
        decodableFor: DecodableForFn<T>,
    ): T? = withContext(Dispatchers.IO) {
        response(closeResponse = false).use { response ->
            val klass = decodableFor(response.code)
                ?: return@withContext null

            val body = response.body
                ?: throw IllegalArgumentException(
                    "The response contains an empty body. Cannot parse into '${klass.simpleName}'.",
                )

            try {
                val adapter = Actito.moshi.adapter(klass.java)

                adapter.fromJson(body.source())
                    ?: throw NetworkException.ParsingException(message = "JSON parsing resulted in a null object.")
            } catch (e: Exception) {
                throw NetworkException.ParsingException(cause = e)
            }
        }
    }

    private fun handleResponse(response: Response, closeResponse: Boolean): Response {
        try {
            if (response.code !in validStatusCodes) {
                // Forcefully close the body. Decodable responses will not proceed.
                response.body?.close()

                throw NetworkException.ValidationException(
                    response = response,
                    validStatusCodes = validStatusCodes,
                )
            }

            return response
        } finally {
            if (closeResponse) response.body?.close()
        }
    }

    public class Builder {
        private var baseUrl: String? = null
        private var url: String? = null
        private var queryItems = mutableMapOf<String, String?>()
        private var authentication: Authentication? = createDefaultAuthentication()
        private var headers = mutableMapOf<String, String>()
        private var method: String? = null
        private var body: RequestBody? = null
        private var validStatusCodes: IntRange = 200..299

        public fun baseUrl(url: String): Builder {
            this.baseUrl = url
            return this
        }

        public fun get(url: String): Builder = method("GET", url, null)

        public fun <T : Any> patch(url: String, body: T? = null): Builder = method("PATCH", url, body)

        public fun <T : Any> post(url: String, body: T? = null): Builder = method("POST", url, body)

        public fun <T : Any> put(url: String, body: T? = null): Builder = method("PUT", url, body)

        public fun <T : Any> delete(url: String, body: T? = null): Builder = method("DELETE", url, body)

        private fun <T : Any> method(method: String, url: String, body: T?): Builder {
            this.method = method
            this.url = url
            this.body = when (body) {
                null -> if (HTTP_METHODS_REQUIRE_BODY.contains(method)) RequestBody.EMPTY else null
                is ByteArray -> body.toRequestBody()
                is FormBody -> body
                else -> try {
                    val klass = when (body) {
                        // Moshi only supports the default collection types therefore we need to cast them down.
                        //
                        // Concrete example: passing a mapOf() internally uses a LinkedHashMap which would cause
                        // Moshi to throw an IllegalArgumentException since there is no specific adapter registered
                        // for that given type.
                        is Map<*, *> -> Map::class.java
                        else -> body::class.java
                    }

                    val adapter = Actito.moshi.adapter<T>(klass)
                    adapter.toJson(body).toRequestBody(MEDIA_TYPE_JSON)
                } catch (e: Exception) {
                    throw NetworkException.ParsingException(message = "Unable to encode body into JSON.", cause = e)
                }
            }

            return this
        }

        public fun query(items: Map<String, String?>): Builder {
            queryItems.putAll(items)
            return this
        }

        public fun query(item: Pair<String, String?>): Builder {
            queryItems[item.first] = item.second
            return this
        }

        public fun query(name: String, value: String?): Builder {
            queryItems[name] = value
            return this
        }

        public fun header(name: String, value: String): Builder {
            headers[name] = value
            return this
        }

        public fun authentication(authentication: Authentication?): Builder {
            this.authentication = authentication
            return this
        }

        public fun validate(validStatusCodes: IntRange = 200..299): Builder {
            this.validStatusCodes = validStatusCodes
            return this
        }

        @Throws(IllegalArgumentException::class)
        public fun build(): ActitoRequest {
            val method = requireNotNull(method) { "Please provide the HTTP method for the request." }

            val request = Request.Builder()
                .url(computeCompleteUrl())
                .apply {
                    headers.forEach {
                        header(it.key, it.value)
                    }

                    authentication?.run {
                        header("Authorization", encode())
                    }
                }
                .method(method, body)
                .build()

            return ActitoRequest(
                request = request,
                validStatusCodes = validStatusCodes,
            )
        }

        public suspend fun response(): Response = build().response(true)

        public fun response(callback: ActitoCallback<Response>): Unit =
            toCallbackFunction(::response)(callback::onSuccess, callback::onFailure)

        public suspend fun responseString(): String = build().responseString()

        public fun responseString(callback: ActitoCallback<String>): Unit =
            toCallbackFunction(::responseString)(callback::onSuccess, callback::onFailure)

        public suspend fun <T : Any> responseDecodable(klass: KClass<T>): T = build().responseDecodable(klass)

        public fun <T : Any> responseDecodable(klass: KClass<T>, callback: ActitoCallback<T>): Unit =
            toCallbackFunction(suspend { responseDecodable(klass) })(callback::onSuccess, callback::onFailure)

        public suspend fun <T : Any> responseDecodable(decodableFor: DecodableForFn<T>): T? =
            build().responseDecodable(decodableFor)

        @Throws(IllegalArgumentException::class)
        private fun computeCompleteUrl(): HttpUrl {
            var url = requireNotNull(url) { "Please provide the URL for the request." }

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                var baseUrl = requireNotNull(baseUrl ?: Actito.servicesInfo?.hosts?.restApi) {
                    "Unable to determine the base url for the request."
                }

                if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
                    baseUrl = "https://$baseUrl"
                }

                url = if (!baseUrl.endsWith("/") && !url.startsWith("/")) {
                    "$baseUrl/$url"
                } else {
                    "$baseUrl$url"
                }
            }

            if (queryItems.isNotEmpty()) {
                return url.toHttpUrl()
                    .newBuilder()
                    .apply {
                        queryItems.forEach { (key, value) -> addQueryParameter(key, value) }
                    }
                    .build()
            }

            return url.toHttpUrl()
        }

        private fun createDefaultAuthentication(): Authentication? {
            val key = Actito.servicesInfo?.applicationKey
            val secret = Actito.servicesInfo?.applicationSecret

            if (key == null || secret == null) {
                logger.warning("Actito application authentication not configured.")
                return null
            }

            return Authentication.Basic(key, secret)
        }
    }

    public sealed class Authentication {
        public abstract fun encode(): String

        public data class Basic(
            val username: String,
            val password: String,
        ) : Authentication() {

            override fun encode(): String = Credentials.basic(username, password)
        }
    }
}
