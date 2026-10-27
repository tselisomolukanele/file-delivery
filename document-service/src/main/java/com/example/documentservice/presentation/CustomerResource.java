package com.example.documentservice.presentation;

import java.util.List;

import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import com.example.documentservice.service.CustomerService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Path("/api/customers")
@Slf4j
public class CustomerResource {

    @Inject 
    CustomerService customerService;
    
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

    @GET    
    @Path("/current/otp")
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed({"file-owner"})
    public String otp() {
        return customerService.getOTP();
    }

    @ServerExceptionMapper
    public Response mapException(ClientErrorException exception) {
        return
            Response
                .status(exception.getResponse().getStatus())
                .entity(exception.getMessage())
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
}
