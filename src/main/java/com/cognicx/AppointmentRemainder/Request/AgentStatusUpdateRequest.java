package com.cognicx.AppointmentRemainder.Request;



import com.cognicx.AppointmentRemainder.model.AuditFields;

import java.io.Serializable;


public class AgentStatusUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String agentId;

    private String status;

    private String updated_date;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUpdated_date() {
        return updated_date;
    }

    public void setUpdated_date(String updated_date) {
        this.updated_date = updated_date;
    }
}
