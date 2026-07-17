package com.precon.mhsclubs.services

import com.precon.mhsclubs.models.Attendance
import com.precon.mhsclubs.models.AttendanceStatus
import com.precon.mhsclubs.models.Event
import com.precon.mhsclubs.models.User
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.slf4j.LoggerFactory
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.time.format.DateTimeFormatter

/**
 * Service for exporting data to CSV format.
 *
 * Provides functionality to export attendance records, event lists,
 * and membership data as CSV files for reporting and analysis.
 */
class CsvExportService {
    companion object {
        private val log = LoggerFactory.getLogger(CsvExportService::class.java)
    }

    /**
     * Exports attendance records for a specific event to CSV.
     *
     * @param event The event to export attendance for
     * @param attendances List of attendance records for the event
     * @param users Map of user IDs to user objects for name lookup
     * @param outputStream The output stream to write the CSV to
     * @return The number of records exported
     */
    fun exportEventAttendanceCsv(
        event: Event,
        attendances: List<Attendance>,
        users: Map<String, User>,
        outputStream: OutputStream
    ): Int {
        val writer = OutputStreamWriter(outputStream)
        
        try {
            // Write header
            writer.writeCsvLine(
                listOf(
                    "Event ID",
                    "Event Title",
                    "Event Date",
                    "User ID",
                    "Name",
                    "Email",
                    "Status",
                    "Recorded At"
                )
            )

            // Write data rows
            attendances.forEach { attendance ->
                val user = users[attendance.userId]
                writer.writeCsvLine(
                    listOf(
                        event.id,
                        event.title,
                        formatDate(event.startTime),
                        attendance.userId,
                        user?.displayName ?: "Unknown",
                        user?.email ?: "",
                        attendance.status.value,
                        formatDateTime(attendance.recordedAt)
                    )
                )
            }

            writer.flush()
            return attendances.size
        } catch (e: Exception) {
            log.error("Failed to export attendance CSV: ${e.message}", e)
            throw e
        } finally {
            writer.close()
        }
    }

    /**
     * Exports all attendance records for a club to CSV.
     *
     * @param clubId The ID of the club
     * @param events List of events for the club
     * @param attendances List of all attendance records for the club's events
     * @param users Map of user IDs to user objects
     * @param outputStream The output stream to write the CSV to
     * @return The number of records exported
     */
    fun exportClubAttendanceCsv(
        clubId: String,
        events: List<Event>,
        attendances: List<Attendance>,
        users: Map<String, User>,
        outputStream: OutputStream
    ): Int {
        val writer = OutputStreamWriter(outputStream)
        
        try {
            // Write header
            writer.writeCsvLine(
                listOf(
                    "Club ID",
                    "Event ID",
                    "Event Title",
                    "Event Date",
                    "User ID",
                    "Name",
                    "Email",
                    "Status",
                    "Recorded At"
                )
            )

            // Write data rows
            attendances.forEach { attendance ->
                val event = events.find { it.id == attendance.eventId }
                val user = users[attendance.userId]
                
                if (event != null) {
                    writer.writeCsvLine(
                        listOf(
                            clubId,
                            event.id,
                            event.title,
                            formatDate(event.startTime),
                            attendance.userId,
                            user?.displayName ?: "Unknown",
                            user?.email ?: "",
                            attendance.status.value,
                            formatDateTime(attendance.recordedAt)
                        )
                    )
                }
            }

            writer.flush()
            return attendances.size
        } catch (e: Exception) {
            log.error("Failed to export club attendance CSV: ${e.message}", e)
            throw e
        } finally {
            writer.close()
        }
    }

    /**
     * Exports a summary of all club memberships to CSV.
     *
     * @param clubId The ID of the club
     * @param clubName The name of the club
     * @param members List of member user IDs
     * @param users Map of user IDs to user objects
     * @param outputStream The output stream to write the CSV to
     * @return The number of records exported
     */
    fun exportMembershipCsv(
        clubId: String,
        clubName: String,
        members: List<String>,
        users: Map<String, User>,
        outputStream: OutputStream
    ): Int {
        val writer = OutputStreamWriter(outputStream)
        
        try {
            // Write header
            writer.writeCsvLine(
                listOf(
                    "Club ID",
                    "Club Name",
                    "User ID",
                    "Name",
                    "Email",
                    "Role"
                )
            )

            // Write data rows
            members.forEach { userId ->
                val user = users[userId]
                if (user != null) {
                    writer.writeCsvLine(
                        listOf(
                            clubId,
                            clubName,
                            user.id,
                            user.displayName,
                            user.email,
                            user.role.value
                        )
                    )
                }
            }

            writer.flush()
            return members.size
        } catch (e: Exception) {
            log.error("Failed to export membership CSV: ${e.message}", e)
            throw e
        } finally {
            writer.close()
        }
    }

    /**
     * Formats an Instant as a date string (YYYY-MM-DD).
     */
    private fun formatDate(instant: Instant): String {
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${localDateTime.year}-${localDateTime.monthNumber.toString().padStart(2, '0')}-${localDateTime.dayOfMonth.toString().padStart(2, '0')}"
    }

    /**
     * Formats an Instant as a date-time string (YYYY-MM-DD HH:MM:SS).
     */
    private fun formatDateTime(instant: Instant): String {
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${localDateTime.year}-${localDateTime.monthNumber.toString().padStart(2, '0')}-${localDateTime.dayOfMonth.toString().padStart(2, '0')} " +
               "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}:${localDateTime.second.toString().padStart(2, '0')}"
    }

    /**
     * Extension function to write a CSV line with proper escaping.
     */
    private fun OutputStreamWriter.writeCsvLine(values: List<String>) {
        val escapedValues = values.map { value ->
            // Escape quotes by doubling them
            val escaped = value.replace("\"", "\"\"")
            // Wrap in quotes if contains comma, newline, or quote
            if (escaped.contains(',') || escaped.contains('\n') || escaped.contains('"')) {
                "\"$escaped\""
            } else {
                escaped
            }
        }
        write(escapedValues.joinToString(",") + "\n")
    }
}
