package com.cognicx.AppointmentRemainder.dao;

import com.cognicx.AppointmentRemainder.Request.AgentRequest;
import com.cognicx.AppointmentRemainder.Request.CallBackScheduleRequest;
import com.cognicx.AppointmentRemainder.Request.MusicAddRequest;
import com.cognicx.AppointmentRemainder.Request.*;
import java.util.List;

public interface AgentDao {

    boolean createCallbackSchedule(CallBackScheduleRequest callBackScheduleRequest);

    boolean agentAsterisk(AgentRequest agentRequest);

    boolean holdMusicAdd(MusicAddRequest musicAddRequest);

    List<Object[]> getCampaignDetforAgent(String userGroup);

    List<String> getDialplanList();

    List<String> getCampaignbyDialplan(String dialplan);

    boolean agentAsteriskMultiaction(AgentRequest agentRequest);

  //  CustomerRequest createCustomer(CustomerRequest customerRequest);
    AgentStatusUpdateRequest updateAgentStatus(AgentStatusUpdateRequest userStatusRequest);
     List<AgentStatusRequest> getAgentStatusList();
    List<NotReadyRequest> getNotReadyStatusList();
   AgentInteractionRequest saveAgentInteraction(AgentInteractionRequest agentInteractionRequest);
   List<AgentInteractionRequest> getAgentInteractionList(String agentId);
   String updateDispositonInagentInteraction(String sipId,String disposition);
   List<AgentInteractionRequest> getAllAgentInteractionList();
}
