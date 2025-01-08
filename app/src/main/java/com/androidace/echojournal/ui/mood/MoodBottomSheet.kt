package com.androidace.echojournal.ui.mood

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.androidace.echojournal.ui.mood.model.Mood
import kotlinx.collections.immutable.PersistentList


@Composable
fun MoodBottomSheet(
    viewModel: MoodBottomSheetViewModel = hiltViewModel()
) {
    val moodList = viewModel.moodList
    MoodBottomSheet(
        moodList =
        moodList,
        onCancel = {},
        onConfirm = {},
    )
}

@Composable
fun MoodBottomSheet(
    moodList: PersistentList<Mood>,
    onCancel: () -> Unit,
    onConfirm: (Mood) -> Unit,
    modifier: Modifier = Modifier
) {
    // Track which mood is currently selected; default = NEUTRAL or null if you prefer
    var selectedMood by remember { mutableStateOf<Mood?>(null) }

    // Container for the bottom sheet content
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "How are you doing?",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(Modifier.height(24.dp))

            // Row of moods
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                moodList.forEach { mood ->
                    MoodItem(
                        mood = mood,
                        isSelected = (mood == selectedMood),
                        onClick = {
                            selectedMood = mood
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Bottom buttons: Cancel and Confirm
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Cancel
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Spacer(Modifier.width(16.dp))

                // Confirm
                Button(
                    onClick = {
                        // Only call onConfirm if a mood is selected
                        selectedMood?.let { onConfirm(it) }
                    },
                    modifier = Modifier.weight(2f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Confirm")
                }
            }
        }
    }

}

@Composable
fun MoodItem(
    mood: Mood,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Decide the tint color for the mood icon & label
    val tintColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        // Slightly faded if not selected
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }

    // A simple column: icon above, label below
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = if (isSelected) painterResource(mood.activeResId) else painterResource(mood.inactiveResId),
            contentDescription = mood.displayName,
            modifier = Modifier.size(40.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = mood.displayName,
            color = tintColor,
            style = MaterialTheme.typography.labelMedium
        )
    }
}