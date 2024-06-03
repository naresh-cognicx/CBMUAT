package com.cognicx.AppointmentRemainder.model;

import com.cognicx.AppointmentRemainder.constant.ApplicationConstant;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;


@Data
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


}

