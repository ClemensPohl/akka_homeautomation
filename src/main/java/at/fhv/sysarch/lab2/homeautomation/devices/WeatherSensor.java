package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.commands.weather.WeatherCommand;
import at.fhv.sysarch.lab2.homeautomation.commands.weather.ReadWeather;
import at.fhv.sysarch.lab2.homeautomation.commands.blinds.BlindsCommand;
import at.fhv.sysarch.lab2.homeautomation.commands.blinds.EnrichedWeather;

public class WeatherSensor extends AbstractBehavior<WeatherCommand> {

    public static Behavior<WeatherCommand> create(ActorRef<BlindsCommand> blinds) {
        return Behaviors.setup(context -> new WeatherSensor(context, blinds));
    }

    private final ActorRef<BlindsCommand> blinds;

    public WeatherSensor(ActorContext<WeatherCommand> context, ActorRef<BlindsCommand> blinds) {
        super(context);
        this.blinds = blinds;
        getContext().getLog().info("WeatherSensor started");
    }

    @Override
    public Receive<WeatherCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReadWeather.class, this::onReadWeather)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<WeatherCommand> onReadWeather(ReadWeather msg) {
        getContext().getLog().info("WeatherSensor received: {}", msg.type);
        blinds.tell(new EnrichedWeather(msg.type));
        return this;
    }

    private WeatherSensor onPostStop() {
        getContext().getLog().info("WeatherSensor stopped");
        return this;
    }
}
