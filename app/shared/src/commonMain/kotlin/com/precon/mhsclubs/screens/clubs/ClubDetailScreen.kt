package com.precon.mhsclubs.screens.clubs

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.precon.mhsclubs.model.Club
import com.precon.mhsclubs.model.Membership
import com.precon.mhsclubs.model.MembershipRole
import com.precon.mhsclubs.model.MembershipStatus
import com.precon.mhsclubs.model.UserRole

/**
 * Club detail screen displaying comprehensive information about a club.
 *
 * Shows club description, meeting details, membership status, and actions
 * like joining/leaving the club.
 *
 * @param club The club to display
 * @param membership The current user's membership in this club, or null if not a member
 * @param userRole The current user's role (Student or Teacher)
 * @param onBackClick Callback when the back button is clicked
 * @param onJoinClick Callback when the user wants to join the club
 * @param onLeaveClick Callback when the user wants to leave the club
 */
@Composable
fun ClubDetailScreen(
    club: Club,
    membership: Membership? = null,
    userRole: UserRole = UserRole.Student,
    onBackClick: () -> Unit = {},
    onJoinClick: () -> Unit = {},
    onLeaveClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top app bar
        TopAppBar(
            title = { Text(club.name) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Club header with logo
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Club logo placeholder
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Club Logo",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Club name and code
                    Text(
                        text = club.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Code: ${club.code}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Category badge
                    Text(
                        text = club.category,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Club description
            if (club.description.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "About",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = club.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Meeting information
            if (club.meetingDay != null || club.meetingTime != null || club.meetingLocation != null) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Meeting Information",
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
                                contentDescription = "Day",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = club.meetingDay ?: "TBD",
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
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = club.meetingTime ?: "TBD",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = club.meetingLocation ?: "TBD",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Membership status and actions
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Your Membership",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (membership != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = "Membership",
                                tint = when (membership.status) {
                                    MembershipStatus.Active -> MaterialTheme.colorScheme.primary
                                    MembershipStatus.Pending -> MaterialTheme.colorScheme.secondary
                                    MembershipStatus.Revoked -> MaterialTheme.colorScheme.error
                                }
                            )
                            Text(
                                text = when (membership.status) {
                                    MembershipStatus.Active -> "Active Member"
                                    MembershipStatus.Pending -> "Pending Approval"
                                    MembershipStatus.Revoked -> "Membership Revoked"
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        if (membership.role != MembershipRole.Member) {
                            Text(
                                text = "Role: ${membership.role.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Leave club button
                        Button(
                            onClick = onLeaveClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Leave Club")
                        }
                    } else {
                        Text(
                            text = "You are not a member of this club",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // Join club button
                        Button(
                            onClick = onJoinClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Join Club")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview
@Composable
fun ClubDetailScreenPreview() {
    MaterialTheme {
        ClubDetailScreen(
            club = Club(
                id = "1",
                sheetSourceId = "sheet1",
                name = "Robotics Club",
                description = "The Robotics Club builds and programs robots for regional and national competitions. We meet weekly to work on our current projects and learn new skills.",
                code = "ROBOT",
                category = "STEM",
                meetingDay = "Monday",
                meetingTime = "3:30 PM - 5:00 PM",
                meetingLocation = "Room 204"
            ),
            membership = Membership(
                id = "1",
                userId = "user1",
                clubId = "1",
                role = MembershipRole.Member,
                status = MembershipStatus.Active
            ),
            userRole = UserRole.Student
        )
    }
}
