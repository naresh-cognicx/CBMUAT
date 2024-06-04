package com.cognicx.AppointmentRemainder.dao.impl;

import com.cognicx.AppointmentRemainder.Request.AgentRequest;
import com.cognicx.AppointmentRemainder.Request.CallBackScheduleRequest;
import com.cognicx.AppointmentRemainder.Request.MusicAddRequest;
import com.cognicx.AppointmentRemainder.constant.AgentQueryConstant;
import com.cognicx.AppointmentRemainder.constant.ApplicationConstant;
import com.cognicx.AppointmentRemainder.constant.CampaignQueryConstant;
import com.cognicx.AppointmentRemainder.constant.SkillsetQueryConstant;
import com.cognicx.AppointmentRemainder.dao.AgentDao;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.cognicx.AppointmentRemainder.Dto.UserDto;
import com.cognicx.AppointmentRemainder.Exception.ApplicationException;
import com.cognicx.AppointmentRemainder.Request.*;
import com.cognicx.AppointmentRemainder.constant.AgentInteractionQueryConstant;
import com.cognicx.AppointmentRemainder.constant.ApplicationConstant;
import com.cognicx.AppointmentRemainder.constant.UserManagementQueryConstant;
import com.cognicx.AppointmentRemainder.constant.UserStatusQueryConstant;
import com.cognicx.AppointmentRemainder.dao.AgentDao;
import com.cognicx.AppointmentRemainder.model.AgentStatusUpdate;
import com.cognicx.AppointmentRemainder.model.Roles;
import com.cognicx.AppointmentRemainder.response.Response;
import org.apache.commons.net.ntp.TimeStamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.*;

@Repository("AgentDao")
@Transactional
public class AgentDaoImpl implements AgentDao {


    private final Logger logger = LoggerFactory.getLogger(AgentDaoImpl.class);

    @Value("${agent.status.api}")
    private String agentStatusApi;

    @Value("${holdmusicadd}")
    private String holdmusicadd;

    @PersistenceContext(unitName = ApplicationConstant.FIRST_PERSISTENCE_UNIT_NAME)
    public EntityManager firstEntityManager;
    @Override
    public boolean createCallbackSchedule(CallBackScheduleRequest callBackScheduleRequest) {
        int insertVal;
        boolean isCreated = false;
        try {
            Query queryObj = firstEntityManager.createNativeQuery(AgentQueryConstant.INSERT_CALL_BACK_SCHEDULE);
            queryObj.setParameter("scheduledDate", callBackScheduleRequest.getScheduledDate());
            queryObj.setParameter("scheduledTime", callBackScheduleRequest.getScheduledTime());
            queryObj.setParameter("type", callBackScheduleRequest.getType());
            queryObj.setParameter("digitalNotification", callBackScheduleRequest.getDigitalNotification());
            queryObj.setParameter("dialplanOrQueue", callBackScheduleRequest.getDialplanOrQueue());
            queryObj.setParameter("AssignCampaign", callBackScheduleRequest.getCampaign());
            queryObj.setParameter("Agent", callBackScheduleRequest.getAgent());

            insertVal = queryObj.executeUpdate();
            logger.info("Update value : "+insertVal);
            if (insertVal > 0) {
                isCreated = true;
            }
        } catch (Exception e) {
            logger.error("Error occurred in AgentDaoImpl::createCallbackSchedule: ", e);
            isCreated = false;
        }
        return isCreated;
    }

