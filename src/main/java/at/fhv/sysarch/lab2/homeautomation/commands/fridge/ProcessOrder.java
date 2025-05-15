package at.fhv.sysarch.lab2.homeautomation.commands.fridge;

import akka.actor.typed.ActorRef;

public class ProcessOrder implements FridgeCommand {
    public final String productName;
    public final int quantity;
    public final ActorRef<FridgeCommand> replyTo;

    public ProcessOrder(String productName, int quantity, ActorRef<FridgeCommand> replyTo) {
        this.productName = productName;
        this.quantity = quantity;
        this.replyTo = replyTo;
    }
}
