package com.precon.mhsclubs.screens.clubs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.precon.mhsclubs.model.Club
import com.precon.mhsclubs.model.UserRole

/**
 * Club list screen displaying all available clubs.
 *
 * Shows a searchable list of clubs with their basic information.
 * Users can click on a club to view its details.
 *
 * @param clubs List of clubs to display
 * @param isLoading Whether clubs are currently being loaded
 * @param onAccountClick Callback when the account button is clicked
 * @param onClubClick Callback when a club is clicked
 */
@Composable
fun ClubListScreen(
    clubs: List<Club> = emptyList(),
    isLoading: Boolean = false,
    onJoinClubClick: () -> Unit = {},
    memberClubIds: Set<String> = emptySet(),
    onClubClick: (String) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top app bar
        TopAppBar(
            title = { Text("MHS Clubs") },
            actions = {
                IconButton(onClick = onJoinClubClick) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Join a club")
                }
            }
        )

        // Search bar
        var searchQuery by remember { mutableStateOf("") }
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search club names") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )

        val visibleClubs = clubs.fuzzyMatch(searchQuery)

        // Club list
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (visibleClubs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No clubs found")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(visibleClubs) { club ->
                    ClubCard(
                        club = club,
                        isMember = club.id in memberClubIds,
                        onClick = { onClubClick(club.id) }
                    )
                }
            }
        }
    }
}

/**
 * Returns club matches ordered by relevance. Exact and prefix matches rank first, while
 * subsequence and small spelling errors keep discovery forgiving on a phone keyboard.
 */
internal fun List<Club>.fuzzyMatch(query: String): List<Club> {
    val normalizedQuery = query.normalizedSearchText()
    if (normalizedQuery.isBlank()) return this

    return mapNotNull { club ->
        val fields = listOf(club.name, club.code, club.category, club.description)
        fields.mapNotNull { field -> field.fuzzyScore(normalizedQuery) }.minOrNull()?.let { score ->
            club to score
        }
    }
        .sortedWith(compareBy<Pair<Club, Int>> { it.second }.thenBy { it.first.name })
        .map { it.first }
}

private fun String.fuzzyScore(query: String): Int? {
    val value = normalizedSearchText()
    if (value.contains(query)) return value.indexOf(query)

    val queryTokens = query.split(' ').filter { it.isNotBlank() }
    val valueTokens = value.split(' ').filter { it.isNotBlank() }
    if (queryTokens.isEmpty()) return 0

    var score = 0
    for (queryToken in queryTokens) {
        val best = valueTokens.minOfOrNull { token ->
            when {
                token.contains(queryToken) -> token.indexOf(queryToken)
                queryToken.isSubsequenceOf(token) -> token.length - queryToken.length
                else -> queryToken.levenshteinDistance(token)
            }
        } ?: return null
        if (best > 2) return null
        score += best
    }
    return score + 10
}

private fun String.normalizedSearchText(): String =
    lowercase().map { character -> if (character.isLetterOrDigit() || character == ' ') character else ' ' }
        .joinToString("").trim()

private fun String.isSubsequenceOf(value: String): Boolean {
    var valueIndex = 0
    for (character in this) {
        valueIndex = value.indexOf(character, valueIndex)
        if (valueIndex < 0) return false
        valueIndex++
    }
    return true
}

private fun String.levenshteinDistance(other: String): Int {
    var previous = IntArray(other.length + 1) { it }
    forEachIndexed { row, character ->
        val current = IntArray(other.length + 1)
        current[0] = row + 1
        other.forEachIndexed { column, otherCharacter ->
            current[column + 1] = minOf(
                previous[column + 1] + 1,
                current[column] + 1,
                previous[column] + if (character == otherCharacter) 0 else 1
            )
        }
        previous = current
    }
    return previous[other.length]
}

/**
 * Card displaying a single club.
 */
@Composable
fun ClubCard(
    club: Club,
    isMember: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Club logo placeholder
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.size(16.dp))

                // Club info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = club.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = club.code,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isMember) {
                        Text(
                            text = "Member",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Club description
            if (club.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = club.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Club category and meeting info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = club.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (club.meetingDay != null) {
                    Text(
                        text = "${club.meetingDay} ${club.meetingTime ?: ""}".trim(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ClubListScreenPreview() {
    MaterialTheme {
        ClubListScreen(
            clubs = listOf(
                Club(
                    id = "1",
                    sheetSourceId = "sheet1",
                    name = "Robotics Club",
                    description = "Build and program robots for competitions",
                    code = "ROBOT",
                    category = "STEM",
                    meetingDay = "Monday",
                    meetingTime = "3:30 PM"
                ),
                Club(
                    id = "2",
                    sheetSourceId = "sheet2",
                    name = "Chess Club",
                    description = "Play chess and improve your skills",
                    code = "CHESS",
                    category = "Games",
                    meetingDay = "Tuesday",
                    meetingTime = "3:15 PM"
                ),
                Club(
                    id = "3",
                    sheetSourceId = "sheet3",
                    name = "Debate Team",
                    description = "Competitive debate and public speaking",
                    code = "DEBATE",
                    category = "Academic",
                    meetingDay = "Wednesday",
                    meetingTime = "3:00 PM"
                )
            ),
            isLoading = false
        )
    }
}
