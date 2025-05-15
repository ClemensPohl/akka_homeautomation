package at.fhv.sysarch.lab2.homeautomation.commands.fridge;

public class OrderCompleted implements FridgeCommand {
    public final String productName;

    public OrderCompleted(String productName) {
        this.productName = productName;
    }
}
