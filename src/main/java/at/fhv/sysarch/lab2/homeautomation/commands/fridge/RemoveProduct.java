package at.fhv.sysarch.lab2.homeautomation.commands.fridge;

public class RemoveProduct implements FridgeCommand {
    public final String productName;

    public RemoveProduct(String productName) {
        this.productName = productName;
    }
}
