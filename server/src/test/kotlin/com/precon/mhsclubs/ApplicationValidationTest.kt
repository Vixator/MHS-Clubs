package com.precon.mhsclubs

import com.google.gson.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApplicationValidationTest {
    @Test
    fun `attendance updates accept each documented status and reject malformed ids`() {
        assertEquals(
            listOf("student_1" to "present", "student_2" to "late", "student_3" to "absent"),
            parseAttendanceUpdates(
                """{"records":[{"userId":"student_1","status":"present"},{"userId":"student_2","status":"late"},{"userId":"student_3","status":"absent"}]}"""
            )
        )
        assertNull(parseAttendanceUpdates("""{"records":[{"userId":"student,eq,other","status":"present"}]}"""))
        assertNull(parseAttendanceUpdates("""{"records":[{"userId":"student_1","status":"unknown"}]}"""))
    }

    @Test
    fun `club managed payload is parsed before the server assigns its club id`() {
        val payload = """{"title":"Robotics update","content":"Meet after school."}""".withClubId("club_1")
            ?: error("Expected valid payload")

        assertEquals("club_1", JsonParser.parseString(payload).asJsonObject["club_id"].asString)
        assertNull("""{"club_id":"other"}""".withClubId("club_1"))
        assertNull("not json".withClubId("club_1"))
    }

    @Test
    fun `route identifiers reject filter syntax`() {
        assertTrue("club_123-abc".isIdentifier())
        assertFalse("club)~or(Id,eq,other".isIdentifier())
    }
}
