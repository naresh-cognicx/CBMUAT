package com.cognicx.AppointmentRemainder.Request;

import lombok.Data;
import org.apache.commons.net.ntp.TimeStamp;

import java.sql.Date;
import java.sql.Timestamp;

@Data
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
}
