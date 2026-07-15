package com.precon.mhsclubs.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Club entity mapped to the `clubs` PostgreSQL table.
 *
 * Represents a school club whose base data is sourced (read-only) from a
 * Google Sheet via the Sheets API and mirrored into PostgreSQL for performance.
 * Meeting detail overrides are stored separately in [ClubOverride].
 *
 * All timestamp fields are ISO-8601 strings matching SQLDelight's TEXT columns.
 */
@Serializable
data class Club(
    val id: String = UUID.randomUUID().toString(),
    val sheetSourceId: String,
    val name: String,
    val description: String = "",
    val logoUrl: String? = null,
    val category: String = "General",
    val meetingDay: String? = null,
    val meetingTime: String? = null,
    val meetingLocation: String? = null,
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
         * Constructs a Club from Google Sheet source data.
         * The club is active by default and has no meeting overrides.
         */
        fun fromSheet(
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
