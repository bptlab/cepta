package org.bptlab.cepta.providers;

import com.google.protobuf.Timestamp;

public class TimeStampProvider {
    // this represents the timestamp 2020-04-28 10:03:40.0
    // equals the proto timestamp {seconds: 1588068220, nanos: 471000000}
    private final long defaultMillis = 1588068220471l;

    private static Timestamp getTimestampFromMillis(long millis) {
        Timestamp timestamp = Timestamp.newBuilder().setSeconds(millis / 1000)
                .setNanos((int) ((millis % 1000) * 1000000)).build();
        return timestamp;
    }

    public Timestamp getDefaultTimestamp() {
        return getTimestampFromMillis(this.defaultMillis);
    }

    public Timestamp getDefaultTimestampWithAddedMinutes(long minutes) {
        final long secondsPerMinute = 60;
        final long millisPerSecond = 1000;
        long newMillis = this.defaultMillis + minutes * secondsPerMinute * millisPerSecond;
        return getTimestampFromMillis(newMillis);
    }

}
