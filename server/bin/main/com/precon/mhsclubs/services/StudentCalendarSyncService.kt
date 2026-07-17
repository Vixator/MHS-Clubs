package com.precon.mhsclubs.services

import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/** The least-privilege scope required for the per-student calendar feature. */
const val CALENDAR_EVENTS_SCOPE = "https://www.googleapis.com/auth/calendar.events"

data class StoredCalendarToken(
    val userId: String,
    val accessToken: String,
    val refreshToken: String?,
    val expiresAt: Instant?,
    val grantedScopes: Set<String>,
    val syncEnabled: Boolean
)

data class RefreshedGoogleToken(
    val accessToken: String,
    val expiresAt: Instant,
    val refreshToken: String? = null
)

data class ClubMeetingSchedule(
    val clubId: String,
    val title: String,
    val description: String = "",
    val location: String? = null,
    /** RFC3339 timestamp. */ val start: String,
    /** RFC3339 timestamp. */ val end: String,
    /** Calendar RRULE values, for example RRULE:FREQ=WEEKLY;BYDAY=MO. */
    val recurrence: List<String> = emptyList()
)

data class CalendarEventMapping(val userId: String, val clubId: String, val googleEventId: String)

/**
 * Persistence boundary. Production implementations must encrypt token columns before writing
 * them. Keeping this separate makes it impossible for the sync logic to accidentally persist
 * an access token in a membership or application log.
 */
interface StudentCalendarSyncStore {
    fun tokenFor(userId: String): StoredCalendarToken?
    fun saveToken(token: StoredCalendarToken)
    fun scheduleFor(clubId: String): ClubMeetingSchedule?
    fun activeStudentIdsFor(clubId: String): List<String>
    fun mappingsFor(userId: String, clubId: String): List<CalendarEventMapping>
    fun saveMapping(mapping: CalendarEventMapping)
    fun removeMapping(mapping: CalendarEventMapping)
}

interface StudentCalendarApi {
    fun insert(accessToken: String, event: ClubMeetingSchedule): String
    fun update(accessToken: String, eventId: String, event: ClubMeetingSchedule)
    fun delete(accessToken: String, eventId: String)
}

fun interface GoogleTokenRefresher {
    fun refresh(refreshToken: String): RefreshedGoogleToken
}

/**
 * Synchronizes against the student's `primary` calendar, never the shared/service calendar.
 * Every public method is failure-isolated: club membership changes must remain successful if
 * consent was declined, a token has been revoked, or Google is temporarily unavailable.
 */
class StudentCalendarSyncService(
    private val store: StudentCalendarSyncStore,
    private val calendar: StudentCalendarApi,
    private val tokenRefresher: GoogleTokenRefresher,
    private val now: () -> Instant = { Instant.now() }
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun syncClubJoin(userId: String, clubId: String) = safely("join", userId, clubId) {
        val token = usableToken(userId) ?: return@safely
        val schedule = store.scheduleFor(clubId) ?: return@safely
        val mappings = store.mappingsFor(userId, clubId)
        if (mappings.isEmpty()) {
            val eventId = calendar.insert(token.accessToken, schedule)
            store.saveMapping(CalendarEventMapping(userId, clubId, eventId))
        } else {
            mappings.forEach { calendar.update(token.accessToken, it.googleEventId, schedule) }
        }
    }

    fun syncClubLeave(userId: String, clubId: String) = safely("leave", userId, clubId) {
        // Deletion still uses a disabled token: opting out must clean up existing events.
        val token = usableToken(userId, requireEnabled = false) ?: return@safely
        store.mappingsFor(userId, clubId).forEach { mapping ->
            calendar.delete(token.accessToken, mapping.googleEventId)
            store.removeMapping(mapping)
        }
    }

    fun syncClubScheduleChange(clubId: String) {
        store.activeStudentIdsFor(clubId).forEach { userId -> syncClubJoin(userId, clubId) }
    }

    private fun usableToken(userId: String, requireEnabled: Boolean = true): StoredCalendarToken? {
        val token = store.tokenFor(userId) ?: return null
        if (requireEnabled && (!token.syncEnabled || CALENDAR_EVENTS_SCOPE !in token.grantedScopes)) return null
        if (token.expiresAt == null || token.expiresAt.isAfter(now().plusSeconds(60))) return token
        val refreshToken = token.refreshToken ?: return null
        val refreshed = tokenRefresher.refresh(refreshToken)
        return token.copy(
            accessToken = refreshed.accessToken,
            refreshToken = refreshed.refreshToken ?: refreshToken,
            expiresAt = refreshed.expiresAt
        ).also(store::saveToken)
    }

    private inline fun safely(action: String, userId: String, clubId: String, block: () -> Unit) {
        try {
            block()
        } catch (exception: Exception) {
            log.warn("Calendar sync {} failed for user {} and club {}; membership change is retained", action, userId, clubId, exception)
        }
    }
}

/** Small in-memory implementation for local development and service tests; not for production. */
class InMemoryStudentCalendarSyncStore : StudentCalendarSyncStore {
    private val tokens = ConcurrentHashMap<String, StoredCalendarToken>()
    val schedules = ConcurrentHashMap<String, ClubMeetingSchedule>()
    val members = ConcurrentHashMap<String, MutableList<String>>()
    private val mappings = ConcurrentHashMap.newKeySet<CalendarEventMapping>()
    override fun tokenFor(userId: String) = tokens[userId]
    override fun saveToken(token: StoredCalendarToken) { tokens[token.userId] = token }
    override fun scheduleFor(clubId: String) = schedules[clubId]
    override fun activeStudentIdsFor(clubId: String) = members[clubId]?.toList().orEmpty()
    override fun mappingsFor(userId: String, clubId: String) = mappings.filter { it.userId == userId && it.clubId == clubId }
    override fun saveMapping(mapping: CalendarEventMapping) { mappings += mapping }
    override fun removeMapping(mapping: CalendarEventMapping) { mappings -= mapping }
}
