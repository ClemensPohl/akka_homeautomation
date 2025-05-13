package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.commands.blinds.BlindsCommand;
import at.fhv.sysarch.lab2.homeautomation.commands.blinds.CloseBlinds;
import at.fhv.sysarch.lab2.homeautomation.commands.blinds.EnrichedWeather;
import at.fhv.sysarch.lab2.homeautomation.commands.blinds.OpenBlinds;
import at.fhv.sysarch.lab2.homeautomation.commands.weather.WeatherTypes;

public class Blinds extends AbstractBehavior<BlindsCommand> {

    private final String identifier;
    private boolean isClosed = false;

    public Blinds(ActorContext<BlindsCommand> context, String identifier) {
        super(context);
        this.identifier = identifier;
        getContext().getLog().info("Blinds actor started");
    }

    public static Behavior<BlindsCommand> create(String identifier) {
        return Behaviors.setup(context -> new Blinds(context, identifier));
    }

    @Override
    public Receive<BlindsCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(EnrichedWeather.class, this::onWeatherUpdate)
                .onMessage(OpenBlinds.class, this::onOpenBlinds)
                .onMessage(CloseBlinds.class, this::onCloseBlinds)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }


    private Behavior<BlindsCommand> onWeatherUpdate(EnrichedWeather msg) {
        if (msg.type == WeatherTypes.SUNNY) {
            isClosed = true;
        } else {
            isClosed = false;
        }
        String status = isClosed ? "CLOSED" : "OPEN";
        getContext().getLog().info("Blinds are now {}", status);
        return this;
    }

    private Behavior<BlindsCommand> onOpenBlinds(OpenBlinds cmd) {
        isClosed = false;
        getContext().getLog().info("Blinds manually OPENED");
        return this;
    }

    private Behavior<BlindsCommand> onCloseBlinds(CloseBlinds cmd) {
        isClosed = true;
        getContext().getLog().info("Blinds manually CLOSED");
        return this;
    }


    private Blinds onPostStop() {
        getContext().getLog().info("Blinds actor {} stopped", identifier);
        return this;
    }
}
