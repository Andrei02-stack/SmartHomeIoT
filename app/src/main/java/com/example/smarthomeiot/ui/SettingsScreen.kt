package com.example.smarthomeiot.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.smarthomeiot.model.ConnectionStatus
import com.example.smarthomeiot.model.MqttSettings
import com.example.smarthomeiot.viewmodel.SmartHomeUiState

@Composable
fun SettingsScreen(
    state: SmartHomeUiState,
    onSettingsChange: (MqttSettings) -> Unit,
    onSave: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onTestConnection: () -> Unit,
    onBack: () -> Unit,
) {
    var portText by remember(state.settings.port) { mutableStateOf(state.settings.port.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF78AED6),
                        Color(0xFFD9C796),
                        Color(0xFF8C714B),
                    ),
                ),
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SettingsHeader(onBack = onBack)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.46f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                ConnectionSummary(state = state)

                OutlinedTextField(
                    value = state.settings.host,
                    onValueChange = { onSettingsChange(state.settings.copy(host = it.trim())) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Broker IP") },
                    placeholder = { Text("192.168.1.100") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = portText,
                    onValueChange = { value ->
                        val filtered = value.filter(Char::isDigit).take(5)
                        portText = filtered
                        onSettingsChange(
                            state.settings.copy(
                                port = filtered.toIntOrNull()?.coerceIn(1, 65535) ?: 1883,
                            ),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Port MQTT") },
                    placeholder = { Text("1883") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.settings.username,
                    onValueChange = { onSettingsChange(state.settings.copy(username = it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Username MQTT") },
                    placeholder = { Text("opțional") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.settings.password,
                    onValueChange = { onSettingsChange(state.settings.copy(password = it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Parolă MQTT") },
                    placeholder = { Text("opțional") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 14.dp),
                    ) {
                        Icon(Icons.Outlined.Save, contentDescription = null)
                        Text(" Save")
                    }
                    if (state.connectionStatus == ConnectionStatus.CONNECTED) {
                        OutlinedButton(
                            onClick = onDisconnect,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 14.dp),
                        ) {
                            Icon(Icons.Outlined.LinkOff, contentDescription = null)
                            Text(" Disconnect")
                        }
                    } else {
                        Button(
                            onClick = onConnect,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                            ),
                        ) {
                            Icon(Icons.Outlined.CloudDone, contentDescription = null)
                            Text(" Connect")
                        }
                    }
                }

                OutlinedButton(
                    onClick = onTestConnection,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp),
                ) {
                    Text("Test Connection")
                }
            }
        }
    }
}

@Composable
private fun SettingsHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Înapoi",
                tint = Color.White,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "MQTT Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Broker Mosquitto local",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.82f),
            )
        }
    }
}

@Composable
private fun ConnectionSummary(state: SmartHomeUiState) {
    val detail = state.connectionDetail
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Status conexiune: ${state.connectionStatus.label}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        if (!detail.isNullOrBlank()) {
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
