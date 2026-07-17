package com.precon.mhsclubs.services

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/** Direct REST client so calls are authenticated with the student's OAuth token. */
class GoogleStudentCalendarClient(
    private val http: HttpClient = HttpClient.newHttpClient(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) : StudentCalendarApi {
    override fun insert(accessToken: String, event: ClubMeetingSchedule): String {
        val response = request("POST", "https://www.googleapis.com/calendar/v3/calendars/primary/events", accessToken, eventBody(event))
        return json.parseToJsonElement(response).jsonObject["id"]?.jsonPrimitive?.content
            ?: error("Calendar insert did not return an event id")
    }

    override fun update(accessToken: String, eventId: String, event: ClubMeetingSchedule) {
        request("PUT", "https://www.googleapis.com/calendar/v3/calendars/primary/events/$eventId", accessToken, eventBody(event))
    }

    override fun delete(accessToken: String, eventId: String) {
        request("DELETE", "https://www.googleapis.com/calendar/v3/calendars/primary/events/$eventId", accessToken, null)
    }

    private fun eventBody(event: ClubMeetingSchedule) = buildJsonObject {
        put("summary", event.title); put("description", event.description)
        event.location?.let { put("location", it) }
        put("start", buildJsonObject { put("dateTime", event.start) })
        put("end", buildJsonObject { put("dateTime", event.end) })
        if (event.recurrence.isNotEmpty()) putJsonArray("recurrence") { event.recurrence.forEach { add(it) } }
    }.toString()

    private fun request(method: String, url: String, token: String, body: String?): String {
        val builder = HttpRequest.newBuilder(URI.create(url)).header("Authorization", "Bearer $token")
        val request = when (method) {
            "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString(requireNotNull(body))).build()
            "PUT" -> builder.PUT(HttpRequest.BodyPublishers.ofString(requireNotNull(body))).build()
            else -> builder.DELETE().build()
        }
        val response = http.send(request, HttpResponse.BodyHandlers.ofString())
        check(response.statusCode() in 200..299) { "Google Calendar returned ${response.statusCode()}: ${response.body()}" }
        return response.body()
    }
}

class GoogleOAuthTokenRefresher(
    private val clientId: String = requireEnv("GOOGLE_OAUTH_CLIENT_ID"),
    private val clientSecret: String = requireEnv("GOOGLE_OAUTH_CLIENT_SECRET"),
    private val http: HttpClient = HttpClient.newHttpClient(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) : GoogleTokenRefresher {
    override fun refresh(refreshToken: String): RefreshedGoogleToken {
        val form = mapOf("client_id" to clientId, "client_secret" to clientSecret, "refresh_token" to refreshToken, "grant_type" to "refresh_token")
            .entries.joinToString("&") { "${it.key}=${URLEncoder.encode(it.value, StandardCharsets.UTF_8)}" }
        val request = HttpRequest.newBuilder(URI.create("https://oauth2.googleapis.com/token"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(form)).build()
        val response = http.send(request, HttpResponse.BodyHandlers.ofString())
        check(response.statusCode() in 200..299) { "Google token refresh returned ${response.statusCode()}" }
        val body = json.parseToJsonElement(response.body()).jsonObject
        return RefreshedGoogleToken(body.getValue("access_token").jsonPrimitive.content, Instant.now().plusSeconds(body.getValue("expires_in").jsonPrimitive.long), body["refresh_token"]?.jsonPrimitive?.content)
    }
    private companion object { fun requireEnv(name: String) = System.getenv(name) ?: error("$name is required for Google Calendar token refresh") }
}
