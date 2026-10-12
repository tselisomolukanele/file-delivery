package com.example.documentservice.presentation;

import java.util.List;
import java.util.Map;

import com.example.documentservice.service.DocumentLinkService;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api")
public class ProfileResource {

    @Inject
    DocumentLinkService documentLinkService;

    @Inject
    private SecurityIdentity securityIdentity;
    
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

    @GET    
    @Path("/documents")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"file-owner"})
    public List<Map<String, String>> documents() {
        return List.of(
                Map.of(
                    "name", "Document.pdf", 
                    "link", documentLinkService.createDocumentLink("Report.pdf", securityIdentity.getPrincipal().getName()).getLink()
                )
        );
    }
}
