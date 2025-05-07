package at.fhv.sysarch.lab2.homeautomation.commands.environment.temperature;

public final class ReadTemperature implements  TemperatureCommand{
    public final Double value;
    
    public ReadTemperature(Double value) {
        this.value = value;
    }
}
