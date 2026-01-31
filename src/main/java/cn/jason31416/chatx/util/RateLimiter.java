package cn.jason31416.chatx.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    public static Set<RateLimiter> limiters = new HashSet<>();

    public Map<String, Long> lastInvocation = new ConcurrentHashMap<>();
    public final long interval;
    public final int limit;
    private int count = 0;

    public RateLimiter(long interval, int limit) {
        this.interval = interval;
        this.limit = limit;
        limiters.add(this);
    }
    public void checkCache() {
        long now = System.currentTimeMillis();
        for (String key : new HashSet<>(lastInvocation.keySet())) {
            if (now - lastInvocation.get(key) > interval) {
                lastInvocation.remove(key);
            }
        }
    }
    public boolean invoke(String key) {
        long now = System.currentTimeMillis();
        long last = lastInvocation.getOrDefault(key, 0L);
        if (now - last < interval) {
            count ++;
            return count < limit;
        }
        count = 0;
        lastInvocation.put(key, now);
        return true;
    }
}
