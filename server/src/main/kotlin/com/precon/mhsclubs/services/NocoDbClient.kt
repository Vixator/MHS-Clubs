package com.precon.mhsclubs.services

import com.precon.mhsclubs.environment
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import com.google.gson.JsonObject
import com.google.gson.JsonParser

/** Server-only NocoDB v2 Records API client. API tokens never leave Ktor. */
class NocoDbClient(
    private val baseUrl: String = environment("NOCODB_BASE_URL").orEmpty(),
    private val apiToken: String = environment("NOCODB_API_TOKEN").orEmpty(),
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

    fun listRecords(resource: String, limit: Int = 100, where: String? = null): String {
        require(limit in 1..100) { "limit must be between 1 and 100" }
        val query = buildString {
            append("limit=$limit")
            where?.takeIf { it.isNotBlank() }?.let { append("&where=").append(encode(it)) }
        }
        return execute(request(recordsUri(tableFor(resource), query)).GET().build())
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

    fun activeClubIds(firebaseUid: String): Set<String> = records(
        listRecords("memberships", where = "(firebase_uid,eq,$firebaseUid)~and(status,eq,active)")
    ).mapNotNull { it.string("club_id", "clubId") }.toSet()

    fun membershipBelongsTo(membershipId: String, firebaseUid: String): Boolean = records(
        listRecords("memberships", where = "(Id,eq,$membershipId)~and(firebase_uid,eq,$firebaseUid)")
    ).isNotEmpty()

    /** Keeps the student-facing data boundary on the server, not in the Android app. */
    fun recordsForClubs(resource: String, clubIds: Set<String>): String {
        if (clubIds.isEmpty()) return "{\"list\":[]}"
        val all = records(listRecords(resource))
        val selected = all.filter { it.string("club_id", "clubId") in clubIds }
        return "{\"list\":[${selected.joinToString(",")}] }"
    }

    fun findClubIdByCalendar(calendarId: String): String? = records(listRecords("clubs"))
        .firstOrNull { it.string("Calendar", "calendar") == calendarId }
        ?.string("Id", "id")

    fun findClubIdByName(clubName: String): String? = records(listRecords("clubs"))
        .singleOrNull { it.string("name")?.trim()?.equals(clubName.trim(), ignoreCase = true) == true }
        ?.string("Id", "id")

    private fun tableFor(resource: String): String {
        check(isConfigured()) { "NocoDB is not configured" }
        val variable = tables[resource] ?: throw NocoDbException("Unknown API resource '$resource'")
        return environment(variable)?.takeIf { it.isNotBlank() }
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

    private fun records(body: String): List<JsonObject> {
        val root = JsonParser.parseString(body).asJsonObject
        val list = root.getAsJsonArray("list") ?: root.getAsJsonArray("data") ?: return emptyList()
        return list.mapNotNull { it.takeIf { value -> value.isJsonObject }?.asJsonObject }
    }

    private fun JsonObject.string(vararg names: String): String? = names.firstNotNullOfOrNull { name ->
        get(name)?.takeIf { !it.isJsonNull }?.asString
    }
}

class NocoDbException(message: String) : RuntimeException(message)
