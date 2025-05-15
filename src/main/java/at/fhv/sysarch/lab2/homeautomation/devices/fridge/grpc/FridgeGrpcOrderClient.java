package at.fhv.sysarch.lab2.homeautomation.devices.fridge.grpc;

import akka.actor.typed.ActorSystem;
import akka.grpc.GrpcClientSettings;

import java.util.concurrent.CompletionStage;

public class FridgeGrpcOrderClient {
    private final FridgeOrderServiceClient client;

    public FridgeGrpcOrderClient(ActorSystem<?> system) {
        GrpcClientSettings settings = GrpcClientSettings.connectToServiceAt("127.0.0.1", 8081, system)
                .withTls(false);
        this.client = FridgeOrderServiceClient.create(settings, system);
    }

    public CompletionStage<OrderReceipt> orderProduct(String name, int amount) {
        return client.orderProduct(OrderRequest.newBuilder()
                .setProductName(name)
                .setAmount(amount)
                .build());
    }
}

