package com.example.smarthomeiot.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthomeiot.data.SettingsRepository
import com.example.smarthomeiot.model.ConnectionStatus
import com.example.smarthomeiot.model.DeviceState
import com.example.smarthomeiot.model.DoorContactState
import com.example.smarthomeiot.model.DoorLockState
import com.example.smarthomeiot.model.DoorState
import com.example.smarthomeiot.model.LightState
import com.example.smarthomeiot.model.LogEntry
import com.example.smarthomeiot.model.MqttSettings
import com.example.smarthomeiot.model.MusicPlaybackState
import com.example.smarthomeiot.model.MusicState
import com.example.smarthomeiot.mqtt.MqttManager
import com.example.smarthomeiot.mqtt.MqttTopics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

data class SmartHomeUiState(
    val settings: MqttSettings = MqttSettings(),
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val connectionDetail: String? = null,
    val devices: DeviceState = DeviceState(),
    val logs: List<LogEntry> = emptyList(),
)

class SmartHomeViewModel(application: Application) :
    AndroidViewModel(application),
    MqttManager.Listener {

    private val settingsRepository = SettingsRepository(application.applicationContext)
    private val mqttManager = MqttManager(this)

    private val _uiState = MutableStateFlow(
        SmartHomeUiState(settings = settingsRepository.load()),
    )
    val uiState: StateFlow<SmartHomeUiState> = _uiState.asStateFlow()

    init {
        addLog("SmartHome IoT pornit")
        val savedSettings = _uiState.value.settings
        if (savedSettings.host.isNotBlank()) {
            addLog("Conectare automată la ${savedSettings.host}:${savedSettings.port}")
            mqttManager.connect(savedSettings)
        }
    }

    fun updateSettings(settings: MqttSettings) {
        _uiState.update { it.copy(settings = settings.copy(port = settings.port.coerceIn(1, 65535))) }
    }

    fun saveSettings() {
        val settings = _uiState.value.settings
        settingsRepository.save(settings)
        addLog("Setări MQTT salvate")
    }

    fun connect() {
        val settings = _uiState.value.settings
        if (settings.host.isBlank()) {
            _uiState.update {
                it.copy(
                    connectionStatus = ConnectionStatus.ERROR,
                    connectionDetail = "Introdu IP-ul brokerului MQTT",
                )
            }
            addLog("Conectare anulată: IP broker lipsă")
            return
        }

        settingsRepository.save(settings)
        addLog("Conectare la ${settings.host}:${settings.port}")
        mqttManager.connect(settings)
    }

    fun testConnection() {
        addLog("Test conexiune MQTT")
        connect()
    }

    fun disconnect() {
        addLog("Deconectare MQTT")
        mqttManager.disconnect()
    }

    fun setCurtainPosition(position: Int) {
        val safePosition = position.coerceIn(0, 100)
        _uiState.update {
            it.copy(devices = it.devices.copy(curtainPosition = safePosition))
        }
        publishJson(
            topic = MqttTopics.CURTAIN_COMMAND,
            payload = JSONObject().put("coord", safePosition).toString(),
            label = "draperie $safePosition%",
        )
    }

    fun lockDoor() {
        _uiState.update {
            it.copy(devices = it.devices.copy(door = it.devices.door.copy(lockState = DoorLockState.LOCKED)))
        }
        publishJson(
            topic = MqttTopics.LOCK_COMMAND,
            payload = JSONObject().put("status", "lock").toString(),
            label = "lock",
        )
    }

    fun unlockDoor() {
        _uiState.update {
            it.copy(devices = it.devices.copy(door = it.devices.door.copy(lockState = DoorLockState.UNLOCKED)))
        }
        publishJson(
            topic = MqttTopics.LOCK_COMMAND,
            payload = JSONObject().put("status", "unlock").toString(),
            label = "unlock",
        )
    }

    fun sendMusicAction(action: String) {
        if (action == "play") {
            val nextState = when (_uiState.value.devices.music.playback) {
                MusicPlaybackState.PLAYING -> MusicPlaybackState.PAUSED
                else -> MusicPlaybackState.PLAYING
            }
            _uiState.update {
                it.copy(devices = it.devices.copy(music = MusicState(nextState)))
            }
        }

        publishJson(
            topic = MqttTopics.MUSIC_COMMAND,
            payload = JSONObject().put("music_action", action).toString(),
            label = "music $action",
        )
    }

    override fun onConnectionStatus(status: ConnectionStatus, detail: String?) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(connectionStatus = status, connectionDetail = detail)
            }
            addLog(
                when (status) {
                    ConnectionStatus.CONNECTED -> "Connected to broker"
                    ConnectionStatus.CONNECTING -> "Connecting to broker"
                    ConnectionStatus.DISCONNECTED -> "Disconnected from broker"
                    ConnectionStatus.ERROR -> "MQTT error: ${detail ?: "necunoscut"}"
                },
            )
        }
    }

    override fun onMessage(topic: String, payload: String) {
        viewModelScope.launch {
            addLog("Received $topic")
            parseIncomingMessage(topic, payload)
        }
    }

    override fun onLog(message: String) {
        viewModelScope.launch {
            addLog(message)
        }
    }

    override fun onCleared() {
        mqttManager.disconnect()
        super.onCleared()
    }

    private fun publishJson(topic: String, payload: String, label: String) {
        mqttManager.publish(topic, payload) { success, error ->
            viewModelScope.launch {
                if (success) {
                    addLog("Sent command: $label")
                } else {
                    addLog("Send failed: $label (${error ?: "eroare MQTT"})")
                }
            }
        }
    }

    private fun parseIncomingMessage(topic: String, payload: String) {
        val json = runCatching { JSONObject(payload) }.getOrElse {
            addLog("JSON invalid pe $topic")
            return
        }

        when (topic) {
            MqttTopics.LIGHT_STATUS -> parseLight(json)
            MqttTopics.PLAYER_STATUS -> parsePlayer(json)
            MqttTopics.LOCK_COMMAND,
            MqttTopics.LOCK_STATUS -> parseDoorLock(json)
            MqttTopics.DOOR_STATUS -> parseDoorContact(json)
        }
    }

    private fun parseLight(json: JSONObject) {
        val lightValue = if (json.has("light")) json.optInt("light") else null
        val status = json.optString("status", "unknown")
        _uiState.update {
            it.copy(devices = it.devices.copy(light = LightState(lightValue, status)))
        }
        addLog("Received light status")
    }

    private fun parsePlayer(json: JSONObject) {
        val status = json.optString("music_status", json.optString("status", "unknown")).lowercase()
        val playback = when (status) {
            "playing", "play" -> MusicPlaybackState.PLAYING
            "paused", "pause", "stopped", "stop" -> MusicPlaybackState.PAUSED
            else -> MusicPlaybackState.UNKNOWN
        }
        _uiState.update {
            it.copy(devices = it.devices.copy(music = MusicState(playback)))
        }
        addLog("Audio ${playback.label}")
    }

    private fun parseDoorLock(json: JSONObject) {
        val status = json.optString("status", "unknown").lowercase()
        val lockState = when (status) {
            "lock", "locked", "incuiata", "încuiată" -> DoorLockState.LOCKED
            "unlock", "unlocked", "descuiata", "descuiată" -> DoorLockState.UNLOCKED
            else -> _uiState.value.devices.door.lockState
        }
        val contactState = parseContactFromJson(json) ?: _uiState.value.devices.door.contactState
        _uiState.update {
            it.copy(devices = it.devices.copy(door = DoorState(lockState, contactState)))
        }
    }

    private fun parseDoorContact(json: JSONObject) {
        val contactState = parseContactFromJson(json) ?: DoorContactState.UNKNOWN
        _uiState.update {
            it.copy(devices = it.devices.copy(door = it.devices.door.copy(contactState = contactState)))
        }
    }

    private fun parseContactFromJson(json: JSONObject): DoorContactState? {
        if (json.has("is_open")) {
            return if (json.optBoolean("is_open")) DoorContactState.OPEN else DoorContactState.CLOSED
        }

        val raw = when {
            json.has("door") -> json.optString("door")
            json.has("reed") -> json.optString("reed")
            json.has("contact") -> json.optString("contact")
            json.has("status") -> json.optString("status")
            else -> ""
        }.lowercase()

        return when (raw) {
            "open", "opened", "deschisa", "deschisă" -> DoorContactState.OPEN
            "closed", "close", "inchisa", "închisă" -> DoorContactState.CLOSED
            "true" -> DoorContactState.CLOSED
            "false" -> DoorContactState.OPEN
            else -> null
        }
    }

    private fun addLog(message: String) {
        _uiState.update { state ->
            state.copy(
                logs = (listOf(LogEntry(message)) + state.logs).take(MAX_LOGS),
            )
        }
    }

    private companion object {
        const val MAX_LOGS = 80
    }
}
