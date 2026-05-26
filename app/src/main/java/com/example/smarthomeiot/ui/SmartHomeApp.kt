package com.example.smarthomeiot.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smarthomeiot.viewmodel.SmartHomeViewModel
import kotlinx.coroutines.delay

private enum class AppScreen {
    SPLASH,
    HOME,
    SETTINGS,
    LOGS,
}

@Composable
fun SmartHomeApp(viewModel: SmartHomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var screen by rememberSaveable { mutableStateOf(AppScreen.SPLASH) }

    LaunchedEffect(Unit) {
        delay(1200)
        screen = AppScreen.HOME
    }

    BackHandler(enabled = screen != AppScreen.HOME) {
        screen = AppScreen.HOME
    }

    AnimatedContent(
        targetState = screen,
        transitionSpec = {
            fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(180))
        },
        label = "screen-transition",
    ) { target ->
        when (target) {
            AppScreen.SPLASH -> SplashScreen()
            AppScreen.HOME -> HomeScreen(
                state = state,
                onSettings = { screen = AppScreen.SETTINGS },
                onLogs = { screen = AppScreen.LOGS },
                onConnect = viewModel::connect,
                onCurtainPosition = viewModel::setCurtainPosition,
                onLock = viewModel::lockDoor,
                onUnlock = viewModel::unlockDoor,
                onMusicAction = viewModel::sendMusicAction,
            )
            AppScreen.SETTINGS -> SettingsScreen(
                state = state,
                onSettingsChange = viewModel::updateSettings,
                onSave = viewModel::saveSettings,
                onConnect = viewModel::connect,
                onDisconnect = viewModel::disconnect,
                onTestConnection = viewModel::testConnection,
                onBack = { screen = AppScreen.HOME },
            )
            AppScreen.LOGS -> LogsScreen(
                logs = state.logs,
                onBack = { screen = AppScreen.HOME },
            )
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(42.dp),
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = "SmartHome IoT",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Connecting to your home...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(26.dp))
            AnimatedVisibility(visible = true) {
                CircularProgressIndicator()
            }
        }
    }
}
