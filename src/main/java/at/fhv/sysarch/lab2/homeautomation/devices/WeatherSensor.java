package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.commands.weather.WeatherCommand;

public class WeatherSensor extends AbstractBehavior<WeatherCommand> {

    public WeatherSensor(ActorContext<WeatherCommand> context) {
        super(context);
    }

    @Override
    public Receive<WeatherCommand> createReceive() {
        return null;
    }
}
