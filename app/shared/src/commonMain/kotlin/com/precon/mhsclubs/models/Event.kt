package com.precon.mhsclubs.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a club event/meeting.
 *
 * Mirrors the `events` table in the SQLDelight schema.
 * Events are always associated with a single club and track
 * scheduling, location, and Google Calendar sync status.
 */
@Serializable
data class Event(
    val id: String,
    val clubId: String,
    val title: String,
    val description: String,
    val location: String?,
    val startTime: Instant,
    val endTime: Instant?,
    val googleCalendarSynced: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
