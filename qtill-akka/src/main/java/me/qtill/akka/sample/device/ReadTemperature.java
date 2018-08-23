package me.qtill.akka.sample.device;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ReadTemperature {
    long requestId;

    public ReadTemperature(long requestId) {
        this.requestId = requestId;
    }


    public long getRequestId() {
        return requestId;
    }
}
