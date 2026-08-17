package com.precon.mhsclubs.api

import com.precon.mhsclubs.models.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Server API base URL. Override per deployment; defaults to the same origin
 * on the web (the Ktor API is reverse-proxied behind the site host) and to
 * localhost for local Android development.
 */
expect fun defaultApiBaseUrl(): String

/**
 * Performs an authenticated HTTP GET, returning the response body text or
 * throwing on a non-2xx status. Each platform supplies its transport.
 *
 * @param url Absolute request URL
 * @param bearerToken Firebase ID token, or null to send the request unauthenticated
 */
expect suspend fun httpGetJson(url: String, bearerToken: String?): String

/**
 * Performs an authenticated HTTP POST with a JSON body, returning the response
 * body text or throwing on a non-2xx status.
 */
expect suspend fun httpPostJson(url: String, body: String, bearerToken: String?): String

/**
 * Performs an authenticated HTTP DELETE, returning the response body text or
 * throwing on a non-2xx status.
 */
expect suspend fun httpDeleteJson(url: String, bearerToken: String?): String

/**
 * Kotlin Multiplatform client for the MHS Clubs Ktor API.
 *
 * Events are loaded from `GET /api/my/events`, which returns every event for
 * the clubs the signed-in user is a member of. Because the list is recomputed
 * on the server from current memberships and current event rows, it reflects
 * event deletions and club joins on the next [loadMyEvents] call — so calling
 * it again after a join or a delete is what keeps the app and website current.
 */
class ClubsApi(
    private val baseUrl: String = defaultApiBaseUrl(),
    private val tokenProvider: suspend () -> String?
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Loads the events for the clubs the current user belongs to.
     *
     * Returns an empty list when the user is in no clubs (e.g. before joining)
     * or when the request fails, so the UI degrades gracefully to "no events"
     * rather than crashing.
     */
    suspend fun loadMyEvents(): List<Event> = withContext(Dispatchers.IO) {
        val token = tokenProvider()
        if (token.isNullOrBlank()) return@withContext emptyList()
        val response = try {
            httpGetJson("$baseUrl/api/my/events", token)
        } catch (_: Exception) {
            return@withContext emptyList()
        }
        parseEvents(response)
    }

    /**
     * Joins the club with [clubId] by creating a membership for the signed-in
     * user. Returns true on success. Joining is what makes that club's events
     * appear on the next [loadMyEvents] call.
     */
    suspend fun joinClub(clubId: String): Boolean = withContext(Dispatchers.IO) {
        if (clubId.isBlank()) return@withContext false
        val token = tokenProvider() ?: return@withContext false
        val body = """{"clubId":"$clubId"}"""
        try {
            val response = httpPostJson("$baseUrl/api/memberships/join", body, token)
            val root = json.parseToJsonElement(response).jsonObject
            root["success"]?.jsonPrimitive?.booleanOrNull == true ||
                root["success"]?.jsonPrimitive?.contentOrNull == "true"
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Deletes an event by id (staff/club-admin only). Returns true on success.
     * After a successful delete the next [loadMyEvents] call no longer lists it.
     */
    suspend fun deleteEvent(eventId: String): Boolean = withContext(Dispatchers.IO) {
        if (eventId.isBlank()) return@withContext false
        val token = tokenProvider() ?: return@withContext false
        try {
            httpDeleteJson("$baseUrl/api/events/$eventId", token)
            true
        } catch (_: Exception) {
            false
        }
    }

    /** Parses the NocoDB list payload wrapped by the server's `{success,data}` envelope. */
    internal fun parseEvents(body: String): List<Event> {
        val root = runCatching { json.parseToJsonElement(body).jsonObject }.getOrNull() ?: return emptyList()
        val data = root["data"] ?: return emptyList()
        val list: JsonArray = when {
            data is JsonArray -> data
            data is JsonObject && data["list"] is JsonArray -> data["list"]!!.jsonArray
            else -> return emptyList()
        }
        return list.mapNotNull { element ->
            val obj = element.jsonObject
            val id = obj.string("Id") ?: obj.string("id") ?: return@mapNotNull null
            Event(
                id = id,
                clubId = obj.string("club_id") ?: obj.string("clubId") ?: "",
                title = obj.string("title") ?: "Untitled",
                description = obj.string("description") ?: "",
                location = obj.string("location"),
                startTime = obj.instant("start_time") ?: obj.instant("startTime")
                    ?: obj.instant("start") ?: Instant.DISTANT_PAST,
                endTime = obj.instant("end_time") ?: obj.instant("endTime"),
                createdAt = obj.instant("created_at") ?: obj.instant("createdAt") ?: Instant.DISTANT_PAST,
                updatedAt = obj.instant("updated_at") ?: obj.instant("updatedAt") ?: Instant.DISTANT_PAST
            )
        }
    }

    private fun JsonObject.string(key: String): String? =
        (this[key] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }

    private fun JsonObject.instant(key: String): Instant? =
        string(key)?.let { runCatching { Instant.parse(it) }.getOrNull() }

}
