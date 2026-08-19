package com.precon.mhsclubs.screens.clubs

import com.precon.mhsclubs.model.Club
import kotlin.test.Test
import kotlin.test.assertEquals

class ClubSearchTest {
    private val clubs = listOf(
        Club(id = "robotics", sheetSourceId = "1", name = "Robotics Club", description = "Build robots", code = "ROBOT", category = "STEM"),
        Club(id = "chess", sheetSourceId = "2", name = "Chess Club", description = "Play chess", code = "CHESS", category = "Games"),
    )

    @Test
    fun `fuzzy search finds a club after a small spelling error`() {
        assertEquals(listOf("robotics"), clubs.fuzzyMatch("robtics").map { it.id })
    }

    @Test
    fun `fuzzy search includes club codes`() {
        assertEquals(listOf("chess"), clubs.fuzzyMatch("ches").map { it.id })
    }
}
