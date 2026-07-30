package com.precon.mhsclubs.screens.announcements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.precon.mhsclubs.model.Club
import com.precon.mhsclubs.ui.FigmaCard
import com.precon.mhsclubs.ui.FigmaClubFilter
import com.precon.mhsclubs.ui.FigmaScreen
import com.precon.mhsclubs.ui.FigmaTitle
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun AnnouncementListScreen(
    announcements: List<Announcement> = emptyList(),
    clubs: List<Club> = emptyList(),
    onAnnouncementClick: (String) -> Unit = {}
) {
    var selectedClubId by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val visible = announcements.filter { selectedClubId == null || selectedClubId == it.clubId }
    FigmaScreen(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(48.dp))
        FigmaTitle("Updates")
        Spacer(Modifier.height(12.dp))
        Box {
            FigmaClubFilter(
                clubs.firstOrNull { it.id == selectedClubId }?.name ?: "All clubs",
                onClick = { expanded = true }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("All clubs") }, onClick = { selectedClubId = null; expanded = false })
                clubs.forEach { club ->
                    DropdownMenuItem(text = { Text(club.name) }, onClick = { selectedClubId = club.id; expanded = false })
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        if (visible.isEmpty()) {
            Text(
                text = "No announcements yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 12.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Posts from your clubs will show up here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visible, key = { it.id }) { announcement ->
                    AnnouncementCard(
                        announcement,
                        clubs.firstOrNull { it.id == announcement.clubId }?.name ?: announcement.title
                    ) { onAnnouncementClick(announcement.id) }
                }
            }
        }
    }
}

@Composable
fun AnnouncementCard(announcement: Announcement, clubName: String, onClick: () -> Unit) {
    // Read theme colours before entering the non-composable annotated-string builder.
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val heading = remember(clubName, announcement.authorName, mutedColor) {
        buildAnnotatedString {
            append(clubName)
            withStyle(SpanStyle(color = mutedColor)) {
                append("  ·  ")
                append(announcement.authorName)
            }
        }
    }
    FigmaCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Text(
            text = heading,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = announcement.postedAt.relativeLabel(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        if (announcement.title.isNotBlank() && announcement.title != clubName) {
            Spacer(Modifier.height(10.dp))
            Text(text = announcement.title, style = MaterialTheme.typography.titleSmall)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = announcement.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

data class Announcement(
    val id: String,
    val clubId: String,
    val title: String,
    val content: String,
    val authorId: String,
    val authorName: String,
    val isActive: Boolean,
    val postedAt: Instant,
    val updatedAt: Instant
)

private fun Instant.relativeLabel(): String = toLocalDateTime(TimeZone.currentSystemDefault()).let {
    val month = it.month.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() }
    val hour = (it.hour + 11) % 12 + 1
    val minute = it.minute.toString().padStart(2, '0')
    "$month ${it.dayOfMonth} · $hour:$minute ${if (it.hour < 12) "AM" else "PM"}"
}
