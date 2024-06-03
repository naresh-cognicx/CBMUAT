package com.cognicx.AppointmentRemainder.Request;

import lombok.Data;

@Data
public class NotReadyRequest {

    private String notReadyCodeId;
    private String notReadyCodeDescription;
}
