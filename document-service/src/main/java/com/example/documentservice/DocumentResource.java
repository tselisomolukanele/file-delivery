package com.example.documentservice;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.Map;

@Path("/api")
public class DocumentResource {

    @GET
    @Path("/documents")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"file-owner"})
    public List<Map<String, String>> documents() {
        return List.of(
                Map.of("id", "DOC-1001", "name", "contract.pdf", "status", "approved"),
                Map.of("id", "DOC-1002", "name", "invoice.pdf", "status", "pending")
        );
    }

    @GET
    @Path("/profiles")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"file-uploader"})
    public List<String> profiles() {
        return List.of(
                "Alice Johnson",
                "Bob Smith",
                "Charlie Brown"
        );
    }
}
