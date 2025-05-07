package at.fhv.sysarch.lab2.homeautomation.commands.airCondition;

public class EnrichedTemperature implements AirConditionCommand{
    private final Double value;
    private final String unit;

    public EnrichedTemperature(Double value, String unit) {
        this.value = value;
        this.unit = unit;
    }

    public Double getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }
}
