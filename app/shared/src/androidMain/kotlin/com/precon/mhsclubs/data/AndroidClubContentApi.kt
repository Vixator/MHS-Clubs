package com.precon.mhsclubs.data

import com.precon.mhsclubs.models.Event
import com.precon.mhsclubs.model.Club
import com.precon.mhsclubs.models.Membership
import com.precon.mhsclubs.models.MembershipRole
import com.precon.mhsclubs.models.MembershipStatus
import com.precon.mhsclubs.models.AttendanceStatus
import com.precon.mhsclubs.models.Attendance
import com.precon.mhsclubs.screens.announcements.Announcement
import com.precon.mhsclubs.screens.rsvp.Rsvp
import com.precon.mhsclubs.screens.rsvp.RsvpStatus
import kotlinx.datetime.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

/** Android-only HTTP adapter. The NocoDB token never reaches this client. */
class AndroidClubContentApi(private val baseUrl: String) : ClubContentApi {
    override suspend fun loadClubs(firebaseIdToken: String): List<Club> = withContext(Dispatchers.IO) { records("/api/clubs", firebaseIdToken) }.mapNotNull { item ->
        runCatching {
            Club(
                id = item.id(), sheetSourceId = item.optString("sheet_source_id", item.id()),
                name = item.string("Club Name", "name", "club_name", "clubName"),
                description = item.optString("Description", item.optString("description", "")),
                logoUrl = item.nullableString("logo_url", "logoUrl"), category = item.optString("category", "General"),
                meetingDay = item.nullableString("Meeting Days", "meeting_day", "meetingDay"),
                meetingTime = item.nullableString("Meeting Time", "meeting_time", "meetingTime"),
                meetingLocation = item.nullableString("Meeting Location", "meeting_location", "meetingLocation"),
                advisorName = item.nullableString("Advisor", "advisor", "advisor_name", "advisorName"),
                contactEmail = item.nullableString("Contact", "contact", "advisor_email", "advisorEmail"),
                calendarId = item.nullableString("calendar_id", "calendarId", "Calendar", "calendar"),
                memberCount = item.nullableInt("member_count", "memberCount"),
                code = item.optString("code", ""), isActive = item.optBoolean("is_active", true)
            )
        }.getOrNull()
    }

    override suspend fun loadMemberships(firebaseIdToken: String): List<Membership> = withContext(Dispatchers.IO) { records("/api/my/memberships", firebaseIdToken) }.mapNotNull { item ->
        item.toMembershipOrNull()
    }

    override suspend fun joinClub(firebaseIdToken: String, clubId: String): Membership = withContext(Dispatchers.IO) {
        JSONObject(request("POST", "/api/memberships/join", firebaseIdToken, "{\"clubId\":\"$clubId\"}"))
            .getJSONObject("data").toMembershipOrNull()
            ?: error("Server returned an invalid membership")
    }

    override suspend fun leaveClub(firebaseIdToken: String, clubId: String) {
        withContext(Dispatchers.IO) {
            request("PUT", "/api/memberships/leave", firebaseIdToken, JSONObject().put("clubId", clubId).toString())
        }
    }

    override suspend fun loadEvents(firebaseIdToken: String): List<Event> = withContext(Dispatchers.IO) { records("/api/my/events", firebaseIdToken) }.mapNotNull { item ->
        runCatching {
            Event(
                id = item.id(),
                clubId = item.string("club_id", "clubId"),
                title = item.optString("title", "Club event"),
                description = item.optString("description", ""),
                location = item.nullableString("location"),
                startTime = Instant.parse(item.string("start_time", "startTime")),
                endTime = item.nullableString("end_time", "endTime")?.let(Instant::parse),
                createdAt = item.nullableString("created_at", "createdAt", "CreatedAt")?.let(Instant::parse)
                    ?: item.nullableString("updated_at", "updatedAt", "UpdatedAt")?.let(Instant::parse)
                    ?: Instant.parse(item.string("start_time", "startTime")),
                updatedAt = item.nullableString("updated_at", "updatedAt", "UpdatedAt")?.let(Instant::parse)
                    ?: Instant.parse(item.string("start_time", "startTime")),
                calendarId = item.nullableString("calendar_id", "calendarId")
            )
        }.getOrNull()
    }

    override suspend fun loadAnnouncements(firebaseIdToken: String): List<Announcement> = withContext(Dispatchers.IO) { records("/api/my/announcements", firebaseIdToken) }.mapNotNull { item ->
        runCatching {
            val posted = item.nullableString("posted_at", "postedAt", "created_at", "createdAt", "CreatedAt", "updated_at", "updatedAt", "UpdatedAt")
                ?.let(Instant::parse) ?: return@runCatching null
            Announcement(
                id = item.id(), clubId = item.string("club_id", "clubId"),
                title = item.optString("title", "Club announcement"), content = item.optString("content", ""),
                authorId = item.optString("author_id", ""), authorName = item.optString("author_name", "Club staff"),
                isActive = item.optBoolean("is_active", true), postedAt = posted,
                updatedAt = item.nullableString("updated_at", "updatedAt", "UpdatedAt")?.let(Instant::parse) ?: posted
            )
        }.getOrNull()
    }

