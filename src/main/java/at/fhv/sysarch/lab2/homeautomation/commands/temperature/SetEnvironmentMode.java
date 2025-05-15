package at.fhv.sysarch.lab2.homeautomation.commands.temperature;

import at.fhv.sysarch.lab2.homeautomation.environment.EnvironmentMode;
import at.fhv.sysarch.lab2.homeautomation.environment.TemperatureEnvironmentActor;


import at.fhv.sysarch.lab2.homeautomation.environment.TemperatureEnvironmentActor.TemperatureEnvironmentCommand;

public class SetEnvironmentMode implements TemperatureEnvironmentCommand {
    public final EnvironmentMode mode;

    public SetEnvironmentMode(EnvironmentMode mode) {
        this.mode = mode;
    }
}
