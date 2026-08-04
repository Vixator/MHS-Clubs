package com.precon.mhsclubs.model

import kotlin.time.Clock
import kotlinx.serialization.Serializable

/**
 * Club entity stored in the NocoDB `clubs` table.
 *
 * Represents a school club imported into NocoDB from the annual club data file.
 *
 * All timestamp fields are ISO-8601 strings.
 */
@Serializable
data class Club(
    val id: String = newId(),
    val sheetSourceId: String,
    val name: String,
    val description: String = "",
    val logoUrl: String? = null,
    val category: String = "General",
    val meetingDay: String? = null,
    val meetingTime: String? = null,
    val meetingLocation: String? = null,
    /** Display name from the clubs table's Advisor field. */
    val advisorName: String? = null,
    /** Adviser email from the clubs table's Contact field. */
    val contactEmail: String? = null,
    /** Google Calendar ID for this club's events */
    val calendarId: String? = null,
    /** Computed from active membership records by the authenticated API. */
    val memberCount: Int? = null,
    val code: String,
    val isActive: Boolean = true,
    val createdAt: String = Clock.System.now().toString(),
    val updatedAt: String = Clock.System.now().toString()
) {
    /**
     * Returns true if this club is archived (hidden from the directory).
     */
    val isArchived: Boolean get() = !isActive

    companion object {
        /**
     * Constructs a Club from an imported source record.
         */
        fun fromImport(
            sheetSourceId: String,
            name: String,
            description: String = "",
            logoUrl: String? = null,
            category: String = "General",
            code: String
        ): Club {
            return Club(
                sheetSourceId = sheetSourceId,
                name = name,
                description = description,
                logoUrl = logoUrl,
                category = category,
                code = code
            )
        }
    }
}
