package com.precon.mhsclubs.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.precon.mhsclubs.ui.FigmaActionButton
import com.precon.mhsclubs.ui.FigmaCard
import com.precon.mhsclubs.ui.FigmaDarkText
import com.precon.mhsclubs.ui.FigmaPageAlt
import com.precon.mhsclubs.ui.FigmaScreen
import com.precon.mhsclubs.ui.FigmaText
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
        Box(Modifier.height(42.dp))
        FigmaTitle("Admin Dashboard", compact = true)
        Box(Modifier.height(8.dp))
        Text(
            text = "Choose a workspace to review current club information and events.",
            color = FigmaText,
            fontSize = 16.sp
        )
        Box(Modifier.height(16.dp))
        ManagementCard(
            title = "Club directory",
            description = "Review club details and available memberships.",
            actionLabel = "Open club directory",
            onClick = onManageClubs
        )
        Box(Modifier.height(12.dp))
        ManagementCard(
            title = "Event schedule",
            description = "Review upcoming club events and their details.",
            actionLabel = "Open event schedule",
            onClick = onManageEvents
        )
        Box(Modifier.height(16.dp))
    }
}

@Composable
private fun ManagementCard(
    title: String,
    description: String,
    actionLabel: String,
    onClick: () -> Unit
) {
        FigmaCard(modifier = Modifier.fillMaxWidth().height(130.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    color = FigmaDarkText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    color = FigmaDarkText,
                    fontSize = 13.sp
                )
                FigmaActionButton(
                    text = actionLabel,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onClick
                )
            }
        }
}

@Preview
@Composable
fun AdminDashboardScreenPreview() {
    AdminDashboardScreen()
}