    @Override
    public boolean agentAsterisk(AgentRequest agentRequest) {
        boolean isUpdate;

        String jsonPayload = "{\n" +
                "    \"queue\": \"" + agentRequest.getQueueId() + "\",\n" +
                "    \"agent\": \"" + agentRequest.getPbxExt() + "\",\n" +
                "    \"agentname\": \"" + agentRequest.getAgentName() + "\",\n" +
                "    \"reason\": \"" + agentRequest.getReason() + "\",\n" +
                "    \"action\": \"" + agentRequest.getAction() + "\",\n" +
                "    \"actionid\": \"" + agentRequest.getPbxExt() + "\"\n" +
                "}";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(agentStatusApi);
            httpPost.setHeader("Content-Type", "application/json");
            StringEntity requestEntity = new StringEntity(jsonPayload);
            httpPost.setEntity(requestEntity);
            logger.info("Request : " + " URL : " + httpPost + " payload : " + requestEntity + " request payload : " + jsonPayload);
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity responseEntity = response.getEntity();
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode == 200) {
                    isUpdate = true;
                } else {
                    isUpdate = false;
                }
                logger.info("Response Status Code: " + response.getStatusLine().getStatusCode());
                if (responseEntity != null) {
                    String responseBody = EntityUtils.toString(responseEntity);
                    logger.info(responseBody);
                }
            } catch (Exception e) {
                StringWriter str = new StringWriter();
                e.printStackTrace(new PrintWriter(str));
                logger.error("Exception :" + str.toString());
                isUpdate = false;
            }
        } catch (Exception e) {
            StringWriter str = new StringWriter();
            e.printStackTrace(new PrintWriter(str));
            logger.error("Exception :" + str.toString());
            isUpdate = false;
        }
        return isUpdate;
    }

    @Override
    public boolean holdMusicAdd(MusicAddRequest musicAddRequest) {

        boolean isUpdate = false;

        String actionId = getActionSequence();
        boolean success = createHoldMusicAddinDB(musicAddRequest,actionId);

        if (success){
            logger.info("Success");
        }else {
            logger.info("Failure");
        }

        String jsonPayload = "{\n" +
                "    \"actionid\":\""+actionId+"\",\n" +
                "    \"name\":\""+musicAddRequest.getName()+"\",\n" +
                "    \"mode\":\""+musicAddRequest.getMode()+"\",\n" +
                "    \"entry\":\""+musicAddRequest.getUrl()+"\",\n" +
                "    \"customer_code\":\""+"tenantID"+"\"\n" +
                "}";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(holdmusicadd);
            httpPost.setHeader("Content-Type", "application/json");
            StringEntity requestEntity = new StringEntity(jsonPayload);
            httpPost.setEntity(requestEntity);
            logger.info("Request : " + " URL : " + httpPost + " payload : " + requestEntity + " request payload : " + jsonPayload);
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity responseEntity = response.getEntity();
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode == 200) {
                    isUpdate = true;
                } else {
                    isUpdate = false;
                }
                logger.info("Response Status Code: " + response.getStatusLine().getStatusCode());
                if (responseEntity != null) {
                    String responseBody = EntityUtils.toString(responseEntity);
                    logger.info(responseBody);
                }
            } catch (Exception e) {
                StringWriter str = new StringWriter();
                e.printStackTrace(new PrintWriter(str));
                logger.error("Exception :" + str.toString());
                isUpdate = false;
            }
        } catch (Exception e) {
            StringWriter str = new StringWriter();
            e.printStackTrace(new PrintWriter(str));
            logger.error("Exception :" + str.toString());
            isUpdate = false;
        }
        return isUpdate;
    }

    private boolean createHoldMusicAddinDB(MusicAddRequest musicAddRequest,String actionId) {
        boolean isCreated = false;
        int insert = 0;
        try {
            Query queryObj = firstEntityManager.createNativeQuery("");

            insert = queryObj.executeUpdate();
            if (insert>0) {
                isCreated = true;
            }else {
                isCreated=false;
            }
        } catch (Exception e) {
            StringWriter str = new StringWriter();
            e.printStackTrace(new PrintWriter(str));
            logger.error("Error occured in CampaignDaoImpl::getActionSequence" + str.toString());
            isCreated = false;
        }
        return isCreated;
    }

    private String getActionSequence() {
        String seq = null;
        try {
            Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_ACTIONID_SEQUENCE);
            Object objResult = queryObj.getSingleResult();
            seq = String.valueOf(objResult);
        } catch (Exception e) {
            StringWriter str = new StringWriter();
            e.printStackTrace(new PrintWriter(str));
            logger.error("Error occured in CampaignDaoImpl::getActionSequence" + str.toString());
        }
        return seq;
    }

    @Override
    public List<Object[]> getCampaignDetforAgent(String userGroup) {
        List<Object[]> resultList = null;
        try {
            Query queryObj = firstEntityManager.createNativeQuery(AgentQueryConstant.GET_CAMPAIGN_DET_BY_USERGROUP_FOR_AGENT);
            queryObj.setParameter("groupName", userGroup);
            resultList = queryObj.getResultList();
        } catch (Exception e) {
            logger.error("Error occured in AgentDaoImpl::getCampaignDetforAgent" + e);
            return resultList;
        }
        return resultList;
    }

    @Override
    public List<String> getCampaignbyDialplan(String dialplan) {
        List<String> resultList = null;
        try {
            Query queryObj = firstEntityManager.createNativeQuery(AgentQueryConstant.GET_CAMPAIGN_DET_BY_DIALPLAN);
            queryObj.setParameter("Queue", dialplan);
            resultList = queryObj.getResultList();
        } catch (Exception e) {
            logger.error("Error occured in AgentDaoImpl::getCampaignbyDialplan" + e);
            return resultList;
        }
        return resultList;
    }

    @Override
    public boolean agentAsteriskMultiaction(AgentRequest agentRequest) {
        boolean isUpdate = false;
        String jsonPayload = null;
        List<String> queueList = new ArrayList<>();
        try {
            Query queryObj1 = firstEntityManager.createNativeQuery("SELECT QueueId from appointment_remainder.agent_skillset_mapping where PbxExt=:PbxExt");
            queryObj1.setParameter("PbxExt", agentRequest.getPbxExt());
            queueList = queryObj1.getResultList();
        }catch (Exception e){
            logger.info("Error on getting Queue Id for this Agent pbxExt : " +agentRequest.getPbxExt());
        }

        logger.info("Queue List for this Agent PbxExt : "+agentRequest.getPbxExt()+" is "+queueList);
        for (String queueId : queueList){
             jsonPayload= "{\n" +
                    "    \"queue\": \"" + queueId + "\",\n" +
                    "    \"agent\": \"" + agentRequest.getPbxExt() + "\",\n" +
                    "    \"agentname\": \"" + agentRequest.getAgentName() + "\",\n" +
                    "    \"reason\": \"" + agentRequest.getReason() + "\",\n" +
                    "    \"action\": \"" + agentRequest.getAction() + "\",\n" +
                    "    \"actionid\": \"" + agentRequest.getPbxExt() + "\"\n" +
                    "}";
             logger.info("Queue : "+jsonPayload);
    }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(agentStatusApi);
            httpPost.setHeader("Content-Type", "application/json");
            StringEntity requestEntity = new StringEntity(jsonPayload);
            httpPost.setEntity(requestEntity);
            logger.info("Request : " + " URL : " + httpPost + " payload : " + requestEntity + " request payload : " + jsonPayload);
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity responseEntity = response.getEntity();
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode == 200) {
                    isUpdate = true;
                } else {
                    isUpdate = false;
                }
                logger.info("Response Status Code: " + response.getStatusLine().getStatusCode());
                if (responseEntity != null) {
                    String responseBody = EntityUtils.toString(responseEntity);
                    logger.info(responseBody);
                }
            } catch (Exception e) {
                StringWriter str = new StringWriter();
                e.printStackTrace(new PrintWriter(str));
                logger.error("Exception :" + str.toString());
                isUpdate = false;
            }
        } catch (Exception e) {
            StringWriter str = new StringWriter();
            e.printStackTrace(new PrintWriter(str));
            logger.error("Exception :" + str.toString());
            isUpdate = false;
        }
        return isUpdate;
    }

    @Override
    public List<String> getDialplanList() {
        List<String> resultList = new ArrayList<>();
        try {
            Query queryObj = firstEntityManager.createNativeQuery(AgentQueryConstant.GET_DIALPLAN_LIST);
            resultList = queryObj.getResultList();
        } catch (Exception e) {
            logger.error("Error occurred in AgentDaoImpl::getDialplanList", e);
        }
        return resultList;
    }
    public AgentStatusUpdateRequest updateAgentStatus(AgentStatusUpdateRequest userStatusRequest) {
        int insertVal;
        try {
            int idValue = getUserKey();
            userStatusRequest.setId(String.valueOf(idValue));
            Query queryObj = firstEntityManager.createNativeQuery(UserStatusQueryConstant.INSERT_AGENT_UPDATE_STATUS);
            queryObj.setParameter("id", idValue);
            queryObj.setParameter("agent_id", userStatusRequest.getAgentId());
            queryObj.setParameter("status", userStatusRequest.getStatus());
            queryObj.setParameter("updated_date_time", userStatusRequest.getUpdated_date());
            insertVal = queryObj.executeUpdate();
            if (insertVal > 0) {
                return userStatusRequest;
            }
        } catch (Exception e) {
            logger.error("Error occured in AgentDaoImpl::updateagentStatus" + e);
            return null;
        }
        return null;
    }

    public Integer getUserKey() {
        String maxVal;
        try {
            Query queryObj = firstEntityManager.createNativeQuery(UserStatusQueryConstant.GET_USER_ID);
            maxVal = (String) queryObj.getSingleResult();
            if (maxVal == null || maxVal.isEmpty()) {
                maxVal = "0";
            }
        } catch (Exception e) {
            logger.error("Error occured in UserManagementDaoImpl::getUserKey" + e);
            return 1;
        }
        return Integer.valueOf(maxVal) + 1;
    }


    public List<AgentStatusRequest> getAgentStatusList() {
        List<Object[]> agentStatusList = new ArrayList<>();
        List<AgentStatusRequest> agentStatusRequestList = new ArrayList<>();
        try {
            Query queryObj = firstEntityManager.createNativeQuery(UserStatusQueryConstant.GET_AGENT_DET);
            agentStatusList = queryObj.getResultList();
            if (agentStatusList != null && !agentStatusList.isEmpty()) {
                for (Object[] obj : agentStatusList) {
                    AgentStatusRequest statusRequest = new AgentStatusRequest();
                    statusRequest.setStatusId(String.valueOf(obj[0]));
                    statusRequest.setStatusName(String.valueOf(obj[1]));
                    agentStatusRequestList.add(statusRequest);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to fetch data from AgentDaoImpl:getAgentStatusList" + e);
            throw new ApplicationException(ApplicationConstant.FAILED_TO_LIST_USER_STATUS, Response.Status.BAD_REQUEST);
        }
        return agentStatusRequestList;
    }

    @Override
    public List<NotReadyRequest> getNotReadyStatusList() {
        List<Object[]> notReadyList = new ArrayList<>();
        List<NotReadyRequest> agentStatusRequestList = new ArrayList<>();
        try {
            Query queryObj = firstEntityManager.createNativeQuery(UserStatusQueryConstant.GET_NOT_READY);
            notReadyList = queryObj.getResultList();
            if (notReadyList != null && !notReadyList.isEmpty()) {
                for (Object[] obj : notReadyList) {
                    NotReadyRequest statusRequest = new NotReadyRequest();
                    statusRequest.setNotReadyCodeId(String.valueOf(obj[0]));
                    statusRequest.setNotReadyCodeDescription(String.valueOf(obj[1]));
                    agentStatusRequestList.add(statusRequest);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to fetch data from AgentDaoImpl:getAgentStatusList" + e);
            throw new ApplicationException(ApplicationConstant.FAILED_TO_LIST_USER_STATUS, Response.Status.BAD_REQUEST);
        }
        return agentStatusRequestList;
    }

    @Override
    public AgentInteractionRequest saveAgentInteraction(AgentInteractionRequest agentInteractionRequest) {
        int insertVal;
        try {
            int idValue = getUniqueId();
            Query queryObj = firstEntityManager.createNativeQuery(AgentInteractionQueryConstant.INSERT_AGENT_INTERACTION);
            queryObj.setParameter("id", idValue);
            queryObj.setParameter("aniNumber", agentInteractionRequest.getAniNumber());
            queryObj.setParameter("agentId", agentInteractionRequest.getAgentId());
            queryObj.setParameter("sipId", agentInteractionRequest.getSipId());
            queryObj.setParameter("dins", agentInteractionRequest.getDins());
            queryObj.setParameter("date", agentInteractionRequest.getDate());
            queryObj.setParameter("arrivalTime", agentInteractionRequest.getArrivalTime());
            queryObj.setParameter("connectedTime", agentInteractionRequest.getConnectedTime());
            queryObj.setParameter("disconnectedTime", agentInteractionRequest.getDisconnectedTime());
            queryObj.setParameter("skillGroup", agentInteractionRequest.getSkillGroup());
            queryObj.setParameter("channel", agentInteractionRequest.getChannel());
            queryObj.setParameter("callStatus", agentInteractionRequest.getCallStatus());
            queryObj.setParameter("duration", agentInteractionRequest.getDuration());
            queryObj.setParameter("direction", agentInteractionRequest.getDirection());
            queryObj.setParameter("recording", agentInteractionRequest.getRecording());
            queryObj.setParameter("download", agentInteractionRequest.isDownload());
            queryObj.setParameter("disposition", agentInteractionRequest.getDisposition());
            insertVal = queryObj.executeUpdate();
            if (insertVal > 0) {
                return agentInteractionRequest;
            }
        } catch (Exception e) {
            logger.error("Error occured in AgentDaoImpl::saveAgentInteraction" + e);
            throw new ApplicationException(e.toString(), Response.Status.INTERNAL_SERVER_ERROR);

        }
        return agentInteractionRequest;
    }

    @Override
    public List<AgentInteractionRequest> getAgentInteractionList(String agentId) {
        List<Object[]> interactionlist = new ArrayList<>();
        List<AgentInteractionRequest> agentInteractionRequestList = new ArrayList<>();
        try {
            Query queryObj = firstEntityManager.createNativeQuery(AgentInteractionQueryConstant.GET_LIST);
            queryObj.setParameter("agentId", agentId);
            interactionlist = queryObj.getResultList();
            if (interactionlist != null && !interactionlist.isEmpty()) {
                for (Object[] obj : interactionlist) {
                    AgentInteractionRequest agentInteractionRequest = new AgentInteractionRequest();
                    agentInteractionRequest.setId(String.valueOf(obj[0]));
                    agentInteractionRequest.setAniNumber(String.valueOf(obj[1]));
                    agentInteractionRequest.setAgentId(String.valueOf(obj[2]));
                    agentInteractionRequest.setSipId(String.valueOf(obj[3]));
                    agentInteractionRequest.setDins(String.valueOf(obj[4]));
                    agentInteractionRequest.setDate(Timestamp.valueOf(String.valueOf(obj[5])));
                    agentInteractionRequest.setArrivalTime(Timestamp.valueOf(String.valueOf(obj[6])));
                    agentInteractionRequest.setConnectedTime(Timestamp.valueOf(String.valueOf(obj[7])));
                    agentInteractionRequest.setDisconnectedTime(Timestamp.valueOf(String.valueOf(obj[8])));
                    agentInteractionRequest.setSkillGroup(String.valueOf(obj[9]));
                    agentInteractionRequest.setChannel(String.valueOf(obj[10]));
                    agentInteractionRequest.setCallStatus(String.valueOf(obj[11]));
                    agentInteractionRequest.setDuration((Integer) obj[12]);
                    agentInteractionRequest.setDirection(String.valueOf(obj[13]));
                    agentInteractionRequest.setRecording(String.valueOf(obj[14]));
                    agentInteractionRequest.setDownload(Boolean.getBoolean(String.valueOf(obj[15])));
                    agentInteractionRequest.setDisposition(String.valueOf(obj[16]));
                    agentInteractionRequestList.add(agentInteractionRequest);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to fetch data from AgentDaoImpl:getAgentInteractionList" + e);
            throw new ApplicationException(e.getMessage(), Response.Status.BAD_REQUEST);
        }
        return agentInteractionRequestList;
    }

    public Integer getUniqueId() {
        String maxVal;
        try {
            Query queryObj = firstEntityManager.createNativeQuery(AgentInteractionQueryConstant.GET_ID);
            maxVal = (String) queryObj.getSingleResult();
            if (maxVal == null || maxVal.isEmpty()) {
                maxVal = "0";
            }
        } catch (Exception e) {
            logger.error("Error occured in UserManagementDaoImpl::getUserKey" + e);
            return 1;
        }
        return Integer.valueOf(maxVal) + 1;
    }
    @Override
    public String updateDispositonInagentInteraction(String sipId, String disposition) {
        String status = null;
        try {
            Query queryObj = firstEntityManager.createNativeQuery(AgentInteractionQueryConstant.UPDATE_DISP);
            queryObj.setParameter("sipId", sipId);
            queryObj.setParameter("disposition", disposition);
            int executeResult = queryObj.executeUpdate();
            if (executeResult > 0) {
                status = "Updated";
            }
            return status;
        } catch (Exception e) {
            logger.error("Error occurred while updating disposition details: " + e.getMessage(), e);
        }
        return status;
    }

    @Override
    public List<AgentInteractionRequest> getAllAgentInteractionList() {
        List<Object[]> interactionlist = new ArrayList<>();
        List<AgentInteractionRequest> agentInteractionRequestList = new ArrayList<>();
        try {
            Query queryObj = firstEntityManager.createNativeQuery(AgentInteractionQueryConstant.GET_ALL_LIST);
            interactionlist = queryObj.getResultList();
            if (interactionlist != null && !interactionlist.isEmpty()) {
                for (Object[] obj : interactionlist) {
                    AgentInteractionRequest agentInteractionRequest = new AgentInteractionRequest();
                    agentInteractionRequest.setId(String.valueOf(obj[0]));
                    agentInteractionRequest.setAniNumber(String.valueOf(obj[1]));
                    agentInteractionRequest.setAgentId(String.valueOf(obj[2]));
                    agentInteractionRequest.setSipId(String.valueOf(obj[3]));
                    agentInteractionRequest.setDins(String.valueOf(obj[4]));
                    agentInteractionRequest.setDate(Timestamp.valueOf(String.valueOf(obj[5])));
                    agentInteractionRequest.setArrivalTime(Timestamp.valueOf(String.valueOf(obj[6])));
                    agentInteractionRequest.setConnectedTime(Timestamp.valueOf(String.valueOf(obj[7])));
                    agentInteractionRequest.setDisconnectedTime(Timestamp.valueOf(String.valueOf(obj[8])));
                    agentInteractionRequest.setSkillGroup(String.valueOf(obj[9]));
                    agentInteractionRequest.setChannel(String.valueOf(obj[10]));
                    agentInteractionRequest.setCallStatus(String.valueOf(obj[11]));
                    agentInteractionRequest.setDuration((Integer) obj[12]);
                    agentInteractionRequest.setDirection(String.valueOf(obj[13]));
                    agentInteractionRequest.setRecording(String.valueOf(obj[14]));
                    boolean booleanValue = obj[15].toString().equals("1");
                    agentInteractionRequest.setDownload(booleanValue);
                    agentInteractionRequest.setDisposition(String.valueOf(obj[16]));
                    agentInteractionRequestList.add(agentInteractionRequest);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to fetch data from AgentDaoImpl:getAllAgentInteractionList" + e);
            throw new ApplicationException(e.getMessage(), Response.Status.BAD_REQUEST);
        }
        return agentInteractionRequestList;
    }
    public Integer getActivityId() {
        String maxVal;
        try {
            Query queryObj = firstEntityManager.createNativeQuery(AgentInteractionQueryConstant.GET_ACT_ID);
            maxVal = (String) queryObj.getSingleResult();
            if (maxVal == null || maxVal.isEmpty()) {
                maxVal = "0";
            }
        } catch (Exception e) {
            logger.error("Error occured in UserManagementDaoImpl::getUserKey" + e);
            return 1;
        }
        return Integer.valueOf(maxVal) + 1;
    }
    @Override
    public AgentActivityRequest saveAgentActToInteraction(AgentActivityRequest agentActivityRequest) {
        int insertVal;
        try{
            int id = getActivityId();
            Query queryObj = firstEntityManager.createNativeQuery(AgentInteractionQueryConstant.INSERT_AGENT_ACTIVITY);
            queryObj.setParameter("id", id);
            queryObj.setParameter("sipId", agentActivityRequest.getAgentId());
            queryObj.setParameter("agentId", agentActivityRequest.getSipId());
            queryObj.setParameter("dins", agentActivityRequest.getDins());
            queryObj.setParameter("callStatus",agentActivityRequest.getCallStatus());
            queryObj.setParameter("activity", agentActivityRequest.getActivity());
            queryObj.setParameter("direction", agentActivityRequest.getDirection());
            LocalDateTime now = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(now);
            agentActivityRequest.setDate(timestamp);
            queryObj.setParameter("created_date",agentActivityRequest.getDate());
            insertVal = queryObj.executeUpdate();
            if (insertVal > 0) {
                return agentActivityRequest;
            }
        } catch (Exception e) {
            logger.error("Error occured in AgentDaoImpl::saveAgentActToInteraction" + e);
            throw new ApplicationException(e.toString(), Response.Status.INTERNAL_SERVER_ERROR);

        }
        return agentActivityRequest;
    }
}
