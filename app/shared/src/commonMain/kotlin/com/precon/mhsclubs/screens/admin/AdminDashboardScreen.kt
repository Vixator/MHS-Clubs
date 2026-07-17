package com.precon.mhsclubs.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Sync
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

/**
 * Admin dashboard screen for teachers and student leaders.
 *
 * Provides quick access to club management, membership management,
 * event creation, and data sync tools.
 *
 * @param clubCount Number of clubs the admin manages
 * @param memberCount Number of members across all clubs
 * @param eventCount Number of upcoming events
 * @param onCreateClub Click handler for creating a new club
 * @param onManageClubs Click handler for managing clubs
 * @param onManageMembers Click handler for managing members
 * @param onCreateEvent Click handler for creating an event
 * @param onManageEvents Click handler for managing events
 * @param onSyncData Click handler for syncing data from Google Sheets
 */
@Composable
fun AdminDashboardScreen(
    clubCount: Int = 0,
    memberCount: Int = 0,
    eventCount: Int = 0,
    onCreateClub: () -> Unit = {},
    onManageClubs: () -> Unit = {},
    onManageMembers: () -> Unit = {},
    onCreateEvent: () -> Unit = {},
    onManageEvents: () -> Unit = {},
    onSyncData: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dashboard header
        Text(
            text = "Admin Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Stats cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Clubs",
                value = clubCount.toString(),
                icon = Icons.Default.Dashboard,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Members",
                value = memberCount.toString(),
                icon = Icons.Default.Group,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Events",
                value = eventCount.toString(),
                icon = Icons.Default.Sync,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Club management section
        Text(
            text = "Club Management",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onCreateClub,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create"
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Create New Club")
                }

                Button(
                    onClick = onManageClubs,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Manage Existing Clubs")
                }
            }
        }

        // Member management section
        Text(
            text = "Member Management",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onManageMembers,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View All Members")
                }

                Button(
                    onClick = onManageMembers,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Approve Pending Memberships")
                }
            }
        }

        // Event management section
        Text(
            text = "Event Management",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onCreateEvent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create"
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Create New Event")
                }

                Button(
                    onClick = onManageEvents,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Manage Existing Events")
                }
            }
        }

        // Data sync section
        Text(
            text = "Data Management",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onSyncData,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync"
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Sync Clubs from Google Sheets")
                }

                Button(
                    onClick = { /* Export CSV */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Export Attendance CSV")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Stat card displaying a single metric.
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
fun AdminDashboardScreenPreview() {
    MaterialTheme {
        AdminDashboardScreen(
            clubCount = 5,
            memberCount = 120,
            eventCount = 8
        )
    }
}
