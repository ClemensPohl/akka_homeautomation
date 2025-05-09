package at.fhv.sysarch.lab2.homeautomation.environment;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.commands.weather.WeatherCommand;
import at.fhv.sysarch.lab2.homeautomation.commands.weather.ReadWeather;
import at.fhv.sysarch.lab2.homeautomation.commands.weather.WeatherTypes;

import java.time.Duration;
import java.util.Random;

public class WeatherEnvironmentActor extends AbstractBehavior<WeatherEnvironmentActor.WeatherEnvironmentCommand> {

    public interface WeatherEnvironmentCommand {}

    public static class Tick implements WeatherEnvironmentCommand {}

    public static class StartSimulation implements WeatherEnvironmentCommand {}

    public static class StopSimulation implements WeatherEnvironmentCommand {}

    public static class SetWeather implements WeatherEnvironmentCommand {
        public final WeatherTypes value;

        public SetWeather(WeatherTypes value) {
            this.value = value;
        }
    }

    private final ActorRef<WeatherCommand> weatherSensor;
    private final TimerScheduler<WeatherEnvironmentCommand> timers;
    private final Random random = new Random();
    private boolean simulate = true;
    private WeatherTypes currentWeather = WeatherTypes.sunny;

    private WeatherEnvironmentActor(ActorContext<WeatherEnvironmentCommand> context,
                                    TimerScheduler<WeatherEnvironmentCommand> timers,
                                    ActorRef<WeatherCommand> weatherSensor) {
        super(context);
        this.weatherSensor = weatherSensor;
        this.timers = timers;

        // Periodic tick
        timers.startTimerAtFixedRate(new Tick(), Duration.ofSeconds(5));
        getContext().getLog().info("WeatherEnvironmentActor started with initial weather: {}", currentWeather);
    }

    public static Behavior<WeatherEnvironmentCommand> create(ActorRef<WeatherCommand> weatherSensor) {
        return Behaviors.withTimers(timers -> Behaviors.setup(ctx -> new WeatherEnvironmentActor(ctx, timers, weatherSensor)));
    }

    @Override
    public Receive<WeatherEnvironmentCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(Tick.class, this::onTick)
                .onMessage(StartSimulation.class, this::onStartSimulation)
                .onMessage(StopSimulation.class, this::onStopSimulation)
                .onMessage(SetWeather.class, this::onSetWeather)
                .build();
    }

    private Behavior<WeatherEnvironmentCommand> onTick(Tick tick) {
        if (simulate) {
            WeatherTypes[] values = WeatherTypes.values();
            WeatherTypes newWeather = values[random.nextInt(values.length)];
            currentWeather = newWeather;
            getContext().getLog().info("Simulated weather: {}", currentWeather);
            weatherSensor.tell(new ReadWeather(currentWeather));
        }
        return this;
    }

    private Behavior<WeatherEnvironmentCommand> onStartSimulation(StartSimulation cmd) {
        simulate = true;
        getContext().getLog().info("Weather simulation started");
        return this;
    }

    private Behavior<WeatherEnvironmentCommand> onStopSimulation(StopSimulation cmd) {
        simulate = false;
        getContext().getLog().info("Weather simulation stopped");
        return this;
    }

    private Behavior<WeatherEnvironmentCommand> onSetWeather(SetWeather cmd) {
        simulate = false;
        currentWeather = cmd.value;
        getContext().getLog().info("Manually set weather to {}", currentWeather);
        weatherSensor.tell(new ReadWeather(currentWeather));
        return this;
    }
}
