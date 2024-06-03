package com.cognicx.AppointmentRemainder.dao;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.cognicx.AppointmentRemainder.Dto.ContactDetDto;
import com.cognicx.AppointmentRemainder.Dto.CustomerDataDto;
import com.cognicx.AppointmentRemainder.Dto.DncContactDto;
import com.cognicx.AppointmentRemainder.Dto.DynamicContactDetDto;
import com.cognicx.AppointmentRemainder.Dto.RetryCountDto;
import com.cognicx.AppointmentRemainder.Dto.RetryDetailsDet;
import com.cognicx.AppointmentRemainder.Dto.SurveyContactDetDto;
import com.cognicx.AppointmentRemainder.Dto.UploadHistoryDto;
import com.cognicx.AppointmentRemainder.Request.CampaignDetRequest;
import com.cognicx.AppointmentRemainder.Request.CampaignStatus;
import com.cognicx.AppointmentRemainder.Request.CampaignWeekDetRequest;
import com.cognicx.AppointmentRemainder.Request.DNCDetRequest;
import com.cognicx.AppointmentRemainder.Request.ReportRequest;
import com.cognicx.AppointmentRemainder.Request.UpdateCallDetRequest;

public interface CampaignDao {

	String createCampaign(CampaignDetRequest campaignDetRequest) throws Exception;

	Map<String, List<CampaignWeekDetRequest>> getCampaignWeekDet();

	List<Object[]> getCampaignDet(String userGroup);

	List<Object[]> getCampaignDet();

	boolean updateCampaignstatusOnComplete(String campaignId);
	boolean updateCampaign(CampaignDetRequest campaignDetRequest) throws Exception;

	boolean startResumeCampaignStatus(String campaginId,String frontStatus) throws Exception;
	boolean stopPauseCampaignStatus(String campaginId,String frontStatus) throws Exception;


	boolean updateCallDetail(UpdateCallDetRequest updateCallDetRequest) throws Exception;

	boolean createContact(ContactDetDto contactDetDto) throws Exception;

	Map<String, List<ContactDetDto>> getContactDet();
	

	boolean validateCampaignName(CampaignDetRequest campaignDetRequest);

	List<Object[]> getSummaryReportDet(ReportRequest reportRequest);
	List<Object[]> getSmsReportDet(ReportRequest reportRequest);

	List<Object[]> getContactDetailReport(ReportRequest reportRequest);

	Map<String, List<Map<Object, Object>>> getCallRetryDetail(List<String> contactIdList);

	Map<String, List<Map<Object, Object>>> getCallRetryDetailAll(List<String> contactIdList);

	List<Object[]> getUploadHistory(ReportRequest reportRequest);

	boolean deleteContactByHistory(UpdateCallDetRequest updateCallDetRequest) throws Exception;

	Integer getTotalContactNo(String HistoryId);

	BigInteger insertUploadHistory(UploadHistoryDto uploadHistoryDto) throws Exception;

	List<CustomerDataDto> getCustomerData();

	List<RetryDetailsDet> getCallRetryDetails(String contact_id);

	RetryCountDto getRetryReport(ReportRequest reportRequest);
	RetryCountDto getRetryReport(ReportRequest reportRequest,String userGroup);
	List<Object[]> getLeadWiseSummary(ReportRequest reportRequest);
	List<Object[]> getLeadWiseSummary(ReportRequest reportRequest,String userGroup);

	List<Object[]> getCallVolumeReport(ReportRequest reportRequest);
	List<Object[]> getCallVolumeReport(ReportRequest reportRequest,String userGroup);
	boolean createDummyContact(ContactDetDto contactDetDto) throws Exception;

	//Added on 05/02/2024	
	boolean getCampaignActiveStatus(CampaignDetRequest campaignActive);
	boolean getCampaignStatus(CampaignStatus campaignStatus);
	String getFrontCampaignStatus(CampaignStatus campaignStatus);
	
	//Added on 14/02/2024

	boolean insertCampaignStatus( CampaignDetRequest campDetRequest)throws Exception;
	boolean updateCampaignStatus(CampaignDetRequest campDetRequest) throws Exception;
	boolean updateCampaignStatusUploadContact(String campaignId) throws Exception;
	public Map<String, Integer> getETC(String campaignId) throws Exception;

	List<Object[]> getCampaignDetForRT(String userGroup) throws Exception;
	List<Object[]> getCampaignDetForRT();
	Integer getCampaignBasedContactCount(String campaignName) throws Exception;
	Integer getCampaginBasedContactStatus(String campaignName,String disposition) throws Exception;

//	boolean updateActiveContDetails(String calluid,String status,String productid,String connectedlinenum,String errorcode) throws Exception;
//
//	boolean insertActiveContDetails(String calluid,String status,String productid,String connectedlinenum) throws Exception;

//	Integer getActiveContDetails(String campaignID) throws Exception;

