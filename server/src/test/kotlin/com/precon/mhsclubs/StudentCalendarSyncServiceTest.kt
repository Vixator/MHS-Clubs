package com.precon.mhsclubs

import com.precon.mhsclubs.services.*
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class StudentCalendarSyncServiceTest {
    private class CalendarDouble : StudentCalendarApi {
        var inserts = 0; var updates = 0; var deletes = 0
        override fun insert(accessToken: String, event: ClubMeetingSchedule) = "google-$inserts".also { inserts++ }
        override fun update(accessToken: String, eventId: String, event: ClubMeetingSchedule) { updates++ }
        override fun delete(accessToken: String, eventId: String) { deletes++ }
    }
    private val noRefresh = GoogleTokenRefresher { error("refresh should not be called") }

    @Test fun `join then schedule change then leave creates updates and cleans mapping`() {
        val store = InMemoryStudentCalendarSyncStore()
        val calendar = CalendarDouble()
        store.saveToken(StoredCalendarToken("student", "access", "refresh", Instant.parse("2030-01-01T00:00:00Z"), setOf(CALENDAR_EVENTS_SCOPE), true))
        store.schedules["club"] = ClubMeetingSchedule("club", "Robotics", start = "2027-09-06T15:30:00-05:00", end = "2027-09-06T16:30:00-05:00", recurrence = listOf("RRULE:FREQ=WEEKLY;BYDAY=MO"))
        val service = StudentCalendarSyncService(store, calendar, noRefresh, { Instant.parse("2026-01-01T00:00:00Z") })

        service.syncClubJoin("student", "club")
        service.syncClubScheduleChange("club") // no membership yet: should do nothing
        store.members["club"] = mutableListOf("student")
        service.syncClubScheduleChange("club")
        service.syncClubLeave("student", "club")

        assertEquals(1, calendar.inserts)
        assertEquals(1, calendar.updates)
        assertEquals(1, calendar.deletes)
        assertEquals(emptyList(), store.mappingsFor("student", "club"))
    }

    @Test fun `missing consent is a silent no-op`() {
        val store = InMemoryStudentCalendarSyncStore(); val calendar = CalendarDouble()
        store.saveToken(StoredCalendarToken("student", "access", null, null, emptySet(), true))
        store.schedules["club"] = ClubMeetingSchedule("club", "Art", start = "2027-09-06T15:30:00Z", end = "2027-09-06T16:30:00Z")
        StudentCalendarSyncService(store, calendar, noRefresh).syncClubJoin("student", "club")
        assertEquals(0, calendar.inserts)
    }
}
