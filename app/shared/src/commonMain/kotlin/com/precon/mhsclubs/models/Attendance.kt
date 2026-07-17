package com.precon.mhsclubs.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a user's attendance record for a specific event.
 *
 * Mirrors the `attendance` table in the SQLDelight schema.
 * Each row is uniquely identified by the (event_id, user_id) pair,
 * ensuring one attendance record per user per event.
 */
@Serializable
data class Attendance(
    val id: String,
    val eventId: String,
    val userId: String,
    val status: AttendanceStatus,
    val recordedAt: Instant,
)

/**
 * The attendance status of a user at a specific event.
 *
 * Maps to the `status` column in the `attendance` table.
 */
sealed class AttendanceStatus(val value: String) {
    object Present : AttendanceStatus("present")
    object Absent : AttendanceStatus("absent")
    object Late : AttendanceStatus("late")

    companion object {
        private val BY_VALUE = listOf(Present, Absent, Late).associateBy(AttendanceStatus::value)

        fun fromValue(value: String): AttendanceStatus =
            BY_VALUE[value] ?: throw IllegalArgumentException("Unknown AttendanceStatus: $value")
    }

    override fun toString(): String = value
}
