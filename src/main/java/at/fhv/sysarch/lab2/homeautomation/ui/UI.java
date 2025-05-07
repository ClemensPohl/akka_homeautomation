package at.fhv.sysarch.lab2.homeautomation.ui;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.commands.airCondition.AirConditionCommand;
import at.fhv.sysarch.lab2.homeautomation.commands.temperature.ReadTemperature;
import at.fhv.sysarch.lab2.homeautomation.commands.temperature.TemperatureCommand;

import java.util.Scanner;

public class UI extends AbstractBehavior<Void> {

    private final ActorRef<TemperatureCommand> tempSensor;
    private final ActorRef<AirConditionCommand> airCondition;

    public static Behavior<Void> create(ActorRef<TemperatureCommand> tempSensor, ActorRef<AirConditionCommand> airCondition) {
        return Behaviors.setup(context -> new UI(context, tempSensor, airCondition));
    }

    private  UI(ActorContext<Void> context, ActorRef<TemperatureCommand> tempSensor, ActorRef<AirConditionCommand> airCondition) {
        super(context);
        this.airCondition = airCondition;
        this.tempSensor = tempSensor;
        new Thread(this::runCommandLine).start();

        getContext().getLog().info("UI started");
    }

    public void runCommandLine() {
        // TODO: Create Actor for UI Input-Handling?
        Scanner scanner = new Scanner(System.in);
        String[] input = null;
        String reader = "";


        while (!reader.equalsIgnoreCase("quit") && scanner.hasNextLine()) {
            reader = scanner.nextLine();
            // TODO: change input handling
            String[] command = reader.split(" ");
            if(command[0].equals("t")) {
                this.tempSensor.tell(new ReadTemperature(Double.valueOf(command[1])));
            }
            // TODO: process Input
        }
        getContext().getLog().info("UI done");
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private UI onPostStop() {
        getContext().getLog().info("UI stopped");
        return this;
    }
}
