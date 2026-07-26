package com.precon.mhsclubs.data

import com.precon.mhsclubs.models.Event
import com.precon.mhsclubs.model.Club
import com.precon.mhsclubs.models.Membership
import com.precon.mhsclubs.models.MembershipRole
import com.precon.mhsclubs.models.MembershipStatus
import com.precon.mhsclubs.screens.announcements.Announcement
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
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
    override suspend fun loadClubs(firebaseIdToken: String): List<Club> = records("/api/clubs", firebaseIdToken).mapNotNull { item ->
        runCatching {
            Club(
                id = item.id(), sheetSourceId = item.stringOrDefault("sheet_source_id", item.id()),
                name = item.string("name"), description = item.stringOrDefault("description", ""),
                logoUrl = item.nullableString("logo_url", "logoUrl"), category = item.stringOrDefault("category", "General"),
                meetingDay = item.nullableString("meeting_day", "meetingDay"), meetingTime = item.nullableString("meeting_time", "meetingTime"),
                meetingLocation = item.nullableString("meeting_location", "meetingLocation"), code = item.string("code")
            )
        }.getOrNull()
    }

    override suspend fun loadMemberships(firebaseIdToken: String): List<Membership> =
        records("/api/my/memberships", firebaseIdToken).mapNotNull { it.toMembershipOrNull() }

    override suspend fun joinClub(firebaseIdToken: String, clubId: String): Membership {
        val response = client.post("${baseUrl.trimEnd('/')}/api/memberships/join") {
            header(HttpHeaders.Authorization, "Bearer $firebaseIdToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("{\"clubId\":\"$clubId\"}")
        }
        if (!response.status.isSuccess()) error("Server request failed with HTTP ${response.status}")
        return json.parseToJsonElement(response.bodyAsText()).jsonObject["data"]!!.jsonObject.toMembershipOrNull()
            ?: error("Server returned an invalid membership")
    }

    override suspend fun leaveClub(firebaseIdToken: String, membershipId: String) {
        val response = client.put("${baseUrl.trimEnd('/')}/api/memberships/$membershipId/leave") {
            header(HttpHeaders.Authorization, "Bearer $firebaseIdToken")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("{}")
        }
        if (!response.status.isSuccess()) error("Server request failed with HTTP ${response.status}")
    }

    override suspend fun loadEvents(firebaseIdToken: String): List<Event> = records("/api/my/events", firebaseIdToken).mapNotNull { item ->
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

    override suspend fun loadAnnouncements(firebaseIdToken: String): List<Announcement> = records("/api/my/announcements", firebaseIdToken).mapNotNull { item ->
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

    private suspend fun records(path: String, token: String): List<JsonObject> = runCatching {
        if (token.isBlank()) return emptyList()
        val response = client.get("${baseUrl.trimEnd('/')}$path") {
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
    private fun JsonObject.toMembershipOrNull(): Membership? = runCatching {
        Membership(
            id = id(), userId = string("firebase_uid", "userId"), clubId = string("club_id", "clubId"),
            role = MembershipRole.fromValue(stringOrDefault("role", "member")),
            status = MembershipStatus.fromValue(stringOrDefault("status", "active")),
            joinedAt = nullableString("joined_at", "joinedAt")?.let(Instant::parse), leaderGrantedAt = null, revokedAt = null
        )
    }.getOrNull()

    private companion object {
        val client = HttpClient(Js)
        val json = Json { ignoreUnknownKeys = true }
    }
}
