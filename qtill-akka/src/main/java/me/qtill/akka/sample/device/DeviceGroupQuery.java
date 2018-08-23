package me.qtill.akka.sample.device;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.FiniteDuration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class DeviceGroupQuery extends AbstractActor {

    public static final class CollectionTimeout {

    }


    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private final Map<ActorRef, String> actorToDeviceId;
    private final long                  requestId;
    private       ActorRef              requester;

    Cancellable queryTimeoutTimer;

    public DeviceGroupQuery(Map<ActorRef, String> actorToDeviceId, long requestId, ActorRef requester, FiniteDuration timeout) {
        this.actorToDeviceId = actorToDeviceId;
        this.requestId = requestId;
        this.requester = requester;

        queryTimeoutTimer = getContext().getSystem().scheduler()
            .scheduleOnce(timeout, getSelf(), new CollectionTimeout(), getContext().dispatcher(), getSelf());
    }

    @Override
    public void preStart() throws Exception {
        for (ActorRef deviceActor : actorToDeviceId.keySet()) {
            getContext().watch(deviceActor);
            deviceActor.tell(new ReadTemperature(0L), getSelf());
        }
    }

    @Override
    public void postStop() throws Exception {
        queryTimeoutTimer.cancel();
    }

    @Override
    public Receive createReceive() {
        return waitingForReplies(new HashMap<>(), actorToDeviceId.keySet());
    }

    public Receive waitingForReplies(
        Map<String, DeviceGroup.TemperatureReading> repliesSoFar,
        Set<ActorRef> stillWaiting) {
        return receiveBuilder()
            .match(RespondTemperature.class, respondTemperature -> {
                ActorRef deviceActor = getSender();
                DeviceGroup.TemperatureReading reading =
                    respondTemperature.getValue()
                        .map(v -> (DeviceGroup.TemperatureReading) new DeviceGroup.Temperature(v))
                        .orElse(new DeviceGroup.TemperatureNotAvaliable());
                receivedResponse(deviceActor, reading, stillWaiting, repliesSoFar);
            })
            .match(Terminated.class, t -> {
                receivedResponse(t.getActor(), new DeviceGroup.DeviceNotAvaliable(), stillWaiting, repliesSoFar);
            })
            .match(CollectionTimeout.class, t -> {
                Map<String, DeviceGroup.TemperatureReading> replies = new HashMap<>(repliesSoFar);
                for (ActorRef deviceActor : stillWaiting) {
                    String deviceId = actorToDeviceId.get(deviceActor);
                    replies.put(deviceId, new DeviceGroup.DeviceTimedOut());
                }
                requester.tell(new DeviceGroup.RespondAllTemperatures(requestId, replies), getSelf());
                getContext().stop(getSelf());
            })
            .build();
    }

    public void receivedResponse(ActorRef deviceActor, DeviceGroup.TemperatureReading reading, Set<ActorRef> stillWaiting, Map<String, DeviceGroup.TemperatureReading> repliesSoFar) {
        // 不再监视已经回复过的actor，即使它停掉了，也不会触发Terminated事件
        getContext().unwatch(deviceActor);

        String deviceId = actorToDeviceId.get(deviceActor);

        // 移除已经回复或者timeout的actor
        Set<ActorRef> newStillWaiting = new HashSet<>(stillWaiting);
        newStillWaiting.remove(deviceActor);

        // 添加到已完成的集合中
        Map<String, DeviceGroup.TemperatureReading> newRepliesSoFar = new HashMap<>(repliesSoFar);
        newRepliesSoFar.put(deviceId, reading);

        // 检查是否已经全部回复
        if (newStillWaiting.isEmpty()) {
            // 如果全部回复了，则回复发起方
            requester.tell(new DeviceGroup.RespondAllTemperatures(requestId, newRepliesSoFar), getSelf());
            getContext().stop(getSelf());
        } else {
            // 否则，返回新的Receive对象，切换到新的当前处理状态
            getContext().become(waitingForReplies(newRepliesSoFar, newStillWaiting));
        }

        /*
        It is quite natural to ask at this point, what have we gained by using the context.become() trick
        instead of making the repliesSoFar and stillWaiting structures mutable fields of the actor (i.e. vars)?
        In this simple example, not that much. The value of this style of state keeping becomes more evident
        when you suddenly have more kinds of states. Since each state might have temporary data that is relevant
        itself, keeping these as fields would pollute the global state of the actor, i.e. it is unclear what fields
         are used in what state. Using parameterized Receive “factory” methods we can keep data private that is
         only relevant to the state. It is still a good exercise to rewrite the query using mutable fields instead of
         context.become(). However, it is recommended to get comfortable with the solution we have used here
         as it helps structuring more complex actor code in a cleaner and more maintainable way.
         */
    }
}
