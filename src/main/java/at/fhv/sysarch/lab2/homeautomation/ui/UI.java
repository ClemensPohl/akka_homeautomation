package at.fhv.sysarch.lab2.homeautomation.ui;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.commands.airCondition.AirConditionCommand;
import at.fhv.sysarch.lab2.homeautomation.commands.blinds.BlindsCommand;
import at.fhv.sysarch.lab2.homeautomation.commands.mediaStation.MediaCommand;
import at.fhv.sysarch.lab2.homeautomation.commands.mediaStation.PlayMovie;
import at.fhv.sysarch.lab2.homeautomation.commands.mediaStation.StopMovie;
import at.fhv.sysarch.lab2.homeautomation.commands.blinds.OpenBlinds;
import at.fhv.sysarch.lab2.homeautomation.commands.blinds.CloseBlinds;
import at.fhv.sysarch.lab2.homeautomation.commands.temperature.ReadTemperature;
import at.fhv.sysarch.lab2.homeautomation.commands.temperature.TemperatureCommand;
import at.fhv.sysarch.lab2.homeautomation.commands.weather.WeatherTypes;
import at.fhv.sysarch.lab2.homeautomation.environment.TemperatureEnvironmentActor;
import at.fhv.sysarch.lab2.homeautomation.environment.WeatherEnvironmentActor;

import java.util.Scanner;

public class UI extends AbstractBehavior<Void> {

    private final ActorRef<TemperatureCommand> tempSensor;
    private final ActorRef<AirConditionCommand> airCondition;
    private final ActorRef<TemperatureEnvironmentActor.TemperatureEnvironmentCommand> tempEnv;
    private final ActorRef<WeatherEnvironmentActor.WeatherEnvironmentCommand> weatherEnv;
    private final ActorRef<BlindsCommand> blinds;
    private final ActorRef<MediaCommand> mediaStation;

    public static Behavior<Void> create(
            ActorRef<TemperatureCommand> tempSensor,
            ActorRef<AirConditionCommand> airCondition,
            ActorRef<TemperatureEnvironmentActor.TemperatureEnvironmentCommand> tempEnv,
            ActorRef<WeatherEnvironmentActor.WeatherEnvironmentCommand> weatherEnv,
            ActorRef<BlindsCommand> blinds,
            ActorRef<MediaCommand> mediaStation
    ) {
        return Behaviors.setup(ctx -> new UI(ctx, tempSensor, airCondition, tempEnv, weatherEnv, blinds, mediaStation));
    }

    private UI(
            ActorContext<Void> context,
            ActorRef<TemperatureCommand> tempSensor,
            ActorRef<AirConditionCommand> airCondition,
            ActorRef<TemperatureEnvironmentActor.TemperatureEnvironmentCommand> tempEnv,
            ActorRef<WeatherEnvironmentActor.WeatherEnvironmentCommand> weatherEnv,
            ActorRef<BlindsCommand> blinds,
            ActorRef<MediaCommand> mediaStation
    ) {
        super(context);
        this.tempSensor = tempSensor;
        this.airCondition = airCondition;
        this.tempEnv = tempEnv;
        this.weatherEnv = weatherEnv;
        this.blinds = blinds;
        this.mediaStation = mediaStation;

        new Thread(this::runCommandLine).start();
        context.getLog().info("UI started");
    }

    private void runCommandLine() {
        Scanner scanner = new Scanner(System.in);
        String input;

        System.out.println("""
                === Home Automation Console ===
                t <value>            → Manually trigger temperature reading
                env-set <value>      → Set fixed environment temperature
                env-start            → Start temperature simulation
                env-stop             → Stop temperature simulation
                weather-set <type>   → Set weather (sunny, cloudy, ...)
                weather-start        → Start weather simulation
                weather-stop         → Stop weather simulation
                blinds-open          → Open blinds
                blinds-close         → Close blinds
                movie-start          → Start a movie
                movie-stop           → Stop the movie
                quit                 → Exit
                """);

        while (scanner.hasNextLine() && !(input = scanner.nextLine()).equalsIgnoreCase("quit")) {
            String[] command = input.trim().split(" ");

            switch (command[0]) {
                case "t":
                    if (command.length == 2)
                        tempSensor.tell(new ReadTemperature(Double.parseDouble(command[1])));
                    break;

                case "env-set":
                    if (command.length == 2)
                        tempEnv.tell(new TemperatureEnvironmentActor.SetTemperature(Double.parseDouble(command[1])));
                    break;

                case "env-start":
                    tempEnv.tell(new TemperatureEnvironmentActor.StartSimulation());
                    break;

                case "env-stop":
                    tempEnv.tell(new TemperatureEnvironmentActor.StopSimulation());
                    break;

                case "weather-set":
                    if (command.length == 2)
                        try {
                            WeatherTypes weatherType = WeatherTypes.valueOf(command[1].toLowerCase());
                            weatherEnv.tell(new WeatherEnvironmentActor.SetWeather(weatherType));
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid weather type. Use 'sunny' or 'cloudy'.");
                        }
                    break;

                case "weather-start":
                    weatherEnv.tell(new WeatherEnvironmentActor.StartSimulation());
                    break;

                case "weather-stop":
                    weatherEnv.tell(new WeatherEnvironmentActor.StopSimulation());
                    break;

                case "blinds-open":
                    blinds.tell(new OpenBlinds());
                    break;
                case "blinds-close":
                    blinds.tell(new CloseBlinds());
                    break;

                case "movie-start":
                    mediaStation.tell(new PlayMovie());
                    break;

                case "movie-stop":
                    mediaStation.tell(new StopMovie());
                    break;

                default:
                    System.out.println("Unknown command.");
            }
        }

        getContext().getLog().info("UI done");
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, sig -> onPostStop()).build();
    }

    private UI onPostStop() {
        getContext().getLog().info("UI stopped");
        return this;
    }
}
