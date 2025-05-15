package at.fhv.sysarch.lab2.homeautomation.environment;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.commands.weather.WeatherTypes;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class MqttEnvironmentActor extends AbstractBehavior<MqttEnvironmentActor.MqttCommand> {

    public interface MqttCommand {}

    public static class MessageReceived implements MqttCommand {
        public final String topic;
        public final String message;

        public MessageReceived(String topic, String message) {
            this.topic = topic;
            this.message = message;
        }
    }

    public static class SetEnvironmentMode implements MqttCommand {
        public final EnvironmentMode mode;

        public SetEnvironmentMode(EnvironmentMode mode) {
            this.mode = mode;
        }
    }

    private final String broker = "tcp://10.0.40.161:1883";
    private final List<String> topics = Arrays.asList("weather/condition", "temperature/value");
    private MqttClient client;
    private final ActorRef<WeatherEnvironmentActor.WeatherEnvironmentCommand> weatherController;
    private final ActorRef<TemperatureEnvironmentActor.TemperatureEnvironmentCommand> temperatureController;

    private EnvironmentMode currentMode = EnvironmentMode.EXTERNAL;

    public static Behavior<MqttCommand> create(
            ActorRef<WeatherEnvironmentActor.WeatherEnvironmentCommand> weatherController,
            ActorRef<TemperatureEnvironmentActor.TemperatureEnvironmentCommand> temperatureController
    ) {
        return Behaviors.setup(ctx -> new MqttEnvironmentActor(ctx, weatherController, temperatureController));
    }

    private MqttEnvironmentActor(
            ActorContext<MqttCommand> context,
            ActorRef<WeatherEnvironmentActor.WeatherEnvironmentCommand> weatherController,
            ActorRef<TemperatureEnvironmentActor.TemperatureEnvironmentCommand> temperatureController
    ) {
        super(context);
        this.weatherController = weatherController;
        this.temperatureController = temperatureController;
        startMqttClient();
    }

    private void startMqttClient() {
        try {
            client = new MqttClient(broker, "environmentController");

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    getContext().getLog().error("MQTT Connection lost", cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) {
                    String payload = new String(mqttMessage.getPayload());
                    getContext().getSelf().tell(new MessageReceived(topic, payload));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);

            IMqttToken connectToken = client.connectWithResult(options);
            connectToken.waitForCompletion();

            if (connectToken.getException() != null) {
                throw connectToken.getException();
            }

            getContext().getLog().info("Connected to MQTT broker: {}", broker);

            for (String topic : topics) {
                IMqttToken subToken = client.subscribeWithResponse(topic);
                subToken.waitForCompletion();
                if (subToken.getException() != null) {
                    getContext().getLog().error("Subscription to {} failed", topic, subToken.getException());
                } else {
                    getContext().getLog().info("Subscribed to topic: {}", topic);
                }
            }

        } catch (MqttException e) {
            getContext().getLog().error("Failed to start MQTT client", e);
        }
    }

    @Override
    public Receive<MqttCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(MessageReceived.class, this::onMessageReceived)
                .onMessage(SetEnvironmentMode.class, this::onSetMode)
                .build();
    }

    private Behavior<MqttCommand> onMessageReceived(MessageReceived msg) {
        // Only react if mode is EXTERNAL
        if (currentMode != EnvironmentMode.EXTERNAL) {
            getContext().getLog().info("Ignoring MQTT message because mode is {}", currentMode);
            return this;
        }

        switch (msg.topic) {
            case "weather/condition":
                WeatherTypes weatherType = parseWeather(msg.message);
                if (weatherType != null) {
                    weatherController.tell(new WeatherEnvironmentActor.SetWeather(weatherType));
                }
                break;

            case "temperature/value":
                Double temperature = parseTemperature(msg.message);
                if (temperature != null) {
                    temperatureController.tell(new TemperatureEnvironmentActor.SetTemperature(temperature));
                }
                break;

            default:
                getContext().getLog().warn("Unknown MQTT topic '{}': {}", msg.topic, msg.message);
        }
        return this;
    }

    private Behavior<MqttCommand> onSetMode(SetEnvironmentMode msg) {
        getContext().getLog().info("MQTT Environment mode set to {}", msg.mode);
        currentMode = msg.mode;
        return this;
    }

    private WeatherTypes parseWeather(String payload) {
        try {
            JSONObject json = new JSONObject(payload);
            String condition = json.getString("condition").toUpperCase();
            return WeatherTypes.valueOf(condition);
        } catch (Exception e) {
            getContext().getLog().error("Failed to parse weather condition: {}", payload, e);
            return null;
        }
    }

    private Double parseTemperature(String payload) {
        try {
            JSONObject json = new JSONObject(payload);
            return json.getDouble("temperature");
        } catch (Exception e) {
            getContext().getLog().error("Failed to parse temperature: {}", payload, e);
            return null;
        }
    }
}
