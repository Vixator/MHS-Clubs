package com.precon.mhsclubs.data

import com.precon.mhsclubs.models.Event
import com.precon.mhsclubs.screens.announcements.Announcement
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.accept
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/** Browser-only HTTP adapter. The NocoDB token never reaches this client. */
class JsClubContentApi(private val baseUrl: String) : ClubContentApi {
    override suspend fun loadEvents(firebaseIdToken: String): List<Event> = records("events", firebaseIdToken).mapNotNull { item ->
        runCatching {
            Event(
                id = item.id(),
                clubId = item.string("club_id", "clubId"),
                title = item.stringOrDefault("title", "Club event"),
                description = item.stringOrDefault("description", ""),
                location = item.nullableString("location"),
                startTime = Instant.parse(item.string("start_time", "startTime")),
                endTime = item.nullableString("end_time", "endTime")?.let(Instant::parse),
                createdAt = item.nullableString("created_at", "createdAt", "CreatedAt")?.let(Instant::parse)
                    ?: item.nullableString("updated_at", "updatedAt", "UpdatedAt")?.let(Instant::parse)
                    ?: Instant.parse(item.string("start_time", "startTime")),
                updatedAt = item.nullableString("updated_at", "updatedAt", "UpdatedAt")?.let(Instant::parse)
                    ?: Instant.parse(item.string("start_time", "startTime"))
            )
        }.getOrNull()
    }

    override suspend fun loadAnnouncements(firebaseIdToken: String): List<Announcement> = records("announcements", firebaseIdToken).mapNotNull { item ->
        runCatching {
            val posted = item.nullableString("posted_at", "postedAt", "created_at", "createdAt", "CreatedAt", "updated_at", "updatedAt", "UpdatedAt")
                ?.let(Instant::parse) ?: return@runCatching null
            Announcement(
                id = item.id(), clubId = item.string("club_id", "clubId"),
                title = item.stringOrDefault("title", "Club announcement"), content = item.stringOrDefault("content", ""),
                authorId = item.stringOrDefault("author_id", ""), authorName = item.stringOrDefault("author_name", "Club staff"),
                isActive = item["is_active"]?.toString()?.toBooleanStrictOrNull() ?: true, postedAt = posted,
                updatedAt = item.nullableString("updated_at", "updatedAt", "UpdatedAt")?.let(Instant::parse) ?: posted
            )
        }.getOrNull()
    }

    private suspend fun records(resource: String, token: String): List<JsonObject> = runCatching {
        if (token.isBlank()) return emptyList()
        val response = client.get("${baseUrl.trimEnd('/')}/api/my/$resource") {
            header(HttpHeaders.Authorization, "Bearer $token")
            accept(ContentType.Application.Json)
        }
        if (!response.status.isSuccess()) return emptyList()
        val payload = json.parseToJsonElement(response.bodyAsText()).jsonObject
        payload["data"]?.jsonObject?.get("list")?.jsonArray.orEmpty().mapNotNull { it as? JsonObject }
    }.getOrElse { emptyList() }

    private fun JsonObject.id() = string("Id", "id")
    private fun JsonObject.string(vararg names: String): String = nullableString(*names)
        ?: error("Missing required field ${names.first()}")
    private fun JsonObject.stringOrDefault(name: String, default: String): String = nullableString(name) ?: default
    private fun JsonObject.nullableString(vararg names: String): String? = names.firstNotNullOfOrNull { name ->
        this[name]?.toString()?.trim('"')?.takeIf { it.isNotBlank() }
    }

    private companion object {
        val client = HttpClient(Js)
        val json = Json { ignoreUnknownKeys = true }
    }
}
