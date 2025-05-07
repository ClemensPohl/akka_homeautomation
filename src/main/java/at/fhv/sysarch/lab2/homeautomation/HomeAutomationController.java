package at.fhv.sysarch.lab2.homeautomation;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.AirCondition;
import at.fhv.sysarch.lab2.homeautomation.devices.TemperatureSensor;
import at.fhv.sysarch.lab2.homeautomation.commands.airCondition.AirConditionCommand;
import at.fhv.sysarch.lab2.homeautomation.commands.environment.temperature.TemperatureCommand;
import at.fhv.sysarch.lab2.homeautomation.ui.UI;

import java.util.UUID;

public class HomeAutomationController extends AbstractBehavior<Void>{

    public static Behavior<Void> create() {
        return Behaviors.setup(HomeAutomationController::new);
    }

    private  HomeAutomationController(ActorContext<Void> context) {
        super(context);
        // TODO: consider guardians and hierarchies. Who should create and communicate with which Actors?
        ActorRef<AirConditionCommand> airCondition = getContext().spawn(AirCondition.create(UUID.randomUUID().toString()), "AirCondition");


        ActorRef<TemperatureCommand> tempSensor = getContext().spawn(TemperatureSensor.create(airCondition), "temperatureSensor");


        ActorRef<Void> ui = getContext().spawn(UI.create(tempSensor, airCondition), "UI");


        getContext().getLog().info("HomeAutomation Application started");
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private HomeAutomationController onPostStop() {
        getContext().getLog().info("HomeAutomation Application stopped");
        return this;
    }
}
