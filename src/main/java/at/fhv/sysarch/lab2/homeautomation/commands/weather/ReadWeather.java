package at.fhv.sysarch.lab2.homeautomation.commands.weather;

public class ReadWeather implements WeatherCommand{
    public final WeatherTypes type;

    public ReadWeather(WeatherTypes type) {
        this.type = type;
    }
}
