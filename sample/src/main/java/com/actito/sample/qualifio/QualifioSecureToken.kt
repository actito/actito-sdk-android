package com.actito.sample.qualifio

import com.actito.sample.BuildConfig
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import timber.log.Timber
import java.util.Date
import java.util.concurrent.TimeUnit

object QualifioSecureToken {
    private const val SUBJECT = "actito-sample-user"

    private val FORM_FIELDS = mapOf(
        "firstname" to "John",
        "lastname" to "Doe",
        "email" to "john.doe@example.com",
    )

    fun generate(campaign: String): String? {
        val secret = BuildConfig.SECURE_TOKEN_SECRET
        if (secret.isBlank()) {
            Timber.w("Qualifio secure token secret not configured. Loading campaign '$campaign' without a token.")
            return null
        }

        val now = System.currentTimeMillis()

        return try {
            JWT.create()
                .withSubject(SUBJECT)
                .withIssuedAt(Date(now))
                .withNotBefore(Date(now - TimeUnit.MINUTES.toMillis(5)))
                .withExpiresAt(Date(now + TimeUnit.HOURS.toMillis(1)))
                .withClaim("form", FORM_FIELDS)
                .sign(Algorithm.HMAC256(secret))
        } catch (e: JWTCreationException) {
            Timber.e(e, "Failed to generate the Qualifio secure token for campaign '$campaign'.")
            null
        }
    }
}
