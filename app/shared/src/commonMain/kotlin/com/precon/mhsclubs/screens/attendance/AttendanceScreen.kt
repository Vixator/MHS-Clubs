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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
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
import com.precon.mhsclubs.models.Attendance
import com.precon.mhsclubs.models.AttendanceStatus
import kotlinx.datetime.Instant

/**
 * Attendance screen for marking and viewing attendance.
 *
 * Teachers can mark attendance for events, and students can view their
 * own attendance history.
 *
 * @param eventId The ID of the event for which to mark attendance
 * @param eventTitle The title of the event
 * @param members List of members with their current attendance status
 * @param isTeacher Whether the current user is a teacher (can mark attendance)
 * @param onMarkAttendance Callback when attendance is marked for a member
 */
@Composable
fun AttendanceScreen(
    eventId: String = "",
    eventTitle: String = "Event",
    members: List<AttendanceMember> = emptyList(),
    isTeacher: Boolean = false,
    onMarkAttendance: (String, AttendanceStatus) -> Unit = { _, _ -> }
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Attendance: $eventTitle",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        if (members.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No members to mark attendance for")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(members) { member ->
                    AttendanceRow(
                        member = member,
                        isTeacher = isTeacher,
                        onMarkAttendance = { status ->
                            onMarkAttendance(member.userId, status)
                        }
                    )
                }
            }
        }

        if (isTeacher) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* Save all attendance */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Save Attendance")
            }
        }
    }
}

/**
 * Row displaying a single member's attendance status.
 */
@Composable
fun AttendanceRow(
    member: AttendanceMember,
    isTeacher: Boolean,
    onMarkAttendance: (AttendanceStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Member",
                    modifier = Modifier.size(24.dp)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = member.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = member.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isTeacher) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Present button
                    Button(
                        onClick = { onMarkAttendance(AttendanceStatus.Present) },
                        modifier = Modifier.height(36.dp),
                        enabled = member.status != AttendanceStatus.Present
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Present",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Late button
                    Button(
                        onClick = { onMarkAttendance(AttendanceStatus.Late) },
                        modifier = Modifier.height(36.dp),
                        enabled = member.status != AttendanceStatus.Late
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Late",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Absent button
                    Button(
                        onClick = { onMarkAttendance(AttendanceStatus.Absent) },
                        modifier = Modifier.height(36.dp),
                        enabled = member.status != AttendanceStatus.Absent
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Absent",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                // Display current status
                val statusText = when (member.status) {
                    AttendanceStatus.Present -> "Present"
                    AttendanceStatus.Late -> "Late"
                    AttendanceStatus.Absent -> "Absent"
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (member.status) {
                        AttendanceStatus.Present -> MaterialTheme.colorScheme.primary
                        AttendanceStatus.Late -> MaterialTheme.colorScheme.secondary
                        AttendanceStatus.Absent -> MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}

/**
 * Member data for attendance marking.
 */
data class AttendanceMember(
    val userId: String,
    val displayName: String,
    val email: String,
    val status: AttendanceStatus
)

@Preview
@Composable
fun AttendanceScreenPreview() {
    MaterialTheme {
        AttendanceScreen(
            eventTitle = "Robotics Competition",
            members = listOf(
                AttendanceMember(
                    userId = "1",
                    displayName = "John Doe",
                    email = "john@students.mcpasd.k12.wi.us",
                    status = AttendanceStatus.Present
                ),
                AttendanceMember(
                    userId = "2",
                    displayName = "Jane Smith",
                    email = "jane@students.mcpasd.k12.wi.us",
                    status = AttendanceStatus.Late
                ),
                AttendanceMember(
                    userId = "3",
                    displayName = "Bob Johnson",
                    email = "bob@students.mcpasd.k12.wi.us",
                    status = AttendanceStatus.Absent
                )
            ),
            isTeacher = true
        )
    }
}
