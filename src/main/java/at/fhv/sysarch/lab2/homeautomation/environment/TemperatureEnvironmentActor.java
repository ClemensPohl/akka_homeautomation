package at.fhv.sysarch.lab2.homeautomation.environment;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.commands.temperature.ReadTemperature;
import at.fhv.sysarch.lab2.homeautomation.commands.temperature.TemperatureCommand;

import java.time.Duration;
import java.util.Random;

public class TemperatureEnvironmentActor extends AbstractBehavior<TemperatureEnvironmentActor.TempActorCommand> {

    public interface TempActorCommand {}

    public static class Tick implements TempActorCommand {}

    public static class StartSimulation implements TempActorCommand {}

    public static class StopSimulation implements TempActorCommand {}

    public static class SetTemperature implements TempActorCommand {
        public final double value;

        public SetTemperature(double value) {
            this.value = value;
        }
    }

    private final ActorRef<TemperatureCommand> sensor;
    private final TimerScheduler<TempActorCommand> timers;
    private final Random random = new Random();
    private boolean simulate = true;
    private double currentTemperature = 21.0;

    private TemperatureEnvironmentActor(ActorContext<TempActorCommand> context,
                                        TimerScheduler<TempActorCommand> timers,
                                        ActorRef<TemperatureCommand> sensor) {
        super(context);
        this.sensor = sensor;
        this.timers = timers;

        // Start periodic updates
        timers.startTimerAtFixedRate(new Tick(), Duration.ofSeconds(2));
        getContext().getLog().info("TemperatureEnvironmentActor started with initial temperature: {}", currentTemperature);
    }

    public static Behavior<TempActorCommand> create(ActorRef<TemperatureCommand> sensor) {
        return Behaviors.withTimers(timers -> Behaviors.setup(ctx -> new TemperatureEnvironmentActor(ctx, timers, sensor)));
    }

    @Override
    public Receive<TempActorCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(Tick.class, this::onTick)
                .onMessage(StartSimulation.class, this::onStart)
                .onMessage(StopSimulation.class, this::onStop)
                .onMessage(SetTemperature.class, this::onSetTemperature)
                .build();
    }

    private Behavior<TempActorCommand> onTick(Tick msg) {
        if (simulate) {
            double delta = -2 + (random.nextDouble()); // Change between -0.5 to +0.5
            currentTemperature += delta;
            getContext().getLog().info("Simulated temperature: {}", currentTemperature);

            sensor.tell(new ReadTemperature(currentTemperature));
        }
        return this;
    }

    private Behavior<TempActorCommand> onStart(StartSimulation msg) {
        simulate = true;
        getContext().getLog().info("Temperature simulation resumed");
        return this;
    }

    private Behavior<TempActorCommand> onStop(StopSimulation msg) {
        simulate = false;
        getContext().getLog().info("Temperature simulation paused");
        return this;
    }

    private Behavior<TempActorCommand> onSetTemperature(SetTemperature msg) {
        simulate = false;
        currentTemperature = msg.value;
        getContext().getLog().info("Manually set temperature to {}", currentTemperature);

        sensor.tell(new ReadTemperature(currentTemperature));
        return this;
    }

}
