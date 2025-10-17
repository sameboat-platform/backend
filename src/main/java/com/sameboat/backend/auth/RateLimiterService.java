package com.sameboat.backend.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Naive in-memory rate limiter keyed by an arbitrary string (e.g., email+IP).
 * Limits to MAX_ATTEMPTS within WINDOW. Intended for login endpoints.
 * Thread-safe per key via CHM.compute(..).
 */
@Component
public class RateLimiterService {
    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(5);

    private final ConcurrentHashMap<String, ArrayDeque<Instant>> buckets = new ConcurrentHashMap<>();

    /** Returns true if the key is currently rate limited (at or above threshold). */
    public boolean isLimited(String key) {
        final Instant now = Instant.now();
        final boolean[] limited = new boolean[1];

        // Atomically clean & check this bucket; remove it if it becomes empty.
        buckets.computeIfPresent(key, (k, q) -> {
            evictOld(q, now);
            limited[0] = q.size() >= MAX_ATTEMPTS;
            return q.isEmpty() ? null : q; // returning null deletes the entry
        });

        return limited[0];
    }

    /** Records a failed attempt at current time; returns true if it hits the limit threshold after recording. */
    public boolean recordFailure(String key) {
        final Instant now = Instant.now();
        final boolean[] limited = new boolean[1];

        buckets.compute(key, (k, q) -> {
            if (q == null) q = new ArrayDeque<>();
            evictOld(q, now);
            q.addLast(now);
            limited[0] = q.size() >= MAX_ATTEMPTS;
            return q;
        });

        if (limited[0]) {
            log.info("Rate limit reached for key={} (>= {} failures within {} min)", key, MAX_ATTEMPTS, WINDOW.toMinutes());
        }
        return limited[0];
    }

    /** Resets the bucket on success (optional soft reset). */
    public void reset(String key) {
        buckets.remove(key);
    }

    private static void evictOld(Deque<Instant> q, Instant now) {
        final Instant cutoff = now.minus(WINDOW);
        while (!q.isEmpty() && q.peekFirst().isBefore(cutoff)) {
            q.removeFirst();
        }
    }
}

