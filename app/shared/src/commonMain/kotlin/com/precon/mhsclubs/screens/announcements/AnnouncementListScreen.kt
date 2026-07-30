package com.precon.mhsclubs.screens.announcements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.precon.mhsclubs.model.Club
import com.precon.mhsclubs.ui.FigmaCard
import com.precon.mhsclubs.ui.FigmaDarkText
import com.precon.mhsclubs.ui.FigmaPill
import com.precon.mhsclubs.ui.FigmaClubFilter
import com.precon.mhsclubs.ui.FigmaRed
import com.precon.mhsclubs.ui.FigmaScreen
import com.precon.mhsclubs.ui.FigmaText
import com.precon.mhsclubs.ui.FigmaTitle
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun AnnouncementListScreen(announcements: List<Announcement> = emptyList(), clubs: List<Club> = emptyList(), onAnnouncementClick: (String) -> Unit = {}) {
    var selectedClubId by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val visible = announcements.filter { selectedClubId == null || selectedClubId == it.clubId }
    FigmaScreen(Modifier.fillMaxSize()) {
        Box(Modifier.height(42.dp))
        FigmaTitle("Announcements")
        Box(Modifier.height(8.dp))
        Box {
            FigmaClubFilter(
                clubs.firstOrNull { it.id == selectedClubId }?.name ?: "Club Selector",
                onClick = { expanded = true }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("All clubs") }, onClick = { selectedClubId = null; expanded = false })
                clubs.forEach { club -> DropdownMenuItem(text = { Text(club.name) }, onClick = { selectedClubId = club.id; expanded = false }) }
            }
        }
        Box(Modifier.height(15.dp))
        LazyColumn(Modifier.fillMaxSize()) {
            items(visible, key = { it.id }) { announcement ->
                AnnouncementCard(
                    announcement,
                    clubs.firstOrNull { it.id == announcement.clubId }?.name ?: announcement.title
                ) { onAnnouncementClick(announcement.id) }
                Box(Modifier.height(22.dp))
            }
            if (visible.isEmpty()) item { Text("No announcements yet", color = FigmaText, fontSize = 14.sp) }
        }
    }
}

@Composable
fun AnnouncementCard(announcement: Announcement, clubName: String, onClick: () -> Unit) {
    FigmaCard(Modifier.fillMaxWidth().height(100.dp), onClick = onClick) {
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text(
                    buildAnnotatedString {
                        append(clubName)
                        append(" · ")
                        withStyle(SpanStyle(color = FigmaRed)) { append(announcement.authorName) }
                    },
                    color = FigmaDarkText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(announcement.content, color = FigmaDarkText, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Column {
                FigmaPill(announcement.postedAt.timeLabel(), borderColor = FigmaText)
                Box(Modifier.height(10.dp))
                FigmaPill(announcement.postedAt.dateLabel(), borderColor = FigmaText)
            }
        }
    }
}

data class Announcement(val id: String, val clubId: String, val title: String, val content: String, val authorId: String, val authorName: String, val isActive: Boolean, val postedAt: Instant, val updatedAt: Instant)
private fun Instant.timeLabel(): String = toLocalDateTime(TimeZone.currentSystemDefault()).let { "${(it.hour + 11) % 12 + 1}:${it.minute.toString().padStart(2, '0')} ${if (it.hour < 12) "AM" else "PM"}" }
private fun Instant.dateLabel(): String = toLocalDateTime(TimeZone.currentSystemDefault()).let { "${it.month.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() }} ${it.dayOfMonth}, ${it.year}" }
