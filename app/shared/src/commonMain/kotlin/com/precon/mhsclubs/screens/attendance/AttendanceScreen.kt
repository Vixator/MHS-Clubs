package com.precon.mhsclubs.screens.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.precon.mhsclubs.models.AttendanceStatus
import com.precon.mhsclubs.ui.FigmaCard
import com.precon.mhsclubs.ui.FigmaBackLabel
import com.precon.mhsclubs.ui.FigmaDarkText
import com.precon.mhsclubs.ui.FigmaPill
import com.precon.mhsclubs.ui.FigmaScreen
import com.precon.mhsclubs.ui.FigmaTan
import com.precon.mhsclubs.ui.FigmaText
import com.precon.mhsclubs.ui.FigmaTitle

@Composable
fun AttendanceScreen(eventId: String = "", eventTitle: String = "Event", members: List<AttendanceMember> = emptyList(), isTeacher: Boolean = false, onMarkAttendance: (String, AttendanceStatus) -> Unit = { _, _ -> }, onSave: () -> Unit = {}, onBackClick: () -> Unit = {}) {
    val checkedIn = members.count { it.status == AttendanceStatus.Present }
    FigmaScreen(Modifier.fillMaxSize()) {
        Box(Modifier.height(20.dp))
        FigmaBackLabel("Event Details", onBackClick)
        FigmaTitle("Take attendance", compact = true)
        Text("$eventTitle · Today", color = FigmaTan, fontSize = 14.sp, modifier = Modifier.padding(start = 10.dp, top = 5.dp))
        Box(Modifier.height(7.dp))
        FigmaCard(Modifier.fillMaxWidth().height(76.dp), borderColor = FigmaTan) {
            Text("$checkedIn / ${members.size}", color = FigmaDarkText, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Text("Checked in / RSVP", color = FigmaDarkText, fontSize = 13.sp)
        }
        Box(Modifier.height(9.dp))
        Text("Roster", color = FigmaText, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Box(Modifier.height(9.dp))
        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(members, key = { it.userId }) { member -> AttendanceRow(member, isTeacher) { onMarkAttendance(member.userId, it); onSave() } }
        }
    }
}

@Composable
fun AttendanceRow(member: AttendanceMember, isTeacher: Boolean, onMarkAttendance: (AttendanceStatus) -> Unit) {
    FigmaCard(Modifier.fillMaxWidth().height(76.dp), borderColor = FigmaTan) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(64.dp).clip(CircleShape).background(com.precon.mhsclubs.ui.FigmaPage), contentAlignment = Alignment.Center) { Text(member.displayName.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString(""), color = com.precon.mhsclubs.ui.FigmaRed, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp) }
            Column(Modifier.weight(1f).padding(start = 16.dp)) {
                Text(member.displayName, color = FigmaDarkText, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Student member", color = FigmaDarkText, fontSize = 13.sp)
            }
            FigmaPill(if (member.status == AttendanceStatus.Present) "Present" else "Absent", borderColor = FigmaText, onClick = { onMarkAttendance(if (member.status == AttendanceStatus.Present) AttendanceStatus.Absent else AttendanceStatus.Present) })
        }
    }
}

data class AttendanceMember(val userId: String, val displayName: String, val email: String, val status: AttendanceStatus)
