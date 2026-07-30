package com.precon.mhsclubs.screens.admin

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.precon.mhsclubs.ui.FigmaActionButton
import com.precon.mhsclubs.ui.FigmaCard
import com.precon.mhsclubs.ui.FigmaScreen
import com.precon.mhsclubs.ui.FigmaTitle

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
    FigmaScreen(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(48.dp))
        FigmaTitle("Admin", compact = true)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Choose a workspace to review current club information and events.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        ManagementCard(
            title = "Club directory",
            description = "Review club details and available memberships.",
            actionLabel = "Open club directory",
            onClick = onManageClubs
        )
        Spacer(Modifier.height(12.dp))
        ManagementCard(
            title = "Event schedule",
            description = "Review upcoming club events and their details.",
            actionLabel = "Open event schedule",
            onClick = onManageEvents
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ManagementCard(
    title: String,
    description: String,
    actionLabel: String,
    onClick: () -> Unit
) {
    FigmaCard(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        FigmaActionButton(
            text = actionLabel,
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick
        )
    }
}

@Preview
@Composable
fun AdminDashboardScreenPreview() {
    AdminDashboardScreen()
}
