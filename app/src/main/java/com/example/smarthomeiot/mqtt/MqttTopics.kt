package com.example.smarthomeiot.mqtt

object MqttTopics {
    const val CURTAIN_COMMAND = "/pico/draperie"
    const val LIGHT_STATUS = "/pico/light"

    const val LOCK_COMMAND = "/pico/lock"
    const val LOCK_STATUS = "/pico/lock_status"
    const val DOOR_STATUS = "/pico/door_status"

    const val MUSIC_COMMAND = "/pico/music"
    const val PLAYER_STATUS = "/pico/player_status"

    val SUBSCRIBE_TOPICS = listOf(
        LIGHT_STATUS,
        LOCK_COMMAND,
        LOCK_STATUS,
        DOOR_STATUS,
        PLAYER_STATUS,
    )
}
