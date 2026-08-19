package com.precon.mhsclubs.models

import com.precon.mhsclubs.model.Club
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/** Builds display-only occurrences from the clubs table; no events-table rows are created. */
fun recurringMeetings(
    clubs: List<Club>,
    from: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.minus(DatePeriod(days = 7)),
    days: Int = 370,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): List<Event> = clubs.flatMap { club ->
    val meetingDays = club.meetingDay.orEmpty().split(',', ';', '&', '/').mapNotNull(::dayOfWeek).toSet()
    val times = Regex("(\\d{1,2}):(\\d{2})\\s*([AaPp][Mm])").findAll(club.meetingTime.orEmpty()).mapNotNull(::clockTime).toList()
    if (meetingDays.isEmpty() || times.isEmpty()) return@flatMap emptyList()
    (0..days).mapNotNull { offset ->
        val date = from.plus(DatePeriod(days = offset))
        if (date.dayOfWeek !in meetingDays) return@mapNotNull null
        val start = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, times.first().first, times.first().second).toInstant(timeZone)
        val end = times.getOrNull(1)?.let { LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, it.first, it.second).toInstant(timeZone) }
        Event(
            id = "meeting_${club.id}_${date}", clubId = club.id, title = "${club.name} Meeting", description = club.description,
            location = club.meetingLocation, startTime = start, endTime = end, createdAt = start, updatedAt = start,
            isScheduledMeeting = true
        )
    }
}

private fun dayOfWeek(raw: String): DayOfWeek? = when (raw.trim().lowercase().take(3)) {
    "mon" -> DayOfWeek.MONDAY
    "tue" -> DayOfWeek.TUESDAY
    "wed" -> DayOfWeek.WEDNESDAY
    "thu" -> DayOfWeek.THURSDAY
    "fri" -> DayOfWeek.FRIDAY
    "sat" -> DayOfWeek.SATURDAY
    "sun" -> DayOfWeek.SUNDAY
    else -> null
}

private fun clockTime(match: MatchResult): Pair<Int, Int>? {
    val hour = match.groupValues[1].toIntOrNull() ?: return null
    val minute = match.groupValues[2].toIntOrNull() ?: return null
    if (hour !in 1..12 || minute !in 0..59) return null
    return ((hour % 12) + if (match.groupValues[3].equals("pm", true)) 12 else 0) to minute
}
