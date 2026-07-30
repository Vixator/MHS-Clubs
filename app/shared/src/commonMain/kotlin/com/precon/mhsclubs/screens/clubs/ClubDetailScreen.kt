package com.precon.mhsclubs.screens.clubs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.precon.mhsclubs.model.Club
import com.precon.mhsclubs.model.UserRole
import com.precon.mhsclubs.models.Membership
import com.precon.mhsclubs.models.MembershipStatus
import com.precon.mhsclubs.ui.FigmaActionButton
import com.precon.mhsclubs.ui.FigmaBackLabel
import com.precon.mhsclubs.ui.FigmaCard
import com.precon.mhsclubs.ui.FigmaDarkText
import com.precon.mhsclubs.ui.FigmaScreen
import com.precon.mhsclubs.ui.FigmaTan
import com.precon.mhsclubs.ui.FigmaText
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
    FigmaScreen(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(Modifier.height(19.dp))
        FigmaBackLabel("${if (membership?.status == MembershipStatus.Active) "My Clubs" else "Clubs List"}", onBackClick)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            FigmaTitle("Club Details", compact = true, modifier = Modifier.weight(1f))
            Text("${club.memberCount ?: 0} members", color = FigmaTan, fontSize = 14.sp)
        }
        Box(Modifier.height(14.dp))
        FigmaCard(Modifier.fillMaxWidth().height(205.dp)) {
            Text(club.name, color = FigmaDarkText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(club.description, color = FigmaDarkText, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
        }
        Box(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            DetailLine("Advisor(s):", club.advisorName)
            DetailLine("Contact:", club.contactEmail)
            DetailLine("Meeting information:", club.meetingDay)
            DetailLine("Meeting times:", club.meetingTime)
            DetailLine("Meeting location:", club.meetingLocation)
        }
        Box(Modifier.height(24.dp))
        val joined = membership?.status == MembershipStatus.Active
        FigmaActionButton(
            text = if (joined) "Remove from My Clubs" else "Add to My Clubs",
            icon = if (joined) Icons.Default.Close else Icons.Default.Add,
            modifier = Modifier.fillMaxWidth(),
            onClick = if (joined) onLeaveClick else onJoinClick
        )
        Box(Modifier.height(30.dp))
    }
}

@Composable
private fun DetailLine(label: String, value: String?) {
    Column {
        Text(label, color = FigmaText, fontSize = 14.sp)
        value?.takeIf { it.isNotBlank() }?.let { Text(it, color = FigmaText, fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp)) }
    }
}
