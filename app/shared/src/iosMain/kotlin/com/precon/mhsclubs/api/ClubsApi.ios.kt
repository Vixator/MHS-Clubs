package com.precon.mhsclubs.api

/**
 * iOS transport for the HTTP helpers.
 *
 * iOS is not yet wired to a networking engine (see README: iOS builds on
 * macOS after Firebase packages are added). Until a URLSession-backed
 * implementation is provided, requests fail fast with a clear message so the
 * rest of the multiplatform contract compiles.
 */
actual suspend fun httpGetJson(url: String, bearerToken: String?): String {
    throw UnsupportedOperationException("Networking is not yet implemented on iOS")
}

actual suspend fun httpPostJson(url: String, body: String, bearerToken: String?): String {
    throw UnsupportedOperationException("Networking is not yet implemented on iOS")
}

actual suspend fun httpDeleteJson(url: String, bearerToken: String?): String {
    throw UnsupportedOperationException("Networking is not yet implemented on iOS")
}

/** Default API base URL for iOS. Override per deployment once wired. */
actual fun defaultApiBaseUrl(): String = "http://localhost:8080"