    override suspend fun loadRsvps(firebaseIdToken: String): List<Rsvp> = withContext(Dispatchers.IO) { records("/api/my/rsvps", firebaseIdToken) }.mapNotNull { item ->
        runCatching {
            val respondedAt = item.nullableString("responded_at", "respondedAt", "CreatedAt", "created_at")?.let(Instant::parse)
                ?: Instant.fromEpochMilliseconds(0)
            Rsvp(
                id = item.id(), eventId = item.string("event_id", "eventId"), userId = item.string("firebase_uid", "userId"),
                status = when (item.optString("status").lowercase()) {
                    "yes", "going" -> RsvpStatus.Going
                    "no", "not_going" -> RsvpStatus.NotGoing
                    else -> RsvpStatus.Maybe
                }, respondedAt = respondedAt
            )
        }.getOrNull()
    }

    override suspend fun respondToRsvp(firebaseIdToken: String, eventId: String, status: String) {
        withContext(Dispatchers.IO) {
            request("POST", "/api/rsvps/respond", firebaseIdToken, JSONObject().put("eventId", eventId).put("status", status).toString())
        }
    }

    override suspend fun loadMemberCount(firebaseIdToken: String, clubId: String): Int = withContext(Dispatchers.IO) {
        val data = JSONObject(request("GET", "/api/clubs/$clubId/member-count", firebaseIdToken)).getJSONObject("data")
        data.optInt("memberCount", 0)
    }

    override suspend fun loadAttendance(firebaseIdToken: String): List<Attendance> = withContext(Dispatchers.IO) {
        records("/api/my/attendance", firebaseIdToken).mapNotNull { item ->
            runCatching {
                Attendance(
                    id = item.id(),
                    eventId = item.string("event_id", "eventId"),
                    userId = item.string("firebase_uid", "user_id", "userId"),
                    status = AttendanceStatus.fromValue(item.string("status")),
                    recordedAt = item.nullableString("recorded_at", "recordedAt", "CreatedAt", "created_at")
                        ?.let(Instant::parse) ?: Instant.fromEpochMilliseconds(0)
                )
            }.getOrNull()
        }
    }

    override suspend fun loadAttendanceRoster(firebaseIdToken: String, clubId: String, eventId: String): List<ClubMember> = withContext(Dispatchers.IO) {
        records("/api/clubs/$clubId/attendance/$eventId", firebaseIdToken).mapNotNull { item ->
            runCatching {
                ClubMember(
                    userId = item.string("userId"), displayName = item.optString("displayName", item.string("userId")),
                    email = item.optString("email", ""),
                    status = item.nullableString("status")?.let(AttendanceStatus::fromValue)
                )
            }.getOrNull()
        }
    }

    override suspend fun saveAttendance(firebaseIdToken: String, clubId: String, eventId: String, records: List<AttendanceUpdate>) {
        withContext(Dispatchers.IO) {
            val values = JSONArray().apply { records.forEach { put(JSONObject().put("userId", it.userId).put("status", it.status.value)) } }
            request("PUT", "/api/clubs/$clubId/attendance/$eventId", firebaseIdToken, JSONObject().put("records", values).toString())
        }
    }

    private fun records(path: String, token: String): List<JSONObject> {
        val response = request("GET", path, token)
        return try {
            val data = JSONObject(response).getJSONObject("data")
            val list: JSONArray = data.optJSONArray("list") ?: JSONArray()
            List(list.length()) { index -> list.getJSONObject(index) }
        } catch (exception: Exception) {
            Log.e(LOG_TAG, "Invalid response from $path: ${response.take(500)}", exception)
            throw exception
        }
    }

    private fun request(method: String, path: String, token: String, body: String? = null): String {
        val url = URL("${baseUrl.trimEnd('/')}$path")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
            body?.let {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
            connectTimeout = 10_000
            readTimeout = 30_000
        }
        body?.let { connection.outputStream.bufferedWriter().use { writer -> writer.write(it) } }
        if (connection.responseCode !in 200..299) {
            val response = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val message = "Server request failed with HTTP ${connection.responseCode}: $response"
            Log.e(LOG_TAG, "$method $path: $message")
            error(message)
        }
        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    private companion object {
        const val LOG_TAG = "MhsClubsApi"
    }

    private fun JSONObject.id() = string("Id", "id")
    private fun JSONObject.string(vararg names: String): String = names.firstNotNullOfOrNull { name ->
        opt(name)?.takeIf { it != JSONObject.NULL }?.toString()?.takeIf { it.isNotBlank() }
    } ?: error("Missing required field ${names.first()}")
    private fun JSONObject.nullableString(vararg names: String): String? = names.firstNotNullOfOrNull { name ->
        opt(name)?.takeIf { it != JSONObject.NULL }?.toString()?.takeIf { it.isNotBlank() }
    }
    private fun JSONObject.nullableInt(vararg names: String): Int? = names.firstNotNullOfOrNull { name ->
        opt(name)?.takeIf { it != JSONObject.NULL }?.toString()?.toIntOrNull()
    }

    override suspend fun syncCalendar(firebaseIdToken: String): Boolean = withContext(Dispatchers.IO) {
        try {
            request("POST", "/api/calendar/sync", firebaseIdToken)
            true
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Calendar sync failed", e)
            false
        }
    }

    private fun JSONObject.toMembershipOrNull(): Membership? = runCatching {
        Membership(
            id = id(), userId = string("firebase_uid", "userId"), clubId = string("club_id", "clubId"),
            role = MembershipRole.fromValue(nullableString("role") ?: "member"),
            status = MembershipStatus.fromValue(optString("status", "active")),
            joinedAt = nullableString("joined_at", "joinedAt")?.let(Instant::parse),
            leaderGrantedAt = null, revokedAt = null
        )
    }.getOrNull()
}
