package com.cognicx.AppointmentRemainder.service.impl;


import com.cognicx.AppointmentRemainder.Exception.ApplicationException;
import com.cognicx.AppointmentRemainder.Request.*;
import com.cognicx.AppointmentRemainder.constant.ApplicationConstant;
import com.cognicx.AppointmentRemainder.dao.AgentDao;
import com.cognicx.AppointmentRemainder.model.AgentStatusUpdate;
import com.cognicx.AppointmentRemainder.response.Response;
import com.cognicx.AppointmentRemainder.service.AgentService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class AgentServiceImpl implements AgentService {

    private static final Logger logger = LogManager.getLogger(AgentServiceImpl.class);

    @Autowired
    AgentDao agentDao;

    @PersistenceContext(unitName = ApplicationConstant.FIRST_PERSISTENCE_UNIT_NAME)
    public EntityManager firstEntityManager;

   /* @Override
    public CustomerRequest createCustomer(CustomerRequest customerRequest) {
        AgentDao
    }*/

    @Override
    public AgentStatusUpdateRequest updateAgentStatus(AgentStatusUpdateRequest agentStatusUpdateRequest) throws Exception {
        try {

            if (agentStatusUpdateRequest.getAgentId() != null) {
                agentDao.updateAgentStatus(agentStatusUpdateRequest);
            }
        } catch (ApplicationException e) {
            throw new ApplicationException(ApplicationConstant.FAILED_TO_UPDATE, Response.Status.ERROR);
        }
        return agentStatusUpdateRequest;
    }

    @Override
    public List<AgentStatusRequest> getAgentStatusList() {
        try{
           return agentDao.getAgentStatusList();

        }
        catch (ApplicationException e){
            throw new ApplicationException(ApplicationConstant.FAILED_TO_LIST_USER_STATUS, Response.Status.BAD_REQUEST);
        }
    }

    @Override
    public List<NotReadyRequest> getNotReadyStatusList() {
        try{
            return agentDao.getNotReadyStatusList();

        }
        catch (ApplicationException e){
            throw new ApplicationException(ApplicationConstant.FAILED_TO_LIST_USER_STATUS, Response.Status.BAD_REQUEST);
        }
    }

    @Override
    public AgentInteractionRequest saveAgentInteraction(AgentInteractionRequest agentInteraction) throws Exception {
        try{
            return agentDao.saveAgentInteraction(agentInteraction);
        }
        catch (ApplicationException e){
            throw new ApplicationException(ApplicationConstant.FAILED_TO_SAVE_AGENT_INTERACTION_PLEASE_CONTACT_YOUR_SYSTEM_ADMIN, Response.Status.INTERNAL_SERVER_ERROR);
        }
        catch (Exception ex){
            logger.error("Error in Save agent interaction:"+ ex.getMessage());
            throw new Exception(ex.getMessage());
        }
    }

    @Override
    public List<AgentInteractionRequest> getAgentInteractionList(String agentId) {
        List<AgentInteractionRequest> agentInteractionRequestList = new ArrayList<>();
        try {
            if (StringUtils.isNotBlank(agentId)) {
                agentInteractionRequestList= agentDao.getAgentInteractionList(agentId);
            } else {
                agentInteractionRequestList = agentDao.getAllAgentInteractionList();
            }
        }
        catch (Exception e){
            logger.info("Exception while fetching agent interaction data"+e);
            throw new ApplicationException(e.getMessage(), Response.Status.ERROR);
        }
        return agentInteractionRequestList;
    }

    @Override
    public String updateDispositonInagentInteraction(String sipId, String disposition) {
        return agentDao.updateDispositonInagentInteraction(sipId,disposition);
    }

    @Override
    public AgentActivityRequest saveAgentActToInteraction(AgentActivityRequest activityDetails) {
        return agentDao.saveAgentActToInteraction(activityDetails);
    }


}
