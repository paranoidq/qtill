package me.qtill.akka.sample.s1;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.Status;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import akka.japi.pf.FI;
import akka.pattern.AskTimeoutException;
import akka.pattern.PatternsCS;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class Printer extends AbstractActor {


    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);


    public Printer() {
    }


    public void stop() {
        try {
            CompletableFuture<Boolean> stopped = PatternsCS.gracefulStop(getSelf(), Duration.ofSeconds(1), "Shutdown").toCompletableFuture();
            stopped.get(3, TimeUnit.SECONDS);
        } catch (AskTimeoutException exception) {

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(Greeting.class, new FI.UnitApply<Greeting>() {
                @Override
                public void apply(Greeting greeting) throws Exception {
                    log.info(greeting.message);
                }
            })
            // 处理返回的异常
            .match(Status.Failure.class, failure -> {
                failure.cause().printStackTrace();
            })
            .build();
    }


    static public Props props() {
        return Props.create(Printer.class, new Creator<Printer>() {
            @Override
            public Printer create() throws Exception {
                return new Printer();
            }
        });
    }

    static public class Greeting {
        public final String message;

        public Greeting(String message) {
            this.message = message;
        }
    }


}
