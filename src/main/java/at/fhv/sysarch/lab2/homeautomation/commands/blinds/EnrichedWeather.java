package at.fhv.sysarch.lab2.homeautomation.commands.blinds;

import at.fhv.sysarch.lab2.homeautomation.commands.weather.WeatherTypes;

public class EnrichedWeather implements BlindsCommand {
    public final WeatherTypes type;

    public EnrichedWeather(WeatherTypes type) {
        this.type = type;
    }
}
