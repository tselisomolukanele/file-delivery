package com.example.documentservice.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RateLimitFilterTest {

    @Test
    void rejectsRequestsBeyondConfiguredLimit() {
        RateLimitFilter filter = new RateLimitFilter(1, 60);
        ContainerRequestContext firstRequest = mock(ContainerRequestContext.class);
        ContainerRequestContext secondRequest = mock(ContainerRequestContext.class);

        when(firstRequest.getHeaderString("X-Forwarded-For")).thenReturn("203.0.113.10");
        when(secondRequest.getHeaderString("X-Forwarded-For")).thenReturn("203.0.113.10");

        filter.filter(firstRequest);
        filter.filter(secondRequest);

        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(secondRequest).abortWith(responseCaptor.capture());

        Response response = responseCaptor.getValue();
        assertNotNull(response);
        assertEquals(Response.Status.TOO_MANY_REQUESTS.getStatusCode(), response.getStatus());
    }
}
