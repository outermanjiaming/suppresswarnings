package com.suppresswarnings.ai;

import java.util.HashMap;
import java.util.Map;

public class StopWatch {
    Map<String, long[]> watches = new HashMap<>();
    long _start;
    long _stop;
    public StopWatch start(String context) {
        long begin = System.currentTimeMillis();
        watches.putIfAbsent(context, new long[]{begin, 0});
        return this;
    }
    public StopWatch stop(String context) {
        long end = System.currentTimeMillis();
        watches.computeIfPresent(context, (k,v)->{v[1] = end;return v;});
        return this;
    }
    public long duration(String context) {
        if(watches.containsKey(context)) {
            long[] xy = watches.get(context);
            return xy[1] - xy[0];
        }
        return 0;
    }
    public StopWatch start() {
        _start = System.currentTimeMillis();
        return this;
    }
    public StopWatch stop() {
        _stop = System.currentTimeMillis();
        return this;
    }
    public long duration() {
        return _stop - _start;
    }
}
