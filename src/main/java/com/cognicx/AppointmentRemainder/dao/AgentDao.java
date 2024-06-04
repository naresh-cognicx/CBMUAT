package com.cognicx.AppointmentRemainder.dao;

import com.cognicx.AppointmentRemainder.Request.*;
import com.cognicx.AppointmentRemainder.model.AgentStatusUpdate;

import java.util.List;

public interface AgentDao {

  //  CustomerRequest createCustomer(CustomerRequest customerRequest);
    AgentStatusUpdateRequest updateAgentStatus(AgentStatusUpdateRequest userStatusRequest);
     List<AgentStatusRequest> getAgentStatusList();
    List<NotReadyRequest> getNotReadyStatusList();
   AgentInteractionRequest saveAgentInteraction(AgentInteractionRequest agentInteractionRequest);
   List<AgentInteractionRequest> getAgentInteractionList(String agentId);
   String updateDispositonInagentInteraction(String sipId,String disposition);
   List<AgentInteractionRequest> getAllAgentInteractionList();
    AgentActivityRequest saveAgentActToInteraction(AgentActivityRequest agentActivityRequest);
}
