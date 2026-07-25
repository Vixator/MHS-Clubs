package com.precon.mhsclubs.screens.clubs

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.precon.mhsclubs.model.Club
import com.precon.mhsclubs.model.UserRole

/**
 * Club list screen displaying all available clubs.
 *
 * Shows a searchable list of clubs with their basic information.
 * Users can click on a club to view its details.
 *
 * @param clubs List of clubs to display
 * @param isLoading Whether clubs are currently being loaded
 * @param onAccountClick Callback when the account button is clicked
 * @param onClubClick Callback when a club is clicked
 */
@Composable
fun ClubListScreen(
    clubs: List<Club> = emptyList(),
    isLoading: Boolean = false,
    onAccountClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onAnnouncementsClick: () -> Unit = {},
    onClubClick: (String) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top app bar
        TopAppBar(
            title = { Text("MHS Clubs") },
            actions = {
                IconButton(onClick = onCalendarClick) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "My club calendar")
                }
                IconButton(onClick = onAnnouncementsClick) {
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = "Announcements")
                }
                IconButton(onClick = onAccountClick) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Account"
                    )
                }
            }
        )

        // Search bar
        var searchQuery by remember { mutableStateOf("") }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Search clubs...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Club list
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (clubs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No clubs found")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(clubs) { club ->
                    ClubCard(
                        club = club,
                        onClick = { onClubClick(club.id) }
                    )
                }
            }
        }
    }
}

/**
 * Card displaying a single club.
 */
@Composable
fun ClubCard(
    club: Club,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Club logo placeholder
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Club Logo",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.size(16.dp))

                // Club info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = club.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = club.code,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Club description
            if (club.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = club.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Club category and meeting info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = club.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (club.meetingDay != null) {
                    Text(
                        text = "${club.meetingDay} ${club.meetingTime ?: ""}".trim(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ClubListScreenPreview() {
    MaterialTheme {
        ClubListScreen(
            clubs = listOf(
                Club(
                    id = "1",
                    sheetSourceId = "sheet1",
                    name = "Robotics Club",
                    description = "Build and program robots for competitions",
                    code = "ROBOT",
                    category = "STEM",
                    meetingDay = "Monday",
                    meetingTime = "3:30 PM"
                ),
                Club(
                    id = "2",
                    sheetSourceId = "sheet2",
                    name = "Chess Club",
                    description = "Play chess and improve your skills",
                    code = "CHESS",
                    category = "Games",
                    meetingDay = "Tuesday",
                    meetingTime = "3:15 PM"
                ),
                Club(
                    id = "3",
                    sheetSourceId = "sheet3",
                    name = "Debate Team",
                    description = "Competitive debate and public speaking",
                    code = "DEBATE",
                    category = "Academic",
                    meetingDay = "Wednesday",
                    meetingTime = "3:00 PM"
                )
            ),
            isLoading = false
        )
    }
}
