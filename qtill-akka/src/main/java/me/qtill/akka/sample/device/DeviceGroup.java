package me.qtill.akka.sample.device;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class DeviceGroup extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private final String groupId;

    private final Map<String, ActorRef> deviceIdToActor = new HashMap<>();
    private final Map<ActorRef, String> actorToDeviceId = new HashMap<>();


    public DeviceGroup(String groupId) {
        this.groupId = groupId;
    }

    public static Props props(String groupId) {
        return Props.create(DeviceGroup.class, groupId);
    }

    public static final class RequestDeviceList {
        private final long requestId;

        public RequestDeviceList(long requestId) {
            this.requestId = requestId;
        }

        public long getRequestId() {
            return requestId;
        }
    }

    public static final class ReplyDeviceList {
        private final long        requestId;
        private final Set<String> ids;

        public ReplyDeviceList(long requestId, Set<String> ids) {
            this.requestId = requestId;
            this.ids = ids;
        }

        public long getRequestId() {
            return requestId;
        }

        public Set<String> getIds() {
            return ids;
        }
    }


    public static final class RequestAllTemperatures {
        private final long requestId;

        public RequestAllTemperatures(long requestId) {
            this.requestId = requestId;
        }
    }

    public static final class RespondAllTemperatures {
        private final long                            requestId;
        private final Map<String, TemperatureReading> temperatures;

        public RespondAllTemperatures(long requestId, Map<String, TemperatureReading> temperatures) {
            this.requestId = requestId;
            this.temperatures = temperatures;
        }
    }


    public static interface TemperatureReading {

    }

    public static final class Temperature implements TemperatureReading {
        public final double value;

        public Temperature(double value) {
            this.value = value;
        }
    }

    public static final class TemperatureNotAvaliable implements TemperatureReading {
    }

    public static final class DeviceNotAvaliable implements TemperatureReading {
    }

    public static final class DeviceTimedOut implements TemperatureReading {

    }


    @Override
    public void preStart() {
        log.info("DeviceGroup {} started", groupId);
    }

    @Override
    public void postStop() {
        log.info("DeviceGroup {} stopped", groupId);
    }


    private void onTrackDevice(DeviceManager.RequestTrackDevice trackMsg) {
        if (this.groupId.equals(trackMsg.getGroupId())) {
            ActorRef deviceActor = deviceIdToActor.get(trackMsg.getDeviceId());
            if (deviceActor != null) {
                deviceActor.forward(trackMsg, getContext());
            } else {
                log.info("Creating device actor for {}", trackMsg.getDeviceId());
                deviceActor = getContext().actorOf(Device.props(groupId, trackMsg.getDeviceId()), "device-" + trackMsg.getDeviceId());

                // death watch, notify if actor stopped
                getContext().watch(deviceActor);

                deviceIdToActor.put(trackMsg.getDeviceId(), deviceActor);
                actorToDeviceId.put(deviceActor, trackMsg.getDeviceId());
                deviceActor.forward(trackMsg, getContext());
            }
        } else {
            log.warning(
                "Ignoring TrackDevice request for {}. This actor is responsible for {}.",
                groupId, this.groupId);
        }
    }

    private void onTerminated(Terminated t) {
        ActorRef deviceActor = t.getActor();
        String deviceId = actorToDeviceId.get(deviceActor);
        log.info("Device actor for {} has been terminated", deviceId);
        actorToDeviceId.remove(deviceActor);
        deviceIdToActor.remove(deviceId);
    }

    private void onDeviceList(RequestDeviceList r) {
        getSender().tell(new ReplyDeviceList(r.getRequestId(), deviceIdToActor.keySet()), getSelf());
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(DeviceManager.RequestTrackDevice.class, this::onTrackDevice)
            .match(Terminated.class, this::onTerminated)
            .match(RequestDeviceList.class, this::onDeviceList)
            .matchAny(o -> {
                log.info("received unkown message");
            })
            .build();
    }

}
