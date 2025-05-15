package at.fhv.sysarch.lab2.homeautomation.devices.fridge;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.commands.fridge.FridgeCommand;
import at.fhv.sysarch.lab2.homeautomation.commands.fridge.AddProduct;
import at.fhv.sysarch.lab2.homeautomation.commands.fridge.RemoveProduct;
import at.fhv.sysarch.lab2.homeautomation.commands.fridge.QueryProducts;
import at.fhv.sysarch.lab2.homeautomation.commands.fridge.ProductsResponse;
import akka.actor.typed.ActorRef;




import java.util.ArrayList;
import java.util.List;

public class Fridge extends AbstractBehavior<FridgeCommand> {

    private final int maxProducts;
    private final double maxWeight;
    private final List<Product> products;

    public static Behavior<FridgeCommand> create(int maxProducts, double maxWeight) {
        return Behaviors.setup(ctx -> new Fridge(ctx, maxProducts, maxWeight));
    }

    private Fridge(ActorContext<FridgeCommand> context, int maxProducts, double maxWeight) {
        super(context);
        this.maxProducts = maxProducts;
        this.maxWeight = maxWeight;
        this.products = new ArrayList<>();
        getContext().getLog().info("Fridge started with capacity {} items and max weight {} kg", maxProducts, maxWeight);
    }

    @Override
    public Receive<FridgeCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(AddProduct.class, this::onAddProduct)
                .onMessage(RemoveProduct.class, this::onRemoveProduct)
                .onMessage(QueryProducts.class, this::onQueryProducts)
                .build();
    }

    private Behavior<FridgeCommand> onQueryProducts(QueryProducts cmd) {
        cmd.replyTo.tell(new ProductsResponse(new ArrayList<>(products)));
        getContext().getLog().info("Sent product list ({} items)", products.size());
        return this;
    }


    private Behavior<FridgeCommand> onAddProduct(AddProduct cmd) {
        int currentCount = SpaceSensor.getProductCount(products);
        double currentWeight = WeightSensor.getTotalWeight(products);

        if (currentCount >= maxProducts) {
            getContext().getLog().warn("Cannot add product: Fridge is full");
        } else if (currentWeight + cmd.product.getWeight() > maxWeight) {
            getContext().getLog().warn("Cannot add product: Weight limit exceeded");
        } else {
            products.add(cmd.product);
            getContext().getLog().info("Here is a Receipt. Added product: {}", cmd.product.getName());
        }
        return this;
    }


    private Behavior<FridgeCommand> onRemoveProduct(RemoveProduct cmd) {
        boolean removed = products.removeIf(p -> p.getName().equalsIgnoreCase(cmd.productName));
        if (removed) {
            getContext().getLog().info("Removed product: {}", cmd.productName);
        } else {
            getContext().getLog().warn("Product not found: {}", cmd.productName);
        }
        return this;
    }
}
