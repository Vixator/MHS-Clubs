package com.precon.mhsclubs.screens.announcements

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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Announcement
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.precon.mhsclubs.models.Announcement
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Announcement list screen displaying club announcements.
 *
 * Shows a list of announcements with their title, content, author, and timestamp.
 *
 * @param announcements List of announcements to display
 * @param onAnnouncementClick Callback when an announcement is clicked
 */
@Composable
fun AnnouncementListScreen(
    announcements: List<Announcement> = emptyList(),
    onAnnouncementClick: (String) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Announcements",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        if (announcements.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Announcement,
                    contentDescription = "No Announcements",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("No announcements yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(announcements) { announcement ->
                    AnnouncementCard(
                        announcement = announcement,
                        onClick = { onAnnouncementClick(announcement.id) }
                    )
                }
            }
        }
    }
}

/**
 * Card displaying a single announcement.
 */
@Composable
fun AnnouncementCard(
    announcement: Announcement,
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
            // Announcement title
            Text(
                text = announcement.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Announcement content
            Text(
                text = announcement.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Announcement metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Author",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = announcement.authorName,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Time",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formatPostedAt(announcement.postedAt),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

/**
 * Formats an Instant as a relative time string.
 */
fun formatPostedAt(instant: Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val now = Instant.fromEpochSeconds(0).toLocalDateTime(TimeZone.currentSystemDefault())
    
    // Simple date formatting for preview
    return "${localDateTime.month.name.take(3)} ${localDateTime.dayOfMonth}"
}

/**
 * Announcement model for the UI layer.
 * This extends the core Announcement model with display-friendly fields.
 */
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

@Preview
@Composable
fun AnnouncementListScreenPreview() {
    MaterialTheme {
        AnnouncementListScreen(
            announcements = listOf(
                Announcement(
                    id = "1",
                    clubId = "1",
                    title = "Robotics Competition Results",
                    content = "Congratulations to everyone who participated in the state robotics competition! We placed 2nd overall and won the Innovation Award.",
                    authorId = "teacher1",
                    authorName = "Mr. Smith",
                    isActive = true,
                    postedAt = Instant.parse("2024-03-16T00:00:00Z"),
                    updatedAt = Instant.parse("2024-03-16T00:00:00Z")
                ),
                Announcement(
                    id = "2",
                    clubId = "1",
                    title = "Next Meeting",
                    content = "Our next meeting will be on Monday at 3:30 PM in Room 204. We'll be working on our new robot design.",
                    authorId = "student1",
                    authorName = "Jane Doe",
                    isActive = true,
                    postedAt = Instant.parse("2024-03-18T00:00:00Z"),
                    updatedAt = Instant.parse("2024-03-18T00:00:00Z")
                )
            )
        )
    }
}
