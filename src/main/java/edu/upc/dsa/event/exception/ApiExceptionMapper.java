package edu.upc.dsa.event.exception;

import edu.upc.dsa.event.model.ApiError;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {

    @Override
    public Response toResponse(ApiException ex) {
        ApiError error = new ApiError(ex.getStatus(), ex.getCode(), ex.getMessage());
        return Response.status(ex.getStatus()).entity(error).type(MediaType.APPLICATION_JSON).build();
    }
}

