package com.example.smarthomeiot.model

data class MqttSettings(
    val host: String = "",
    val port: Int = 1883,
    val username: String = "",
    val password: String = "",
)

enum class ConnectionStatus(val label: String) {
    DISCONNECTED("Disconnected"),
    CONNECTING("Connecting"),
    CONNECTED("Connected"),
    ERROR("Error"),
}

enum class DoorLockState(val label: String) {
    LOCKED("Încuiată"),
    UNLOCKED("Descuiată"),
    UNKNOWN("Necunoscut"),
}

enum class DoorContactState(val label: String) {
    CLOSED("Ușă închisă"),
    OPEN("Ușă deschisă"),
    UNKNOWN("Contact necunoscut"),
}

enum class MusicPlaybackState(val label: String) {
    PLAYING("Playing"),
    PAUSED("Paused"),
    UNKNOWN("Unknown"),
}

data class LightState(
    val value: Int? = null,
    val rawStatus: String = "unknown",
) {
    val label: String
        get() {
            val normalized = rawStatus.lowercase()
            return when {
                normalized.contains("dark") || normalized.contains("intuneric") ||
                    normalized.contains("întuneric") -> "Întuneric"
                normalized.contains("dim") || normalized.contains("low") ||
                    normalized.contains("slab") -> "Lumină slabă"
                normalized.contains("bright") || normalized.contains("puternic") -> "Lumină puternică"
                value == null -> "Necunoscut"
                value < 250 -> "Întuneric"
                value < 550 -> "Lumină slabă"
                else -> "Lumină puternică"
            }
        }

    val isBright: Boolean
        get() = label == "Lumină puternică"
}

data class MusicState(
    val playback: MusicPlaybackState = MusicPlaybackState.UNKNOWN,
)

data class DoorState(
    val lockState: DoorLockState = DoorLockState.UNKNOWN,
    val contactState: DoorContactState = DoorContactState.UNKNOWN,
)

data class DeviceState(
    val curtainPosition: Int = 0,
    val light: LightState = LightState(),
    val door: DoorState = DoorState(),
    val music: MusicState = MusicState(),
)

data class LogEntry(
    val message: String,
    val timestampMillis: Long = System.currentTimeMillis(),
)
