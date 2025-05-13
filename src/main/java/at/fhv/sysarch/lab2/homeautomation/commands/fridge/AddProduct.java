package at.fhv.sysarch.lab2.homeautomation.commands.fridge;

import at.fhv.sysarch.lab2.homeautomation.devices.fridge.Product;

public class AddProduct implements FridgeCommand {
    public final Product product;

    public AddProduct(Product product) {
        this.product = product;
    }
}
