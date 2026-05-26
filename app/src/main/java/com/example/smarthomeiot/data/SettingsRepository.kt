package com.example.smarthomeiot.data

import android.content.Context
import com.example.smarthomeiot.model.MqttSettings

class SettingsRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): MqttSettings = MqttSettings(
        host = preferences.getString(KEY_HOST, DEFAULT_HOST).orEmpty(),
        port = preferences.getInt(KEY_PORT, DEFAULT_PORT),
        username = preferences.getString(KEY_USERNAME, DEFAULT_USERNAME).orEmpty(),
        password = preferences.getString(KEY_PASSWORD, DEFAULT_PASSWORD).orEmpty(),
    )

    fun save(settings: MqttSettings) {
        preferences.edit()
            .putString(KEY_HOST, settings.host.trim())
            .putInt(KEY_PORT, settings.port.coerceIn(1, 65535))
            .putString(KEY_USERNAME, settings.username.trim())
            .putString(KEY_PASSWORD, settings.password)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "smart_home_mqtt_settings"
        const val KEY_HOST = "host"
        const val KEY_PORT = "port"
        const val KEY_USERNAME = "username"
        const val KEY_PASSWORD = "password"
        const val DEFAULT_HOST = "172.20.10.2"
        const val DEFAULT_PORT = 1883
        const val DEFAULT_USERNAME = ""
        const val DEFAULT_PASSWORD = ""
    }
}
