package com.example.documentservice.presentation;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import com.example.documentservice.persistence.model.DocumentAuditAction;
import com.example.documentservice.persistence.model.DocumentAuditAuthType;
import com.example.documentservice.service.CustomerService;
import com.example.documentservice.service.DocumentAuditService;
import com.example.documentservice.service.DocumentDataService;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import lombok.extern.slf4j.Slf4j;

@Path("/api/customers")
@Slf4j
public class CustomerResource {

    @Inject 
    CustomerService customerService;

    @Inject 
    DocumentDataService documentDataService;
    
    @Inject
    private SecurityIdentity securityIdentity;

    @Inject
    DocumentAuditService documentAuditService;

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"file-uploader"})
    public CustomerDTO addCustomer(CustomerDTO customerDTO) {
      return customerService.addCustomer(customerDTO);
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"file-uploader"})
    public List<CustomerDTO> customers() {
        return customerService.getAllCustomers();
    }

    @GET    
    @Path("/current/documents")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"file-owner"})
    public List<DocumentDTO> documents() {
        return customerService.getCustomerDocuments();
    }

    @POST
    @Path("/current/otp")
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed({"file-owner"})
    public String otp() {
        return customerService.getOTP();
    }

    @GET
    @Path("/current/document/download/{documentId}")
    @RolesAllowed({"file-owner"})
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(long documentId) {

        var customerName = securityIdentity.getPrincipal().getName();
        var documentData = documentDataService.downloadFile(customerName, documentId);

        ResponseBuilder response = Response.ok(documentData.getData());
        response.header("Content-Disposition", buildContentDisposition(documentData.getFilename()));
        response.header("Content-Type", documentData.getMimetype());

        documentAuditService.recordDocumentAccess(
            documentData.getDocumentId(),
            documentData.getOwner(),
            DocumentAuditAction.DOWNLOAD,
            DocumentAuditAuthType.SECURE
        );

        return response.build();
    }

    private static String buildContentDisposition(String filename) {
        String safeFilename = filename == null || filename.isBlank() ? "download" : filename;
        String encodedFilename = URLEncoder.encode(safeFilename, StandardCharsets.UTF_8)
            .replace("+", "%20");
        return "attachment; filename=\"" + safeFilename.replace("\"", "\\\"") + "\"; filename*=UTF-8''" + encodedFilename;
    }
}
