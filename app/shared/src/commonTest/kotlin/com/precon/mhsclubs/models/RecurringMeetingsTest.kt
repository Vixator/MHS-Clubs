package com.precon.mhsclubs.models

import com.precon.mhsclubs.model.Club
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

class RecurringMeetingsTest {
    @Test
    fun `derived occurrence uses the stable meeting key for its calendar date`() {
        val club = Club(
            id = "robotics", sheetSourceId = "1", name = "Robotics", code = "ROBOT",
            meetingDay = "Monday", meetingTime = "3:30 PM - 5:00 PM", meetingLocation = "Room 204"
        )

        val events = recurringMeetings(clubs = listOf(club), from = LocalDate(2026, 7, 27), days = 0, timeZone = TimeZone.of("America/Chicago"))

        assertEquals(listOf("meeting_robotics_2026-07-27"), events.map { it.id })
        assertTrue(events.single().isScheduledMeeting)
        assertEquals("Room 204", events.single().location)
    }
}
