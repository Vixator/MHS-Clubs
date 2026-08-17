package com.precon.mhsclubs.api

import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * JVM transport for [httpGetJson] using [HttpURLConnection]. Shared by the
 * desktop/JVM target and the Android host tests.
 */
actual suspend fun httpGetJson(url: String, bearerToken: String?): String =
    request(url, "GET", null, bearerToken)

actual suspend fun httpPostJson(url: String, body: String, bearerToken: String?): String =
    request(url, "POST", body, bearerToken, contentType = "application/json")

actual suspend fun httpDeleteJson(url: String, bearerToken: String?): String =
    request(url, "DELETE", null, bearerToken)

/** Default API base URL for the JVM/desktop target. */
actual fun defaultApiBaseUrl(): String =
    System.getProperty("mhsclubs.apiBaseUrl") ?: System.getenv("MHS_CLUBS_API_URL") ?: "http://localhost:8080"

private fun request(
    url: String,
    method: String,
    body: String?,
    bearerToken: String?,
    contentType: String? = null
): String {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = method
        setRequestProperty("Accept", "application/json")
        if (contentType != null) setRequestProperty("Content-Type", contentType)
        if (bearerToken != null) setRequestProperty("Authorization", "Bearer $bearerToken")
        connectTimeout = 10_000
        readTimeout = 15_000
        doOutput = body != null
    }
    return try {
        if (body != null) {
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { it.write(body) }
        }
        val code = connection.responseCode
        if (code !in 200..299) {
            throw RuntimeException("HTTP $code")
        }
        connection.inputStream.bufferedReader().use { it.readText() }
    } finally {
        connection.disconnect()
    }
}
