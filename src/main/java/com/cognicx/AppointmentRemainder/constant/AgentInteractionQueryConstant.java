package com.cognicx.AppointmentRemainder.constant;

public class AgentInteractionQueryConstant {
    public static final String INSERT_AGENT_INTERACTION = "INSERT INTO appointment_remainder.agent_interaction (id, aniNumber, agentId, sipId, dins, date, arrivalTime,connectedTime, disconnectedTime, skillGroup, channel, callStatus, duration, direction, recording, download, disposition) VALUES (:id, :aniNumber, :agentId, :sipId, :dins, :date, :arrivalTime, :connectedTime, :disconnectedTime, :skillGroup, :channel, :callStatus, :duration, :direction, :recording, :download, :disposition)";
    public static final String GET_ID = "select max(id) from appointment_remainder.agent_interaction";
    public static final String GET_LIST ="SELECT *  FROM appointment_remainder.agent_interaction WHERE agentId = :agentId ORDER BY date Asc";
    public static final String GET_ALL_LIST ="SELECT *  FROM appointment_remainder.agent_interaction";

    public static final String UPDATE_DISP="UPDATE appointment_remainder.agent_interaction SET disposition = :disposition WHERE sipId = :sipId";
}
