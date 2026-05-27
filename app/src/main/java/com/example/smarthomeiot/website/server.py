from flask import Flask, jsonify, render_template, request
from paho.mqtt import client as mqtt
from time import sleep
import json
import os
from datetime import datetime

app = Flask(__name__)

# Global variable to track the current status
status = "Paused"
status_lock = "unlocked"
crt_coord = 50
client = None
connected = False
connection_detail = "Broker local 172.20.10.3:1883"
logs = []
mqtt_settings = {
    "host": os.environ.get("MQTT_HOST", "172.20.10.3"),
    "port": int(os.environ.get("MQTT_PORT", "1883")),
    "username": os.environ.get("MQTT_USERNAME", "website"),
    "password": os.environ.get("MQTT_PASSWORD", ""),
}
SSL_CERT = "/home/lucian/iot_certificates/cert/CA/smarthomeiot.local.crt"
SSL_KEY = "/home/lucian/iot_certificates/cert/CA/smarthomeiot.local.key"

def add_log(message):
    logs.insert(0, {
        "time": datetime.now().strftime("%H:%M:%S"),
        "message": message,
    })
    del logs[60:]

def current_state():
    return {
        "status": status,
        "lock": status_lock,
        "coord": crt_coord,
        "connected": connected,
        "connection_detail": connection_detail,
        "logs": logs,
        "settings": mqtt_settings,
    }

def publish_mqtt(topic, payload, qos):
    global client

    if client is None:
        add_log(f"MQTT client indisponibil. Publish omis: {topic}")
        return

    client.publish(topic, payload, qos=qos)
    add_log(f"Publish {topic}: {payload}")

# Functions for button actions
def play_action():
    global status
    global client

    if status == "Playing":
        status = "Paused"
        publish_mqtt("/pico/music", json.dumps({"music_action" : "play", "music_status" : "paused"}), qos=1)
    else:
        status = "Playing"
        publish_mqtt("/pico/music", json.dumps({"music_action" : "play", "music_status" : "playing"}), qos=1)
    
    add_log("Play/Pause button pressed")

def prev_action():
    global status
    global client
    status = "Playing"

    publish_mqtt("/pico/music", json.dumps({"music_action" : "prev", "music_status" : "playing"}), qos=1)

    add_log("Previous button pressed")

def next_action():
    global status
    global client
    status = "Playing"

    publish_mqtt("/pico/music", json.dumps({"music_action" : "next", "music_status" : "playing"}), qos=1)

    add_log("Next button pressed")

def curtain_coord(delta):
    global client
    
    publish_mqtt("/pico/draperie", json.dumps({"coord" : delta}), qos=1)
    
    add_log(f"Curtain coord sent: {delta}%")

def lock_action():
    global client
    global status_lock

    publish_mqtt("/pico/lock", json.dumps({"status" : "lock"}), qos=1)
    status_lock = "locked"
    
    add_log("Lock action sent")

def unlock_action():
    global client
    global status_lock

    publish_mqtt("/pico/lock", json.dumps({"status" : "unlock"}), qos=1)
    status_lock = "unlocked"
    
    add_log("Unlock action sent")

def morning_scene():
    curtain_coord(100)

def leave_scene():
    global status_lock

    curtain_coord(0)
    if status_lock == "unlocked":
        lock_action()

def handle_action(form):
    global crt_coord

    if "play" in form:
        play_action()
    elif "prev" in form:
        prev_action()
    elif "next" in form:
        next_action()
    elif "morning" in form:
        morning_scene()
        crt_coord = 100
        add_log("Scene applied: Good Morning")
    elif "leave" in form:
        leave_scene()
        crt_coord = 0
        add_log("Scene applied: Leave Home")
    elif "lock" in form:
        if status_lock == "unlocked":
            lock_action()
        else:
            add_log("Already locked")
    elif "unlock" in form:
        if status_lock == "locked":
            unlock_action()
        else:
            add_log("Already unlocked")
    elif "coordinate" in form:
        crt_coord = int(form["coordinate"])
        curtain_coord(crt_coord)

@app.route("/", methods=["GET", "POST"])
def index():
    if request.method == "POST":
        handle_action(request.form)
    
    return render_template("index.html", **current_state())

@app.route("/api/action", methods=["POST"])
def api_action():
    data = request.get_json(silent=True) or request.form.to_dict()
    handle_action(data)
    return jsonify(current_state())

@app.route("/logs")
def logs_page():
    return render_template("logs.html", **current_state())

@app.route("/settings", methods=["GET", "POST"])
def settings_page():
    global connection_detail

    if request.method == "POST":
        mqtt_settings["host"] = request.form.get("host", mqtt_settings["host"]).strip() or "127.0.0.1"
        mqtt_settings["port"] = int(request.form.get("port", mqtt_settings["port"]) or 1883)
        mqtt_settings["username"] = request.form.get("username", mqtt_settings["username"])
        mqtt_settings["password"] = request.form.get("password", mqtt_settings["password"])
        connection_detail = f"Broker local {mqtt_settings['host']}:{mqtt_settings['port']}"
        add_log("MQTT settings saved")

    return render_template("settings.html", **current_state())

def on_connect(*args):
    global connected
    connected = True
    add_log("MQTT connected")

def start_mqtt_client():
    global client
    global connected

    client = mqtt.Client(client_id="webclient")
    client.username_pw_set(mqtt_settings["username"], mqtt_settings["password"])
    connected = False
    client.on_connect = on_connect
    client.loop_start()
    client.connect(mqtt_settings["host"], mqtt_settings["port"])

    while not connected:
        sleep(1)

    client.publish("TEST", "123")

def ssl_context():
    if os.path.exists(SSL_CERT) and os.path.exists(SSL_KEY):
        return (SSL_CERT, SSL_KEY)
    return None

if __name__ == "__main__":
    start_mqtt_client()
    app.run(host="0.0.0.0", port=5000, debug=False, ssl_context=ssl_context())
