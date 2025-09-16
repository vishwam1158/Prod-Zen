package com.viz.prodzen.ui.screens.intervention

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun InterventionScreen(
    interventionType: String,
    onClose: () -> Unit,
    onContinue: () -> Unit,
    viewModel: InterventionViewModel = hiltViewModel()
) {
    val intentionText by viewModel.intentionText.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (interventionType) {
                "PAUSE_EXERCISE" -> PauseScreenContent(onClose, onContinue)
                "REQUIRE_INTENTION" -> IntentionScreenContent(
                    intentionText = intentionText,
                    onTextChange = { viewModel.onIntentionTextChanged(it) },
                    onClose = onClose,
                    onContinue = onContinue
                )
                "LIMIT_EXCEEDED" -> LimitExceededScreenContent(onClose)
                "FOCUS_SESSION" -> FocusSessionScreenContent(onClose)
                else -> PauseScreenContent(onClose, onContinue) // Default fallback
            }
        }
    }
}

@Composable
fun InterventionLayout(
    icon: ImageVector,
    title: String,
    message: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxHeight()
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        content()
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun PauseScreenContent(onClose: () -> Unit, onContinue: () -> Unit) {
    InterventionLayout(
        icon = Icons.Default.Snooze,
        title = "Take a Breath",
        message = "You're about to open this app. Is this what you intended to do right now?"
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Continue to App", fontSize = 16.sp, modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close App", fontSize = 16.sp, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Composable
fun IntentionScreenContent(
    intentionText: String,
    onTextChange: (String) -> Unit,
    onClose: () -> Unit,
    onContinue: () -> Unit
) {
    InterventionLayout(
        icon = Icons.Default.Shield,
        title = "What's Your Intention?",
        message = "State your purpose for using this app to continue."
    ) {
        OutlinedTextField(
            value = intentionText,
            onValueChange = onTextChange,
            label = { Text("e.g., 'To check messages from family'") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onContinue,
            enabled = intentionText.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Continue with Intention", fontSize = 16.sp, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Nevermind", fontSize = 16.sp, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun LimitExceededScreenContent(onClose: () -> Unit) {
    InterventionLayout(
        icon = Icons.Default.TimerOff,
        title = "Time's Up!",
        message = "You've reached your daily limit for this app. Come back tomorrow!"
    ) {
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Got It, Close App", fontSize = 16.sp, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun FocusSessionScreenContent(onClose: () -> Unit) {
    InterventionLayout(
        icon = Icons.Default.Shield,
        title = "Focus Session Active",
        message = "This app is blocked to help you stay focused. You can resume using it after your session ends."
    ) {
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Back to Focusing", fontSize = 16.sp, modifier = Modifier.padding(8.dp))
        }
    }
}