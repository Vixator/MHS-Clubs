package com.precon.mhsclubs.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.precon.mhsclubs.model.Club
import com.precon.mhsclubs.models.Event
import com.precon.mhsclubs.ui.FigmaCard
import com.precon.mhsclubs.ui.FigmaClubFilter
import com.precon.mhsclubs.ui.FigmaPill
import com.precon.mhsclubs.ui.FigmaScreen
import com.precon.mhsclubs.ui.FigmaTitle
import com.precon.mhsclubs.ui.textTertiary
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Composable
fun CalendarScreen(
    events: List<Event> = emptyList(),
    clubs: List<Club> = emptyList(),
    currentDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    onDateSelected: (LocalDate) -> Unit = {},
    onEventClick: (String) -> Unit = {}
) {
    var month by remember { mutableStateOf(LocalDate(currentDate.year, currentDate.month, 1)) }
    var selectedClubId by remember { mutableStateOf<String?>(null) }
    var filterExpanded by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val visible = events.filter { !it.isScheduledMeeting && (selectedClubId == null || selectedClubId == it.clubId) }
    val agenda = selectedDate?.let { date -> visible.filter { it.date() == date } }
        ?: visible.filter { it.startTime >= Clock.System.now() }.sortedBy { it.startTime }.take(3)
    val eventDays = remember(visible) { visible.map { it.date() }.toSet() }

    FigmaScreen(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(48.dp))
        FigmaTitle("Calendar")
        Spacer(Modifier.height(16.dp))
        MonthPanel(
            month = month,
            today = currentDate,
            selected = selectedDate,
            eventDays = eventDays,
            onPrevious = { month = month.minus(DatePeriod(months = 1)) },
            onNext = { month = month.plus(DatePeriod(months = 1)) }
        ) { date ->
            selectedDate = if (selectedDate == date) null else date
            onDateSelected(date)
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = selectedDate?.pretty() ?: "Upcoming",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Box {
                FigmaClubFilter(
                    clubs.firstOrNull { it.id == selectedClubId }?.name ?: "All clubs",
                    onClick = { filterExpanded = true }
                )
                DropdownMenu(expanded = filterExpanded, onDismissRequest = { filterExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("All clubs") },
                        onClick = { selectedClubId = null; filterExpanded = false }
                    )
                    clubs.forEach { club ->
                        DropdownMenuItem(
                            text = { Text(club.name) },
                            onClick = { selectedClubId = club.id; filterExpanded = false }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(agenda, key = { it.id }) { event ->
                CalendarEventCard(event) { onEventClick(event.id) }
            }
            if (agenda.isEmpty()) {
                item {
                    Text(
                        text = "No upcoming events${selectedDate?.let { " on ${it.pretty()}" } ?: ""}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthPanel(
    month: LocalDate,
    today: LocalDate,
    selected: LocalDate?,
    eventDays: Set<LocalDate>,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSelect: (LocalDate) -> Unit
) {
    val gridStart = month.minus(DatePeriod(days = (month.dayOfWeek.ordinal + 1) % 7))
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            MonthArrow(Icons.Default.ChevronLeft, "Previous month", onPrevious)
            Spacer(Modifier.size(4.dp))
            MonthArrow(Icons.Default.ChevronRight, "Next month", onNext)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        repeat(6) { week ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { day ->
                    val date = gridStart.plus(DatePeriod(days = week * 7 + day))
                    DayCell(
                        date = date,
                        inMonth = date.month == month.month,
                        isToday = date == today,
                        isSelected = date == selected,
                        hasEvent = date in eventDays,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelect(date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthArrow(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    inMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    hasEvent: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
                .then(
                    if (isToday && !isSelected) {
                        Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    } else Modifier
                )
                .clickable(onClick = onClick),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected || isToday) FontWeight.SemiBold else FontWeight.Normal,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    !inMonth -> MaterialTheme.colorScheme.textTertiary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            if (hasEvent) {
                Spacer(Modifier.height(2.dp))
                Box(
                    Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary
                        )
                )
            }
        }
    }
}

@Composable
private fun CalendarEventCard(event: Event, onClick: () -> Unit) {
    FigmaCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (event.isScheduledMeeting) {
            MaterialTheme.colorScheme.outlineVariant
        } else {
            MaterialTheme.colorScheme.outline
        },
        onClick = onClick
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = event.date().pretty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FigmaPill(
                text = event.startTime.clockLabel(),
                background = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                borderColor = null
            )
        }
        if (event.description.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun Event.date(): LocalDate = startTime.toLocalDateTime(TimeZone.currentSystemDefault()).date

private fun LocalDate.pretty(): String =
    "${month.name.lowercase().replaceFirstChar { it.uppercase() }} $dayOfMonth"

private fun Instant.clockLabel(): String =
    toLocalDateTime(TimeZone.currentSystemDefault()).let {
        "${(it.hour + 11) % 12 + 1}:${it.minute.toString().padStart(2, '0')} ${if (it.hour < 12) "AM" else "PM"}"
    }
