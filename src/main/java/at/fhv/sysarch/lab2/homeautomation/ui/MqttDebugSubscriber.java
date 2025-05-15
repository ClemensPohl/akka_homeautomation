package at.fhv.sysarch.lab2.homeautomation.ui;

import org.eclipse.paho.client.mqttv3.*;

public class MqttDebugSubscriber {
    public static void main(String[] args) throws Exception {
        String broker = "tcp://10.0.40.161:1883";
        String clientId = "debugSubscriber";

        MqttClient client = new MqttClient(broker, clientId);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("Connection lost: " + cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                System.out.println("Received on topic '" + topic + "': " + message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        client.connect(options);
        client.subscribe("#");  // Subscribe to all topics

        System.out.println("Subscribed to all topics. Waiting for messages...");

        // Keep it running
        Thread.sleep(10 * 60 * 1000); // 10 minutes
        client.disconnect();
    }
}
