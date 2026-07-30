package com.precon.mhsclubs.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.precon.mhsclubs.model.Club
import com.precon.mhsclubs.models.Event
import com.precon.mhsclubs.ui.FigmaCard
import com.precon.mhsclubs.ui.FigmaDarkText
import com.precon.mhsclubs.ui.FigmaPill
import com.precon.mhsclubs.ui.FigmaClubFilter
import com.precon.mhsclubs.ui.FigmaRed
import com.precon.mhsclubs.ui.FigmaScreen
import com.precon.mhsclubs.ui.FigmaTan
import com.precon.mhsclubs.ui.FigmaText
import com.precon.mhsclubs.ui.FigmaTitle
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Composable
fun CalendarScreen(events: List<Event> = emptyList(), clubs: List<Club> = emptyList(), currentDate: LocalDate = Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds()).toLocalDateTime(TimeZone.currentSystemDefault()).date, onDateSelected: (LocalDate) -> Unit = {}, onEventClick: (String) -> Unit = {}) {
    var month by remember { mutableStateOf(LocalDate(currentDate.year, currentDate.month, 1)) }
    var scheduled by remember { mutableStateOf(true) }
    var selectedClubId by remember { mutableStateOf<String?>(null) }
    var filterExpanded by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val visible = events.filter { it.isScheduledMeeting == scheduled && (selectedClubId == null || selectedClubId == it.clubId) }
    val agenda = selectedDate?.let { date -> visible.filter { it.date() == date } } ?: visible.filter { it.startTime >= Clock.System.now() }.sortedBy { it.startTime }.take(3)
    FigmaScreen(Modifier.fillMaxSize()) {
        Box(Modifier.height(42.dp))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val useCompactHeader = maxWidth < 352.dp
            Row(Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically) {
                FigmaTitle(
                    "Calendar",
                    modifier = if (useCompactHeader) Modifier.weight(1f) else Modifier.width(176.dp)
                )
                com.precon.mhsclubs.ui.FigmaSegmentedControl(
                    "Scheduled", "Un-Scheduled", scheduled,
                    { scheduled = true }, { scheduled = false },
                    if (useCompactHeader) Modifier.weight(1f).height(36.dp) else Modifier.width(176.dp).height(36.dp)
                )
            }
        }
        Box(Modifier.height(6.dp))
        MonthPanel(month, selectedDate, onPrevious = { month = month.minus(DatePeriod(months = 1)) }, onNext = { month = month.plus(DatePeriod(months = 1)) }) { date -> selectedDate = date; onDateSelected(date) }
        Box(Modifier.height(9.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Events for: ${selectedDate?.pretty() ?: "Upcoming"}", color = FigmaText, fontSize = 18.sp, modifier = Modifier.weight(1f))
            Box {
                FigmaClubFilter(
                    clubs.firstOrNull { it.id == selectedClubId }?.name ?: "Club Selector",
                    onClick = { filterExpanded = true }
                )
                DropdownMenu(expanded = filterExpanded, onDismissRequest = { filterExpanded = false }) {
                    DropdownMenuItem(text = { Text("All clubs") }, onClick = { selectedClubId = null; filterExpanded = false })
                    clubs.forEach { club -> DropdownMenuItem(text = { Text(club.name) }, onClick = { selectedClubId = club.id; filterExpanded = false }) }
                }
            }
        }
        Box(Modifier.height(9.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
            items(agenda, key = { it.id }) { event -> CalendarEventCard(event, { onEventClick(event.id) }) }
            if (agenda.isEmpty()) item { Text("No ${if (scheduled) "scheduled" else "unscheduled"} events.", color = FigmaText, fontSize = 14.sp) }
        }
    }
}

@Composable
private fun MonthPanel(month: LocalDate, selected: LocalDate?, onPrevious: () -> Unit, onNext: () -> Unit, onSelect: (LocalDate) -> Unit) {
    val start = month.minus(DatePeriod(days = (month.dayOfWeek.ordinal + 1) % 7))
    Column(Modifier.fillMaxWidth().height(328.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)).background(FigmaTan).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}", color = FigmaDarkText, fontWeight = FontWeight.Medium, fontSize = 17.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Default.KeyboardArrowLeft, "Previous month", tint = FigmaDarkText, modifier = Modifier.clickable(onClick = onPrevious))
            Icon(Icons.Default.KeyboardArrowRight, "Next month", tint = FigmaDarkText, modifier = Modifier.clickable(onClick = onNext))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach { Text(it, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold) } }
        repeat(5) { week -> Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.SpaceBetween) { repeat(7) { day -> val date = start.plus(DatePeriod(days = week * 7 + day)); Box(Modifier.size(38.dp).clip(androidx.compose.foundation.shape.CircleShape).background(if (date == selected) Color.Black else Color.Transparent).clickable { onSelect(date) }, contentAlignment = Alignment.Center) { Text(date.dayOfMonth.toString(), color = if (date.month == month.month) FigmaDarkText else Color(0xFF685A4B), fontSize = 18.sp) } } } }
    }
}

@Composable
private fun CalendarEventCard(event: Event, onClick: () -> Unit) {
    FigmaCard(Modifier.fillMaxWidth().height(100.dp), borderColor = if (event.isScheduledMeeting) Color.White else FigmaRed, onClick = onClick) {
        Text(event.title, color = FigmaDarkText, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(event.description, color = FigmaDarkText, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
    }
}

private fun Event.date(): LocalDate = startTime.toLocalDateTime(TimeZone.currentSystemDefault()).date
private fun LocalDate.pretty(): String = "${month.name.lowercase().replaceFirstChar { it.uppercase() }} $dayOfMonth"
