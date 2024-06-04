package com.cognicx.AppointmentRemainder.Request;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class AgentActivityRequest {
    private String id;
    private String sipId;
    private String agentId;
    private String dins;
    private String callStatus;
    private String activity;
    private String direction;
    private Timestamp date;
}
