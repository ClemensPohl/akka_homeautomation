package at.fhv.sysarch.lab2.homeautomation.devices.fridge;

public class Product {
    private final String name;
    private final double weight;
    private final double price;

    public Product(String name, double weight, double price) {
        this.name = name;
        this.weight = weight;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public double getWeight() {
        return weight;
    }

    public double getPrice() {
        return price;
    }
}

