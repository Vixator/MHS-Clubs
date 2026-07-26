package com.precon.mhsclubs.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Staff workspace exposing the currently implemented administration flows.
 *
 * @param onManageClubs Opens the club directory.
 * @param onManageEvents Opens the event schedule.
 */
@Composable
fun AdminDashboardScreen(
    onManageClubs: () -> Unit = {},
    onManageEvents: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Admin Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Choose a workspace to review current club information and events.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ManagementCard(
            title = "Club directory",
            description = "Review club details and available memberships.",
            actionLabel = "Open club directory",
            onClick = onManageClubs
        )

        ManagementCard(
            title = "Event schedule",
            description = "Review upcoming club events and their details.",
            actionLabel = "Open event schedule",
            onClick = onManageEvents
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ManagementCard(
    title: String,
    description: String,
    actionLabel: String,
    onClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Preview
@Composable
fun AdminDashboardScreenPreview() {
    MaterialTheme {
        AdminDashboardScreen()
    }
}
