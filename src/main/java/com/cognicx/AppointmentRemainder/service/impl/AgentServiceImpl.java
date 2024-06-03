package com.cognicx.AppointmentRemainder.service.impl;

import com.cognicx.AppointmentRemainder.Request.*;
import com.cognicx.AppointmentRemainder.dao.AgentDao;
import com.cognicx.AppointmentRemainder.response.GenericResponse;
import com.cognicx.AppointmentRemainder.service.AgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AgentServiceImpl implements AgentService {
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
    public ResponseEntity<GenericResponse> agentAsteriskLogin(AgentRequest agentRequest) {
        GenericResponse genericResponse = new GenericResponse();
        try {
            boolean isCreated = agentDao.agentAsterisk(agentRequest);
            if (isCreated) {
                genericResponse.setStatus(200);
                genericResponse.setValue("Success");
                genericResponse.setMessage("Agent Login Successfully");
            } else {
                genericResponse.setStatus(400);
                genericResponse.setValue("Failure");
                genericResponse.setMessage("Error occurred while logout the agent from Asterisk");
            }
        } catch (Exception e) {
            logger.error("Error in AgentServiceImpl::agent Asterisk: ", e);
            genericResponse.setStatus(400);
            genericResponse.setValue("Failure");
            genericResponse.setMessage("Error occurred while login the agent from Asterisk");
        }
        return new ResponseEntity<>(genericResponse, HttpStatus.OK);
    }


    @Override
    public ResponseEntity<GenericResponse> agentAsteriskLogout(AgentRequest agentRequest) {
        GenericResponse genericResponse = new GenericResponse();
        try {
            boolean isCreated = agentDao.agentAsterisk(agentRequest);
            if (isCreated) {
                genericResponse.setStatus(200);
                genericResponse.setValue("Success");
                genericResponse.setMessage("Agent Logout Successfully");
            } else {
                genericResponse.setStatus(400);
                genericResponse.setValue("Failure");
                genericResponse.setMessage("Error occurred while logout the agent from Asterisk");
            }
        } catch (Exception e) {
            logger.error("Error in AgentServiceImpl::logout agent Asterisk: ", e);
            genericResponse.setStatus(400);
            genericResponse.setValue("Failure");
            genericResponse.setMessage("Error occurred while logout the agent from Asterisk");
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


    public List<AgentCampaignDetRequest> getCampaignDetListForAgent(String userGroup) {
        List<AgentCampaignDetRequest> campaignDetListforAgent = new ArrayList<>();
        List<Object[]> campainDetObjList = agentDao.getCampaignDetforAgent(userGroup);
        if (campainDetObjList != null && !campainDetObjList.isEmpty()) {
            for (Object[] obj : campainDetObjList) {
                AgentCampaignDetRequest agentCampaignDetRequest = new AgentCampaignDetRequest();
                agentCampaignDetRequest.setCampaignId(String.valueOf(obj[0]));
                agentCampaignDetRequest.setCampaignName(String.valueOf(obj[1]));

                campaignDetListforAgent.add(agentCampaignDetRequest);
            }
        }
        return campaignDetListforAgent;
    }
}
