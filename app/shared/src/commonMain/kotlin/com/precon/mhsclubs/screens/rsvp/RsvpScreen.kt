package com.precon.mhsclubs.screens.rsvp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.precon.mhsclubs.models.Event
import com.precon.mhsclubs.ui.FigmaActionButton
import com.precon.mhsclubs.ui.FigmaBackLabel
import com.precon.mhsclubs.ui.FigmaCard
import com.precon.mhsclubs.ui.FigmaPill
import com.precon.mhsclubs.ui.FigmaScreen
import com.precon.mhsclubs.ui.FigmaSegmentedControl
import com.precon.mhsclubs.ui.FigmaTitle
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun RsvpScreen(
    event: Event,
    currentRsvp: Rsvp? = null,
    showAttendance: Boolean = false,
    onRsvp: (RsvpStatus) -> Unit = {},
    onAttendanceClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val past = event.startTime < kotlin.time.Clock.System.now()
    val going = currentRsvp?.status != RsvpStatus.NotGoing
    FigmaScreen(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(12.dp))
        FigmaBackLabel("Back", onBackClick)
        FigmaTitle("Event Details", compact = true)
        Spacer(Modifier.height(4.dp))
        Text(
            text = event.startTime.detailDate(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        FigmaCard(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                FigmaPill(
                    text = event.timeRange(),
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
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
            event.location?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(14.dp))
                FigmaPill(it, icon = Icons.Default.LocationOn)
            }
        }
        Spacer(Modifier.height(28.dp))
        Text("Will you be there?", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (past) "RSVP is closed for this event." else "You can update your RSVP anytime.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        FigmaSegmentedControl(
            "Going", "Not going", going,
            { onRsvp(RsvpStatus.Going) },
            { onRsvp(RsvpStatus.NotGoing) },
            Modifier.fillMaxWidth(),
            enabled = !past
        )
        if (showAttendance) {
            Spacer(Modifier.height(24.dp))
            FigmaActionButton(
                text = "Take attendance",
                modifier = Modifier.fillMaxWidth(),
                background = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.primary,
                onClick = onAttendanceClick
            )
        }
    }
}

data class Rsvp(
    val id: String,
    val eventId: String,
    val userId: String,
    val status: RsvpStatus,
    val respondedAt: Instant
)

sealed class RsvpStatus(val value: String) {
    object Going : RsvpStatus("going")
    object Maybe : RsvpStatus("maybe")
    object NotGoing : RsvpStatus("not_going")
}

private fun Event.timeRange(): String {
    val start = startTime.clockLabel()
    val end = endTime?.clockLabel()
    return if (end != null) "$start – $end" else start
}

private fun Instant.clockLabel(): String =
    toLocalDateTime(TimeZone.currentSystemDefault()).let {
        "${(it.hour + 11) % 12 + 1}:${it.minute.toString().padStart(2, '0')} ${if (it.hour < 12) "AM" else "PM"}"
    }

private fun Instant.detailDate(): String =
    toLocalDateTime(TimeZone.currentSystemDefault()).let {
        val month = it.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }
        "$month ${it.dayOfMonth}, ${it.year}"
    }
