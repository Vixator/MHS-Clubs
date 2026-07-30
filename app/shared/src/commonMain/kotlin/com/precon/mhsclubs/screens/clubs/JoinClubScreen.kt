package com.precon.mhsclubs.screens.clubs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.precon.mhsclubs.ui.FigmaActionButton
import com.precon.mhsclubs.ui.FigmaBackLabel
import com.precon.mhsclubs.ui.FigmaOutlinedTextField
import com.precon.mhsclubs.ui.FigmaScreen
import com.precon.mhsclubs.ui.FigmaTan
import com.precon.mhsclubs.ui.FigmaText
import com.precon.mhsclubs.ui.FigmaTitle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check

/**
 * Join club screen for entering a club name.
 *
 * Allows students to join a club by entering its name.
 *
 * @param onBackClick Callback when the back button is clicked
 * @param onJoinClick Callback when the user attempts to join with a club name
 * @param isLoading Whether a join request is in progress
 * @param errorMessage Error message to display, if any
 */
@Composable
fun JoinClubScreen(
    onBackClick: () -> Unit = {},
    onJoinClick: (String) -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var clubName by remember { mutableStateOf("") }

    FigmaScreen(Modifier.fillMaxSize()) {
        Box(Modifier.height(19.dp))
        FigmaBackLabel("My Clubs", onBackClick)
        FigmaTitle("Join a Club", compact = true)
        Box(Modifier.height(14.dp))
        Text(
            text = "Enter the name of the club you want to join:",
            color = FigmaText,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
        Box(Modifier.height(20.dp))
        FigmaOutlinedTextField(
            value = clubName,
            onValueChange = { clubName = it },
            placeholder = "e.g., Robotics Club",
            singleLine = true,
            isError = errorMessage != null,
            keyboardType = KeyboardType.Text,
            errorMessage = errorMessage,
            modifier = Modifier.fillMaxWidth()
        )
        Box(Modifier.height(24.dp))
        if (isLoading) {
            CircularProgressIndicator(color = FigmaTan, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            FigmaActionButton(
                text = "Join Club",
                icon = Icons.Default.Check,
                modifier = Modifier.fillMaxWidth(),
                enabled = clubName.isNotBlank(),
                onClick = { onJoinClick(clubName) }
            )
        }
        Box(Modifier.height(24.dp))
        Text(
            text = "Search for the club name exactly as it appears in the club directory.",
            color = FigmaText.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
    }
}

@Preview
@Composable
fun JoinClubScreenPreview() {
    JoinClubScreen()
}

@Preview
@Composable
fun JoinClubScreenWithErrorPreview() {
    JoinClubScreen(
        errorMessage = "Club name not found. Please check and try again."
    )
}
