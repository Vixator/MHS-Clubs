package com.precon.mhsclubs.screens.clubs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.precon.mhsclubs.model.Club
import com.precon.mhsclubs.models.Event
import com.precon.mhsclubs.ui.FigmaCard
import com.precon.mhsclubs.ui.FigmaDarkText
import com.precon.mhsclubs.ui.FigmaPage
import com.precon.mhsclubs.ui.FigmaPill
import com.precon.mhsclubs.ui.FigmaScreen
import com.precon.mhsclubs.ui.FigmaRed
import com.precon.mhsclubs.ui.FigmaTan
import com.precon.mhsclubs.ui.FigmaText
import com.precon.mhsclubs.ui.FigmaTitle
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun ClubListScreen(
    clubs: List<Club> = emptyList(),
    isLoading: Boolean = false,
    onJoinClubClick: () -> Unit = {},
    memberClubIds: Set<String> = emptySet(),
    nextMeetings: Map<String, Event> = emptyMap(),
    onClubClick: (String) -> Unit = {},
    title: String = "My Clubs",
    backLabel: String? = null,
    onBackClick: (() -> Unit)? = null
) {
    FigmaScreen(Modifier.fillMaxSize()) {
        if (backLabel != null && onBackClick != null) {
            Text(
                "‹  $backLabel",
                color = FigmaTan,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(top = 20.dp, bottom = 10.dp)
                    .clickable(onClick = onBackClick)
            )
        } else Box(Modifier.height(42.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            FigmaTitle(title, compact = title != "My Clubs", modifier = Modifier.weight(1f))
            if (title == "My Clubs") Icon(Icons.Default.Add, "Browse school clubs", tint = FigmaText, modifier = Modifier.padding(8.dp).clickable(onClick = onJoinClubClick))
        }
        Box(Modifier.height(11.dp))
        if (isLoading) {
            Text("Loading clubs…", color = FigmaText, modifier = Modifier.padding(16.dp))
        } else if (clubs.isEmpty()) {
            Text(if (title == "My Clubs") "Add a club to see it here." else "No clubs available.", color = FigmaText, modifier = Modifier.padding(top = 20.dp))
        } else if (title == "School Clubs") {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxSize()) {
                items(clubs, key = { it.id }) { club ->
                    Row(
                        modifier = Modifier.fillMaxWidth().height(52.dp).background(FigmaTan).clickable { onClubClick(club.id) }.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (club.id in memberClubIds) Icon(Icons.Default.StarBorder, null, tint = FigmaDarkText)
                        if (club.id in memberClubIds) Box(Modifier.width(13.dp))
                        Text(if (club.id in memberClubIds) "${club.name}" else club.name, color = FigmaDarkText, fontSize = 17.sp, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.KeyboardArrowRight, null, tint = FigmaDarkText)
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxSize()) {
                items(clubs, key = { it.id }) { club ->
                    ClubCard(club, club.id in memberClubIds, nextMeetings[club.id], onClick = { onClubClick(club.id) })
                }
            }
        }
    }
}

@Composable
fun ClubCard(club: Club, isMember: Boolean = false, nextMeeting: Event? = null, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier.fillMaxWidth().height(205.dp).clip(shape).background(FigmaTan)
            .border(1.dp, Color.White, shape).clickable(onClick = onClick)
    ) {
        Column(Modifier.align(Alignment.TopStart).padding(start = 14.dp, top = 17.dp).width(180.dp)) {
            Text(club.name, color = FigmaDarkText, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Meeting details", color = FigmaDarkText, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
        }
        FigmaPill(
            nextMeeting?.let(::formatTime) ?: (club.meetingTime ?: "TBD"),
            background = FigmaPage.copy(alpha = .55f), contentColor = FigmaDarkText,
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 9.dp, top = 10.dp)
        )
        Box(Modifier.align(Alignment.TopEnd).offset(x = (-52).dp, y = 45.dp).width(1.dp).height(75.dp).background(FigmaDarkText))
        FigmaPill(
            nextMeeting?.endTime?.let(::formatEndTime) ?: "TBD",
            borderColor = FigmaRed, background = FigmaPage.copy(alpha = .55f), contentColor = FigmaDarkText,
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 9.dp, top = 120.dp)
        )
        FigmaPill(club.meetingLocation ?: "TBD", borderColor = FigmaDarkText, modifier = Modifier.align(Alignment.BottomStart).padding(start = 14.dp, bottom = 8.dp))
        FigmaPill(nextMeeting?.let(::formatDate) ?: (club.meetingDay ?: "TBD"), borderColor = FigmaDarkText, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 9.dp, bottom = 8.dp))
    }
}

private fun formatTime(event: Event): String = event.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).let { "${(it.hour + 11) % 12 + 1}:${it.minute.toString().padStart(2, '0')} ${if (it.hour < 12) "AM" else "PM"}" }
private fun formatEndTime(time: kotlinx.datetime.Instant): String = time.toLocalDateTime(TimeZone.currentSystemDefault()).let { "${(it.hour + 11) % 12 + 1}:${it.minute.toString().padStart(2, '0')} ${if (it.hour < 12) "AM" else "PM"}" }
private fun formatDate(event: Event): String = event.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).let { "${it.month.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() }} ${it.dayOfMonth}, ${it.year}" }
