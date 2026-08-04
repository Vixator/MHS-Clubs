package com.precon.mhsclubs.screens.clubs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.precon.mhsclubs.model.Club
import com.precon.mhsclubs.models.Event
import com.precon.mhsclubs.ui.FigmaBackLabel
import com.precon.mhsclubs.ui.FigmaActionButton
import com.precon.mhsclubs.ui.FigmaCard
import com.precon.mhsclubs.ui.FigmaPill
import com.precon.mhsclubs.ui.FigmaScreen
import com.precon.mhsclubs.ui.FigmaTitle
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val DIRECTORY_TITLE = "Add Clubs"

@Composable
fun ClubListScreen(
    clubs: List<Club> = emptyList(),
    isLoading: Boolean = false,
    memberClubIds: Set<String> = emptySet(),
    nextMeetings: Map<String, Event> = emptyMap(),
    onClubClick: (String) -> Unit = {},
    title: String = "My Clubs",
    backLabel: String? = null,
    onBackClick: (() -> Unit)? = null,
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null
) {
    val isDirectory = title == DIRECTORY_TITLE
    FigmaScreen(Modifier.fillMaxSize()) {
        if (backLabel != null && onBackClick != null) {
            Spacer(Modifier.height(12.dp))
            FigmaBackLabel(backLabel, onBackClick)
        } else {
            Spacer(Modifier.height(48.dp))
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            FigmaTitle(title, compact = isDirectory, modifier = Modifier.weight(1f))
        }
        if (clubs.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isDirectory) "${clubs.size} clubs at Middleton High" else "${clubs.size} active",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(20.dp))
        when {
            isLoading -> ClubListSkeleton()
            errorMessage != null && onRetry != null -> ClubListLoadError(errorMessage, onRetry)
            clubs.isEmpty() -> EmptyClubs(isDirectory)
            isDirectory -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(clubs, key = { it.id }) { club ->
                    DirectoryRow(
                        club = club,
                        isMember = club.id in memberClubIds,
                        onClick = { onClubClick(club.id) }
                    )
                }
            }
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(clubs, key = { it.id }) { club ->
                    ClubCard(club, club.id in memberClubIds, nextMeetings[club.id]) { onClubClick(club.id) }
                }
            }
        }
    }
}

@Composable
private fun ClubListLoadError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        FigmaActionButton(
            text = "Try again",
            onClick = onRetry
        )
    }
}

/**
 * A club summary card. Height follows its content, and the accent colour appears
 * exactly once — on the next-meeting time.
 */
@Composable
fun ClubCard(club: Club, isMember: Boolean = false, nextMeeting: Event? = null, onClick: () -> Unit) {
    FigmaCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = club.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                club.category.takeIf { it.isNotBlank() && it.lowercase() != "general" }?.let { category ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isMember) {
                FigmaPill(
                    text = "Joined",
                    background = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    borderColor = null
                )
            }
        }
        club.description.takeIf { it.isNotBlank() }?.let { description ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            val meetingTime = nextMeeting?.let(::formatTimeRange) ?: club.meetingTime
            if (meetingTime != null) {
                FigmaPill(
                    text = meetingTime,
                    background = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    borderColor = null
                )
            }
            val day = nextMeeting?.let(::formatDate) ?: club.meetingDay
            if (day != null) FigmaPill(day)
            club.meetingLocation?.takeIf { it.isNotBlank() }?.let {
                FigmaPill(it, icon = Icons.Default.LocationOn)
            }
        }
    }
}

@Composable
private fun DirectoryRow(
    club: Club,
    isMember: Boolean,
    onClick: () -> Unit
) {
    val shape = MaterialTheme.shapes.small
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .clickable(onClick = onClick)
            .heightIn(min = 60.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = club.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            listOfNotNull(club.meetingDay, club.meetingLocation)
                .filter { it.isNotBlank() }
                .takeIf { it.isNotEmpty() }
                ?.let { details ->
                    Text(
                        text = details.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
        }
        if (isMember) {
            FigmaPill(
                text = "Joined",
                background = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                borderColor = null
            )
            Spacer(Modifier.size(8.dp))
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun EmptyClubs(isDirectory: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Group,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (isDirectory) "No clubs available" else "No clubs yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (isDirectory) {
                "The club directory has not been published for this year."
            } else {
                "Tap + to browse school clubs and add your first one."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** Placeholder cards shown while the first club request is in flight. */
@Composable
private fun ClubListSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
            )
        }
    }
}

private fun formatTimeRange(event: Event): String {
    val start = event.startTime.clockLabel()
    val end = event.endTime?.clockLabel()
    return if (end != null) "$start – $end" else start
}

private fun kotlinx.datetime.Instant.clockLabel(): String =
    toLocalDateTime(TimeZone.currentSystemDefault()).let {
        "${(it.hour + 11) % 12 + 1}:${it.minute.toString().padStart(2, '0')} ${if (it.hour < 12) "AM" else "PM"}"
    }

private fun formatDate(event: Event): String =
    event.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).let {
        "${it.month.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() }} ${it.dayOfMonth}"
    }
