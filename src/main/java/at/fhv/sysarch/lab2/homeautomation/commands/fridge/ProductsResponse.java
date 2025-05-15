package at.fhv.sysarch.lab2.homeautomation.commands.fridge;
import at.fhv.sysarch.lab2.homeautomation.devices.fridge.Product;

import java.util.List;

public class ProductsResponse {
    private final List<Product> products;
    public ProductsResponse(List<Product> products) {
        this.products = products;
    }
    public List<Product> products() {
        return products;
    }
}

