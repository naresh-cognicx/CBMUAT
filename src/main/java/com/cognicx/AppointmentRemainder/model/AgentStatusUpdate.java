package com.cognicx.AppointmentRemainder.model;



import lombok.EqualsAndHashCode;

import javax.persistence.*;



@Entity
@Table(name = "agent_update_status", schema = "appointment_remainder")
@EqualsAndHashCode(callSuper = false)
public class AgentStatusUpdate extends AuditFields {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(name = "agent_id", nullable = false)
    private String agentId;

    @Column(name = "status")
    private String Status;

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
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
}

