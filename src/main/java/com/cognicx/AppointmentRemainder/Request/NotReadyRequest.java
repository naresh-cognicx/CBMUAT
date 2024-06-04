package com.cognicx.AppointmentRemainder.Request;


public class NotReadyRequest {

    private String notReadyCodeId;
    private String notReadyCodeDescription;

    public String getNotReadyCodeId() {
        return notReadyCodeId;
    }

    public void setNotReadyCodeId(String notReadyCodeId) {
        this.notReadyCodeId = notReadyCodeId;
    }

    public String getNotReadyCodeDescription() {
        return notReadyCodeDescription;
    }

    public void setNotReadyCodeDescription(String notReadyCodeDescription) {
        this.notReadyCodeDescription = notReadyCodeDescription;
    }
}
