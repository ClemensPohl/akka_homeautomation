package at.fhv.sysarch.lab2.homeautomation.devices.fridge.grpc;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.commands.fridge.*;

public class OrderProcessorActor extends AbstractBehavior<FridgeCommand> {
    private final FridgeGrpcOrderClient grpcClient;

    public static Behavior<FridgeCommand> create(FridgeGrpcOrderClient grpcClient) {
        return Behaviors.setup(ctx -> new OrderProcessorActor(ctx, grpcClient));
    }

    private OrderProcessorActor(ActorContext<FridgeCommand> ctx, FridgeGrpcOrderClient grpcClient) {
        super(ctx);
        this.grpcClient = grpcClient;
    }

    @Override
    public Receive<FridgeCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ProcessOrder.class, this::onProcessOrder)
                .build();
    }

    private Behavior<FridgeCommand> onProcessOrder(ProcessOrder cmd) {
        grpcClient.orderProduct(cmd.productName, cmd.quantity)
                .thenAccept(receipt -> {
                    // Notify the fridge that the order is completed
                    cmd.replyTo.tell(new OrderCompleted(cmd.productName));
                    // Optionally: pass more info from receipt if needed
                });
        return this;
    }
}

