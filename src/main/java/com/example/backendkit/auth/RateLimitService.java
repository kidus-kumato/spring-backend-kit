package com.example.backendkit.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class RateLimitService {
    private static final Logger log=LoggerFactory.getLogger(RateLimitService.class);
    private final StringRedisTemplate redis;
    public RateLimitService(StringRedisTemplate redis) { this.redis=redis; }
    public boolean allow(String key, int limit, Duration window) {
        try { Long count=redis.opsForValue().increment("rate:"+key); if (count != null && count == 1) redis.expire("rate:"+key, window); return count == null || count <= limit; }
        catch (RuntimeException ex) { log.warn("rate_limit_backend_unavailable=true key={}", key); return true; }
    }
}
