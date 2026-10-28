package com.example.documentservice.presentation;

import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider 
public class GlobalExceptionMapper {
 
    @ServerExceptionMapper(BadRequestException.class)
    public Response mapException(BadRequestException exception) {
        return
            Response
                .status(exception.getResponse().getStatus())
                .entity(exception.getMessage())
                .type(MediaType.TEXT_PLAIN)
                .build();
    }

    @ServerExceptionMapper(NotFoundException.class)
    public Response mapException(NotFoundException exception) {
        return
            Response
                .status(exception.getResponse().getStatus())
                .entity(exception.getMessage())
                .type(MediaType.TEXT_PLAIN)
                .build();
    }

    @ServerExceptionMapper(InternalServerErrorException.class)
    public Response mapException(InternalServerErrorException exception) {
        return
            Response
                .status(exception.getResponse().getStatus())
                .entity(exception.getMessage())
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
}
