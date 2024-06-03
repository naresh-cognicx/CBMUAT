package com.cognicx.AppointmentRemainder.Request;


import com.cognicx.AppointmentRemainder.model.AuditFields;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class AgentStatusUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String agentId;

    private String status;

    private String updated_date;
}
