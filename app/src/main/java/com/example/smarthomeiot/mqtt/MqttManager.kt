package com.example.smarthomeiot.mqtt

import com.example.smarthomeiot.model.ConnectionStatus
import com.example.smarthomeiot.model.MqttSettings
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.UUID

class MqttManager(private val listener: Listener) {
    interface Listener {
        fun onConnectionStatus(status: ConnectionStatus, detail: String? = null)
        fun onMessage(topic: String, payload: String)
        fun onLog(message: String)
    }

    private var client: MqttAsyncClient? = null

    fun connect(settings: MqttSettings) {
        val host = settings.host.trim()
        if (host.isBlank()) {
            listener.onConnectionStatus(ConnectionStatus.ERROR, "Broker IP lipsă")
            return
        }

        disconnectInternal(notify = false)
        listener.onConnectionStatus(ConnectionStatus.CONNECTING)

        try {
            val serverUri = buildServerUri(host, settings.port)
            val mqttClient = MqttAsyncClient(
                serverUri,
                "SmartHomeAndroid-${UUID.randomUUID().toString().take(8)}",
                MemoryPersistence(),
            )
            client = mqttClient
            mqttClient.setCallback(callbackFor(mqttClient))

            val options = MqttConnectOptions().apply {
                isCleanSession = true
                isAutomaticReconnect = true
                connectionTimeout = 6
                keepAliveInterval = 30
                if (settings.username.isNotBlank()) {
                    userName = settings.username
                }
                if (settings.password.isNotBlank()) {
                    password = settings.password.toCharArray()
                }
            }

            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    listener.onConnectionStatus(ConnectionStatus.CONNECTED, serverUri)
                    subscribeToStatusTopics(mqttClient)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    listener.onConnectionStatus(
                        ConnectionStatus.ERROR,
                        exception?.message ?: "Conectarea MQTT a eșuat",
                    )
                }
            })
        } catch (exception: MqttException) {
            listener.onConnectionStatus(ConnectionStatus.ERROR, exception.message)
        } catch (exception: IllegalArgumentException) {
            listener.onConnectionStatus(ConnectionStatus.ERROR, exception.message)
        }
    }

    fun disconnect() {
        disconnectInternal(notify = true)
    }

    fun publish(topic: String, payload: String, onResult: (Boolean, String?) -> Unit) {
        val mqttClient = client
        if (mqttClient?.isConnected != true) {
            onResult(false, "MQTT nu este conectat")
            return
        }

        try {
            val message = MqttMessage(payload.toByteArray(Charsets.UTF_8)).apply {
                qos = 0
                isRetained = false
            }
            mqttClient.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    onResult(true, null)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    onResult(false, exception?.message)
                }
            })
        } catch (exception: MqttException) {
            onResult(false, exception.message)
        }
    }

    private fun callbackFor(mqttClient: MqttAsyncClient): MqttCallbackExtended =
        object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                if (reconnect) {
                    listener.onConnectionStatus(ConnectionStatus.CONNECTED, serverURI)
                    subscribeToStatusTopics(mqttClient)
                }
            }

            override fun connectionLost(cause: Throwable?) {
                listener.onConnectionStatus(
                    ConnectionStatus.DISCONNECTED,
                    cause?.message ?: "Conexiunea MQTT s-a pierdut",
                )
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                listener.onMessage(topic, message.payload.toString(Charsets.UTF_8))
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) = Unit
        }

    private fun subscribeToStatusTopics(mqttClient: MqttAsyncClient) {
        MqttTopics.SUBSCRIBE_TOPICS.forEach { topic ->
            try {
                mqttClient.subscribe(topic, 0, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        listener.onLog("Subscribed: $topic")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        listener.onLog("Subscribe failed: $topic")
                    }
                })
            } catch (exception: MqttException) {
                listener.onLog("Subscribe failed: $topic")
            }
        }
    }

    private fun disconnectInternal(notify: Boolean) {
        val mqttClient = client
        client = null
        if (mqttClient == null) {
            if (notify) listener.onConnectionStatus(ConnectionStatus.DISCONNECTED)
            return
        }

        try {
            if (mqttClient.isConnected) {
                mqttClient.disconnect(null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        if (notify) listener.onConnectionStatus(ConnectionStatus.DISCONNECTED)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        if (notify) {
                            listener.onConnectionStatus(ConnectionStatus.ERROR, exception?.message)
                        }
                    }
                })
            } else if (notify) {
                listener.onConnectionStatus(ConnectionStatus.DISCONNECTED)
            }
        } catch (exception: MqttException) {
            if (notify) listener.onConnectionStatus(ConnectionStatus.ERROR, exception.message)
        }
    }

    private fun buildServerUri(host: String, port: Int): String {
        if (host.startsWith("tcp://") || host.startsWith("ssl://")) {
            return host
        }
        val safePort = port.coerceIn(1, 65535)
        return "tcp://$host:$safePort"
    }
}
