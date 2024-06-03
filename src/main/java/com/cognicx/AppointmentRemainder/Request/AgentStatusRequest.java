package com.cognicx.AppointmentRemainder.Request;

import lombok.Data;

import java.io.Serializable;
@Data
public class AgentStatusRequest {

        private String statusId;
        private String statusName;
}
