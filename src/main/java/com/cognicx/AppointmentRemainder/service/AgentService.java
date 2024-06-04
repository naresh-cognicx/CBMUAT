package com.cognicx.AppointmentRemainder.service;

import com.cognicx.AppointmentRemainder.Dto.UserDto;
import com.cognicx.AppointmentRemainder.Request.*;
import com.cognicx.AppointmentRemainder.model.AgentStatusUpdate;
import org.springframework.stereotype.Service;

import java.util.List;


public interface AgentService {


    //CustomerRequest createCustomer(CustomerRequest customerRequest);
    AgentStatusUpdateRequest updateAgentStatus(AgentStatusUpdateRequest userStatusRequest) throws Exception;

    List<AgentStatusRequest> getAgentStatusList();

    List<NotReadyRequest> getNotReadyStatusList();

    AgentInteractionRequest saveAgentInteraction(AgentInteractionRequest agentInteraction) throws Exception;

    List<AgentInteractionRequest> getAgentInteractionList(String agentId);

    String updateDispositonInagentInteraction(String sipId, String disposition);

    AgentActivityRequest saveAgentActToInteraction(AgentActivityRequest activityDetails);
}
