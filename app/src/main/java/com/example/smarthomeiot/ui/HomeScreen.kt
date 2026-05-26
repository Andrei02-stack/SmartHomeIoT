package com.example.smarthomeiot.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.smarthomeiot.model.ConnectionStatus
import com.example.smarthomeiot.model.DoorLockState
import com.example.smarthomeiot.model.MusicPlaybackState
import com.example.smarthomeiot.viewmodel.SmartHomeUiState
import kotlin.math.roundToInt

private val ClimateColor = Color(0xFF31A7D8)
private val LightColor = Color(0xFFFFC83D)
private val SecurityColor = Color(0xFF2D9B73)
private val AudioColor = Color(0xFFE58B2D)
private val CurtainColor = Color(0xFF4C8DDA)
private val BackgroundTop = Color(0xFF78AED6)
private val BackgroundMid = Color(0xFFD9C796)
private val BackgroundBottom = Color(0xFF8C714B)
private const val LiquidPanelAlpha = 0.74f
private const val LiquidStrongAlpha = 0.82f

@Composable
fun HomeScreen(
    state: SmartHomeUiState,
    onSettings: () -> Unit,
    onLogs: () -> Unit,
    onConnect: () -> Unit,
    onCurtainPosition: (Int) -> Unit,
    onLock: () -> Unit,
    onUnlock: () -> Unit,
    onMusicAction: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(homeBackgroundBrush()),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 158.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                HomeHeader(
                    state = state,
                    onSettings = onSettings,
                    onLogs = onLogs,
                )
            }
            item {
                StatusStrip(
                    state = state,
                    onConnect = onConnect,
                )
            }
            item {
                SectionHeader(title = "Scenes")
            }
            item {
                SceneRow(
                    onMorning = { onCurtainPosition(100) },
                    onLeave = {
                        onLock()
                        onCurtainPosition(0)
                    },
                )
            }
            item {
                SectionHeader(title = "Nursery")
            }
            item {
                NurseryGrid(
                    state = state,
                    onCurtainPosition = onCurtainPosition,
                )
            }
            item {
                SectionHeader(title = "Entry")
            }
            item {
                DoorTile(state = state, onLock = onLock, onUnlock = onUnlock)
            }
            item {
                SectionHeader(title = "Media")
            }
            item {
                AudioTile(state = state, onMusicAction = onMusicAction)
            }
        }

        HomeBottomBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            onLogs = onLogs,
            onSettings = onSettings,
        )
    }
}

@Composable
private fun HomeHeader(
    state: SmartHomeUiState,
    onSettings: () -> Unit,
    onLogs: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "< SmartHome IoT",
                color = Color.White.copy(alpha = 0.86f),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onLogs) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = "Logs",
                    tint = Color.White.copy(alpha = 0.86f),
                )
            }
            IconButton(onClick = onSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Setări MQTT",
                    tint = Color.White.copy(alpha = 0.86f),
                )
            }
        }
        Text(
            text = "My Home",
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "MQTT ${state.connectionStatus.label}",
            color = Color.White.copy(alpha = 0.82f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StatusStrip(
    state: SmartHomeUiState,
    onConnect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HomeStatusChip(
            title = "Climate",
            subtitle = state.devices.light.value?.let { "$it lux" } ?: "No Data",
            icon = if (state.devices.light.isBright) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
            accent = ClimateColor,
            selected = true,
        )
        HomeStatusChip(
            title = "Curtains",
            subtitle = "${state.devices.curtainPosition}% Open",
            icon = Icons.Outlined.Tune,
            accent = CurtainColor,
        )
        HomeStatusChip(
            title = "Security",
            subtitle = state.devices.door.lockState.label,
            icon = if (state.devices.door.lockState == DoorLockState.LOCKED) {
                Icons.Outlined.Lock
            } else {
                Icons.Outlined.LockOpen
            },
            accent = SecurityColor,
        )
        HomeStatusChip(
            title = "Broker",
            subtitle = state.connectionStatus.label,
            icon = if (state.connectionStatus == ConnectionStatus.CONNECTED) {
                Icons.Outlined.CloudDone
            } else {
                Icons.Outlined.CloudOff
            },
            accent = if (state.connectionStatus == ConnectionStatus.CONNECTED) SecurityColor else Color.White,
            onClick = onConnect,
        )
    }
}

@Composable
private fun HomeStatusChip(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val container = if (selected) {
        liquidPanelColor(strong = true)
    } else {
        Color(0xFF4E5B62).copy(alpha = 0.48f)
    }
    val titleColor = if (selected) MaterialTheme.colorScheme.onSurface else Color.White
    val subtitleColor = if (selected) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        Color.White.copy(alpha = 0.75f)
    }

    Row(
        modifier = Modifier
            .background(container, RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = if (selected) 0.55f else 0.24f),
                shape = RoundedCornerShape(8.dp),
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(19.dp),
        )
        Column {
            Text(
                text = title,
                color = titleColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                text = subtitle,
                color = subtitleColor,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = "$title >",
        color = Color.White.copy(alpha = 0.9f),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 2.dp),
    )
}

@Composable
private fun SceneRow(
    onMorning: () -> Unit,
    onLeave: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SceneButton(
            title = "Good Morning",
            icon = Icons.Outlined.LightMode,
            accent = LightColor,
            modifier = Modifier.weight(1f),
            onClick = onMorning,
        )
        SceneButton(
            title = "Leave Home",
            icon = Icons.Outlined.Lock,
            accent = Color.White.copy(alpha = 0.82f),
            modifier = Modifier.weight(1f),
            onClick = onLeave,
            dark = true,
        )
    }
}

