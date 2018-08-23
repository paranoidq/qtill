package me.qtill.akka.sample.device;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class RecordTemperature {

    private long   requestId;
    private double value;

    public RecordTemperature(long requestId, double value) {
        this.requestId = requestId;
        this.value = value;
    }

    public long getRequestId() {
        return requestId;
    }

    public double getValue() {
        return value;
    }
}
