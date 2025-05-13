package at.fhv.sysarch.lab2.homeautomation.devices.fridge;

import java.util.List;

public class WeightSensor {
    public static double getTotalWeight(List<Product> products) {
        return products.stream().mapToDouble(Product::getWeight).sum();
    }
}


