package org.bptlab.cepta.utils.functions;

import com.google.protobuf.Duration;
import org.bptlab.cepta.models.internal.correlateable_event.CorrelateableEventOuterClass.CorrelateableEvent;

public interface DistanceFunction{

    public double distanceBetween(CorrelateableEvent a, CorrelateableEvent b);

    public DistanceFunction setMaxDistance(Number value);

    public DistanceFunction setMaxTimespan(Duration value);
}
