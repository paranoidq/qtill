package me.qtill.akka.sample.device;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: 漏洞？？？跳过DeviceGroup的管理，直接通过DeviceManager注册Device
 *
 * 代码层面无法避免？只能通过封装隐藏功能？？
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class DeviceManager extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props() {
        return Props.create(DeviceManager.class);
    }

    public static class RequestTrackDevice {

        private final String groupId;
        private final String deviceId;

        public RequestTrackDevice(String groupId, String deviceId) {
            this.groupId = groupId;
            this.deviceId = deviceId;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getDeviceId() {
            return deviceId;
        }
    }

    public static class DeviceRegistered {
    }

    private final Map<String, ActorRef> groupIdToActor = new HashMap<>();
    private final Map<ActorRef, String> actorToGroupId = new HashMap<>();


    @Override
    public void preStart() {
        log.info("DeviceManager started");
    }

    @Override
    public void postStop() {
        log.info("DeviceManager stopped");
    }

    private void onTrackDevice(RequestTrackDevice trackMsg) {
        String groupId = trackMsg.getGroupId();
        ActorRef ref = groupIdToActor.get(groupId);
        if (ref != null) {
            ref.forward(trackMsg, getContext());
        } else {
            log.info("Creating device group actor for {}", groupId);
            ActorRef groupActor = getContext().actorOf(DeviceGroup.props(groupId));

            getContext().watch(groupActor);
            groupActor.forward(trackMsg, getContext());
            groupIdToActor.put(groupId, groupActor);
            actorToGroupId.put(groupActor, groupId);
        }
    }

    private void onTerminated(Terminated t) {
        ActorRef groupActor = t.getActor();
        String groupId = actorToGroupId.get(groupActor);
        log.info("Device group actor for {} has been terminated", groupId);
        actorToGroupId.remove(groupActor);
        groupIdToActor.remove(groupId);
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(RequestTrackDevice.class, this::onTrackDevice)
            .match(Terminated.class, this::onTerminated)
            .build();
    }


}
