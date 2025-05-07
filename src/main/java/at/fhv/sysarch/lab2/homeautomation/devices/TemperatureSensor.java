package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.commands.airCondition.AirConditionCommand;
import at.fhv.sysarch.lab2.homeautomation.commands.airCondition.EnrichedTemperature;
import at.fhv.sysarch.lab2.homeautomation.commands.environment.temperature.ReadTemperature;
import at.fhv.sysarch.lab2.homeautomation.commands.environment.temperature.TemperatureCommand;

public class TemperatureSensor extends AbstractBehavior<TemperatureCommand> {

    public static Behavior<TemperatureCommand> create(ActorRef<AirConditionCommand> airCondition) {
        return Behaviors.setup(context -> new TemperatureSensor(context, airCondition));
    }

    private final ActorRef<AirConditionCommand> airCondition;

    public TemperatureSensor(ActorContext<TemperatureCommand> context, ActorRef<AirConditionCommand> airCondition) {
        super(context);
        this.airCondition = airCondition;

        getContext().getLog().info("TemperatureSensor started");
    }

    @Override
    public Receive<TemperatureCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReadTemperature.class, this::onReadTemperature)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<TemperatureCommand> onReadTemperature(ReadTemperature r) {
        getContext().getLog().info("TemperatureSensor received {}", r.value);
        this.airCondition.tell(new EnrichedTemperature(r.value, "Celsius"));
        return this;
    }

    private TemperatureSensor onPostStop() {
        getContext().getLog().info("TemperatureSensor actor {}-{} stopped");
        return this;
    }

}
