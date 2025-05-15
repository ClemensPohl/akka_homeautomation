package at.fhv.sysarch.lab2.homeautomation.devices.fridge;

import akka.actor.typed.Behavior;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.commands.fridge.*;

import java.util.ArrayList;
import java.util.List;

public class Fridge extends AbstractBehavior<FridgeCommand> {

    private final int maxProducts;
    private final double maxWeight;
    private final List<Product> products;
    private final ActorRef<FridgeCommand> orderProcessor;

    public static Behavior<FridgeCommand> create(int maxProducts, double maxWeight, ActorRef<FridgeCommand> orderProcessor) {
        return Behaviors.setup(ctx -> new Fridge(ctx, maxProducts, maxWeight, orderProcessor));
    }

    private Fridge(ActorContext<FridgeCommand> context, int maxProducts, double maxWeight, ActorRef<FridgeCommand> orderProcessor) {
        super(context);
        this.maxProducts = maxProducts;
        this.maxWeight = maxWeight;
        this.products = new ArrayList<>();
        this.orderProcessor = orderProcessor;
        getContext().getLog().info("Fridge started with capacity {} items and max weight {} kg", maxProducts, maxWeight);
    }

    @Override
    public Receive<FridgeCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(AddProduct.class, this::onAddProduct)
                .onMessage(RemoveProduct.class, this::onRemoveProduct)
                .onMessage(OrderCompleted.class, this::onOrderCompleted)
                .build();
    }

    private Behavior<FridgeCommand> onOrderCompleted(OrderCompleted orderCompleted) {
        getContext().getLog().info("Order completed for product: {}", orderCompleted.productName);
        // Optionally, add new product to fridge, update state, etc.
        return this;
    }

    private Behavior<FridgeCommand> onAddProduct(AddProduct cmd) {
        // ... (same as before)
        products.add(cmd.product);
        getContext().getLog().info("Added product: {}", cmd.product.getName());
        return this;
    }

    private Behavior<FridgeCommand> onRemoveProduct(RemoveProduct cmd) {
        boolean removed = products.removeIf(p -> p.getName().equalsIgnoreCase(cmd.productName));
        if (removed) {
            getContext().getLog().info("Removed product: {}", cmd.productName);
            checkStockAndOrder(cmd.productName);
        } else {
            getContext().getLog().warn("Product not found: {}", cmd.productName);
        }
        return this;
    }

    private void checkStockAndOrder(String productName) {
        long count = products.stream()
                .filter(p -> p.getName().equalsIgnoreCase(productName))
                .count();
        if (count == 0) {
            orderProcessor.tell(new ProcessOrder(productName, 1, getContext().getSelf()));
        }
    }

}
