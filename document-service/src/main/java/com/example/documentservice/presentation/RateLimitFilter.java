package com.example.documentservice.presentation;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.github.bucket4j.BandwidthBuilder;
import io.github.bucket4j.Bucket;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
@Priority(Priorities.USER)
public class RateLimitFilter implements ContainerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int capacity;
    private final long refillDurationInSeconds;

    @Inject
    public RateLimitFilter(
        @ConfigProperty(name = "rate.limit.capacity", defaultValue = "60") int capacity,
        @ConfigProperty(name = "rate.limit.refill-duration-seconds", defaultValue = "60") long refillDurationInSeconds
    ) {
        this.capacity = capacity;
        this.refillDurationInSeconds = refillDurationInSeconds;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String clientKey = resolveClientKey(requestContext);
        Bucket bucket = buckets.computeIfAbsent(clientKey, key -> createBucket());

        if (!bucket.tryConsume(1)) {
            requestContext.abortWith(
                Response.status(Response.Status.TOO_MANY_REQUESTS)
                    .entity("Rate limit exceeded. Please try again later.")
                    .type(MediaType.TEXT_PLAIN)
                    .build()
            );
        }
    }

    private Bucket createBucket() {
        return Bucket.builder()
            .addLimit(
                BandwidthBuilder
                    .builder()
                    .capacity(capacity).refillGreedy(capacity, Duration.ofSeconds(refillDurationInSeconds))
                    .build())
            .build();
    }

    private String resolveClientKey(ContainerRequestContext requestContext) {
        String forwardedFor = requestContext.getHeaderString("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = requestContext.getHeaderString("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return requestContext.getSecurityContext() != null && requestContext.getSecurityContext().getUserPrincipal() != null
            ? requestContext.getSecurityContext().getUserPrincipal().getName()
            : "anonymous";
    }
}
