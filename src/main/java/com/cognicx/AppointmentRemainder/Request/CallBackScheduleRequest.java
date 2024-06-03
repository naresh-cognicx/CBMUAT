package com.cognicx.AppointmentRemainder.Request;

public class CallBackScheduleRequest {

    private String scheduledTime;
    private String scheduledDate;
    private String type;
    private String digitalNotification;
    private String dialplanOrQueue;
    private String campaign;
    private String agent;

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(String scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDigitalNotification() {
        return digitalNotification;
    }

    public void setDigitalNotification(String digitalNotification) {
        this.digitalNotification = digitalNotification;
    }

    public String getDialplanOrQueue() {
        return dialplanOrQueue;
    }

    public void setDialplanOrQueue(String dialplanOrQueue) {
        this.dialplanOrQueue = dialplanOrQueue;
    }

    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(String campaign) {
        this.campaign = campaign;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    @Override
    public String toString() {
        return "CallBackScheduleRequest{" +
                "scheduledTime='" + scheduledTime + '\'' +
                ", scheduledDate='" + scheduledDate + '\'' +
                ", type='" + type + '\'' +
                ", digitalNotification='" + digitalNotification + '\'' +
                ", dialplanOrQueue='" + dialplanOrQueue + '\'' +
                ", campaign='" + campaign + '\'' +
                ", agent='" + agent + '\'' +
                '}';
    }
}
