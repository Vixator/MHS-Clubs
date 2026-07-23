package com.precon.mhsclubs.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.precon.mhsclubs.models.Event
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Calendar screen displaying events in a monthly view.
 *
 * Shows a calendar grid with days and events, allowing users to see
 * upcoming club meetings and events.
 *
 * @param events List of events to display on the calendar
 * @param currentDate The currently selected date
 * @param onDateSelected Callback when a date is selected
 * @param onEventClick Callback when an event is clicked
 */
@Composable
fun CalendarScreen(
    events: List<Event> = emptyList(),
    currentDate: LocalDate = LocalDate(2024, Month.MARCH, 15),
    onDateSelected: (LocalDate) -> Unit = {},
    onEventClick: (String) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Calendar header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { /* Previous month */ }) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous Month"
                )
            }

            Text(
                text = "${currentDate.month.name} ${currentDate.year}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { /* Next month */ }) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next Month"
                )
            }
        }

        // Calendar grid
        CalendarGrid(
            events = events,
            currentDate = currentDate,
            onDateSelected = onDateSelected,
            onEventClick = onEventClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Upcoming events list
        Text(
            text = "Upcoming Events",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (events.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "No Events",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("No upcoming events")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(events) { event ->
                    CalendarEventCard(
                        event = event,
                        onClick = { onEventClick(event.id) }
                    )
                }
            }
        }
    }
}

/**
 * Simple calendar grid showing days of the month.
 * This is a simplified version - a full calendar would be more complex.
 */
@Composable
fun CalendarGrid(
    events: List<Event>,
    currentDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onEventClick: (String) -> Unit
) {
    // Group events by date
    val eventsByDate = events.groupBy { event ->
        event.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    // For simplicity, just show the current week
    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Day headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Date rows (simplified - would normally calculate actual dates)
        // For preview, just show a few dates
        val sampleDates = listOf(1, 2, 3, 4, 5, 6, 7)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            sampleDates.forEach { date ->
                val hasEvent = eventsByDate.containsKey(LocalDate(currentDate.year, currentDate.month, date))
                DayCell(
                    date = date,
                    hasEvent = hasEvent,
                    onClick = { onDateSelected(LocalDate(currentDate.year, currentDate.month, date)) }
                )
            }
        }
    }
}

/**
 * A single day cell in the calendar grid.
 */
@Composable
fun DayCell(
    date: Int,
    hasEvent: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.toString(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(4.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        if (hasEvent) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape)
            )
        }
    }
}

/**
 * Card displaying an event in the calendar view.
 */
@Composable
fun CalendarEventCard(
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
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = formatDate(event.startTime),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = formatTime(event.startTime, event.endTime),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
fun CalendarScreenPreview() {
    MaterialTheme {
        CalendarScreen(
            events = listOf(
                Event(
                    id = "1",
                    clubId = "1",
                    title = "Robotics Meeting",
                    description = "Weekly team meeting",
                    location = "Room 204",
                    startTime = Instant.parse("2024-03-15T15:30:00Z"),
                    endTime = Instant.parse("2024-03-15T17:00:00Z"),
                    createdAt = Instant.parse("2024-01-01T00:00:00Z"),
                    updatedAt = Instant.parse("2024-01-01T00:00:00Z")
                ),
                Event(
                    id = "2",
                    clubId = "2",
                    title = "Chess Tournament",
                    description = "School chess tournament",
                    location = "Library",
                    startTime = Instant.parse("2024-03-20T10:00:00Z"),
                    endTime = Instant.parse("2024-03-20T15:00:00Z"),
                    createdAt = Instant.parse("2024-01-01T00:00:00Z"),
                    updatedAt = Instant.parse("2024-01-01T00:00:00Z")
                )
            )
        )
    }
}
