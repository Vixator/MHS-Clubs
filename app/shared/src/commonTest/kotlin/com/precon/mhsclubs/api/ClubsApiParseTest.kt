package com.precon.mhsclubs.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [ClubsApi.parseEvents], the parsing that turns the server's
 * membership-scoped event payload into the list shown on the app/website
 * calendar. These verify the two product behaviors without any network:
 *  - events for joined clubs are surfaced, and
 *  - a deleted event (absent from the payload) does not appear.
 */
class ClubsApiParseTest {
    private fun api() = ClubsApi(baseUrl = "http://localhost", tokenProvider = { null })

    @Test
    fun `parses events from a NocoDB list envelope`() {
        val body = """
            {"success":true,"data":{"list":[
              {"Id":"e1","club_id":"c1","title":"Meeting","description":"Weekly",
               "location":"Room 1","start_time":"2024-03-15T15:30:00Z","end_time":"2024-03-15T17:00:00Z",
               "created_at":"2024-01-01T00:00:00Z","updated_at":"2024-01-01T00:00:00Z"}
            ]}}
        """.trimIndent()
        val events = api().parseEvents(body)
        assertEquals(1, events.size)
        val event = events.first()
        assertEquals("e1", event.id)
        assertEquals("c1", event.clubId)
        assertEquals("Meeting", event.title)
        assertEquals("Room 1", event.location)
    }

    @Test
    fun `deleted event absent from payload does not appear`() {
        // After a server-side delete, e1 is gone from the payload; only e2 remains.
        val body = """
            {"success":true,"data":{"list":[
              {"Id":"e2","club_id":"c1","title":"Tournament","description":"",
               "start_time":"2024-03-20T10:00:00Z","created_at":"2024-01-01T00:00:00Z","updated_at":"2024-01-01T00:00:00Z"}
            ]}}
        """.trimIndent()
        val events = api().parseEvents(body)
        assertEquals(1, events.size)
        assertTrue(events.none { it.id == "e1" })
        assertEquals("e2", events.first().id)
    }

    @Test
    fun `returns empty list when the user is in no clubs`() {
        val body = """{"success":true,"data":{"list":[]}}"""
        assertEquals(emptyList(), api().parseEvents(body))
    }

    @Test
    fun `returns empty list on malformed response`() {
        assertEquals(emptyList(), api().parseEvents("not json"))
    }
}
