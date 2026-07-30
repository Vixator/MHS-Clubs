package com.precon.mhsclubs.screens.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.precon.mhsclubs.models.Event
import com.precon.mhsclubs.ui.FigmaCard
import com.precon.mhsclubs.ui.FigmaPill
import com.precon.mhsclubs.ui.FigmaScreen
import com.precon.mhsclubs.ui.FigmaTitle
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Event list screen displaying upcoming and past events.
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
        Spacer(Modifier.height(48.dp))
        FigmaTitle("Events", compact = true)
        if (events.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${events.size} scheduled",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(20.dp))
        if (events.isEmpty()) {
            Text(
                text = "No events yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 40.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Club meetings and events will appear here once they are scheduled.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(events, key = { it.id }) { event ->
                    EventCard(event = event, onClick = { onEventClick(event.id) })
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
    FigmaCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = formatDate(event.startTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FigmaPill(
                text = formatTime(event.startTime, event.endTime),
                background = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                borderColor = null
            )
        }
        if (event.description.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        event.location?.takeIf { it.isNotBlank() }?.let { location ->
            Spacer(Modifier.height(14.dp))
            FigmaPill(location, icon = Icons.Default.LocationOn)
        }
    }
}

/**
 * Formats an Instant as a date string.
 */
fun formatDate(instant: Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val month = localDateTime.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    return "$month ${localDateTime.dayOfMonth}, ${localDateTime.year}"
}

/**
 * Formats start and end times as a time range string.
 */
fun formatTime(startTime: Instant, endTime: Instant?): String {
    val startTimeStr = startTime.clockLabel()
    if (endTime != null) return "$startTimeStr – ${endTime.clockLabel()}"
    return startTimeStr
}

private fun Instant.clockLabel(): String =
    toLocalDateTime(TimeZone.currentSystemDefault()).let {
        "${(it.hour + 11) % 12 + 1}:${it.minute.toString().padStart(2, '0')} ${if (it.hour < 12) "AM" else "PM"}"
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
