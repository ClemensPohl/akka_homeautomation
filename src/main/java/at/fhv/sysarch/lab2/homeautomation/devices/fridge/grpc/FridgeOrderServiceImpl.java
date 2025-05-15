package at.fhv.sysarch.lab2.homeautomation.devices.fridge.grpc;//package at.fhv.sysarch.lab2.homeautomation.external.grpc;

import akka.actor.typed.ActorSystem;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class FridgeOrderServiceImpl implements FridgeOrderService {
    private final ActorSystem<?> system;

    public FridgeOrderServiceImpl(ActorSystem<?> system) {
        this.system = system;
    }

    @Override
    public CompletionStage<OrderReceipt> orderProduct(OrderRequest request) {
        // Simulate order processing
        OrderReceipt receipt = OrderReceipt.newBuilder()
                .setProductName(request.getProductName())
                .setAmount(request.getAmount())
                .setPrice(2.99 * request.getAmount())
                .setMessage("Order successful")
                .build();
        return CompletableFuture.completedFuture(receipt);
    }
}
