package com.sameboat.backend.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Naive in-memory rate limiter keyed by an arbitrary string (e.g., email+IP).
 * Limits to MAX_ATTEMPTS within WINDOW. Intended for login endpoints.
 */
@Component
public class RateLimiterService {
    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(5);

    private final Map<String, Deque<Instant>> buckets = new ConcurrentHashMap<>();

    /** Returns true if the key is currently rate limited (at or above threshold). */
    public boolean isLimited(String key) {
        cleanup(key);
        var q = buckets.get(key);
        return q != null && q.size() >= MAX_ATTEMPTS;
    }

    /** Records a failed attempt at current time; returns true if it hits the limit threshold after recording. */
    public boolean recordFailure(String key) {
        var now = Instant.now();
        var q = buckets.computeIfAbsent(key, k -> new ArrayDeque<>());
        q.addLast(now);
        evictOld(q, now);
        boolean limited = q.size() >= MAX_ATTEMPTS;
        if (limited) {
            log.info("Rate limit reached for key={} ({} failures within {} min)", key, q.size(), WINDOW.toMinutes());
        }
        return limited;
    }

    /** Resets the bucket on success (optional soft reset). */
    public void reset(String key) {
        buckets.remove(key);
    }

    private void cleanup(String key) {
        var q = buckets.get(key);
        if (q == null) return;
        evictOld(q, Instant.now());
        if (q.isEmpty()) buckets.remove(key);
    }

    private void evictOld(Deque<Instant> q, Instant now) {
        var cutoff = now.minus(WINDOW);
        while (!q.isEmpty() && q.peekFirst().isBefore(cutoff)) {
            q.removeFirst();
        }
    }
}

