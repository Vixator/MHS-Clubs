package com.precon.mhsclubs.screens.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.precon.mhsclubs.models.Event
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
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
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Events",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        if (events.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No events found")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Event title
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Event date and time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Date",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formatDate(event.startTime),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Event time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Time",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formatTime(event.startTime, event.endTime),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Event location
            if (event.location != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = event.location,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Event description
            if (event.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
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
    val startTimeStr = "${startLocal.hour}:${startLocal.minute.toString().padStart(2, '0')}"
    
    if (endTime != null) {
        val endLocal = endTime.toLocalDateTime(TimeZone.currentSystemDefault())
        val endTimeStr = "${endLocal.hour}:${endLocal.minute.toString().padStart(2, '0')}"
        return "$startTimeStr - $endTimeStr"
    }
    
    return startTimeStr
}

@Preview
@Composable
fun EventListScreenPreview() {
    MaterialTheme {
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
}
