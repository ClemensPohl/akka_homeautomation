package at.fhv.sysarch.lab2.homeautomation.commands.fridge;

import akka.actor.typed.ActorRef;

public class QueryProducts implements FridgeCommand {
    public final ActorRef<ProductsResponse> replyTo;
    public QueryProducts(ActorRef<ProductsResponse> replyTo) {
        this.replyTo = replyTo;
    }
}

