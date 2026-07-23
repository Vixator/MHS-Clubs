package com.precon.mhsclubs.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a club event/meeting.
 *
 * Represents an event stored in NocoDB and displayed in the built-in calendar.
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
    val createdAt: Instant,
    val updatedAt: Instant,
)
