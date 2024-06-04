package com.cognicx.AppointmentRemainder.Request;


import java.sql.Timestamp;


public class AgentInteractionRequest {
    private String id;
    private String aniNumber;
    private String agentId;
    private String sipId;
    private String dins;
    private Timestamp date;
    private Timestamp arrivalTime;
    private Timestamp connectedTime;
    private Timestamp disconnectedTime;
    private String skillGroup;
    private String channel;
    private String callStatus;
    private int duration;
    private String direction;
    private String recording;
    private boolean download;
    private String disposition;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAniNumber() {
        return aniNumber;
    }

    public void setAniNumber(String aniNumber) {
        this.aniNumber = aniNumber;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getSipId() {
        return sipId;
    }

    public void setSipId(String sipId) {
        this.sipId = sipId;
    }

    public String getDins() {
        return dins;
    }

    public void setDins(String dins) {
        this.dins = dins;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public Timestamp getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Timestamp arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Timestamp getConnectedTime() {
        return connectedTime;
    }

    public void setConnectedTime(Timestamp connectedTime) {
        this.connectedTime = connectedTime;
    }

    public Timestamp getDisconnectedTime() {
        return disconnectedTime;
    }

    public void setDisconnectedTime(Timestamp disconnectedTime) {
        this.disconnectedTime = disconnectedTime;
    }

    public String getSkillGroup() {
        return skillGroup;
    }

    public void setSkillGroup(String skillGroup) {
        this.skillGroup = skillGroup;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(String callStatus) {
        this.callStatus = callStatus;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getRecording() {
        return recording;
    }

    public void setRecording(String recording) {
        this.recording = recording;
    }

    public boolean isDownload() {
        return download;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }

    public String getDisposition() {
        return disposition;
    }

    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }
}
