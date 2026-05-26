import network
from umqtt.simple import MQTTClient
from machine import Pin, PWM
from time import sleep, sleep_us
import sys
import json
import requests  # Pentru cereri HTTP

try:
    from audio_sample import AUDIO_DATA, SAMPLE_RATE
except ImportError:
    AUDIO_DATA = None
    SAMPLE_RATE = 8000

# Wi-Fi and MQTT Broker settings
try:
    from config import (
        WIFI_SSID,
        WIFI_PASSWORD,
        MQTT_BROKER,
        MQTT_PORT,
        MQTT_USER,
        MQTT_PASSWORD,
        TELEGRAM_BOT_TOKEN,
        TELEGRAM_CHAT_ID,
    )
except ImportError:
    WIFI_SSID = "YOUR_WIFI_SSID"
    WIFI_PASSWORD = "YOUR_WIFI_PASSWORD"
    MQTT_BROKER = "YOUR_MQTT_BROKER_IP"
    MQTT_PORT = 1883
    MQTT_USER = b"YOUR_MQTT_USER"
    MQTT_PASSWORD = b"YOUR_MQTT_PASSWORD"
    TELEGRAM_BOT_TOKEN = ""
    TELEGRAM_CHAT_ID = ""

MQTT_TOPIC = "/pico/music"  # Topic to listen for messages
USER = MQTT_USER
USER_PASSWORD = MQTT_PASSWORD

# Define LED pin
LED_PIN = "LED"  # Onboard LED on Raspberry Pi Pico
led = Pin(LED_PIN, Pin.OUT)

# Define the GPIO pin to activate
PLAY_PIN = 10  # Replace with your desired GPIO pin number
PREV_PIN = 11
NEXT_PIN = 12
AUDIO_PIN = 15  # Connect the speaker/amplifier input to this GPIO for embedded audio tests.

# Initialize the pin as an output
play_pin = Pin(PLAY_PIN, Pin.OUT)
prev_pin = Pin(PREV_PIN, Pin.OUT)
next_pin = Pin(NEXT_PIN, Pin.OUT)
audio_pwm = PWM(Pin(AUDIO_PIN))
is_playing = False

prev_pin.value(0)
next_pin.value(0)
play_pin.value(0)
led.value(0)
audio_pwm.freq(62500)
audio_pwm.duty_u16(0)

def stop_audio():
    audio_pwm.duty_u16(0)

def play_embedded_audio():
    if AUDIO_DATA is None:
        print("No audio_sample.py found; using LED-only test")
        return

    print("Playing embedded audio sample")
    delay_us = int(1000000 / SAMPLE_RATE)
    led.value(1)
    for sample in AUDIO_DATA:
        if not is_playing:
            break
        audio_pwm.duty_u16(sample * 257)
        sleep_us(delay_us)
    stop_audio()

def prev():
    print("Activating prev pin")
    prev_pin.value(1)  # Turn on the pin
    sleep(0.3)             # Keep it on for 1 second
    prev_pin.value(0) # Turn off the pin
    led.value(1)
    sleep(0.1)
    led.value(0)

def next():
    print("Activating next pin")
    next_pin.value(1)
    sleep(0.3)
    next_pin.value(0)
    for _ in range(2):
        led.value(1)
        sleep(0.08)
        led.value(0)
        sleep(0.08)
    
def play(status):
    global is_playing

    print("Activating play pin")
    play_pin.value(1)  # Turn on the pin
    sleep(0.3)             # Keep it on for 1 second
    play_pin.value(0) # Turn off the pin

    if status == "paused":
        is_playing = False
        led.value(0)
        stop_audio()
        print("Paused")
    else:
        is_playing = True
        print("Playing")
        play_embedded_audio()

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
    mess = msg.decode()
    mess_json = json.loads(mess)

    if mess_json["music_action"] == "play":
        print("play")
        play(mess_json.get("music_status", "playing"))
    elif mess_json["music_action"] == "prev":
        print("prev")
        prev()
    elif mess_json["music_action"] == "next":
        print("next")
        next()
        send_telegram_message("Next button pressed!")

# Connect to MQTT broker
def connect_mqtt():
    client = MQTTClient("pico_client", MQTT_BROKER, port=MQTT_PORT, user=USER, password=USER_PASSWORD)
    client.set_callback(mqtt_callback)
    client.connect()
    print("Connected to MQTT Broker")
    client.subscribe(MQTT_TOPIC, qos=1)
    print(f"Subscribed to topic: {MQTT_TOPIC}")
    return client

def send_telegram_message(message):
    if not TELEGRAM_BOT_TOKEN or not TELEGRAM_CHAT_ID:
        print("Telegram not configured")
        return

    url = f"https://api.telegram.org/bot{TELEGRAM_BOT_TOKEN}/sendMessage"
    payload = {"chat_id": TELEGRAM_CHAT_ID, "text": message}
    try:
        response = requests.post(url, json=payload)
        response.close()
        print("Message sent successfully!")
    except Exception as e:
        print(f"Error sending message: {e}")

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
