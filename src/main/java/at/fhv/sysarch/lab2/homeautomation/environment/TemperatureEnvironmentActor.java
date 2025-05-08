package at.fhv.sysarch.lab2.homeautomation.environment;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.commands.temperature.ReadTemperature;
import at.fhv.sysarch.lab2.homeautomation.commands.temperature.TemperatureCommand;

import java.time.Duration;
import java.util.Random;

public class TemperatureEnvironmentActor extends AbstractBehavior<TemperatureEnvironmentActor.TemperatureEnvironmentCommand> {

    public interface TemperatureEnvironmentCommand {}

    public static class Tick implements TemperatureEnvironmentCommand {}

    public static class StartSimulation implements TemperatureEnvironmentCommand {}

    public static class StopSimulation implements TemperatureEnvironmentCommand {}

    public static class SetTemperature implements TemperatureEnvironmentCommand {
        public final double value;

        public SetTemperature(double value) {
            this.value = value;
        }
    }

    private final ActorRef<TemperatureCommand> sensor;
    private final TimerScheduler<TemperatureEnvironmentCommand> timers;
    private final Random random = new Random();
    private boolean simulate = true;
    private double currentTemperature = 22.0;

    private TemperatureEnvironmentActor(ActorContext<TemperatureEnvironmentCommand> context,
                                        TimerScheduler<TemperatureEnvironmentCommand> timers,
                                        ActorRef<TemperatureCommand> sensor) {
        super(context);
        this.sensor = sensor;
        this.timers = timers;

        timers.startTimerAtFixedRate(new Tick(), Duration.ofSeconds(2));
        getContext().getLog().info("TemperatureEnvironmentActor started with initial temperature: {}", currentTemperature);
    }

    public static Behavior<TemperatureEnvironmentCommand> create(ActorRef<TemperatureCommand> sensor) {
        return Behaviors.withTimers(timers -> Behaviors.setup(ctx -> new TemperatureEnvironmentActor(ctx, timers, sensor)));
    }

    @Override
    public Receive<TemperatureEnvironmentCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(Tick.class, this::onTick)
                .onMessage(StartSimulation.class, this::onStart)
                .onMessage(StopSimulation.class, this::onStop)
                .onMessage(SetTemperature.class, this::onSetTemperature)
                .build();
    }

    private Behavior<TemperatureEnvironmentCommand> onTick(Tick msg) {
        int minTemp = 18;
        int maxTemp = 25;

        if (simulate) {
            double delta = (currentTemperature > minTemp) ? -0.5 : (currentTemperature < maxTemp ? 0.5 : 0);
            currentTemperature += delta;
            currentTemperature = Math.max(minTemp, Math.min(currentTemperature, maxTemp));

            sensor.tell(new ReadTemperature(currentTemperature));
        }

        return this;
    }


    private Behavior<TemperatureEnvironmentCommand> onStart(StartSimulation msg) {
        simulate = true;
        getContext().getLog().info("Temperature simulation resumed");
        return this;
    }

    private Behavior<TemperatureEnvironmentCommand> onStop(StopSimulation msg) {
        simulate = false;
        getContext().getLog().info("Temperature simulation paused");
        return this;
    }

    private Behavior<TemperatureEnvironmentCommand> onSetTemperature(SetTemperature msg) {
        simulate = false;
        currentTemperature = msg.value;
        getContext().getLog().info("Manually set temperature to {}", currentTemperature);

        sensor.tell(new ReadTemperature(currentTemperature));
        return this;
    }

}
