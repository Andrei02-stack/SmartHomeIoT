import network
from umqtt.simple import MQTTClient
from machine import Pin, PWM
from time import sleep
import utime
import sys
import json

# Wi-Fi and MQTT Broker settings
try:
    from config import WIFI_SSID, WIFI_PASSWORD, MQTT_BROKER, MQTT_PORT, MQTT_USER, MQTT_PASSWORD
except ImportError:
    WIFI_SSID = "YOUR_WIFI_SSID"
    WIFI_PASSWORD = "YOUR_WIFI_PASSWORD"
    MQTT_BROKER = "YOUR_MQTT_BROKER_IP"
    MQTT_PORT = 1883
    MQTT_USER = b"YOUR_MQTT_USER"
    MQTT_PASSWORD = b"YOUR_MQTT_PASSWORD"

MQTT_TOPIC = "/pico/lock"  # Topic to listen for messages
USER = MQTT_USER
USER_PASSWORD = MQTT_PASSWORD

# Define the GPIO pin to activate
status = "unlocked"
LOCK_PIN = 14  # Replace with your desired GPIO pin number

# Initialize the pin as an output
lock_pin = PWM(Pin(LOCK_PIN))
lock_pin.freq(50)

def setServoAngle(position):
    lock_pin.duty_u16(position)
    utime.sleep(0.01)

def lock():
    global status
    
    print("Locking...")
    if status == "unlocked":
        setServoAngle(3932)
        status = "locked"

def unlock():
    global status

    print("Unlocking...")
    if status == "locked":
        setServoAngle(1802)
        status = "unlocked"

# Connect to Wi-Fi
def connect_wifi():
    wlan = network.WLAN(network.STA_IF)
    wlan.active(True)
    wlan.connect(WIFI_SSID, WIFI_PASSWORD)
    
    while not wlan.isconnected():
        print("Connecting to Wi-Fi...")
        sleep(1)
    print("Connected to Wi-Fi:", wlan.ifconfig())

# Callback function for MQTT messages
def mqtt_callback(topic, msg):
    print(f"Received message on topic {topic.decode()}: {msg.decode()}")
    crt_msg = msg.decode()
    crt_msg_json = json.loads(crt_msg)

    if crt_msg_json["status"] == "unlock":
        unlock()
    elif crt_msg_json["status"] == "lock":
        lock()

# Connect to MQTT broker
def connect_mqtt():
    client = MQTTClient("pico_client_lock", MQTT_BROKER, port=MQTT_PORT, user=USER, password=USER_PASSWORD)
    client.set_callback(mqtt_callback)
    client.connect()
    print("Connected to MQTT Broker")
    client.subscribe(MQTT_TOPIC, qos=1)
    print(f"Subscribed to topic: {MQTT_TOPIC}")
    return client

# Main program
try:
    connect_wifi()
    mqtt_client = connect_mqtt()

    while True:
        try:
            mqtt_client.check_msg()  # Wait for incoming messages
            sleep(1)
        except AssertionError:
            continue

except KeyboardInterrupt:
    print("Disconnected")
    mqtt_client.disconnect()
finally:
    if status == "locked":
        unlock()
