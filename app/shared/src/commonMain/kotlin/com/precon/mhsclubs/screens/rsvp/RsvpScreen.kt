package com.precon.mhsclubs.screens.rsvp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.precon.mhsclubs.models.Event
import com.precon.mhsclubs.models.Rsvp
import com.precon.mhsclubs.models.RsvpStatus
import kotlinx.datetime.Instant

/**
 * RSVP screen for responding to an event invitation.
 *
 * Displays event details and allows the user to RSVP with their attendance status.
 *
 * @param event The event to RSVP for
 * @param currentRsvp The user's current RSVP, or null if they haven't responded yet
 * @param onRsvp Callback when the user submits their RSVP
 */
@Composable
fun RsvpScreen(
    event: Event,
    currentRsvp: Rsvp? = null,
    onRsvp: (RsvpStatus) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "RSVP: ${event.title}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        // Event details
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Event Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formatDate(event.startTime),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Time",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formatTime(event.startTime, event.endTime),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (event.location != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = event.location,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (event.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // RSVP options
        Text(
            text = "Your Response",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Going button
            Button(
                onClick = { onRsvp(RsvpStatus.Going) },
                modifier = Modifier.fillMaxWidth(),
                enabled = currentRsvp?.status != RsvpStatus.Going
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Going",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("I'm Going")
            }

            // Maybe button
            Button(
                onClick = { onRsvp(RsvpStatus.Maybe) },
                modifier = Modifier.fillMaxWidth(),
                enabled = currentRsvp?.status != RsvpStatus.Maybe
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "Maybe",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Maybe")
            }

            // Not going button
            Button(
                onClick = { onRsvp(RsvpStatus.NotGoing) },
                modifier = Modifier.fillMaxWidth(),
                enabled = currentRsvp?.status != RsvpStatus.NotGoing
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Not Going",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("I Can't Make It")
            }
        }

        // Current RSVP status
        if (currentRsvp != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Current response: ${currentRsvp.status.name}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

/**
 * Formats an Instant as a date string.
 */
fun formatDate(instant: Instant): String {
    val localDateTime = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    return "${localDateTime.month.name} ${localDateTime.dayOfMonth}, ${localDateTime.year}"
}

/**
 * Formats start and end times as a time range string.
 */
fun formatTime(startTime: Instant, endTime: Instant?): String {
    val startLocal = startTime.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    val startTimeStr = "${startLocal.hour}:${startLocal.minute.toString().padStart(2, '0')}"
    
    if (endTime != null) {
        val endLocal = endTime.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        val endTimeStr = "${endLocal.hour}:${endLocal.minute.toString().padStart(2, '0')}"
        return "$startTimeStr - $endTimeStr"
    }
    
    return startTimeStr
}

/**
 * RSVP model for the UI layer.
 */
data class Rsvp(
    val id: String,
    val eventId: String,
    val userId: String,
    val status: RsvpStatus,
    val respondedAt: Instant
)

/**
 * RSVP status enum.
 */
sealed class RsvpStatus(val value: String) {
    object Going : RsvpStatus("going")
    object Maybe : RsvpStatus("maybe")
    object NotGoing : RsvpStatus("not_going")

    companion object {
        private val BY_VALUE = entries.associateBy(RsvpStatus::value)

        fun fromValue(value: String): RsvpStatus =
            BY_VALUE[value] ?: throw IllegalArgumentException("Unknown RsvpStatus: $value")
    }

    override fun toString(): String = value
}

@Preview
@Composable
fun RsvpScreenPreview() {
    MaterialTheme {
        RsvpScreen(
            event = Event(
                id = "1",
                clubId = "1",
                title = "Robotics Competition",
                description = "Annual robotics competition at the state fair. All team members should attend.",
                location = "State Fair Grounds, Building A",
                startTime = Instant.parse("2024-03-15T09:00:00Z"),
                endTime = Instant.parse("2024-03-15T17:00:00Z"),
                googleCalendarSynced = false,
                createdAt = Instant.parse("2024-01-01T00:00:00Z"),
                updatedAt = Instant.parse("2024-01-01T00:00:00Z")
            )
        )
    }
}
