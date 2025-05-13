package at.fhv.sysarch.lab2.homeautomation;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import at.fhv.sysarch.lab2.homeautomation.commands.airCondition.AirConditionCommand;
import at.fhv.sysarch.lab2.homeautomation.commands.temperature.TemperatureCommand;
import at.fhv.sysarch.lab2.homeautomation.commands.weather.WeatherCommand;
import at.fhv.sysarch.lab2.homeautomation.commands.blinds.BlindsCommand;
import at.fhv.sysarch.lab2.homeautomation.commands.mediaStation.MediaCommand;

import at.fhv.sysarch.lab2.homeautomation.devices.AirCondition;
import at.fhv.sysarch.lab2.homeautomation.devices.TemperatureSensor;
import at.fhv.sysarch.lab2.homeautomation.devices.WeatherSensor;
import at.fhv.sysarch.lab2.homeautomation.devices.Blinds;
import at.fhv.sysarch.lab2.homeautomation.devices.MediaStation;

import at.fhv.sysarch.lab2.homeautomation.environment.TemperatureEnvironmentActor;
import at.fhv.sysarch.lab2.homeautomation.environment.WeatherEnvironmentActor;
import at.fhv.sysarch.lab2.homeautomation.ui.UI;

import java.util.UUID;

public class HomeAutomationController extends AbstractBehavior<Void> {

    public static Behavior<Void> create() {
        return Behaviors.setup(HomeAutomationController::new);
    }

    private HomeAutomationController(ActorContext<Void> context) {
        super(context);

        ActorRef<AirConditionCommand> airCondition =
                context.spawn(AirCondition.create(UUID.randomUUID().toString()), "AirCondition");

        ActorRef<TemperatureCommand> temperatureSensor =
                context.spawn(TemperatureSensor.create(airCondition), "TemperatureSensor");

        ActorRef<TemperatureEnvironmentActor.TemperatureEnvironmentCommand> temperatureEnv =
                context.spawn(TemperatureEnvironmentActor.create(temperatureSensor), "TemperatureEnvironment");

        ActorRef<BlindsCommand> blinds =
                context.spawn(Blinds.create(UUID.randomUUID().toString()), "Blinds");

        ActorRef<WeatherCommand> weatherSensor =
                context.spawn(WeatherSensor.create(blinds), "WeatherSensor");

        ActorRef<WeatherEnvironmentActor.WeatherEnvironmentCommand> weatherEnv =
                context.spawn(WeatherEnvironmentActor.create(weatherSensor), "WeatherEnvironment");

        ActorRef<MediaCommand> mediaStation =
                context.spawn(MediaStation.create(blinds), "MediaStation");

        ActorRef<Void> ui = context.spawn(
                UI.create(temperatureSensor, airCondition, temperatureEnv, weatherEnv, blinds, mediaStation),
                "UI");

        getContext().getLog().info("HomeAutomation Application started");
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder()
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private HomeAutomationController onPostStop() {
        getContext().getLog().info("HomeAutomation Application stopped");
        return this;
    }
}
