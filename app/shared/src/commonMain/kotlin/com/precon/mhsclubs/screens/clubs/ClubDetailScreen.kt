package com.precon.mhsclubs.screens.clubs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.precon.mhsclubs.model.Club
import com.precon.mhsclubs.model.UserRole
import com.precon.mhsclubs.models.Membership
import com.precon.mhsclubs.models.MembershipStatus
import com.precon.mhsclubs.ui.FigmaActionButton
import com.precon.mhsclubs.ui.FigmaBackLabel
import com.precon.mhsclubs.ui.FigmaCard
import com.precon.mhsclubs.ui.FigmaDivider
import com.precon.mhsclubs.ui.FigmaScreen
import com.precon.mhsclubs.ui.FigmaTitle

@Composable
fun ClubDetailScreen(
    club: Club,
    membership: Membership? = null,
    userRole: UserRole = UserRole.Student,
    onBackClick: () -> Unit = {},
    onJoinClick: () -> Unit = {},
    onLeaveClick: () -> Unit = {}
) {
    val joined = membership?.status == MembershipStatus.Active
    FigmaScreen(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(12.dp))
        FigmaBackLabel(if (joined) "My Clubs" else "Clubs List", onBackClick)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            FigmaTitle("Club Details", compact = true, modifier = Modifier.weight(1f))
            Text(
                text = "${club.memberCount ?: 0} members",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(20.dp))
        FigmaCard(Modifier.fillMaxWidth()) {
            Text(club.name, style = MaterialTheme.typography.titleLarge)
            club.category.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(2.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            club.description.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(10.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            DetailLine("Advisor", club.advisorName)
            DetailLine("Contact", club.contactEmail)
            DetailLine("Meeting days", club.meetingDay)
            DetailLine("Meeting times", club.meetingTime)
            DetailLine("Location", club.meetingLocation)
        }
        Spacer(Modifier.height(28.dp))
        FigmaActionButton(
            text = if (joined) "Remove from My Clubs" else "Add to My Clubs",
            icon = if (joined) Icons.Default.Close else Icons.Default.Add,
            background = if (joined) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary,
            contentColor = if (joined) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.fillMaxWidth(),
            onClick = if (joined) onLeaveClick else onJoinClick
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun DetailLine(label: String, value: String?) {
    Column {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "Not listed",
            style = MaterialTheme.typography.bodyLarge,
            color = if (value.isNullOrBlank()) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onBackground
            }
        )
        Spacer(Modifier.height(12.dp))
        FigmaDivider()
    }
}
