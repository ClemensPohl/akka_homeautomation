package at.fhv.sysarch.lab2.homeautomation.devices.fridge.grpc;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.grpc.javadsl.ServiceHandler;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;

import java.util.concurrent.CompletionStage;

public class OrderProcessorServer {
    public static void main(String[] args) throws Exception {
        ActorSystem<Void> system = ActorSystem.create(Behaviors.empty(), "OrderProcessorSystem");
        FridgeOrderServiceImpl orderService = new FridgeOrderServiceImpl(system);

        var service = ServiceHandler.concatOrNotFound(
                FridgeOrderServiceHandlerFactory.create(orderService, system)
        );

        CompletionStage<ServerBinding> binding = Http.get(system)
                .newServerAt("127.0.0.1", 8081)
                .bind(service);

        System.out.println("OrderProcessor gRPC server running at 127.0.0.1:8081");
    }
}