@Composable
private fun SceneButton(
    title: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier,
    dark: Boolean = false,
    onClick: () -> Unit,
) {
    val container = if (dark) {
        Color(0xFF5E5548).copy(alpha = 0.58f)
    } else {
        liquidPanelColor(strong = true)
    }
    val textColor = if (dark) Color.White else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier
            .height(58.dp)
            .background(container, RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = if (dark) 0.2f else 0.5f),
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(23.dp),
        )
        Text(
            text = title,
            color = textColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun NurseryGrid(
    state: SmartHomeUiState,
    onCurtainPosition: (Int) -> Unit,
) {
    BoxWithConstraints {
        if (maxWidth >= 620.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    CurtainTile(state = state, onCurtainPosition = onCurtainPosition)
                }
                Box(modifier = Modifier.weight(1f)) {
                    SensorsTile(state = state)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CurtainTile(state = state, onCurtainPosition = onCurtainPosition)
                SensorsTile(state = state)
            }
        }
    }
}

@Composable
private fun CurtainTile(
    state: SmartHomeUiState,
    onCurtainPosition: (Int) -> Unit,
) {
    var sliderValue by remember(state.devices.curtainPosition) {
        mutableFloatStateOf(state.devices.curtainPosition.toFloat())
    }
    val sliderPercent = sliderValue.roundToInt().coerceIn(0, 100)

    HomeTile(
        title = "Shades",
        subtitle = "$sliderPercent% Open",
        icon = Icons.Outlined.Tune,
        accent = CurtainColor,
    ) {
        Text(
            text = "$sliderPercent%",
            style = MaterialTheme.typography.headlineLarge,
            color = CurtainColor,
            fontWeight = FontWeight.Bold,
        )
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onCurtainPosition(sliderPercent) },
            valueRange = 0f..100f,
            steps = 99,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MiniAction(text = "0", modifier = Modifier.weight(1f)) { onCurtainPosition(0) }
            MiniAction(text = "50", modifier = Modifier.weight(1f)) { onCurtainPosition(50) }
            MiniAction(text = "100", modifier = Modifier.weight(1f)) { onCurtainPosition(100) }
        }
    }
}

@Composable
private fun SensorsTile(state: SmartHomeUiState) {
    val light = state.devices.light
    HomeTile(
        title = "Light Sensor",
        subtitle = light.label,
        icon = if (light.isBright) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
        accent = LightColor,
    ) {
        Text(
            text = light.value?.let { "$it" } ?: "--",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Ambient light",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DoorTile(
    state: SmartHomeUiState,
    onLock: () -> Unit,
    onUnlock: () -> Unit,
) {
    val door = state.devices.door
    val locked = door.lockState == DoorLockState.LOCKED
    HomeTile(
        title = "Front Door",
        subtitle = "${door.lockState.label} · ${door.contactState.label}",
        icon = if (locked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
        accent = SecurityColor,
        wide = true,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(
                onClick = onLock,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = SecurityColor),
                contentPadding = PaddingValues(vertical = 13.dp),
            ) {
                Icon(Icons.Outlined.Lock, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lock")
            }
            OutlinedButton(
                onClick = onUnlock,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 13.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
            ) {
                Icon(Icons.Outlined.LockOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Unlock")
            }
        }
    }
}

@Composable
private fun AudioTile(
    state: SmartHomeUiState,
    onMusicAction: (String) -> Unit,
) {
    val playing = state.devices.music.playback == MusicPlaybackState.PLAYING
    HomeTile(
        title = "Audio",
        subtitle = state.devices.music.playback.label,
        icon = Icons.Outlined.MusicNote,
        accent = AudioColor,
        wide = true,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconRoundButton(
                icon = Icons.Outlined.SkipPrevious,
                onClick = { onMusicAction("prev") },
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = { onMusicAction("play") },
                modifier = Modifier.weight(1.35f),
                colors = ButtonDefaults.buttonColors(containerColor = AudioColor),
                contentPadding = PaddingValues(vertical = 13.dp),
            ) {
                Icon(
                    imageVector = if (playing) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Play")
            }
            IconRoundButton(
                icon = Icons.Outlined.SkipNext,
                onClick = { onMusicAction("next") },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HomeTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    wide: Boolean = false,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (wide) 0.dp else 176.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = liquidPanelColor()),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.46f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(accent.copy(alpha = 0.14f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(23.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun MiniAction(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun IconRoundButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 13.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
    ) {
        Icon(imageVector = icon, contentDescription = null)
    }
}

@Composable
private fun HomeBottomBar(
    modifier: Modifier = Modifier,
    onLogs: () -> Unit,
    onSettings: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF7A705A).copy(alpha = 0.66f), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomItem(icon = Icons.Outlined.Home, label = "Home", selected = true)
        BottomItem(icon = Icons.Outlined.History, label = "Logs", onClick = onLogs)
        BottomItem(icon = Icons.Outlined.Settings, label = "Settings", onClick = onSettings)
    }
}

@Composable
private fun BottomItem(
    icon: ImageVector,
    label: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 18.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Color.White else Color.White.copy(alpha = 0.45f),
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.45f),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun homeBackgroundBrush(): Brush {
    return Brush.verticalGradient(
        listOf(
            BackgroundTop,
            BackgroundMid,
            BackgroundBottom,
        ),
    )
}

@Composable
private fun glassColor(): Color {
    return Color.White
}

@Composable
private fun liquidPanelColor(strong: Boolean = false): Color {
    return glassColor().copy(alpha = if (strong) LiquidStrongAlpha else LiquidPanelAlpha)
}
