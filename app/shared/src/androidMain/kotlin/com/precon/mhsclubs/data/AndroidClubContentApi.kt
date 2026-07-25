package com.precon.mhsclubs.data

import com.precon.mhsclubs.models.Event
import com.precon.mhsclubs.screens.announcements.Announcement
import kotlinx.datetime.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Android-only HTTP adapter. The NocoDB token never reaches this client. */
class AndroidClubContentApi(private val baseUrl: String) : ClubContentApi {
    override suspend fun loadEvents(firebaseIdToken: String): List<Event> = withContext(Dispatchers.IO) { records("events", firebaseIdToken) }.mapNotNull { item ->
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
                    ?: Instant.parse(item.string("start_time", "startTime"))
            )
        }.getOrNull()
    }

    override suspend fun loadAnnouncements(firebaseIdToken: String): List<Announcement> = withContext(Dispatchers.IO) { records("announcements", firebaseIdToken) }.mapNotNull { item ->
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

    private fun records(resource: String, token: String): List<JSONObject> {
        val url = URL("${baseUrl.trimEnd('/')}/api/my/$resource")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
            connectTimeout = 10_000
            readTimeout = 30_000
        }
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        if (connection.responseCode !in 200..299) error("Server request failed with HTTP ${connection.responseCode}")
        val data = JSONObject(response).getJSONObject("data")
        val list: JSONArray = data.optJSONArray("list") ?: JSONArray()
        return List(list.length()) { index -> list.getJSONObject(index) }
    }

    private fun JSONObject.id() = string("Id", "id")
    private fun JSONObject.string(vararg names: String): String = names.firstNotNullOfOrNull { name ->
        opt(name)?.takeIf { it != JSONObject.NULL }?.toString()?.takeIf { it.isNotBlank() }
    } ?: error("Missing required field ${names.first()}")
    private fun JSONObject.nullableString(vararg names: String): String? = names.firstNotNullOfOrNull { name ->
        opt(name)?.takeIf { it != JSONObject.NULL }?.toString()?.takeIf { it.isNotBlank() }
    }
}
