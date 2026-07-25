package com.precon.mhsclubs.services

import com.precon.mhsclubs.environment
import com.precon.mhsclubs.resolveEnvironmentPath
import com.google.auth.oauth2.GoogleCredentials
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.FileInputStream
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

/**
 * Imports Google Calendar events into the server-owned NocoDB events table.
 * The service account is the only Google identity used; students and teachers never grant
 * the app Calendar permissions. Each club calendar must be shared with that service account.
 */
class GoogleCalendarSyncService(
    private val nocoDb: NocoDbClient,
    private val http: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(),
    private val credentialsPath: String = environment("GOOGLE_CALENDAR_SERVICE_ACCOUNT")
        ?: environment("FIREBASE_SERVICE_ACCOUNT").orEmpty()
) {
    private val gson = com.google.gson.Gson()

    fun isConfigured(): Boolean = credentialsPath.isNotBlank() && resolveEnvironmentPath(credentialsPath).isFile

    /** Syncs the supplied clubs (or every club when [clubIds] is null). */
    fun sync(clubIds: Set<String>? = null): CalendarSyncResult {
        check(isConfigured()) { "Google Calendar is not configured (set GOOGLE_CALENDAR_SERVICE_ACCOUNT)" }
        val clubs = records(nocoDb.listRecords("clubs"))
            .filter { clubIds == null || string(it, "Id", "id") in clubIds }
            .mapNotNull { club ->
                val id = string(club, "Id", "id") ?: return@mapNotNull null
                val calendarId = string(club, "Calendar", "calendar")?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                ClubCalendar(id, calendarId)
            }
        val existing = records(nocoDb.listRecords("events")).associateBy { string(it, "google_event_id") }
        var created = 0
        var updated = 0
        var ignored = 0
        clubs.forEach { club ->
            googleEvents(club.calendarId).forEach { googleEvent ->
                val googleId = string(googleEvent, "id") ?: return@forEach
                val start = eventTime(googleEvent.getAsJsonObject("start")) ?: return@forEach
                val end = eventTime(googleEvent.getAsJsonObject("end"))
                val record = JsonObject().apply {
                    addProperty("club_id", club.clubId)
                    addProperty("google_event_id", googleId)
                    addProperty("title", string(googleEvent, "summary") ?: "Club event")
                    addProperty("description", string(googleEvent, "description") ?: "")
                    addProperty("location", string(googleEvent, "location") ?: "")
                    addProperty("start_time", start)
                    if (end != null) addProperty("end_time", end)
                }
                val old = existing[googleId]
                if (old == null) {
                    nocoDb.createRecord("events", gson.toJson(record))
                    created++
                } else {
                    val oldId = string(old, "Id", "id") ?: return@forEach
                    nocoDb.updateRecord("events", oldId, gson.toJson(record))
                    updated++
                }
            }
        }
        return CalendarSyncResult(clubs.size, created, updated, ignored)
    }

    private fun googleEvents(calendarId: String): List<JsonObject> {
        val result = mutableListOf<JsonObject>()
        var pageToken: String? = null
        do {
            val query = buildString {
                append("singleEvents=true&showDeleted=false&maxResults=2500")
                pageToken?.let { append("&pageToken=").append(encode(it)) }
            }
            val response = http.send(
                HttpRequest.newBuilder(URI.create("https://www.googleapis.com/calendar/v3/calendars/${encode(calendarId)}/events?$query"))
                    .header("Authorization", "Bearer ${accessToken()}")
                    .timeout(Duration.ofSeconds(20))
                    .GET().build(),
                HttpResponse.BodyHandlers.ofString()
            )
            if (response.statusCode() !in 200..299) throw NocoDbException("Google Calendar request failed with HTTP ${response.statusCode()}")
            val root = JsonParser.parseString(response.body()).asJsonObject
            root.getAsJsonArray("items")?.forEach { if (it.isJsonObject) result += it.asJsonObject }
            pageToken = string(root, "nextPageToken")
        } while (pageToken != null)
        return result
    }

    private fun accessToken(): String = FileInputStream(resolveEnvironmentPath(credentialsPath)).use { input ->
        GoogleCredentials.fromStream(input)
            .createScoped(listOf("https://www.googleapis.com/auth/calendar.readonly"))
            .refreshAccessToken().tokenValue
    }

    private fun records(body: String): List<JsonObject> {
        val root = JsonParser.parseString(body).asJsonObject
        val array: JsonArray = root.getAsJsonArray("list") ?: root.getAsJsonArray("data") ?: JsonArray()
        return array.mapNotNull { it.takeIf { value -> value.isJsonObject }?.asJsonObject }
    }

    private fun eventTime(value: JsonObject?): String? = value?.let { string(it, "dateTime", "date") }
    private fun string(value: JsonObject, vararg names: String): String? = names.firstNotNullOfOrNull { name ->
        value.get(name)?.takeIf { !it.isJsonNull }?.asString
    }
    private fun encode(value: String) = URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20")
}

data class CalendarSyncResult(val calendars: Int, val created: Int, val updated: Int, val ignored: Int)
private data class ClubCalendar(val clubId: String, val calendarId: String)
