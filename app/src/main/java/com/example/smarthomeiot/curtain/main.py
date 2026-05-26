import network
from umqtt.simple import MQTTClient
from machine import Pin
from time import sleep
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

MQTT_TOPIC = "/pico/draperie"  # Topic to listen for messages
USER = MQTT_USER
USER_PASSWORD = MQTT_PASSWORD

# Define the GPIO pin to activate
FORWARD_PIN = 20  # Replace with your desired GPIO pin number
BACKWARD_PIN = 26

crt_coord = 50

# Initialize the pin as an output
forward_pin = Pin(FORWARD_PIN, Pin.OUT)
backward_pin = Pin(BACKWARD_PIN, Pin.OUT)

forward_pin.value(0)
backward_pin.value(0)

def forward(delta):
    print("Forward: " + str(delta))
    backward_pin.value(0)
    forward_pin.value(1)  # Turn on the pin
    sleep(delta / 10)             # Keep it on for 1 second
    forward_pin.value(0) # Turn off the pin

def backward(delta):
    print("Backward: " + str(delta))
    forward_pin.value(0)
    backward_pin.value(1)
    sleep(delta / 10)
    backward_pin.value(0)

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
    global crt_coord

    print(f"Received message on topic {topic.decode()}: {msg.decode()}")
    crt_msg = msg.decode()
    json_msg = json.loads(str(crt_msg))
    new_coord = int(json_msg['coord'])

    diff = new_coord - crt_coord

    if diff < 0:
        backward(-diff)
    elif diff > 0:
        forward(diff)
    
    crt_coord = new_coord

# Connect to MQTT broker
def connect_mqtt():
    client = MQTTClient("pico_client_draperie", MQTT_BROKER, port=MQTT_PORT, user=USER, password=USER_PASSWORD)
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
