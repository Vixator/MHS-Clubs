package com.precon.mhsclubs.services

import com.precon.mhsclubs.environment
import com.precon.mhsclubs.auth.UserIdentity
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap
import com.google.gson.JsonObject
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive

/** Server-only NocoDB v2 Records API client. API tokens never leave Ktor. */
class NocoDbClient(
    private val baseUrl: String = environment("NOCODB_BASE_URL").orEmpty(),
    private val apiToken: String = environment("NOCODB_API_TOKEN").orEmpty(),
    private val http: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()
) {
    data class MembershipJoinResult(val record: String, val created: Boolean)

    private data class CachedRecordList(val body: String, val fetchedAt: Instant)

    private val recordListCache = ConcurrentHashMap<String, CachedRecordList>()
    private val recordListCacheTtl = Duration.ofSeconds(30)
    private val tables = mapOf(
        "clubs" to "NOCODB_CLUBS_TABLE",
        "users" to "NOCODB_USERS_TABLE",
        "memberships" to "NOCODB_MEMBERSHIPS_TABLE",
        "events" to "NOCODB_EVENTS_TABLE",
        "rsvps" to "NOCODB_RSVPS_TABLE",
        "attendance" to "NOCODB_ATTENDANCE_TABLE",
        "announcements" to "NOCODB_ANNOUNCEMENTS_TABLE"
    )

    fun isConfigured(): Boolean = baseUrl.isNotBlank() && apiToken.isNotBlank() &&
        tables.values.all { environment(it)?.isNotBlank() == true }

    fun listRecords(resource: String, limit: Int = 100, where: String? = null): String =
        listRecordsPage(resource, limit, 0, where)

    /** Returns every matching record instead of silently stopping at NocoDB's page limit. */
    fun listAllRecords(resource: String, where: String? = null): String {
        val all = JsonArray()
        var offset = 0
        while (true) {
            val page = records(listRecordsPage(resource, limit = 100, offset = offset, where = where))
            page.forEach(all::add)
            if (page.size < 100) break
            offset += 100
        }
        return JsonObject().apply { add("list", all) }.toString()
    }

    /** The directory intentionally excludes archived clubs. */
    fun listActiveClubs(): String {
        val active = records(listAllRecords("clubs")).filter { it.boolean("is_active") != false }
        return JsonObject().apply {
            add("list", JsonArray().also { list -> active.forEach(list::add) })
        }.toString()
    }

    private fun listRecordsPage(resource: String, limit: Int, offset: Int, where: String?): String {
        require(limit in 1..100) { "limit must be between 1 and 100" }
        require(offset >= 0) { "offset must not be negative" }
        val cacheKey = "$resource|$limit|$offset|${where.orEmpty()}"
        val now = Instant.now()
        recordListCache[cacheKey]
            ?.takeIf { it.fetchedAt.plus(recordListCacheTtl).isAfter(now) }
            ?.let { return it.body }
        val query = buildString {
            append("limit=$limit")
            if (offset > 0) append("&offset=$offset")
            where?.takeIf { it.isNotBlank() }?.let { append("&where=").append(encode(it)) }
        }
        return execute(request(recordsUri(tableFor(resource), query)).GET().build()).also { body ->
            recordListCache[cacheKey] = CachedRecordList(body, now)
        }
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
            .build()).also { invalidateRecordLists(resource) }

    fun updateRecord(resource: String, id: String, jsonBody: String): String =
        execute(request(recordsUri(tableFor(resource)))
            .header("Content-Type", "application/json")
            .method("PATCH", HttpRequest.BodyPublishers.ofString(withRecordId(jsonBody, id)))
            .build()).also { invalidateRecordLists(resource) }

    fun deleteRecord(resource: String, id: String): String =
        execute(request(recordsUri(tableFor(resource)))
            .header("Content-Type", "application/json")
            .method("DELETE", HttpRequest.BodyPublishers.ofString(recordIdList(id)))
            .build()).also { invalidateRecordLists(resource) }

    /** Checks the active membership record that grants a student administration of one club. */
    fun isClubAdmin(firebaseUid: String, clubId: String): Boolean = records(
        listAllRecords("memberships", where = "(firebase_uid,eq,$firebaseUid)~and(club_id,eq,$clubId)~and(status,eq,active)")
    ).any { it.boolean("is_club_admin") == true }

    fun activeClubIds(firebaseUid: String): Set<String> = records(
        listAllRecords("memberships", where = "(firebase_uid,eq,$firebaseUid)~and(status,eq,active)")
    ).mapNotNull { it.string("club_id", "clubId") }.toSet()

    fun membershipBelongsTo(membershipId: String, firebaseUid: String): Boolean = records(
        listAllRecords("memberships", where = "(Id,eq,$membershipId)~and(firebase_uid,eq,$firebaseUid)")
    ).isNotEmpty()

    /** Revokes every active duplicate membership for the selected club, not just one row. */
    fun revokeMembershipsForClub(firebaseUid: String, membershipId: String): Int? {
        val selected = records(
            listAllRecords("memberships", where = "(Id,eq,$membershipId)~and(firebase_uid,eq,$firebaseUid)")
        ).singleOrNull() ?: return null
        val clubId = selected.string("club_id", "clubId") ?: return null
        val activeIds = records(
            listAllRecords("memberships", where = "(firebase_uid,eq,$firebaseUid)~and(club_id,eq,$clubId)~and(status,eq,active)")
        ).mapNotNull { it.string("Id", "id") }
        activeIds.forEach { id ->
            updateRecord("memberships", id, """{"status":"revoked","is_club_admin":false}""")
        }
        return activeIds.size
    }

    fun isActiveMember(firebaseUid: String, clubId: String): Boolean = records(
        listAllRecords("memberships", where = "(firebase_uid,eq,$firebaseUid)~and(club_id,eq,$clubId)~and(status,eq,active)")
    ).isNotEmpty()

    fun activeMemberIds(clubId: String): Set<String> = records(
        listAllRecords("memberships", where = "(club_id,eq,$clubId)~and(status,eq,active)")
    ).mapNotNull { it.string("firebase_uid", "userId") }.toSet()

    fun activeMemberCount(clubId: String): Int = activeMemberIds(clubId).size

    fun clubExists(clubId: String): Boolean = clubById(clubId) != null

    fun clubIsActive(clubId: String): Boolean = clubById(clubId)
        ?.let { it.boolean("is_active") != false }
        ?: false

    /** Creates an active membership, or restores the student's earlier membership record. */
    fun joinMembership(firebaseUid: String, clubId: String): MembershipJoinResult {
        val existing = records(
            listAllRecords("memberships", where = "(firebase_uid,eq,$firebaseUid)~and(club_id,eq,$clubId)")
        ).firstOrNull()
        if (existing == null) {
            return MembershipJoinResult(
                createRecord(
                    "memberships",
                    """{"firebase_uid":"$firebaseUid","club_id":"$clubId","status":"active","role":"member","is_club_admin":false}"""
                ),
                created = true
            )
        }

        val membershipId = existing.string("Id", "id")
            ?: throw NocoDbException("Existing membership is missing its record ID")
        val isActive = existing.string("status")?.equals("active", ignoreCase = true) == true
        val hasRole = !existing.string("role").isNullOrBlank()
        if (isActive && hasRole) return MembershipJoinResult(existing.toString(), created = false)
        updateRecord("memberships", membershipId, """{"status":"active","role":"member","is_club_admin":false}""")
        return MembershipJoinResult(
            recordById("memberships", membershipId)
                ?: throw NocoDbException("Updated membership could not be read back"),
            created = false
        )
    }

    /** Adviser authorization is based strictly on the clubs table's Contact email. */
    fun canAdvise(identity: UserIdentity, clubId: String): Boolean = clubById(clubId)
        ?.string("Contact", "contact", "advisor_email", "advisorEmail")
        ?.trim()
        ?.equals(identity.email.trim(), ignoreCase = true) == true

    fun clubForEventKey(eventId: String): String? = scheduledMeetingClubId(eventId) ?: records(listRecords("events", where = "(Id,eq,$eventId)"))
        .singleOrNull()
        ?.string("club_id", "clubId")

    fun eventBelongsToClub(eventId: String, clubId: String): Boolean = clubForEventKey(eventId) == clubId

    fun eventStart(eventId: String): Instant? {
        val scheduled = scheduledMeetingClubId(eventId)
        if (scheduled != null) {
            val date = scheduledDate(eventId) ?: return null
            val meetingTime = clubById(scheduled)?.string("Meeting Time", "meeting_time", "meetingTime") ?: return null
            val match = Regex("(\\d{1,2}):(\\d{2})\\s*([AaPp][Mm])").find(meetingTime) ?: return null
            val hour12 = match.groupValues[1].toIntOrNull() ?: return null
            val minute = match.groupValues[2].toIntOrNull() ?: return null
            val hour = (hour12 % 12) + if (match.groupValues[3].equals("pm", true)) 12 else 0
            return ZonedDateTime.of(date, LocalTime.of(hour, minute), schoolZone).toInstant()
        }
        return records(listRecords("events", where = "(Id,eq,$eventId)"))
            .singleOrNull()
            ?.string("start_time", "startTime")
            ?.let { runCatching { Instant.parse(it) }.getOrNull() }
    }

    fun findRsvp(firebaseUid: String, eventId: String): String? = records(
        listAllRecords("rsvps", where = "(firebase_uid,eq,$firebaseUid)~and(event_id,eq,$eventId)")
    ).firstOrNull()?.string("Id", "id")

    /** Returns roster records with their saved attendance status, without exposing unrelated users. */
    fun attendanceRoster(clubId: String, eventId: String): String {
        val memberIds = activeMemberIds(clubId)
        val users = records(listAllRecords("users")).associateBy { it.string("firebase_uid", "firebaseUid") }
        val attendance = records(listAllRecords("attendance", where = "(event_id,eq,$eventId)")).associateBy { it.string("user_id", "firebase_uid", "userId") }
        val list = JsonArray()
        memberIds.forEach { uid ->
            val user = users[uid]
            val row = JsonObject().apply {
                addProperty("userId", uid)
                addProperty("displayName", user?.string("display_name", "displayName", "name") ?: uid)
                addProperty("email", user?.string("email") ?: "")
                attendance[uid]?.string("status")?.let { addProperty("status", it) }
            }
            list.add(row)
        }
        return JsonObject().apply { add("list", list) }.toString()
    }

    fun upsertAttendance(clubId: String, eventId: String, userId: String, status: String) {
        val existing = records(listAllRecords("attendance", where = "(event_id,eq,$eventId)~and(user_id,eq,$userId)"))
            .singleOrNull()?.string("Id", "id")
        val body = """{"club_id":"$clubId","event_id":"$eventId","user_id":"$userId","status":"$status"}"""
        if (existing == null) createRecord("attendance", body) else updateRecord("attendance", existing, """{"status":"$status"}""")
    }

    /** Keeps the student-facing data boundary on the server, not in the Android app. */
    fun recordsForClubs(resource: String, clubIds: Set<String>): String {
        if (clubIds.isEmpty()) return "{\"list\":[]}"
        val all = records(listAllRecords(resource))
        val selected = all.filter {
            it.string("club_id", "clubId") in clubIds &&
                (resource != "announcements" || it.boolean("is_active") != false)
        }
        return "{\"list\":[${selected.joinToString(",")}] }"
    }

    fun findClubIdByCalendar(calendarId: String): String? = records(listAllRecords("clubs"))
        .firstOrNull { it.string("Calendar", "calendar") == calendarId }
        ?.string("Id", "id")

    fun findClubIdByName(clubName: String): String? = records(listAllRecords("clubs"))
        .singleOrNull { it.string("name")?.trim()?.equals(clubName.trim(), ignoreCase = true) == true }
        ?.string("Id", "id")

    private fun clubById(clubId: String): JsonObject? = records(listRecords("clubs", where = "(Id,eq,$clubId)")).singleOrNull()

    private fun scheduledClubId(eventId: String): String? = Regex("^meeting_(.+)_\\d{4}-\\d{2}-\\d{2}$")
        .matchEntire(eventId)?.groupValues?.get(1)

    private fun scheduledDate(eventId: String): LocalDate? = Regex("_(\\d{4}-\\d{2}-\\d{2})$")
        .find(eventId)?.groupValues?.get(1)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    /** Rejects fabricated recurring-event IDs that do not fall on the club's published meeting day. */
    private fun scheduledMeetingClubId(eventId: String): String? {
        val clubId = scheduledClubId(eventId) ?: return null
        val date = scheduledDate(eventId) ?: return null
        val club = clubById(clubId) ?: return null
        val meetingDays = club.string("Meeting Days", "meeting_day", "meetingDay")
            .orEmpty()
            .split(',', ';', '&', '/')
            .map { it.trim().lowercase().take(3) }
            .toSet()
        return clubId.takeIf { date.dayOfWeek.name.lowercase().take(3) in meetingDays }
    }

    private val schoolZone: ZoneId = ZoneId.of("America/Chicago")

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

    private fun withRecordId(body: String, id: String): String = JsonParser.parseString(requireJson(body)).asJsonObject
        .apply { add("Id", recordIdValue(id)) }
        .toString()

    private fun recordIdList(id: String): String = JsonArray().apply { add(recordIdValue(id)) }.toString()

    private fun recordIdValue(id: String) = id.toLongOrNull()?.let(::JsonPrimitive) ?: JsonPrimitive(id)

    private fun invalidateRecordLists(resource: String) {
        recordListCache.keys.removeIf { it.startsWith("$resource|") }
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

    private fun JsonObject.boolean(name: String): Boolean? = get(name)
        ?.takeIf { !it.isJsonNull }
        ?.asBoolean
}

class NocoDbException(message: String) : RuntimeException(message)
