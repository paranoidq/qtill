package me.qtill.akka.sample.device;

import java.util.Optional;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class RespondTemperature {

    long             requestId;
    Optional<Double> value;

    public RespondTemperature(long requestId, Optional<Double> value) {
        this.requestId = requestId;
        this.value = value;
    }

    public long getRequestId() {
        return requestId;
    }

    public Optional<Double> getValue() {
        return value;
    }
}
