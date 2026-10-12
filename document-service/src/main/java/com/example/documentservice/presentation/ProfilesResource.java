package com.example.documentservice.presentation;

import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api")
public class ProfilesResource {
    
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
