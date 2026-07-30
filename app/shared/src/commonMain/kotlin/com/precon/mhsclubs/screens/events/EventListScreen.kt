package com.precon.mhsclubs.screens.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.precon.mhsclubs.models.Event
import com.precon.mhsclubs.ui.FigmaCard
import com.precon.mhsclubs.ui.FigmaDarkText
import com.precon.mhsclubs.ui.FigmaScreen
import com.precon.mhsclubs.ui.FigmaTan
import com.precon.mhsclubs.ui.FigmaText
import com.precon.mhsclubs.ui.FigmaTitle
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Event list screen displaying upcoming and past events.
 *
 * Shows a list of events with their date, time, and location.
 * Users can click on an event to view its details and RSVP.
 *
 * @param events List of events to display
 * @param onEventClick Callback when an event is clicked
 */
@Composable
fun EventListScreen(
    events: List<Event> = emptyList(),
    onEventClick: (String) -> Unit = {}
) {
    FigmaScreen(Modifier.fillMaxSize()) {
        Box(Modifier.height(42.dp))
        FigmaTitle("Events", compact = true)
        Box(Modifier.height(11.dp))
        if (events.isEmpty()) {
            Text(
                "No events found",
                color = FigmaText,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 20.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(events) { event ->
                    EventCard(
                        event = event,
                        onClick = { onEventClick(event.id) }
                    )
                }
            }
        }
    }
}

/**
 * Card displaying a single event.
 */
@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit
) {
    FigmaCard(
        modifier = Modifier.fillMaxWidth().height(106.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = event.title,
                color = FigmaDarkText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Date",
                    modifier = Modifier.size(16.dp),
                    tint = FigmaTan
                )
                Text(
                    text = formatDate(event.startTime),
                    color = FigmaDarkText,
                    fontSize = 14.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Time",
                    modifier = Modifier.size(16.dp),
                    tint = FigmaTan
                )
                Text(
                    text = formatTime(event.startTime, event.endTime),
                    color = FigmaDarkText,
                    fontSize = 14.sp
                )
            }

            if (event.location != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(16.dp),
                        tint = FigmaTan
                    )
                    Text(
                        text = event.location,
                        color = FigmaDarkText,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (event.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.description,
                    color = FigmaDarkText,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Formats an Instant as a date string.
 */
fun formatDate(instant: Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.month.name} ${localDateTime.dayOfMonth}, ${localDateTime.year}"
}

/**
 * Formats start and end times as a time range string.
 */
fun formatTime(startTime: Instant, endTime: Instant?): String {
    val startLocal = startTime.toLocalDateTime(TimeZone.currentSystemDefault())
    val startHour = (startLocal.hour + 11) % 12 + 1
    val startTimeStr = "$startHour:${startLocal.minute.toString().padStart(2, '0')} ${if (startLocal.hour < 12) "AM" else "PM"}"

    if (endTime != null) {
        val endLocal = endTime.toLocalDateTime(TimeZone.currentSystemDefault())
        val endHour = (endLocal.hour + 11) % 12 + 1
        val endTimeStr = "$endHour:${endLocal.minute.toString().padStart(2, '0')} ${if (endLocal.hour < 12) "AM" else "PM"}"
        return "$startTimeStr - $endTimeStr"
    }

    return startTimeStr
}

@Preview
@Composable
fun EventListScreenPreview() {
    EventListScreen(
        events = listOf(
            Event(
                id = "1",
                clubId = "1",
                title = "Robotics Competition",
                description = "Annual robotics competition at the state fair",
                location = "State Fair Grounds",
                startTime = Instant.parse("2024-03-15T09:00:00Z"),
                endTime = Instant.parse("2024-03-15T17:00:00Z"),
                createdAt = Instant.parse("2024-01-01T00:00:00Z"),
                updatedAt = Instant.parse("2024-01-01T00:00:00Z")
            ),
            Event(
                id = "2",
                clubId = "1",
                title = "Weekly Meeting",
                description = "Regular team meeting to work on projects",
                location = "Room 204",
                startTime = Instant.parse("2024-03-20T15:30:00Z"),
                endTime = Instant.parse("2024-03-20T17:00:00Z"),
                createdAt = Instant.parse("2024-01-01T00:00:00Z"),
                updatedAt = Instant.parse("2024-01-01T00:00:00Z")
            )
        )
    )
}
