package com.cognicx.AppointmentRemainder.Exception;


import com.cognicx.AppointmentRemainder.response.Response;

public class ApplicationException extends RuntimeException{
    private static final long serialVersionUID = -2697866270051230169L;

    private Response.Status status;

    public ApplicationException(String message, Response.Status status) {
        super(message);
        this.status = status;
    }

    public Response.Status getStatus() {
        return status;
    }

    public void setStatus(Response.Status status) {
        this.status = status;
    }

}
