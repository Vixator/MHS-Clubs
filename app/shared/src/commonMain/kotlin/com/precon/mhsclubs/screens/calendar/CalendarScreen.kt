package com.precon.mhsclubs.screens.calendar

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.precon.mhsclubs.models.Event
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/** A touch-first agenda that keeps one complete week in view at a time. */
@Composable
fun CalendarScreen(
    events: List<Event> = emptyList(),
    currentDate: LocalDate = Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds())
        .toLocalDateTime(TimeZone.currentSystemDefault()).date,
    onDateSelected: (LocalDate) -> Unit = {},
    onEventClick: (String) -> Unit = {}
) {
    var weekStart by remember(currentDate) { mutableStateOf(currentDate.startOfWeek()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { weekStart = weekStart.minus(DatePeriod(days = 7)) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous week")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = weekStart.weekRangeLabel(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Weekly agenda",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { weekStart = weekStart.plus(DatePeriod(days = 7)) }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next week")
            }
        }

        TextButton(
            onClick = { weekStart = currentDate.startOfWeek() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Today")
        }

        AnimatedContent(targetState = weekStart, label = "week change") { visibleWeekStart ->
            WeekAgenda(
                weekStart = visibleWeekStart,
                events = events,
                onDateSelected = onDateSelected,
                onEventClick = onEventClick
            )
        }
    }
}

@Composable
private fun WeekAgenda(
    weekStart: LocalDate,
    events: List<Event>,
    onDateSelected: (LocalDate) -> Unit,
    onEventClick: (String) -> Unit
) {
    val eventsByDate = remember(events) {
        events.groupBy { event -> event.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).date }
    }
    val days = remember(weekStart) { List(7) { weekStart.plus(DatePeriod(days = it)) } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(days, key = { it.toString() }) { date ->
            WeekDay(
                date = date,
                events = eventsByDate[date].orEmpty(),
                onDateSelected = { onDateSelected(date) },
                onEventClick = onEventClick
            )
        }
    }
}

@Composable
private fun WeekDay(
    date: LocalDate,
    events: List<Event>,
    onDateSelected: () -> Unit,
    onEventClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onDateSelected)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.size(48.dp)
            ) {
                androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                    Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            Column {
                Text(date.dayOfWeek.name.lowercase().replaceFirstChar { it.titlecase() }, style = MaterialTheme.typography.titleMedium)
                Text(date.month.name.lowercase().replaceFirstChar { it.titlecase() }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (events.isEmpty()) "Free" else "${events.size} event${if (events.size == 1) "" else "s"}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (events.isEmpty()) {
            Text(
                text = "No club events",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 60.dp, bottom = 12.dp)
            )
        } else {
            events.forEach { event ->
                CalendarEventRow(event = event, onClick = { onEventClick(event.id) })
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun CalendarEventRow(event: Event, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 60.dp, bottom = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(formatTime(event.startTime, event.endTime), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            event.location?.takeIf { it.isNotBlank() }?.let { location ->
                Text(location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

private fun LocalDate.startOfWeek(): LocalDate = minus(DatePeriod(days = (dayOfWeek.ordinal + 1) % 7))

private fun LocalDate.weekRangeLabel(): String {
    val weekEnd = plus(DatePeriod(days = 6))
    val startMonth = month.name.lowercase().replaceFirstChar { it.titlecase() }
    val endMonth = weekEnd.month.name.lowercase().replaceFirstChar { it.titlecase() }
    return if (month == weekEnd.month) "$startMonth $dayOfMonth–${weekEnd.dayOfMonth}, $year" else "$startMonth $dayOfMonth – $endMonth ${weekEnd.dayOfMonth}, ${weekEnd.year}"
}

fun formatTime(startTime: Instant, endTime: Instant?): String {
    val startLocal = startTime.toLocalDateTime(TimeZone.currentSystemDefault())
    val start = "${startLocal.hour}:${startLocal.minute.toString().padStart(2, '0')}"
    val end = endTime?.toLocalDateTime(TimeZone.currentSystemDefault())?.let { "${it.hour}:${it.minute.toString().padStart(2, '0')}" }
    return end?.let { "$start – $it" } ?: start
}

@Preview
@Composable
fun CalendarScreenPreview() {
    MaterialTheme {
        CalendarScreen(
            events = listOf(
                Event(
                    id = "1", clubId = "robotics", title = "Robotics Meeting", description = "Weekly team meeting", location = "Room 204",
                    startTime = Instant.parse("2026-07-27T15:30:00Z"), endTime = Instant.parse("2026-07-27T17:00:00Z"),
                    createdAt = Instant.parse("2026-07-01T00:00:00Z"), updatedAt = Instant.parse("2026-07-01T00:00:00Z")
                )
            )
        )
    }
}
