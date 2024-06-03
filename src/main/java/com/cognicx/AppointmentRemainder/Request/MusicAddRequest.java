package com.cognicx.AppointmentRemainder.Request;

public class MusicAddRequest {
    private String actionId;
    private String name;
    private String mode;
    private String url;

    private String customerCode;

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    @Override
    public String toString() {
        return "MusicAddRequest{" +
                "actionId='" + actionId + '\'' +
                ", name='" + name + '\'' +
                ", mode='" + mode + '\'' +
                ", url='" + url + '\'' +
                ", customerCode='" + customerCode + '\'' +
                '}';
    }
}
