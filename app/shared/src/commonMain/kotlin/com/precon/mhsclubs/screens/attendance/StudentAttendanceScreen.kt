package com.precon.mhsclubs.screens.attendance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.precon.mhsclubs.model.Club
import com.precon.mhsclubs.models.Attendance
import com.precon.mhsclubs.models.AttendanceStatus
import com.precon.mhsclubs.models.Event
import com.precon.mhsclubs.ui.FigmaBackLabel
import com.precon.mhsclubs.ui.FigmaCard
import com.precon.mhsclubs.ui.FigmaPill
import com.precon.mhsclubs.ui.FigmaScreen
import com.precon.mhsclubs.ui.FigmaTitle
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/** A student's read-only attendance history across every event in their active clubs. */
@Composable
fun StudentAttendanceScreen(
    events: List<Event>,
    clubs: List<Club>,
    attendance: List<Attendance>?,
    isLoading: Boolean,
    errorMessage: String? = null,
    onBackClick: () -> Unit
) {
    val clubNames = clubs.associate { it.id to it.name }
    val attendanceByEvent = attendance.orEmpty().associateBy { it.eventId }
    val now = Clock.System.now()
    FigmaScreen(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(12.dp))
        FigmaBackLabel("Account", onBackClick)
        FigmaTitle("My Attendance", compact = true)
        Spacer(Modifier.height(4.dp))
        Text(
            "Every scheduled meeting and club event in your clubs.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        when {
            isLoading -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            errorMessage != null -> Text(errorMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            events.isEmpty() -> Text(
                "Join a club to see its attendance here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events.sortedByDescending { it.startTime }, key = { it.id }) { event ->
                    StudentAttendanceRow(event, clubNames[event.clubId], attendanceByEvent[event.id], now)
                }
            }
        }
    }
}

@Composable
private fun StudentAttendanceRow(event: Event, clubName: String?, attendance: Attendance?, now: Instant) {
    val (label, background, contentColor) = attendanceBadge(attendance?.status, event.startTime > now)
    FigmaCard(Modifier.fillMaxWidth()) {
        Column {
            Text(event.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(
                listOfNotNull(clubName, if (event.isScheduledMeeting) "Scheduled meeting" else "Club event", event.startTime.dateLabel()).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(10.dp))
            FigmaPill(label, background = background, contentColor = contentColor, borderColor = null)
        }
    }
}

@Composable
private fun attendanceBadge(status: AttendanceStatus?, isUpcoming: Boolean): Triple<String, androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> =
    when (status) {
        AttendanceStatus.Present -> Triple("Present", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
        AttendanceStatus.Late -> Triple("Late", MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onTertiary)
        AttendanceStatus.Absent -> Triple("Absent", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
        null -> if (isUpcoming) {
            Triple("Upcoming", MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Triple("Not recorded", MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

private fun Instant.dateLabel(): String = toLocalDateTime(TimeZone.currentSystemDefault()).let {
    "${it.month.name.take(3).lowercase().replaceFirstChar { character -> character.uppercase() }} ${it.dayOfMonth}, ${it.year}"
}
