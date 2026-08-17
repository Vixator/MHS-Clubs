package com.precon.mhsclubs.api

import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.js.json

/** WebAssembly (wasmJs) transport for [httpGetJson] using the browser `fetch` API. */
actual suspend fun httpGetJson(url: String, bearerToken: String?): String =
    jsRequest(url, "GET", null, authHeaders(bearerToken))

actual suspend fun httpPostJson(url: String, body: String, bearerToken: String?): String =
    jsRequest(url, "POST", body, jsonHeaders(bearerToken))

actual suspend fun httpDeleteJson(url: String, bearerToken: String?): String =
    jsRequest(url, "DELETE", null, authHeaders(bearerToken))

actual fun defaultApiBaseUrl(): String {
    val origin: String? = js("((typeof window !== 'undefined' && window.location && window.location.origin) || null)")
    return if (origin.isNullOrEmpty()) "http://localhost:8080" else origin
}

private fun authHeaders(bearerToken: String?): dynamic {
    val headers = json("Accept" to "application/json")
    if (bearerToken != null) headers["Authorization"] = "Bearer $bearerToken"
    return headers
}

private fun jsonHeaders(bearerToken: String?): dynamic {
    val headers = json("Accept" to "application/json", "Content-Type" to "application/json")
    if (bearerToken != null) headers["Authorization"] = "Bearer $bearerToken"
    return headers
}

private suspend fun jsRequest(url: String, method: String, body: String?, headers: dynamic): String {
    val response = jsFetch(url, method, body, headers).await<dynamic>()
    val status: Int = response.status as Int
    if (status !in 200..299) throw RuntimeException("HTTP $status")
    return (response.text() as Promise<String>).await()
}

private fun jsFetch(url: String, method: String, body: String?, headers: dynamic): Promise<dynamic> =
    js("(function(u, m, b, h){ var opts = { method: m, headers: h }; if (b != null) opts.body = b; return fetch(u, opts); })")(url, method, body, headers)
