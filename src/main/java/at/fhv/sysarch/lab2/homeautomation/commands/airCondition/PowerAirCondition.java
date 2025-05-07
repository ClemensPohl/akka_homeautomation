package at.fhv.sysarch.lab2.homeautomation.commands.airCondition;

public final class PowerAirCondition implements AirConditionCommand {
    public final Boolean value;

    public PowerAirCondition(Boolean value) {
        this.value = value;
    }
}
