package com.precon.mhsclubs.screens.clubs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top bar with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = "Join a Club",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Instructions
        Text(
            text = "Enter the name of the club you want to join:",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = clubName,
            onValueChange = { clubName = it },
            label = { Text("Club name") },
            placeholder = { Text("e.g., Robotics Club") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Error message
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Join button
        Button(
            onClick = { onJoinClick(clubName) },
            enabled = clubName.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                // Loading indicator would go here
                Text("Joining...")
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Join"
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Join Club")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Club code help
        Text(
            text = "Search for the club name exactly as it appears in the club directory.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
fun JoinClubScreenPreview() {
    MaterialTheme {
        JoinClubScreen()
    }
}

@Preview
@Composable
fun JoinClubScreenWithErrorPreview() {
    MaterialTheme {
        JoinClubScreen(
            errorMessage = "Club name not found. Please check and try again."
        )
    }
}
