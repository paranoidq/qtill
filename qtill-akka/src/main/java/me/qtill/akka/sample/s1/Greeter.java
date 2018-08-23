package me.qtill.akka.sample.s1;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class Greeter extends AbstractActor {

    private final String   message;
    private final ActorRef printerActor;
    private       String   greeting = "";


    public Greeter(String message, ActorRef printerActor) {
        this.message = message;
        this.printerActor = printerActor;
    }

    static public Props props(String message, ActorRef printerActor) {
        return Props.create(Greeter.class, () -> new Greeter(message, printerActor));
    }

    static public class WhoToGreet {
        public final String who;

        public WhoToGreet(String who) {
            this.who = who;
        }
    }

    static public class Greet {
        public Greet() {
        }
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(WhoToGreet.class, whoToGreet -> {
                    this.greeting = message + ", " + whoToGreet.who;
                }
            )
            .match(
                Greet.class, greet -> {
                    printerActor.tell(new Printer.Greeting(greeting), getSelf());
                }
            )
            .build();
    }
}
