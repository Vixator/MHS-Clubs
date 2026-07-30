package com.precon.mhsclubs.screens.rsvp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.precon.mhsclubs.models.Event
import com.precon.mhsclubs.ui.FigmaActionButton
import com.precon.mhsclubs.ui.FigmaBackLabel
import com.precon.mhsclubs.ui.FigmaCard
import com.precon.mhsclubs.ui.FigmaDarkText
import com.precon.mhsclubs.ui.FigmaPill
import com.precon.mhsclubs.ui.FigmaScreen
import com.precon.mhsclubs.ui.FigmaSegmentedControl
import com.precon.mhsclubs.ui.FigmaTan
import com.precon.mhsclubs.ui.FigmaText
import com.precon.mhsclubs.ui.FigmaTitle
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun RsvpScreen(event: Event, currentRsvp: Rsvp? = null, showAttendance: Boolean = false, onRsvp: (RsvpStatus) -> Unit = {}, onAttendanceClick: () -> Unit = {}, onBackClick: () -> Unit = {}) {
    val past = event.startTime < kotlin.time.Clock.System.now()
    val going = currentRsvp?.status != RsvpStatus.NotGoing
    FigmaScreen(Modifier.fillMaxSize()) {
        Box(Modifier.height(19.dp))
        FigmaBackLabel("My Clubs", onBackClick)
        FigmaTitle("Event Details", compact = true)
        Text(event.startTime.detailDate(event.location), color = FigmaText, fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
        Box(Modifier.height(5.dp))
        FigmaCard(Modifier.fillMaxWidth().height(106.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(event.title, color = FigmaDarkText, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(event.description, color = FigmaDarkText, fontSize = 13.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
                Column { FigmaPill(event.startTime.timeLabel(), borderColor = FigmaText); event.endTime?.let { FigmaPill(it.timeLabel(), borderColor = FigmaText, modifier = Modifier.padding(top = 10.dp)) } }
            }
        }
        Box(Modifier.height(19.dp))
        Row(Modifier.fillMaxWidth()) {
            Text("Will you be there?", color = FigmaText, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
            FigmaSegmentedControl("Yes", "No", going, { if (!past) onRsvp(RsvpStatus.Going) }, { if (!past) onRsvp(RsvpStatus.NotGoing) }, Modifier.height(32.dp).fillMaxWidth(.48f))
        }
        Text(if (past) "RSVP is closed for this event." else "You can update your RSVP anytime.", color = FigmaText, fontSize = 12.sp, modifier = Modifier.padding(top = 7.dp))
        if (showAttendance) { Box(Modifier.height(20.dp)); FigmaActionButton("Attendance", Modifier.fillMaxWidth(), onClick = onAttendanceClick) }
    }
}

data class Rsvp(val id: String, val eventId: String, val userId: String, val status: RsvpStatus, val respondedAt: Instant)
sealed class RsvpStatus(val value: String) { object Going : RsvpStatus("going"); object Maybe : RsvpStatus("maybe"); object NotGoing : RsvpStatus("not_going") }
private fun Instant.timeLabel(): String = toLocalDateTime(TimeZone.currentSystemDefault()).let { "${(it.hour + 11) % 12 + 1}:${it.minute.toString().padStart(2, '0')} ${if (it.hour < 12) "AM" else "PM"}" }
private fun Instant.detailDate(location: String?): String = toLocalDateTime(TimeZone.currentSystemDefault()).let { "${it.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }} ${it.dayOfMonth}, ${it.year}${location?.takeIf(String::isNotBlank)?.let { place -> " • $place" } ?: ""}" }
