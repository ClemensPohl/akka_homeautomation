package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.commands.mediaStation.*;
import at.fhv.sysarch.lab2.homeautomation.commands.blinds.*;

public class MediaStation extends AbstractBehavior<MediaCommand> {

    public static Behavior<MediaCommand> create(ActorRef<BlindsCommand> blinds) {
        return Behaviors.setup(context -> new MediaStation(context, blinds));
    }

    private final ActorRef<BlindsCommand> blinds;
    private boolean moviePlaying = false;

    private MediaStation(ActorContext<MediaCommand> context, ActorRef<BlindsCommand> blinds) {
        super(context);
        this.blinds = blinds;
        context.getLog().info("MediaStation started");
    }

    @Override
    public Receive<MediaCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(PlayMovie.class, this::onPlayMovie)
                .onMessage(StopMovie.class, this::onStopMovie)
                .build();
    }

    private Behavior<MediaCommand> onPlayMovie(PlayMovie msg) {
        if (moviePlaying) {
            getContext().getLog().info("Cannot play movie: already playing.");
        } else {
            moviePlaying = true;
            getContext().getLog().info("Movie started.");
        }
        return this;
    }

    private Behavior<MediaCommand> onStopMovie(StopMovie msg) {
        if (!moviePlaying) {
            getContext().getLog().info("No movie is currently playing.");
        } else {
            moviePlaying = false;
            getContext().getLog().info("Movie stopped.");
        }
        return this;
    }

    public boolean isMoviePlaying() {
        return moviePlaying;
    }
}
