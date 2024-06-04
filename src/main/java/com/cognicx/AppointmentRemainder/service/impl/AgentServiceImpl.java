package com.cognicx.AppointmentRemainder.service.impl;


import com.cognicx.AppointmentRemainder.Exception.ApplicationException;
import com.cognicx.AppointmentRemainder.Request.*;
import com.cognicx.AppointmentRemainder.constant.ApplicationConstant;
import com.cognicx.AppointmentRemainder.dao.AgentDao;
import com.cognicx.AppointmentRemainder.response.GenericResponse;
import com.cognicx.AppointmentRemainder.response.Response;
import com.cognicx.AppointmentRemainder.service.AgentService;

import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AgentServiceImpl implements AgentService {
//    private static final Logger logger = LogManager.getLogger(AgentServiceImpl.class);
    private static final Logger logger = LoggerFactory.getLogger(AgentServiceImpl.class);

    @Autowired
    AgentDao agentDao;

    @Override
    public ResponseEntity<GenericResponse> createCallbackSchedule(CallBackScheduleRequest callBackScheduleRequest) {
        GenericResponse genericResponse = new GenericResponse();
        try {
            boolean isCreated = agentDao.createCallbackSchedule(callBackScheduleRequest);
            if (isCreated) {
                genericResponse.setStatus(200);
                genericResponse.setValue("Success");
                genericResponse.setMessage("Call Scheduled successfully");
            } else {
                genericResponse.setStatus(400);
                genericResponse.setValue("Failure");
                genericResponse.setMessage("Error occurred while scheduling the call");
            }
        } catch (Exception e) {
            logger.error("Error in AgentServiceImpl::createCallbackSchedule: ", e);
            genericResponse.setStatus(400);
            genericResponse.setValue("Failure");
            genericResponse.setMessage("Error occurred while creating the callback schedule");
        }
        return new ResponseEntity<>(genericResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GenericResponse> agentAsteriskCampaignbasedLogin(AgentRequest agentRequest) {
        GenericResponse genericResponse = new GenericResponse();
        String status = agentRequest.getAction();
        try {
            boolean isCreated = agentDao.agentAsterisk(agentRequest);
            if (isCreated) {
                genericResponse.setStatus(200);
                genericResponse.setValue("Success");
                genericResponse.setMessage("Agent "+status+" Successfully");
            } else {
                genericResponse.setStatus(400);
                genericResponse.setValue("Failure");
                genericResponse.setMessage("Error occurred while "+status+" the agent from Asterisk");
            }
        } catch (Exception e) {
            logger.error("Error in AgentServiceImpl::agent Asterisk: ", e);
            genericResponse.setStatus(400);
            genericResponse.setValue("Failure");
            genericResponse.setMessage("Error occurred while "+status+" the agent from Asterisk");
        }
        return new ResponseEntity<>(genericResponse, HttpStatus.OK);
    }


    @Override
    public ResponseEntity<GenericResponse> holdMusicAdd(MusicAddRequest musicAddRequest) {
        GenericResponse genericResponse = new GenericResponse();
        try {
            boolean isCreated = agentDao.holdMusicAdd(musicAddRequest);
            if (isCreated) {
                genericResponse.setStatus(200);
                genericResponse.setValue("Success");
                genericResponse.setMessage("Hold Music Added Successfully");
            } else {
                genericResponse.setStatus(400);
                genericResponse.setValue("Failure");
                genericResponse.setMessage("Error occurred while adding hold Music ");
            }
        } catch (Exception e) {
            logger.error("Error in AgentServiceImpl::adding hold Music: ", e);
            genericResponse.setStatus(400);
            genericResponse.setValue("Failure");
            genericResponse.setMessage("Error occurred while adding hold Music");
        }
        return new ResponseEntity<>(genericResponse, HttpStatus.OK);
    }

//    @Override
//    public AgentStatusUpdateRequest updateAgentStatus(AgentStatusUpdateRequest agentStatusUpdateRequest) throws Exception {
//        try {
//
//            if (agentStatusUpdateRequest.getAgentId() != null) {
//                agentDao.updateAgentStatus(agentStatusUpdateRequest);
//            }
//        } catch (ApplicationException e) {
//            throw new ApplicationException(ApplicationConstant.FAILED_TO_UPDATE, Response.Status.ERROR);
//        }
//        return agentStatusUpdateRequest;
//    }

    @Override
    public ResponseEntity<GenericResponse> getCampaignDetail(String userGroup) {
        GenericResponse genericResponse = new GenericResponse();
        List<AgentCampaignDetRequest> campaignDetList = null;
        try {
            campaignDetList = getCampaignDetListForAgent(userGroup);
            genericResponse.setStatus(200);
            genericResponse.setValue(campaignDetList);
            genericResponse.setMessage("Success");
        } catch (Exception e) {
            logger.error("Error in AgentServiceImpl::getCampaignDetailForAgent " + e);
            genericResponse.setStatus(400);
            genericResponse.setValue("Failure");
            genericResponse.setMessage("No data Found");
        }

        return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GenericResponse> getDialplanList() {
        GenericResponse genericResponse = new GenericResponse();
        List<String> dialplanList = null;
        try {
            dialplanList = agentDao.getDialplanList();
            genericResponse.setStatus(200);
            genericResponse.setValue(dialplanList);
            genericResponse.setMessage("Success");
        } catch (Exception e) {
            logger.error("Error in AgentServiceImpl::getDialplanList " + e);
            genericResponse.setStatus(400);
            genericResponse.setValue("Failure");
            genericResponse.setMessage("No data Found");
        }
        return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GenericResponse> getCampaignbyDialplan(String dialplan) {
        GenericResponse genericResponse = new GenericResponse();
        List<String> getCampaignbyDialplan = null;
        try {
            getCampaignbyDialplan = agentDao.getCampaignbyDialplan(dialplan);
            genericResponse.setStatus(200);
            genericResponse.setValue(getCampaignbyDialplan);
            genericResponse.setMessage("Success");
        } catch (Exception e) {
            logger.error("Error in AgentServiceImpl::getCampaignbyDialplan " + e);
            genericResponse.setStatus(400);
            genericResponse.setValue("Failure");
            genericResponse.setMessage("No data Found");
        }
        return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GenericResponse> agentAsteriskMultiLogin(AgentRequest agentRequest) {
        GenericResponse genericResponse = new GenericResponse();
        String status = agentRequest.getAction();
        try {
            boolean isCreated = agentDao.agentAsteriskMultiaction(agentRequest);
            if (isCreated) {
                genericResponse.setStatus(200);
                genericResponse.setValue("Success");
                genericResponse.setMessage("Agent "+status+" Successfully");
            } else {
                genericResponse.setStatus(400);
                genericResponse.setValue("Failure");
                genericResponse.setMessage("Error occurred while "+status+" the agent ");
            }
        } catch (Exception e) {
            logger.error("Error in AgentServiceImpl::agent Asterisk: ", e);
            genericResponse.setStatus(400);
            genericResponse.setValue("Failure");
            genericResponse.setMessage("Error occurred while "+status+" the agent from Asterisk");
        }
        return new ResponseEntity<>(genericResponse, HttpStatus.OK);
    }


    public List<AgentCampaignDetRequest> getCampaignDetListForAgent(String userGroup) {
        List<AgentCampaignDetRequest> campaignDetListforAgent = new ArrayList<>();
        List<Object[]> campainDetObjList = agentDao.getCampaignDetforAgent(userGroup);
        if (campainDetObjList != null && !campainDetObjList.isEmpty()) {
            for (Object[] obj : campainDetObjList) {
                AgentCampaignDetRequest agentCampaignDetRequest = new AgentCampaignDetRequest();
                agentCampaignDetRequest.setCampaignId(String.valueOf(obj[0]));
                agentCampaignDetRequest.setCampaignName(String.valueOf(obj[1]));
                agentCampaignDetRequest.setVdnQueueId(String.valueOf(obj[2]));
                campaignDetListforAgent.add(agentCampaignDetRequest);
            }
        }
        return campaignDetListforAgent;
    }

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
            if (!agentId.isEmpty() && agentId!=null) {
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

}
