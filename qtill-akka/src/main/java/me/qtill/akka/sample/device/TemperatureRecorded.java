package me.qtill.akka.sample.device;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class TemperatureRecorded {
    private long requestId;

    public TemperatureRecorded(long requestId) {
        this.requestId = requestId;
    }

    public long getRequestId() {
        return requestId;
    }
}
