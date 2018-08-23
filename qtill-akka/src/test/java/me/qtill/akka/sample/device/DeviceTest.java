package me.qtill.akka.sample.device;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.testkit.javadsl.TestKit;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class DeviceTest {

    private ActorSystem system;

    @Before
    public void setUp() throws Exception {
        system = ActorSystem.create("test");
    }

    @Test
    public void testReplyWithEmptyReadingIfNoTemperatureIsKnown() {
        TestKit probe = new TestKit(system);
        ActorRef deviceActor = system.actorOf(Device.props("groud", "device"));
        deviceActor.tell(new ReadTemperature(42L), probe.getRef());
        RespondTemperature resposne = probe.expectMsgClass(RespondTemperature.class);
        assertEquals(42L, resposne.getRequestId());
        assertEquals(Optional.empty(), resposne.getValue());
    }


    @Test
    public void testReplyWithLatestTemperatureReading() {
        TestKit probe = new TestKit(system);
        ActorRef deviceActor = system.actorOf(Device.props("group", "device"));

        // read when no recording
        deviceActor.tell(new ReadTemperature(0L), probe.getRef());
        RespondTemperature response0 = probe.expectMsgClass(RespondTemperature.class);
        assertEquals(0L, response0.getRequestId());
        assertEquals(Optional.empty(), response0.value);


        // first recording
        deviceActor.tell(new RecordTemperature(1L, 24.0), probe.getRef());
        assertEquals(1L, probe.expectMsgClass(TemperatureRecorded.class).getRequestId());

        deviceActor.tell(new ReadTemperature(2L), probe.getRef());
        RespondTemperature response = probe.expectMsgClass(RespondTemperature.class);
        assertEquals(2L, response.getRequestId());
        assertEquals(Optional.of(24.0), response.value);


        deviceActor.tell(new RecordTemperature(3L, 55.0), probe.getRef());
        assertEquals(3L, probe.expectMsgClass(TemperatureRecorded.class).getRequestId());


        deviceActor.tell(new ReadTemperature(4L), probe.getRef());
        RespondTemperature response2 = probe.expectMsgClass(RespondTemperature.class);
        assertEquals(4L, response2.requestId);
        assertEquals(Optional.of(55.0), response2.value);
    }


    @Test
    public void testReplyToRegistrationRequests() {
        TestKit probe = new TestKit(system);
        ActorRef deviceActor = system.actorOf(Device.props("group", "device"));

        // tell的含义是相反的，这里调用tell的含义是【往】deviceActor发送消息
        deviceActor.tell(new DeviceManager.RequestTrackDevice("group", "device"), probe.getRef());
        // 收到ack
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
        assertEquals(deviceActor, probe.getLastSender());
    }


    @Test
    public void testIgnoreWrongRegistrationRequests() {
        TestKit probe = new TestKit(system);
        ActorRef deviceActor = system.actorOf(Device.props("group", "device"));

        deviceActor.tell(new DeviceManager.RequestTrackDevice("wrongGroup", "device"), probe.getRef());
        probe.expectNoMessage(Duration.ofSeconds(1));

        deviceActor.tell(new DeviceManager.RequestTrackDevice("group", "wrongDevice"), probe.getRef());
        probe.expectNoMessage(Duration.ofSeconds(1));
    }

    @Test
    public void testRegisterDeviceActor() {
        TestKit probe = new TestKit(system);
        ActorRef groupActor = system.actorOf(DeviceGroup.props("group"));

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device1"), probe.getRef());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
        ActorRef deviceActor1 = probe.getLastSender();

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device2"), probe.getRef());

        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
        ActorRef deviceActor2 = probe.getLastSender();
        assertNotEquals(deviceActor1, deviceActor2);

        // Check that the device actors are working
        deviceActor1.tell(new RecordTemperature(0L, 1.0), probe.getRef());
        assertEquals(0L, probe.expectMsgClass(TemperatureRecorded.class).getRequestId());
        deviceActor2.tell(new RecordTemperature(1L, 2.0), probe.getRef());
        assertEquals(1L, probe.expectMsgClass(TemperatureRecorded.class).getRequestId());
    }

    @Test
    public void testIgnoreRequestsForWrongGroupId() {
        TestKit probe = new TestKit(system);
        ActorRef groupActor = system.actorOf(DeviceGroup.props("group"));

        groupActor.tell(new DeviceManager.RequestTrackDevice("wrongGroup", "device1"), probe.getRef());
        probe.expectNoMsg();
    }


    @Test
    public void testReturnSameActorForSameDeviceId() {
        TestKit probe = new TestKit(system);
        ActorRef groupActor = system.actorOf(DeviceGroup.props("group"));

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device1"), probe.getRef());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
        ActorRef deviceActor1 = probe.getLastSender();

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device1"), probe.getRef());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
        ActorRef deviceActor2 = probe.getLastSender();

        // test: use existed deviceActor
        assertEquals(deviceActor1, deviceActor2);
    }


    @Test
    public void testListActiveDevices() {
        TestKit probe = new TestKit(system);
        ActorRef groupActor = system.actorOf(DeviceGroup.props("group"));

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device1"), probe.getRef());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device2"), probe.getRef());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);

        groupActor.tell(new DeviceGroup.RequestDeviceList(0L), probe.getRef());
        DeviceGroup.ReplyDeviceList reply = probe.expectMsgClass(DeviceGroup.ReplyDeviceList.class);
        assertEquals(0L, reply.getRequestId());
        assertEquals(Stream.of("device1", "device2").collect(Collectors.toSet()), reply.getIds());
    }

    @Test
    public void testListActiveDevicesAfterOneShutsDown() {
        TestKit probe = new TestKit(system);
        ActorRef groupActor = system.actorOf(DeviceGroup.props("group"));

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device1"), probe.getRef());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
        ActorRef toShutDown = probe.getLastSender();

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device2"), probe.getRef());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);

        groupActor.tell(new DeviceGroup.RequestDeviceList(0L), probe.getRef());
        DeviceGroup.ReplyDeviceList reply = probe.expectMsgClass(DeviceGroup.ReplyDeviceList.class);
        assertEquals(0L, reply.getRequestId());
        assertEquals(Stream.of("device1", "device2").collect(Collectors.toSet()), reply.getIds());

        probe.watch(toShutDown);
        // shutdown actor
        toShutDown.tell(PoisonPill.getInstance(), ActorRef.noSender());

        probe.expectTerminated(toShutDown);

        // using awaitAssert to retry because it might take longer for the groupActor
        // to see the Terminated, that order is undefined
        probe.awaitAssert(() -> {
            groupActor.tell(new DeviceGroup.RequestDeviceList(1L), probe.getRef());
            DeviceGroup.ReplyDeviceList r =
                probe.expectMsgClass(DeviceGroup.ReplyDeviceList.class);
            assertEquals(1L, r.getRequestId());
            assertEquals(Stream.of("device2").collect(Collectors.toSet()), r.getIds());
            return null;
        });
    }
}