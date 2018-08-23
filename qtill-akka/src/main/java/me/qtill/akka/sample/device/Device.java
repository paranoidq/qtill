package me.qtill.akka.sample.device;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.Optional;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class Device extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private final String groupId;
    private final String deviceId;

    public Device(String groupId, String deviceId) {
        this.groupId = groupId;
        this.deviceId = deviceId;
    }

    public static Props props(String groupId, String deviceId) {
        return Props.create(Device.class, groupId, deviceId);
    }

    private Optional<Double> lastTemperatureReading = Optional.empty();


    @Override
    public void preStart() {
        log.info("device actor {}-{} started", groupId, deviceId);
    }


    @Override
    public void postStop() {
        log.info("device actor {}-{} stopped", groupId, deviceId);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()

            .match(DeviceManager.RequestTrackDevice.class, requestTrackDevice -> {
                // 收到广播，并根据groupId和deviceId进行匹配和过滤，只有匹配的actor会响应
                if (this.groupId.equals(requestTrackDevice.getGroupId()) && this.deviceId.equals(requestTrackDevice.getDeviceId())) {
                    getSender().tell(new DeviceManager.DeviceRegistered(), getSelf());
                } else {
                    log.warning("Ignoring track device request for {}-{}, This actor is responsible for {}-{}.",
                        requestTrackDevice.getGroupId(), requestTrackDevice.getDeviceId(), this.groupId, this.deviceId);
                }
            })
            .match(RecordTemperature.class, recordTemperature -> {
                log.info("recorded temperature reading {} with {}", recordTemperature.getValue(), recordTemperature.getRequestId());
                lastTemperatureReading = Optional.of(recordTemperature.getValue());
                // ack
                getSender().tell(new TemperatureRecorded(recordTemperature.getRequestId()), getSender());
            })
            .match(ReadTemperature.class, r -> {
                // ack
                getSender().tell(new RespondTemperature(r.getRequestId(), lastTemperatureReading), getSelf());
            })
            .build();
    }
}
