package com.precon.mhsclubs.services

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

/** Server-only NocoDB v2 Records API client. API tokens never leave Ktor. */
class NocoDbClient(
    private val baseUrl: String = System.getenv("NOCODB_BASE_URL").orEmpty(),
    private val apiToken: String = System.getenv("NOCODB_API_TOKEN").orEmpty(),
    private val http: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()
) {
    private val tables = mapOf(
        "clubs" to "NOCODB_CLUBS_TABLE",
        "users" to "NOCODB_USERS_TABLE",
        "memberships" to "NOCODB_MEMBERSHIPS_TABLE",
        "events" to "NOCODB_EVENTS_TABLE",
        "rsvps" to "NOCODB_RSVPS_TABLE",
        "attendance" to "NOCODB_ATTENDANCE_TABLE",
        "announcements" to "NOCODB_ANNOUNCEMENTS_TABLE"
    )

    fun isConfigured(): Boolean = baseUrl.isNotBlank() && apiToken.isNotBlank()

    fun listRecords(resource: String, limit: Int = 100): String {
        require(limit in 1..100) { "limit must be between 1 and 100" }
        return execute(request(recordsUri(tableFor(resource), "limit=$limit")).GET().build())
    }

    fun recordById(resource: String, id: String): String? {
        val response = send(request(recordUri(tableFor(resource), id)).GET().build())
        return when (response.statusCode()) {
            404 -> null
            in 200..299 -> response.body()
            else -> throw NocoDbException("NocoDB request failed with HTTP ${response.statusCode()}")
        }
    }

    fun createRecord(resource: String, jsonBody: String): String =
        execute(request(recordsUri(tableFor(resource)))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requireJson(jsonBody)))
            .build())

    fun updateRecord(resource: String, id: String, jsonBody: String): String =
        execute(request(recordUri(tableFor(resource), id))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(requireJson(jsonBody)))
            .build())

    fun deleteRecord(resource: String, id: String): String =
        execute(request(recordUri(tableFor(resource), id)).DELETE().build())

    /** Checks the membership record that grants a student administration of one club. */
    fun isClubAdmin(firebaseUid: String, clubId: String): Boolean {
        val where = "where=(firebase_uid,eq,$firebaseUid)~and(club_id,eq,$clubId)&limit=1"
        val response = execute(request(recordsUri(tableFor("memberships"), where)).GET().build())
        return Regex("\\\"is_club_admin\\\"\\s*:\\s*true").containsMatchIn(response)
    }

    private fun tableFor(resource: String): String {
        check(isConfigured()) { "NocoDB is not configured" }
        val variable = tables[resource] ?: throw NocoDbException("Unknown API resource '$resource'")
        return System.getenv(variable)?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("NocoDB table '$resource' is not configured ($variable)")
    }

    private fun execute(request: HttpRequest): String {
        val response = send(request)
        if (response.statusCode() !in 200..299) {
            throw NocoDbException("NocoDB request failed with HTTP ${response.statusCode()}")
        }
        return response.body()
    }

    private fun send(request: HttpRequest): HttpResponse<String> = http.send(
        request,
        HttpResponse.BodyHandlers.ofString()
    )

    private fun request(uri: URI): HttpRequest.Builder = HttpRequest.newBuilder(uri)
        .header("xc-token", apiToken)
        .header("Accept", "application/json")
        .timeout(Duration.ofSeconds(15))

    private fun recordsUri(tableId: String, query: String = ""): URI =
        URI.create("${baseUrl.trimEnd('/')}/api/v2/tables/${encode(tableId)}/records${if (query.isBlank()) "" else "?$query"}")

    private fun recordUri(tableId: String, id: String): URI =
        URI.create("${baseUrl.trimEnd('/')}/api/v2/tables/${encode(tableId)}/records/${encode(id)}")

    private fun requireJson(body: String): String = body.trim().also {
        require(it.startsWith("{") && it.endsWith("}")) { "Request body must be a JSON object" }
    }

    private fun encode(value: String) = URLEncoder.encode(value, StandardCharsets.UTF_8)
}

class NocoDbException(message: String) : RuntimeException(message)
