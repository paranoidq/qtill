package me.qtill.akka.sample.s1;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.PatternsCS;


/**
 * @author paranoidq
 * @since 1.0.0
 */
public class AkkaQuickStart {

    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("hello-akka");
        try {

            final ActorRef printerActor = system.actorOf(Printer.props(), "printerActor");
            final ActorRef howdyActor = system.actorOf(Greeter.props("howdy", printerActor), "howdyActor");
            final ActorRef helloActor = system.actorOf(Greeter.props("hello", printerActor), "helloActor");
            final ActorRef goodDayActor = system.actorOf(Greeter.props("good day", printerActor), "goodDayActor");


            howdyActor.tell(new Greeter.WhoToGreet("akka"), howdyActor);
            howdyActor.tell(new Greeter.Greet(), howdyActor);


            howdyActor.tell(new Greeter.WhoToGreet("Lightbend"), ActorRef.noSender());
            howdyActor.tell(new Greeter.Greet(), ActorRef.noSender());

            helloActor.tell(new Greeter.WhoToGreet("Java"), ActorRef.noSender());
            helloActor.tell(new Greeter.Greet(), ActorRef.noSender());

            goodDayActor.tell(new Greeter.WhoToGreet("Play"), ActorRef.noSender());
            goodDayActor.tell(new Greeter.Greet(), ActorRef.noSender());

            System.out.println(">>> Press ENTER to exit <<<");
            System.in.read();
        } catch (Exception e) {

        } finally {
            system.terminate();
        }

    }
}
