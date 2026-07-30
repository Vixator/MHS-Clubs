package com.precon.mhsclubs.screens.attendance

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.precon.mhsclubs.models.AttendanceStatus
import com.precon.mhsclubs.ui.FigmaBackLabel
import com.precon.mhsclubs.ui.FigmaCard
import com.precon.mhsclubs.ui.FigmaMonogram
import com.precon.mhsclubs.ui.FigmaPill
import com.precon.mhsclubs.ui.FigmaScreen
import com.precon.mhsclubs.ui.FigmaTitle

@Composable
fun AttendanceScreen(
    eventId: String = "",
    eventTitle: String = "Event",
    members: List<AttendanceMember> = emptyList(),
    isTeacher: Boolean = false,
    onMarkAttendance: (String, AttendanceStatus) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {}
) {
    val checkedIn = members.count { it.status == AttendanceStatus.Present }
    FigmaScreen(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(12.dp))
        FigmaBackLabel("Event Details", onBackClick)
        FigmaTitle("Attendance", compact = true)
        Spacer(Modifier.height(4.dp))
        Text(
            text = eventTitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        FigmaCard(Modifier.fillMaxWidth(), borderColor = MaterialTheme.colorScheme.outline) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "$checkedIn of ${members.size}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Marked present",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Roster", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        if (members.isEmpty()) {
            Text(
                text = "No active members in this club yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(members, key = { it.userId }) { member ->
                    AttendanceRow(member, isTeacher) { onMarkAttendance(member.userId, it) }
                }
            }
        }
    }
}

@Composable
fun AttendanceRow(member: AttendanceMember, isTeacher: Boolean, onMarkAttendance: (AttendanceStatus) -> Unit) {
    val label = when (member.status) {
        AttendanceStatus.Present -> "Present"
        AttendanceStatus.Late -> "Late"
        AttendanceStatus.Absent -> "Absent"
    }
    val background = when (member.status) {
        AttendanceStatus.Present -> MaterialTheme.colorScheme.primary
        AttendanceStatus.Late -> MaterialTheme.colorScheme.tertiary
        AttendanceStatus.Absent -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = when (member.status) {
        AttendanceStatus.Present -> MaterialTheme.colorScheme.onPrimary
        AttendanceStatus.Late -> MaterialTheme.colorScheme.onTertiary
        AttendanceStatus.Absent -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    FigmaCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            FigmaMonogram(member.displayName, Modifier.size(44.dp))
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(
                    text = member.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = member.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            FigmaPill(
                text = label,
                background = background,
                contentColor = contentColor,
                borderColor = if (member.status == AttendanceStatus.Absent) MaterialTheme.colorScheme.outlineVariant else null,
                onClick = if (isTeacher) {
                    {
                        onMarkAttendance(
                            when (member.status) {
                                AttendanceStatus.Present -> AttendanceStatus.Late
                                AttendanceStatus.Late -> AttendanceStatus.Absent
                                AttendanceStatus.Absent -> AttendanceStatus.Present
                            }
                        )
                    }
                } else null
            )
        }
    }
}

data class AttendanceMember(
    val userId: String,
    val displayName: String,
    val email: String,
    val status: AttendanceStatus
)
