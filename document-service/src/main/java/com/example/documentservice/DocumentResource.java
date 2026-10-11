package com.example.documentservice;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.Map;

@Path("/api")
public class DocumentResource {

    @GET
    @Path("/documents")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    public List<Map<String, String>> documents() {
        return List.of(
                Map.of("id", "DOC-1001", "name", "contract.pdf", "status", "approved"),
                Map.of("id", "DOC-1002", "name", "invoice.pdf", "status", "pending")
        );
    }

    @GET
    @Path("/profile")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"user", "admin"})
    public Map<String, Object> profile(SecurityContext securityContext) {
        var principal = securityContext.getUserPrincipal();
        return Map.of(
                "username", principal != null ? principal.getName() : "anonymous",
                "issuer", "http://localhost:8080/realms/document-service",
                "roles", securityContext.isUserInRole("admin") ? List.of("admin") : List.of("user")
        );
    }
}
