package com.example.documentservice.presentation;

import com.example.documentservice.service.DocumentDataService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("api/email")
public class EmailResource {

    @Inject 
    DocumentDataService documentDataService;
    
    @GET
    @Path("/document-link")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDocumentLink(
        @QueryParam("documentId") long documentId,
        @QueryParam("username") String username) {

        return
            Response
            .ok(documentDataService.getDocumentLink(documentId, username))
            .build();
    }
}