	Integer getActiveContErrorDetails(String campaignName,String[] errorcodes) throws Exception;
	
	boolean insertSurveyContactDet(Map<String,Object> mapSurveyContact)throws Exception; 
	
	boolean createDnc(DNCDetRequest dNCDetRequest);

	List<Object[]> getdnsDet();

	boolean updateDns(DNCDetRequest dNCDetRequest);

	boolean createContactone(DncContactDto contactDetDto); 
	
	 Integer getCampBasedDNCSize(String campaignId);
	 List<String> getCampaignBasedDNClist(String dncID);
	 
	 Map<String, List<SurveyContactDetDto>> getSurveyContactDet();

	int getCountToCall(String productID);

	boolean updateActiveContDetails(String calluid,String status,String productid,String connectedlinenum,String errorcode,String campaignName) throws Exception;

	boolean insertActiveContDetails(String calluid,String status,String productid,String connectedlinenum,String campaignName) throws Exception;

	Integer getActiveContDetails(String campaignID) throws Exception;
	Integer getCompletedCountDetails(String campaignName,Integer campaigncount) throws Exception;

    boolean DeleteContact(DncContactDto contactDetDto);

	boolean updateDeviceEvent(String state,String extn) throws Exception;
	
	boolean updateAgentLoginDetail(String state,String extn)throws Exception;
	
	boolean updateAgentLogoutDetail(String state,String extn)throws Exception;
	
	String getIdleAgentExtn()throws Exception;
	
	String getExtn() throws Exception;
	String getCampaignCompletedTimeRT(String campaign_id) throws Exception;
	String getDNCIDusingCampaignID(String campaignID);
	
	 Integer getCampRetryCount(String campaignId);
	 String getCampIdbasedonactionId(String actionId);
	 String getLanguageBasedonActionId(String actionId);
	 boolean updateSMSStatus(String actionId);

	boolean updateContactDetail(String campaignId, String contactNo, String actionId, String callStatus);

	int getinProgressCallCount(String campaignId);

	boolean checkContactIsHangUp(String actionId, String phone);

	List<SurveyContactDetDto> getContactDetRetry(String campaignId, String retryCount);

	List<Object[]> getSmswebReportDet(String actionID);

	boolean updateNTCStatus(String campaginId);

	boolean updateNTCStatusbyActionId(String actionid);

	List<Object[]> makeInprogressintoNoAnswer(String campaignId);

	boolean saveRetryDetails(RetryDetailsDet retryDetailsDet, String campaignId,String actionId);

	Timestamp getRTCampaignStartDate(String campaignID);

	boolean updateNTCStatusinRetryDet(String campaign_id);
	
	boolean createDynamicContact(DynamicContactDetDto contactDetDto,Map<String,String> mapDynamicMapFields) throws Exception;
	
	List<Object[]> getContMappingDet(String account) throws Exception;
	List<DynamicContactDetDto> getDynamicContactDet(Map<String,String> mapDynamicFields, String campaign_id) ;
	boolean updateAgentDynamicContact(String actionId, String customer_mobile_number,String agent_userid,String campaignId) ;
	
	//List<DynamicContactDetDto> getAgentBasedContactDetail(Map<String,String> mapDynamicFields, String campaign_id,String agent_userid);
	//Map<String,List<DynamicContactDetDto>> getPreviewAgentBasedContactDetail(Map<String,String> mapDynamicFields, String agent_userid);
	Map<String,List<DynamicContactDetDto>> getcampBasedAssignedContactDetail(Map<String,String> mapDynamicFields, String campaign_id);
	Map<String,List<DynamicContactDetDto>> getPreviewAgentBasedContactDetail(Map<String,String> mapDynamicFields, String agent_userid);
	
	//Map<String, List<DynamicContactDetDto>> getPreviewAgentBasedContactDetail(Map<String,String> mapDynamicFields,String agent_userid);
	//List<DynamicContactDetDto> getPreviewAgentBasedContactDetail(Map<String,String> mapDynamicFields,String agent_userid);
	boolean updateAssignedAgentDynamicContact(String actionId, String agent_userid, String campaign_id,String customer_mobile_number);
	Map<String, List<DynamicContactDetDto>> getSupervisorAgentContactDet(Map<String, String> mapDynamicFields,String supervisor);

	DynamicContactDetDto getCustomerDetail(String customerNumber);
}
