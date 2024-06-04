package com.cognicx.AppointmentRemainder.dao.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.persistence.*;

import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.cognicx.AppointmentRemainder.Dto.CallRetryReport;
import com.cognicx.AppointmentRemainder.Dto.ContactDetDto;
import com.cognicx.AppointmentRemainder.Dto.DynamicContactDetDto;
import com.cognicx.AppointmentRemainder.Dto.CustomerDataDto;
import com.cognicx.AppointmentRemainder.Dto.DncContactDto;
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
import com.cognicx.AppointmentRemainder.constant.ApplicationConstant;
import com.cognicx.AppointmentRemainder.constant.CampaignQueryConstant;
import com.cognicx.AppointmentRemainder.dao.CampaignDao;
import com.cognicx.AppointmentRemainder.model.UploadHistoryDet;
import com.cognicx.AppointmentRemainder.response.GenericResponse;

@Repository("CampaignDao")
@Transactional
public class CampaignDaoImpl implements CampaignDao {
	private Logger logger = LoggerFactory.getLogger(CampaignDaoImpl.class);

	@Value("${defaultCampaign}")
	private String defaultcampaign;

	@PersistenceContext(unitName = ApplicationConstant.FIRST_PERSISTENCE_UNIT_NAME)
	public EntityManager firstEntityManager;

	@Autowired
	@Qualifier("firstJdbcTemplate")
	JdbcTemplate firstJdbcTemplate;

	// private SessionFactory sessionFactory;

	@Override
	public String createCampaign(CampaignDetRequest campaignDetRequest) throws Exception {
		String campaignId = null;
		boolean isInserted;
		int insertVal;
		try {
			int idValue = getCampaignId();
			logger.info("Id Value for Campaign : " + idValue);
			if (idValue > 9)
				campaignId = "C_" + String.valueOf(idValue);
			else
				campaignId = "C_0" + String.valueOf(idValue);

			logger.info("Campaign Id created : " + campaignId);
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.INSERT_CAMPAIGN_DET);
			queryObj.setParameter("campaignId", campaignId);
			queryObj.setParameter("name", campaignDetRequest.getCampaignName());
			queryObj.setParameter("desc", campaignDetRequest.getCampaignName());
			if ("true".equalsIgnoreCase(campaignDetRequest.getCampaignActive()))
				queryObj.setParameter("status", 1);
			else
				queryObj.setParameter("status", 0);
			queryObj.setParameter("maxAdvTime", campaignDetRequest.getMaxAdvNotice());
			queryObj.setParameter("retryDelay", campaignDetRequest.getRetryDelay());
			queryObj.setParameter("retryCount", campaignDetRequest.getRetryCount());
			queryObj.setParameter("concurrentCall", campaignDetRequest.getConcurrentCall());
			queryObj.setParameter("startDate", campaignDetRequest.getStartDate());
			queryObj.setParameter("startTime", campaignDetRequest.getStartTime());
			queryObj.setParameter("endDate", campaignDetRequest.getEndDate());
			queryObj.setParameter("endTime", campaignDetRequest.getEndTime());
			queryObj.setParameter("ftpLocation", campaignDetRequest.getFtpLocation());
			queryObj.setParameter("dncId", campaignDetRequest.getDncId());
			queryObj.setParameter("DailingMode", campaignDetRequest.getDailingMode());
			queryObj.setParameter("Queue", campaignDetRequest.getQueue());
			queryObj.setParameter("dispositionID", campaignDetRequest.getDispositionID());
			queryObj.setParameter("groupname", campaignDetRequest.getUserGroup());

			queryObj.setParameter("Dailingoption", campaignDetRequest.getDailingoption());

			queryObj.setParameter("previewOption", campaignDetRequest.getPreviewOption());

			if (!"".equalsIgnoreCase(campaignDetRequest.getFtpUsername())
					&& !"".equalsIgnoreCase(campaignDetRequest.getFtpPassword()))
				queryObj.setParameter("ftpCredentials",
						campaignDetRequest.getFtpUsername() + ";" + campaignDetRequest.getFtpPassword());
			else
				queryObj.setParameter("ftpCredentials", null);
			queryObj.setParameter("fileName", campaignDetRequest.getFileName());
			// queryObj.setParameter("callBefore", campaignDetRequest.getCallBefore());
			insertVal = queryObj.executeUpdate();
			if (insertVal > 0) {
				campaignDetRequest.setCampaignId(campaignId);
				insertCampaignStatus(campaignDetRequest);
				isInserted = createCampaignWeek(campaignDetRequest);
				if (isInserted)
					return campaignId;
			}
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl::createCampaign" + str.toString());
			return null;
		}
		return null;
	}

	private Integer getCampaignId() {
		Integer maxVal;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CAMPAIGN_ID);
			maxVal = (Integer) queryObj.getSingleResult();
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getCampaignId" + e);
			return 1;
		}
		return maxVal + 1;
	}

	private boolean createCampaignWeek(CampaignDetRequest campaignDetRequest) throws Exception {
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.INSERT_CAMPAIGN_WEEK_DET);
			for (CampaignWeekDetRequest campaignWeekDetRequest : campaignDetRequest.getWeekDaysTime()) {
				queryObj.setParameter("campaignId", campaignDetRequest.getCampaignId());
				queryObj.setParameter("day", campaignWeekDetRequest.getDay());
				if ("true".equalsIgnoreCase(campaignWeekDetRequest.getActive()))
					queryObj.setParameter("status", 1);
				else
					queryObj.setParameter("status", 0);
				queryObj.setParameter("startTime", campaignWeekDetRequest.getStartTime());
				queryObj.setParameter("endTime", campaignWeekDetRequest.getEndTime());
				queryObj.executeUpdate();
			}
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::createCampaignWeek" + e);
			throw e;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getCampaignDet(String userGroup) {
		List<Object[]> resultList = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CAMPAIGN_DET_BY_USERGROUP);
			queryObj.setParameter("groupName", userGroup);
			resultList = queryObj.getResultList();
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getCampaignDet" + e);
			return resultList;
		}
		return resultList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getCampaignDet() {
		List<Object[]> resultList = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CAMPAIGN_DET);
			// queryObj.setParameter("groupName", userGroup);
			resultList = queryObj.getResultList();
			if (resultList != null && !resultList.isEmpty()) {
				// for (Object[] row : resultList) {
				// CampaignDetRequest value = (CampaignDetRequest)row;
				// logger.info("Campaign Details : "+value.toString());
				//// for (Object column : row) {
				//// logger.info("result list ::" + column.toString() + "\t");
				//// }
				//// logger.info("");
				// }
			} else {
				logger.info("No campaign details found.");
			}
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getCampaignDet" + e);
			return resultList;
		}
		return resultList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getCampaignDetForRT() {
		List<Object[]> resultList = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CAMPAIGN_DET_RT);
			resultList = queryObj.getResultList();
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getCampaignDet" + e);
			return resultList;
		}
		return resultList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, List<CampaignWeekDetRequest>> getCampaignWeekDet() {
		List<Object[]> resultList;
		Map<String, List<CampaignWeekDetRequest>> campaignWeekDet = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CAMPAIGN_WEEK_DET);
			resultList = queryObj.getResultList();
			if (resultList != null && !resultList.isEmpty()) {
				String preVal = "";
				campaignWeekDet = new LinkedHashMap<>();
				for (Object[] obj : resultList) {
					if (!preVal.equalsIgnoreCase(String.valueOf(obj[1]))) {
						preVal = String.valueOf(obj[1]);
						campaignWeekDet.put(preVal, new ArrayList<CampaignWeekDetRequest>());
					}
					CampaignWeekDetRequest campaignWeekDetRequest = new CampaignWeekDetRequest();
					campaignWeekDetRequest.setCampaignId(preVal);
					campaignWeekDetRequest.setCampaignWeekId(String.valueOf(obj[0]));
					campaignWeekDetRequest.setDay(String.valueOf(obj[2]));
					campaignWeekDetRequest.setActive(String.valueOf(obj[3]));
					campaignWeekDetRequest.setStartTime(String.valueOf(obj[4]));
					campaignWeekDetRequest.setEndTime(String.valueOf(obj[5]));
					campaignWeekDet.get(preVal).add(campaignWeekDetRequest);
				}
			}
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getCampaignWeekDet" + e);
			return campaignWeekDet;
		}
		return campaignWeekDet;
	}

	@Override
	public boolean updateCampaign(CampaignDetRequest campaignDetRequest) throws Exception {
		boolean isupdated;
		int insertVal;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.UPDATE_CAMPAIGN_DET);
			queryObj.setParameter("name", campaignDetRequest.getCampaignName());
			if ("true".equalsIgnoreCase(campaignDetRequest.getCampaignActive()))
				queryObj.setParameter("status", 1);
			else
				queryObj.setParameter("status", 0);
			queryObj.setParameter("maxAdvTime", campaignDetRequest.getMaxAdvNotice());
			queryObj.setParameter("retryDelay", campaignDetRequest.getRetryDelay());
			queryObj.setParameter("retryCount", campaignDetRequest.getRetryCount());
			queryObj.setParameter("concurrentCall", campaignDetRequest.getConcurrentCall());
			queryObj.setParameter("startDate", campaignDetRequest.getStartDate());
			queryObj.setParameter("startTime", campaignDetRequest.getStartTime());
			queryObj.setParameter("endDate", campaignDetRequest.getEndDate());
			queryObj.setParameter("endTime", campaignDetRequest.getEndTime());
			queryObj.setParameter("ftpLocation", campaignDetRequest.getFtpLocation());
			queryObj.setParameter("dncId", campaignDetRequest.getDncId());
			queryObj.setParameter("DailingMode", campaignDetRequest.getDailingMode());
			queryObj.setParameter("Queue", campaignDetRequest.getQueue());
			queryObj.setParameter("dispositionID", campaignDetRequest.getDispositionID());
			queryObj.setParameter("groupname", campaignDetRequest.getUserGroup());
			queryObj.setParameter("Dailingoption", campaignDetRequest.getDailingoption());
			queryObj.setParameter("previewOption", campaignDetRequest.getPreviewOption());


			if (!"".equalsIgnoreCase(campaignDetRequest.getFtpUsername())
					&& !"".equalsIgnoreCase(campaignDetRequest.getFtpPassword()))
				queryObj.setParameter("ftpCredentials",
						campaignDetRequest.getFtpUsername() + ";" + campaignDetRequest.getFtpPassword());
			else
				queryObj.setParameter("ftpCredentials", null);
			// queryObj.setParameter("callBefore", campaignDetRequest.getCallBefore());
			queryObj.setParameter("fileName", campaignDetRequest.getFileName());
			queryObj.setParameter("campaignId", campaignDetRequest.getCampaignId());
			insertVal = queryObj.executeUpdate();
			if (insertVal > 0) {
				updateCampaignStatus(campaignDetRequest);
				isupdated = updateCampaignWeek(campaignDetRequest);
				if (isupdated)
					return true;
			}
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::updateCampaign" + e);
			return false;
		}
		return false;
	}

	private boolean updateCampaignWeek(CampaignDetRequest campaignDetRequest) throws Exception {
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.UPDATE_CAMPAIGN_WEEK_DET);
			for (CampaignWeekDetRequest campaignWeekDetRequest : campaignDetRequest.getWeekDaysTime()) {
				queryObj.setParameter("day", campaignWeekDetRequest.getDay());
				if ("true".equalsIgnoreCase(campaignWeekDetRequest.getActive()))
					queryObj.setParameter("status", 1);
				else
					queryObj.setParameter("status", 0);
				queryObj.setParameter("startTime", campaignWeekDetRequest.getStartTime());
				queryObj.setParameter("endTime", campaignWeekDetRequest.getEndTime());
				queryObj.setParameter("campaignWeekId", campaignWeekDetRequest.getCampaignWeekId());
				queryObj.executeUpdate();
			}
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::updateCampaignWeek" + e);
			throw e;
		}
		return true;
	}

	// @Override
	// public boolean updateCallDetail(UpdateCallDetRequest updateCallDetRequest)
	// throws Exception {
	// int insertVal, retryCount = 0;
	// try {
	// updateCallDetRequest.setRetryCount(getCallRetryCount(updateCallDetRequest.getContactId()));
	// if (!"ANSWERED".equalsIgnoreCase(updateCallDetRequest.getCallStatus())) {
	// retryCount = updateCallDetRequest.getRetryCount() + 1;
	// } else if ("ANSWERED".equalsIgnoreCase(updateCallDetRequest.getCallStatus()))
	// {
	// if (updateCallDetRequest.getCallerResponse() == null
	// || updateCallDetRequest.getCallerResponse().isEmpty()) {
	// updateCallDetRequest.setCallerResponse("0");
	// }
	// retryCount = updateCallDetRequest.getRetryCount();
	// }
	// Query queryObj =
	// firstEntityManager.createNativeQuery(CampaignQueryConstant.UPADTE_SURVEY_CALL_DET);
	// queryObj.setParameter("callerResponse",
	// updateCallDetRequest.getCallerResponse());
	// queryObj.setParameter("callStatus", updateCallDetRequest.getCallStatus());
	// queryObj.setParameter("callDuration",
	// updateCallDetRequest.getCallDuration());
	// queryObj.setParameter("retryCount", retryCount);
	// queryObj.setParameter("contactId", updateCallDetRequest.getContactId());
	// insertVal = queryObj.executeUpdate();
	// queryObj =
	// firstEntityManager.createNativeQuery(CampaignQueryConstant.INSERT_CALL_RETRY_DET);
	// queryObj.setParameter("contactId", updateCallDetRequest.getContactId());
	// queryObj.setParameter("callStatus", updateCallDetRequest.getCallStatus());
	// queryObj.setParameter("retryCount", retryCount);
	// queryObj.executeUpdate();
	// if (insertVal > 0) {
	// return true;
	// }
	// } catch (Exception e) {
	// logger.error("Error occured in CampaignDaoImpl::updateCallDetail" + e);
	// return false;
	// }
	// return false;
	// }

	@Override
	public synchronized boolean updateCallDetail(UpdateCallDetRequest updateCallDetRequest) throws Exception {
		int insertVal, retryCount = 0;
		String callStatus = "";
		try {
			logger.info("Disposition :: " + updateCallDetRequest.getDisposition());
			logger.info("Dial Status :: " + updateCallDetRequest.getDialstatus());
			updateCallDetRequest.setRetryCount(getCallRetryCount(updateCallDetRequest.getActionid()));
			if (!"ANSWERED".equalsIgnoreCase(updateCallDetRequest.getDisposition())) {
				retryCount = updateCallDetRequest.getRetryCount() + 1;
			} else if ("ANSWERED".equalsIgnoreCase(updateCallDetRequest.getDisposition())) {
				if (updateCallDetRequest.getCallerResponse() == null
						|| updateCallDetRequest.getCallerResponse().isEmpty()) {
					updateCallDetRequest.setCallerResponse("0");
				}
				retryCount = updateCallDetRequest.getRetryCount();
			}

			// if (updateCallDetRequest.getDisposition().equalsIgnoreCase("ANSWERED")) {
			// callStatus = "ANSWERED";
			// } else if (updateCallDetRequest.getDisposition().equalsIgnoreCase("BUSY")) {
			// callStatus = "BUSY";
			// } else if (updateCallDetRequest.getDisposition().equalsIgnoreCase("BUSY") &&
			// updateCallDetRequest.getDialstatus().equalsIgnoreCase("BUSY") &&
			// updateCallDetRequest.getHangupcode().equals("17")) {
			// callStatus = "BUSY";
			// } else if (updateCallDetRequest.getDisposition().equalsIgnoreCase("NO
			// ANSWER") && updateCallDetRequest.getDialstatus().equalsIgnoreCase("BUSY") &&
			// updateCallDetRequest.getHangupcode().equals("17")) {
			// callStatus = "BUSY";
			// } else if (updateCallDetRequest.getDisposition().equalsIgnoreCase("NO
			// ANSWER") && updateCallDetRequest.getHangupcode().equals("16") &&
			// updateCallDetRequest.getDialstatus().equalsIgnoreCase("NOANSWER")) {
			// callStatus = "NO ANSWER";
			// } else if (updateCallDetRequest.getDisposition().equalsIgnoreCase("FAILED")
			// && updateCallDetRequest.getHangupcode().equals("16") &&
			// updateCallDetRequest.getDialstatus().equalsIgnoreCase("NOANSWER")) {
			// callStatus = "NOT REACHABLE";
			// } else {
			// callStatus = "UNKNOWN";
			// }

			if (updateCallDetRequest.getDisposition().equalsIgnoreCase("ANSWERED")) {
				callStatus = "ANSWERED";
			} else if (updateCallDetRequest.getDisposition().equalsIgnoreCase("BUSY")) {
				callStatus = "BUSY";
			} else if (updateCallDetRequest.getDisposition().equalsIgnoreCase("NO ANSWER")) {
				callStatus = "NO ANSWER";
			} else if (updateCallDetRequest.getDisposition().equalsIgnoreCase("FAILED")) {
				callStatus = "FAILED";
			} else {
				callStatus = "NO ANSWER";
			}

			// Query queryObj =
			// firstEntityManager.createNativeQuery(CampaignQueryConstant.UPADTE_SURVEY_CALL_DET);
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.UPADTE_SURVEY_CALL_DET_CT);
			queryObj.setParameter("callerResponse", updateCallDetRequest.getCallerResponse());
			queryObj.setParameter("callStatus", callStatus);
			queryObj.setParameter("callDuration", updateCallDetRequest.getCallduration());
			queryObj.setParameter("retryCount", retryCount);
			queryObj.setParameter("call_start_time", updateCallDetRequest.getCallStartTime());
			queryObj.setParameter("call_end_time", updateCallDetRequest.getCallEndTime());
			queryObj.setParameter("actionId", updateCallDetRequest.getActionid());
			queryObj.setParameter("survey_rating", updateCallDetRequest.getSurveyrating());
			queryObj.setParameter("SMS_Triggered", "NO");
			insertVal = queryObj.executeUpdate();
			/*
			 * queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.
			 * INSERT_CALL_RETRY_DET); queryObj.setParameter("contactId",
			 * updateCallDetRequest.getActionid()); queryObj.setParameter("callStatus",
			 * updateCallDetRequest.getDisposition()); queryObj.setParameter("retryCount",
			 * retryCount); queryObj.executeUpdate();
			 */
			//
			if (insertVal > 0) {
				List<Object[]> resultList = null;
				// List<UpdateCallDetRequest> callDetRequest = new ArrayList();
				String campaign_id = null;
				String customer_mobile_number = null;
				String contact_id = null;
				try {
					Query queryObjDet = firstEntityManager
							.createNativeQuery(CampaignQueryConstant.GET_CAMPAIGNID_FOR_RETRY_DET);
					queryObjDet.setParameter("actionId", updateCallDetRequest.getActionid());
					resultList = queryObjDet.getResultList();
					if (resultList != null && !resultList.isEmpty()) {
						for (Object[] obj : resultList) {
							campaign_id = String.valueOf(obj[0]);
							customer_mobile_number = String.valueOf(obj[1]);
							contact_id = String.valueOf(obj[2]);
						}
					}
				} catch (Exception e) {
					StringWriter str = new StringWriter();
					e.printStackTrace(new PrintWriter(str));
					logger.error("Error occured in CampaignDaoImpl::updateCallDetail" + str.toString());
				}
				Query queryObjRetry = firstEntityManager.createNativeQuery(CampaignQueryConstant.INSERT_CALL_RETRY_DET);
				queryObjRetry.setParameter("campaign_id", campaign_id);
				queryObjRetry.setParameter("contactId", contact_id);
				queryObjRetry.setParameter("callStatus", callStatus);
				queryObjRetry.setParameter("callDuration", updateCallDetRequest.getCallduration());
				queryObjRetry.setParameter("retryCount", retryCount);
				queryObjRetry.setParameter("contact_number", customer_mobile_number);
				queryObjRetry.executeUpdate();
				if (insertVal > 0) {
					return true;
				}
			}
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl::updateCallDetail" + str.toString());
			return false;
		}
		return false;
	}

	@Override
	public boolean createContact(ContactDetDto contactDetDto) throws Exception {
		boolean isUpdated = false;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.INSERT_CONTACT_DET_CT);
			queryObj.setParameter("campaignId", contactDetDto.getCampaignId());
			queryObj.setParameter("campaignName", contactDetDto.getCampaignName());
			queryObj.setParameter("subskill_set", contactDetDto.getSubskill_set());
			// queryObj.setParameter("customer_mobile_number", number);
			queryObj.setParameter("last_four_digits", contactDetDto.getLastFourDigits());
			queryObj.setParameter("customer_mobile_number", contactDetDto.getCustomerMobileNumber());
			queryObj.setParameter("total_due", contactDetDto.getTotalDue());
			queryObj.setParameter("minimum_payment", contactDetDto.getMinPayment());
			queryObj.setParameter("due_date", contactDetDto.getDueDate());
			queryObj.setParameter("language", "EN");
			// queryObj.setParameter("contact_id", contactDetDto.getContactId());
			// queryObj.setParameter("language", contactDetDto.getLanguage());
			queryObj.setParameter("callStatus", contactDetDto.getCallStatus());
			queryObj.setParameter("historyId", contactDetDto.getHistoryId());
			// queryObj.setParameter("due_date",
			// getCampaignEndDate(contactDetDto.getCampaignName()));
			logger.info(
					"campaign due date in insert contact det : " + getCampaignEndDate(contactDetDto.getCampaignName()));
			String seq = getActionSequence();
			logger.info("Action Sequence Value :" + seq);
			queryObj.setParameter("actionId", seq);

			queryObj.executeUpdate();

			isUpdated = true;

		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl::createContact" + str.toString());
			logger.error("Error occured in CampaignDaoImpl::createContact" + e);
			logger.error("campaignId" + contactDetDto.getCampaignId());
			logger.error("campaignName" + contactDetDto.getCampaignName());
			logger.error("last_four_digits" + contactDetDto.getLastFourDigits());
			logger.error("customer_mobile_number" + contactDetDto.getCustomerMobileNumber());
			logger.error("total_due" + contactDetDto.getTotalDue());
			logger.error("minimum_payment" + contactDetDto.getMinPayment());
			logger.error("due_date" + contactDetDto.getDueDate());
			logger.error("language" + "en-US");
			logger.error("callStatus" + "New");
			logger.error("historyId" + contactDetDto.getHistoryId());
			isUpdated = false;
			throw e;
		}
		return isUpdated;
	}

	// public static final String CHECK_CONTACT_NUMBER = "select count(*) from
	// appointment_remainder.contact_det where customer_mobile_number
	// =:customer_mobile_number and campaign_id =:campaign_id";
	private int checkMobileIsPresentOrNot(String number, String campaignId) {
		int result = 0;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.CHECK_CONTACT_NUMBER);
			queryObj.setParameter("customer_mobile_number", number);
			queryObj.setParameter("campaign_id", campaignId);
			result = (int) queryObj.getSingleResult(); // Count the number of results returned by the query
			logger.info("Result : " + result);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return result;
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

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, List<ContactDetDto>> getContactDet() {
		List<Object[]> resultList;
		Map<String, List<ContactDetDto>> campaignDetMap = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CONTACT_DET);
			resultList = queryObj.getResultList();
			if (resultList != null && !resultList.isEmpty()) {
				String preVal = "";
				campaignDetMap = new LinkedHashMap<>();
				for (Object[] obj : resultList) {
					if (!preVal.equalsIgnoreCase(String.valueOf(obj[0]))) {
						preVal = String.valueOf(obj[0]);
						campaignDetMap.put(preVal, new ArrayList<ContactDetDto>());
					}
					ContactDetDto contactDetDto = new ContactDetDto();
					contactDetDto.setCampaignId(preVal);
					contactDetDto.setCampaignName(String.valueOf(obj[1]));
					/*
					 * contactDetDto.setDoctorName(String.valueOf(obj[2]));
					 * contactDetDto.setPatientName(String.valueOf(obj[3]));
					 * contactDetDto.setContactNo(String.valueOf(obj[4]));
					 * contactDetDto.setAppointmentDate(String.valueOf(obj[5]));
					 */
					contactDetDto.setLastFourDigits(String.valueOf(obj[2]));
					contactDetDto.setCustomerMobileNumber(String.valueOf(obj[3]));
					contactDetDto.setTotalDue(String.valueOf(obj[4]));
					contactDetDto.setMinPayment(String.valueOf(obj[5]));
					contactDetDto.setDueDate(String.valueOf(obj[6]));
					contactDetDto.setLanguage(String.valueOf(obj[7]));
					contactDetDto.setContactId(String.valueOf(obj[8]));
					contactDetDto.setCallRetryCount(String.valueOf(obj[9]));
					contactDetDto.setUpdatedDate(String.valueOf(obj[10]));
					contactDetDto.setCallStatus(String.valueOf(obj[11]));
					campaignDetMap.get(preVal).add(contactDetDto);
				}
			}
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getContactDet" + e);
			return campaignDetMap;
		}
		return campaignDetMap;
	}

	@Override
	public boolean validateCampaignName(CampaignDetRequest campaignDetRequest) {
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.VALIDATE_CAMPAIGN_NAME);
			queryObj.setParameter("name", campaignDetRequest.getCampaignName());
			int result = (int) queryObj.getSingleResult();
			if (result > 0)
				return false;
			else
				return true;
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::validateCampaignName" + e);
			return true;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getSummaryReportDet(ReportRequest reportRequest) {
		List<Object[]> resultList = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_SUMMARY_REPORT_DET);
			queryObj.setParameter("startDate", reportRequest.getStartDate());
			queryObj.setParameter("endDate", reportRequest.getEndDate());
			queryObj.setParameter("name", reportRequest.getCampaignId());
			resultList = queryObj.getResultList();
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getSummaryReportDet" + e);
			return resultList;
		}
		return resultList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getContactDetailReport(ReportRequest reportRequest) {
		List<Object[]> resultList = null;
		StringBuilder query = null;
		try {
			query = new StringBuilder(CampaignQueryConstant.GET_CONTACT_DET_REPORT);
			if (reportRequest.getStartDate() != null && !reportRequest.getStartDate().isEmpty()
					&& reportRequest.getEndDate() != null && !reportRequest.getEndDate().isEmpty()) {
				query.append(" cast(due_date as date) between :startDate and :endDate and ");
			}
			if (reportRequest.getCampaignId() != null && !reportRequest.getCampaignId().isEmpty()) {
				query.append(" campaign_id=:name and ");
			}
			if (reportRequest.getContactNo() != null && !reportRequest.getContactNo().isEmpty()) {
				query.append(" customer_mobile_number=:customer_mobile_number and ");
			}
			// if (reportRequest.getDoctorName() != null &&
			// !reportRequest.getDoctorName().isEmpty()) {
			// query.append(" doctor_name=:doctorName and ");
			// }
			if (reportRequest.getCallerChoice() != null && !reportRequest.getCallerChoice().isEmpty()) {
				query.append(" caller_response=:callerResponse and ");
			}
			query.append(" call_status is not null ");
			Query queryObj = firstEntityManager.createNativeQuery(query.toString());
			if (reportRequest.getStartDate() != null && !reportRequest.getStartDate().isEmpty()
					&& reportRequest.getEndDate() != null && !reportRequest.getEndDate().isEmpty()) {
				queryObj.setParameter("startDate", reportRequest.getStartDate());
				queryObj.setParameter("endDate", reportRequest.getEndDate());
			}
			if (reportRequest.getCampaignId() != null && !reportRequest.getCampaignId().isEmpty()) {
				queryObj.setParameter("name", reportRequest.getCampaignId());
			}
			if (reportRequest.getContactNo() != null && !reportRequest.getContactNo().isEmpty()) {
				queryObj.setParameter("customer_mobile_number", reportRequest.getContactNo());
			}
			/*
			 * if (reportRequest.getDoctorName() != null &&
			 * !reportRequest.getDoctorName().isEmpty()) {
			 * queryObj.setParameter("doctorName", reportRequest.getDoctorName()); }
			 */
			if (reportRequest.getCallerChoice() != null && !reportRequest.getCallerChoice().isEmpty()) {
				queryObj.setParameter("callerResponse", reportRequest.getCallerChoice());
			}

			resultList = queryObj.getResultList();
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getContactDetailReport" + e);
			return resultList;
		}
		return resultList;
	}

	// @SuppressWarnings("unchecked")
	// @Override
	// public Map<String, List<Map<Object, Object>>> getCallRetryDetail(List<String>
	// contactIdList) {
	// List<Object[]> resultList;
	// Map<String, List<Map<Object, Object>>> callRetryDetMap = null;
	// try {
	// Query queryObj =
	// firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CALL_RETRY_DET);
	// queryObj.setParameter("contactIdList", contactIdList);
	// resultList = queryObj.getResultList();
	// if (resultList != null && !resultList.isEmpty()) {
	// String preVal = "";
	// callRetryDetMap = new LinkedHashMap<>();
	// for (Object[] obj : resultList) {
	// if (!preVal.equalsIgnoreCase(String.valueOf(obj[0]))) {
	// preVal = String.valueOf(obj[0]);
	// callRetryDetMap.put(preVal, new ArrayList<Map<Object, Object>>());
	// }
	// Map<Object, Object> retryDetailsMap = new LinkedHashMap<>();
	// retryDetailsMap.put("callStatus", obj[1]);
	// retryDetailsMap.put("date", obj[2]);
	// callRetryDetMap.get(preVal).add(retryDetailsMap);
	// }
	// }
	// } catch (Exception e) {
	// logger.error("Error occured in CampaignDaoImpl::getCallRetryDetail" + e);
	// return callRetryDetMap;
	// }
	// return callRetryDetMap;
	// }

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, List<Map<Object, Object>>> getCallRetryDetail(List<String> contactIdList) {
		logger.info("In getCallRetryDetail");
		Map<String, List<Map<Object, Object>>> callRetryDetMap = new LinkedHashMap<>();

		try {
			int batchSize = 100; // Code change done by Naresh
			List<List<String>> batches = partitionList(contactIdList, batchSize);

			for (List<String> batch : batches) {
				Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CALL_RETRY_DET);
				queryObj.setParameter("contactIdList", batch);
				List<Object[]> resultList = queryObj.getResultList();
				if (resultList != null && !resultList.isEmpty()) {
					String preVal = "";
					for (Object[] obj : resultList) {
						if (!preVal.equalsIgnoreCase(String.valueOf(obj[0]))) {
							preVal = String.valueOf(obj[0]);
							callRetryDetMap.put(preVal, new ArrayList<>());
						}
						Map<Object, Object> retryDetailsMap = new LinkedHashMap<>();
						retryDetailsMap.put("callStatus", obj[1]);
						retryDetailsMap.put("date", obj[2]);
						callRetryDetMap.get(preVal).add(retryDetailsMap);
					}
				}
			}
			logger.info("Successfully completed the getCallRetryDetail process.");
		} catch (Exception e) {
			logger.error("Error occurred in CampaignDaoImpl::getCallRetryDetail", e);
			return callRetryDetMap;
		}
		return callRetryDetMap;
	}

	private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
		List<List<T>> batches = new ArrayList<>();
		for (int i = 0; i < list.size(); i += batchSize) {
			int end = Math.min(i + batchSize, list.size());
			batches.add(new ArrayList<>(list.subList(i, end)));
		}
		return batches;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, List<Map<Object, Object>>> getCallRetryDetailAll(List<String> contactIdList) {
		logger.info("In getCallRetryDetail");
		Map<String, List<Map<Object, Object>>> callRetryDetMap = new LinkedHashMap<>();

		try {
			int batchSize = 100; // Code change done by Naresh
			List<List<String>> batches = partitionList(contactIdList, batchSize);

			for (List<String> batch : batches) {
				Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CALL_RETRY_DET);
				queryObj.setParameter("contactIdList", batch);
				List<Object[]> resultList = queryObj.getResultList();
				if (resultList != null && !resultList.isEmpty()) {
					String preVal = "";
					for (Object[] obj : resultList) {
						if (!preVal.equalsIgnoreCase(String.valueOf(obj[0]))) {
							preVal = String.valueOf(obj[0]);
							callRetryDetMap.put(preVal, new ArrayList<>());
						}
						Map<Object, Object> retryDetailsMap = new LinkedHashMap<>();
						retryDetailsMap.put("callStatus", obj[1]);
						retryDetailsMap.put("date", obj[2]);
						callRetryDetMap.get(preVal).add(retryDetailsMap);
					}
				}
			}
			logger.info("Successfully completed the getCallRetryDetail process.");
		} catch (Exception e) {
			logger.error("Error occurred in CampaignDaoImpl::getCallRetryDetail", e);
			return callRetryDetMap;
		}
		return callRetryDetMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getUploadHistory(ReportRequest reportRequest) {
		List<Object[]> resultList = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_UPLOAD_HISTORY_DETIALS);
			queryObj.setParameter("startDate", reportRequest.getStartDate());
			queryObj.setParameter("endDate", reportRequest.getEndDate());
			resultList = queryObj.getResultList();
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getCallRetryDetail" + e);
			return resultList;
		}
		return resultList;
	}

	@Override
	public boolean deleteContactByHistory(UpdateCallDetRequest updateCallDetRequest) throws Exception {
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.DELETE_CONTACT_BY_HISTORY);
			queryObj.setParameter("historyId", updateCallDetRequest.getHistoryId());
			queryObj.executeUpdate();
			queryObj = firstEntityManager.createNativeQuery(
					"Update appointment_remainder.upload_history_det set status=0 where upload_history_id=:historyId");
			queryObj.setParameter("historyId", updateCallDetRequest.getHistoryId());
			queryObj.executeUpdate();
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::deleteContactByHistory" + e);
			return false;
		}
		return true;
	}

	@Override
	public Integer getTotalContactNo(String HistoryId) {
		int count;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(
					"select count(*) from appointment_remainder.contact_det where upload_history_id=:historyId");
			queryObj.setParameter("historyId", HistoryId);
			count = (int) queryObj.getSingleResult();
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getTotalContactNo" + e);
			return 0;
		}
		return count;
	}

	@Override
	public BigInteger insertUploadHistory(UploadHistoryDto uploadHistoryDto) throws Exception {
		UploadHistoryDet uploadHistoryDet = null;
		try {
			uploadHistoryDet = new UploadHistoryDet();
			uploadHistoryDet.setCampaignId(uploadHistoryDto.getCampaignId());
			uploadHistoryDet.setCampaignName(uploadHistoryDto.getCampaignName());
			// uploadHistoryDet.setUploadedOn(new Date());
			uploadHistoryDet.setFilename(uploadHistoryDto.getFilename());
			firstEntityManager.persist(uploadHistoryDet);
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::insertUploadHistory" + e);
			throw e;
		}
		return uploadHistoryDet.getUploadHistoryId();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerDataDto> getCustomerData() {
		List<Object[]> resultList;
		List<CustomerDataDto> customerList = new ArrayList();
		;
		Map<String, List<DynamicContactDetDto>> campaignDetMap = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CUSTOMER_DATA);
			resultList = queryObj.getResultList();
			if (resultList != null && !resultList.isEmpty()) {

				for (Object[] obj : resultList) {

					CustomerDataDto customerDataDto = new CustomerDataDto();
					customerDataDto.setCutomerDataId(String.valueOf(obj[0]));
					customerDataDto.setLastFourDigits(String.valueOf(obj[0]));
					customerDataDto.setMobileNumber(String.valueOf(obj[0]));
					customerDataDto.setTotalDue(String.valueOf(obj[0]));
					customerDataDto.setMinimumPayment(String.valueOf(obj[0]));
					customerDataDto.setDueDate(String.valueOf(obj[0]));
					customerDataDto.setStatus(String.valueOf(obj[0]));
					customerList.add(customerDataDto);
				}
			}
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getCustomerData" + e);
			return customerList;
		}
		return customerList;
	}

	// private Integer getCallRetryCount(String contactId) {
	// Integer retryCount;
	// try {
	// Query queryObj =
	// firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CALL_RETRY_COUNT);
	// queryObj.setParameter("contact_id", contactId);
	// retryCount = (Integer) queryObj.getSingleResult();
	// } catch (Exception e) {
	// logger.error("Error occured in CampaignDaoImpl::getCallRetryCount" + e);
	// return 1;
	// }
	// return retryCount;
	// }
	private Integer getCallRetryCount(String actionId) {
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_SURVEY_CALL_RETRY_COUNT_CT);
			queryObj.setParameter("actionId", actionId);
			Integer retryCount = (Integer) queryObj.getSingleResult();
			return retryCount != null ? retryCount : 0;
		} catch (Exception e) {
			logger.error("Error occurred in CampaignDaoImpl::getCallRetryCount", e);
			return 0; // Or consider rethrowing a custom exception
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public RetryCountDto getRetryReport(ReportRequest reportRequest) {
		List<Object[]> resultList;
		List<CallRetryReport> retryReportList = new ArrayList();
		RetryCountDto retryCountDto = new RetryCountDto();
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CONTACT_DET_BY_DATE_RANGE);
			queryObj.setParameter("startDate", reportRequest.getStartDate());
			queryObj.setParameter("endDate", reportRequest.getEndDate());
			resultList = queryObj.getResultList();
			if (resultList != null && !resultList.isEmpty()) {
				for (Object[] obj : resultList) {
					CallRetryReport callRetryReport = new CallRetryReport();
					callRetryReport.setCampaignId(String.valueOf(obj[0]));
					callRetryReport.setCampaignName(String.valueOf(obj[1]));
					callRetryReport.setLastFourDigits(String.valueOf(obj[2]));
					callRetryReport.setCustomerMobileNumber(String.valueOf(obj[3]));
					callRetryReport.setTotalDue(String.valueOf(obj[4]));
					callRetryReport.setMinPayment(String.valueOf(obj[5]));
					callRetryReport.setDueDate(String.valueOf(obj[6]));
					callRetryReport.setLanguage(String.valueOf(obj[7]));
					callRetryReport.setContactId(String.valueOf(obj[8]));
					callRetryReport.setCallRetryCount(String.valueOf(obj[9]));
					callRetryReport.setUpdatedDate(String.valueOf(obj[10]));
					callRetryReport.setCallStatus(String.valueOf(obj[11]));
					// callRetryReport.setRetryList(getCallRetryDetails(reportRequest.getContactId()));
					allocateRetryCount(retryCountDto, Integer.parseInt(callRetryReport.getCallRetryCount()));
					retryReportList.add(callRetryReport);
				}
			}
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getRetryReport" + e);
			return retryCountDto;
		}
		return retryCountDto;
	}

	@Override
	public RetryCountDto getRetryReport(ReportRequest reportRequest, String userGroup) {
		List<Object[]> resultList;
		List<CallRetryReport> retryReportList = new ArrayList();
		RetryCountDto retryCountDto = new RetryCountDto();
		try {
			Query queryObj = firstEntityManager
					.createNativeQuery(CampaignQueryConstant.GET_CONTACT_DET_BY_DATE_RANGE_BY_USER_GROUP);
			queryObj.setParameter("startDate", reportRequest.getStartDate());
			queryObj.setParameter("endDate", reportRequest.getEndDate());
			queryObj.setParameter("userGroup", userGroup);
			resultList = queryObj.getResultList();
			if (resultList != null && !resultList.isEmpty()) {
				for (Object[] obj : resultList) {
					CallRetryReport callRetryReport = new CallRetryReport();
					callRetryReport.setCampaignId(String.valueOf(obj[0]));
					callRetryReport.setCampaignName(String.valueOf(obj[1]));
					callRetryReport.setLastFourDigits(String.valueOf(obj[2]));
					callRetryReport.setCustomerMobileNumber(String.valueOf(obj[3]));
					callRetryReport.setTotalDue(String.valueOf(obj[4]));
					callRetryReport.setMinPayment(String.valueOf(obj[5]));
					callRetryReport.setDueDate(String.valueOf(obj[6]));
					callRetryReport.setLanguage(String.valueOf(obj[7]));
					callRetryReport.setContactId(String.valueOf(obj[8]));
					callRetryReport.setCallRetryCount(String.valueOf(obj[9]));
					callRetryReport.setUpdatedDate(String.valueOf(obj[10]));
					callRetryReport.setCallStatus(String.valueOf(obj[11]));
					// callRetryReport.setRetryList(getCallRetryDetails(reportRequest.getContactId()));
					allocateRetryCount(retryCountDto, Integer.parseInt(callRetryReport.getCallRetryCount()));
					retryReportList.add(callRetryReport);
				}
			}
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getRetryReport" + e);
			return retryCountDto;
		}
		return retryCountDto;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RetryDetailsDet> getCallRetryDetails(String contact_id) {
		List<Object[]> resultList;
		List<RetryDetailsDet> retryList = new ArrayList();
		;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CALL_RETRY_DETAILS);
			queryObj.setParameter("contact_id", contact_id);
			resultList = queryObj.getResultList();
			if (resultList != null && !resultList.isEmpty()) {

				for (Object[] obj : resultList) {

					RetryDetailsDet retryDetailsDet = new RetryDetailsDet();
					retryDetailsDet.setCallRetryId(String.valueOf(obj[0]));
					retryDetailsDet.setContactId(String.valueOf(obj[1]));
					retryDetailsDet.setCallStatus(String.valueOf(obj[2]));
					retryDetailsDet.setRecAddedDate(String.valueOf(obj[3]));
					retryDetailsDet.setRetryCount(String.valueOf(obj[4]));
					retryDetailsDet.setCallDuration(String.valueOf(obj[5]));
					retryList.add(retryDetailsDet);
				}
			}
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getCallRetryDetails" + e);
			return retryList;
		}
		return retryList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getLeadWiseSummary(ReportRequest reportRequest) {
		List<Object[]> resultList = null;
		try {
			Query queryObj = firstEntityManager
					.createNativeQuery(CampaignQueryConstant.GET_LEAD_WISE_SUMMARY_REPORT_DET);
			queryObj.setParameter("startDate", reportRequest.getStartDate());
			queryObj.setParameter("endDate", reportRequest.getEndDate());
			resultList = queryObj.getResultList();
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getSummaryReportDet" + e);
			return resultList;
		}
		return resultList;
	}

	@Override
	public List<Object[]> getLeadWiseSummary(ReportRequest reportRequest, String userGroup) {
		List<Object[]> resultList = null;
		try {
			Query queryObj = firstEntityManager
					.createNativeQuery(CampaignQueryConstant.GET_LEAD_WISE_SUMMARY_REPORT_DET_BY_USER_GROUP);
			queryObj.setParameter("startDate", reportRequest.getStartDate());
			queryObj.setParameter("endDate", reportRequest.getEndDate());
			queryObj.setParameter("userGroup", userGroup);
			resultList = queryObj.getResultList();
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getSummaryReportDet" + e);
			return resultList;
		}
		return resultList;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Object[]> getCallVolumeReport(ReportRequest reportRequest) {
		List<Object[]> resultList = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CALL_VOLUME_REPORT_DET);
			queryObj.setParameter("startDate", reportRequest.getStartDate());
			queryObj.setParameter("endDate", reportRequest.getEndDate());
			resultList = queryObj.getResultList();
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getSummaryReportDet" + e);
			return resultList;
		}
		return resultList;
	}

	@Override
	public List<Object[]> getCallVolumeReport(ReportRequest reportRequest, String userGroup) {
		List<Object[]> resultList = null;
		try {
			Query queryObj = firstEntityManager
					.createNativeQuery(CampaignQueryConstant.GET_CALL_VOLUME_REPORT_DET_BY_USER_GROUP);
			queryObj.setParameter("startDate", reportRequest.getStartDate());
			queryObj.setParameter("endDate", reportRequest.getEndDate());
			queryObj.setParameter("userGroup", userGroup);
			resultList = queryObj.getResultList();
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getSummaryReportDet" + e);
			return resultList;
		}
		return resultList;
	}

	private RetryCountDto allocateRetryCount(RetryCountDto retryCountDto, Integer retryCount) {
		Integer temp = 0;
		switch (retryCount) {

		case 1:
			temp = retryCountDto.getRetryOne();
			retryCountDto.setRetryOne(++temp);
			break;
		case 2:
			temp = retryCountDto.getRetryTwo();
			retryCountDto.setRetryTwo(++temp);
			break;
		case 3:
			temp = retryCountDto.getRetryThree();
			retryCountDto.setRetryThree(++temp);
			break;
		case 4:
			temp = retryCountDto.getRetryFour();
			retryCountDto.setRetryFour(++temp);
			break;
		case 5:
			temp = retryCountDto.getRetryFive();
			retryCountDto.setRetryFive(++temp);
			break;
		case 6:
			temp = retryCountDto.getRetrySix();
			retryCountDto.setRetrySix(++temp);
			break;
		case 7:
			temp = retryCountDto.getRetrySeven();
			retryCountDto.setRetrySeven(++temp);
			break;
		case 8:
			temp = retryCountDto.getRetryEight();
			retryCountDto.setRetryEight(++temp);
			break;
		default:
			temp = retryCountDto.getAbove();
			retryCountDto.setAbove(++temp);
		}

		return retryCountDto;

	}

	@Override
	public boolean createDummyContact(ContactDetDto contactDetDto) throws Exception {

		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.INSERT_DUMMY_CONTACT_DET);
			queryObj.setParameter("campaign_id", contactDetDto.getCampaignId());
			queryObj.setParameter("mobile_number", contactDetDto.getCampaignName());
			queryObj.setParameter("unix_time", contactDetDto.getContactNo());
			queryObj.setParameter("due_date", contactDetDto.getAppointmentDate());
			queryObj.setParameter("contact_id", contactDetDto.getCallStatus());
			queryObj.executeUpdate();

		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::createDummyContact" + e);
			logger.error("campaignId" + contactDetDto.getCampaignId());
			logger.error("mobile_number" + contactDetDto.getCampaignName());
			logger.error("unix_time" + contactDetDto.getContactNo());
			logger.error("due_date" + contactDetDto.getAppointmentDate());
			logger.error("contact_id" + contactDetDto.getCallStatus());
			throw e;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean getCampaignStatus(CampaignStatus campaignStatus) {
		List<Object[]> resultList = null;
		boolean campStatus = false;
		try {

			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CAMPAIGN_STATUS);
			queryObj.setParameter("campaign_id", campaignStatus.getCampaignId());
			resultList = queryObj.getResultList();
			if (resultList != null && !resultList.isEmpty()) {
				for (Object obj : resultList) {
					campStatus = (Boolean) obj;
				}
			}
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl::getCampaignStatus" + str.toString());
		}
		return campStatus;
	}

	@Override
	public String getFrontCampaignStatus(CampaignStatus campaignStatus) {
		List<Object[]> resultList = null;
		String campFrontStatus = null;
		try {

			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CAMPAIGN_FRONT_STATUS);
			queryObj.setParameter("campaign_id", campaignStatus.getCampaignId());
			resultList = queryObj.getResultList();
			if (resultList != null && !resultList.isEmpty()) {
				for (Object obj : resultList) {
					campFrontStatus = (String) obj;
				}
				logger.info("Campaign Front Status :" + campFrontStatus);
			}

		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl::getCampaignStatus" + str.toString());
		}
		return campFrontStatus;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean getCampaignActiveStatus(CampaignDetRequest campaignActive) {
		List<Object[]> resultList = null;
		boolean campStatus = false;
		try {

			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CAMPAIGN_ACTIVE_STATUS);
			queryObj.setParameter("campaign_id", campaignActive.getCampaignId());
			resultList = queryObj.getResultList();
			if (resultList != null && !resultList.isEmpty()) {
				for (Object obj : resultList) {
					campStatus = (Boolean) obj;
				}
			}
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl::getCampaignActiveStatus" + str.toString());
		}
		return campStatus;
	}

	// Added on 14/02/2024
	@Override
	public boolean insertCampaignStatus(CampaignDetRequest campDetRequest) {
		boolean insertionStatus;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.INSERT_CAMPAIGN_STATUS_NEW);
			queryObj.setParameter("campaignId", campDetRequest.getCampaignId());
			queryObj.setParameter("campaign_status", campDetRequest.isSchedulerEnabled());
			if (campDetRequest.isSchedulerEnabled()) {
				queryObj.setParameter("campaign_frontendstatus", "notready");
			} else {
				queryObj.setParameter("campaign_frontendstatus", "notready");
			}
			queryObj.executeUpdate();
			insertionStatus = true;
			logger.error("Insert Campaign Status successfully for the Campaign ID :" + campDetRequest.getCampaignId()
					+ " and It's status :" + campDetRequest.getCampaignActive());
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl:: Insertion Campaign Status" + str.toString());
			logger.error("campaign_id" + campDetRequest.getCampaignId());
			logger.error("campaign_status" + campDetRequest.isSchedulerEnabled());
			throw e;
		}
		return insertionStatus;
	}

	@Override
	public boolean updateCampaignStatus(CampaignDetRequest campDetRequest) {
		boolean updateStatus;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.UPDATE_CAMPAIGN_STATUS);
			queryObj.setParameter("campaign_status", campDetRequest.isSchedulerEnabled());
			queryObj.setParameter("campaignId", campDetRequest.getCampaignId());
			/*
			 * if(campDetRequest.isSchedulerEnabled()) {
			 * queryObj.setParameter("campaign_frontendstatus", "Start"); }else {
			 * queryObj.setParameter("campaign_frontendstatus", "Stop"); }
			 */
			queryObj.executeUpdate();
			updateStatus = true;
			logger.error("Updated Campaign Status successfully for the Campaign ID :" + campDetRequest.getCampaignId()

					+ " and It' Status " + campDetRequest.getCampaignActive());


		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl:: Update Campaign Status" + e);
			logger.error("campaign_id" + campDetRequest.getCampaignId());
			logger.error("campaign_status" + campDetRequest.isSchedulerEnabled());
			throw e;
		}
		return updateStatus;
	}

	@Override
	public boolean updateCampaignStatusUploadContact(String campaignId) {
		boolean updateStatus;
		try {
			Query queryObj = firstEntityManager
					.createNativeQuery(CampaignQueryConstant.UPDATE_CAMPAIGN_STATUS_ON_UPLOADCONTACT);
			queryObj.setParameter("campaignId", campaignId);
			queryObj.setParameter("campaign_frontendstatus", "Start");
			queryObj.executeUpdate();
			updateStatus = true;
			logger.info("Updated Campaign Status successfully for the Campaign ID :" + campaignId
					+ " and It' Status true ");
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl:: Update Campaign Status" + e);
			logger.error("campaign_id" + campaignId);
			logger.error(" error in update campaign_status on upload contact");
			throw e;
		}
		return updateStatus;
	}

	@Override
	public boolean stopPauseCampaignStatus(String campaginId, String frontStatus) throws Exception {
		boolean updateStatus;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.UPDATE_CAMPAIGN_FRONT_STATUS);
			queryObj.setParameter("campaign_status", false);
			queryObj.setParameter("campaignId", campaginId);
			queryObj.setParameter("campaign_frontendstatus", frontStatus);
			queryObj.executeUpdate();
			updateStatus = true;
			logger.error("Updated Campaign Status successfully for the Campaign ID :" + campaginId
					+ " and It' Status is false");
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl:: Update Campaign Status" + e);
			logger.error("campaign_id" + campaginId);
			logger.error("eror in updating campaign_status ");
			throw e;
		}
		return updateStatus;
	}

	@Override
	public boolean startResumeCampaignStatus(String campaginId, String frontStatus) throws Exception {
		boolean updateStatus;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.UPDATE_CAMPAIGN_FRONT_STATUS);
			queryObj.setParameter("campaign_status", true);
			queryObj.setParameter("campaignId", campaginId);
			queryObj.setParameter("campaign_frontendstatus", frontStatus);
			queryObj.executeUpdate();
			updateStatus = true;
			logger.error("Updated Campaign Status successfully for the Campaign ID :" + campaginId
					+ " and It' Status is true");
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl:: Update Campaign Status" + e);
			logger.error("campaign_id" + campaginId);
			logger.error("eror in updating campaign_status ");
			throw e;
		}
		return updateStatus;
	}

	@Override
	public boolean updateCampaignstatusOnComplete(String campaignId) {
		boolean updateStatus;
		try {
			Query queryObj = firstEntityManager
					.createNativeQuery(CampaignQueryConstant.UPDATE_CAMPAIGN_STATUS_ON_COMPLETE);
			queryObj.setParameter("campaignId", campaignId);
			queryObj.setParameter("campaign_frontendstatus", "Completed");
			queryObj.executeUpdate();
			updateStatus = true;
			logger.error("Updated Campaign Status successfully for the Campaign ID :" + campaignId
					+ " and It' Status true ");
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl:: Update Campaign Status" + e);
			logger.error("campaign_id" + campaignId);
			logger.error(" error in update campaign_status on upload contact");
			throw e;
		}
		return updateStatus;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getCampaignDetForRT(String userGroup) {
		List<Object[]> resultList = null;
		try {
			Query queryObj = firstEntityManager
					.createNativeQuery(CampaignQueryConstant.GET_CAMPAIGN_DET_RT_BY_USERGROUP);
			queryObj.setParameter("userGroup", userGroup);
			resultList = queryObj.getResultList();
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getCampaignDet" + e);
			return resultList;
		}
		return resultList;
	}

	@Override
	public Integer getCampaignBasedContactCount(String campaignName) {
		Integer maxVal;
		try {
			// Query queryObj =
			// firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CAMPAIGN_BASED_COUNT);
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CAMPAIGN_BASED_COUNT_CT);
			queryObj.setParameter("campaignname", campaignName);
			maxVal = (Integer) queryObj.getSingleResult();
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl::getCampaign based Contact Count" + str.toString());
			return 0;
		}
		return maxVal;
	}

	@Override
	public Integer getCampaginBasedContactStatus(String campaignName, String disposition) {
		Integer maxVal;
		try {
			// Query queryObj =
			// firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CAMPAIGN_BASED_CONT_STATUS);
			Query queryObj = firstEntityManager
					.createNativeQuery(CampaignQueryConstant.GET_CAMPAIGN_BASED_CONT_STATUS_CT);
			queryObj.setParameter("campaignname", campaignName);
			queryObj.setParameter("disposition", disposition);
			maxVal = (Integer) queryObj.getSingleResult();
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl::getCampaign based Contact Status" + str.toString());
			return 0;
		}
		return maxVal;
	}

	// @Override
	// public boolean updateActiveContDetails(String calluid, String status, String
	// productid, String connectedlinenum, String errorcode) {
	// boolean updateStatus;
	// try {
	// Query queryObj =
	// firstEntityManager.createNativeQuery(CampaignQueryConstant.UPDATE_ACTIVE_CONTACT_DET);
	// queryObj.setParameter("calluid", calluid);
	// queryObj.setParameter("status", status);
	// queryObj.setParameter("productid", productid);
	// queryObj.setParameter("connectedlinenum", connectedlinenum);
	// queryObj.setParameter("errorcode", errorcode);
	// queryObj.executeUpdate();
	// updateStatus = true;
	// logger.error("Updated Active Contact Details for the Product ID :" +
	// productid);
	// } catch (Exception e) {
	// StringWriter str = new StringWriter();
	// e.printStackTrace(new PrintWriter(str));
	// logger.error("Error occured in CampaignDaoImpl:: Update Active Contact
	// Status" + str.toString());
	// throw e;
	// }
	// return updateStatus;
	// }
	//

	public boolean updateActiveContDetails(String calluid, String status, String productid, String connectedlinenum,
			String errorcode, String campaignName) {
		boolean updateStatus;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.UPDATE_ACTIVE_CONTACT_DET);
			queryObj.setParameter("calluid", calluid);
			queryObj.setParameter("status", status);
			queryObj.setParameter("connectedlinenum", connectedlinenum);
			queryObj.setParameter("errorcode", errorcode);
			queryObj.setParameter("campaignname", campaignName);
			queryObj.executeUpdate();
			updateStatus = true;

			logger.info("Updated Active Contact Details for the Product ID :" + productid);

		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl:: Update Active Contact Status" + str.toString());
			throw e;
		}
		return updateStatus;
	}
	// @Override
	// public boolean insertActiveContDetails(String calluid, String status, String
	// productid, String connectedlinenum) {
	// boolean updateStatus;
	// try {
	// Query queryObj =
	// firstEntityManager.createNativeQuery(CampaignQueryConstant.INSERT_ACTIVE_CONTACT_DET);
	// queryObj.setParameter("calluid", calluid);
	// queryObj.setParameter("status", status);
	// queryObj.setParameter("productid", productid);
	// queryObj.setParameter("connectedlinenum", connectedlinenum);
	// queryObj.executeUpdate();
	// updateStatus = true;
	// logger.error("Updated Active Contact Details for the Product ID :" +
	// productid);
	// } catch (Exception e) {
	// StringWriter str = new StringWriter();
	// e.printStackTrace(new PrintWriter(str));
	// logger.error("Error occured in CampaignDaoImpl:: Insert Active Contact
	// Status" + str.toString());
	// throw e;
	// }
	// return updateStatus;
	// }

	public boolean insertActiveContDetails(String calluid, String status, String productid, String connectedlinenum,
			String campaignName) {
		boolean updateStatus;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.INSERT_ACTIVE_CONTACT_DET);
			queryObj.setParameter("calluid", calluid);
			queryObj.setParameter("status", status);
			queryObj.setParameter("productid", productid);
			queryObj.setParameter("connectedlinenum", connectedlinenum);
			queryObj.setParameter("campaignname", campaignName);
			queryObj.executeUpdate();
			updateStatus = true;
			logger.error("Updated Active Contact Details for the Product ID :" + productid);
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl:: Insert Active Contact Status" + str.toString());
			throw e;
		}
		return updateStatus;
	}

	// @Override
	// public Integer getActiveContDetails(String campaignID) throws Exception {
	// Integer maxVal;
	// try {
	// Query queryObj =
	// firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_ACTIVE_CONTACT_DET);
	// queryObj.setParameter("campaignId", campaignID);
	// queryObj.setParameter("status", "Connected");
	//
	// maxVal = (Integer) queryObj.getSingleResult();
	// } catch (Exception e) {
	// StringWriter str = new StringWriter();
	// e.printStackTrace(new PrintWriter(str));
	// logger.error("Error occured in CampaignDaoImpl::getCampaign based Contact
	// Status" + str.toString());
	// return 0;
	// }
	// return maxVal;
	// }

	@Override
	public Integer getActiveContDetails(String campaignName) throws Exception {
		Integer maxVal;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_ACTIVE_CONTACT_DET);
			queryObj.setParameter("campaignname", campaignName);
			queryObj.setParameter("status", "Connected");

			maxVal = (Integer) queryObj.getSingleResult();
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl::getCampaign based Contact Status" + str.toString());
			return 0;
		}
		return maxVal;
	}

	@Override
	public Integer getActiveContErrorDetails(String campaignName, String[] errorcodes) throws Exception {
		Integer maxVal;
		try {

			StringBuilder sqlQuery = new StringBuilder(CampaignQueryConstant.GET_ACTIVE_CONTACT_ERR_DET);
			for (int i = 0; i < errorcodes.length; i++) {
				if (i > 0) {
					sqlQuery.append(", ");
				}
				sqlQuery.append(":id").append(i);
			}
			sqlQuery.append(")");

			Query queryObj = firstEntityManager.createNativeQuery(sqlQuery.toString());
			queryObj.setParameter("campaignname", campaignName);
			// Setting values for the IN clause
			for (int i = 0; i < errorcodes.length; i++) {
				queryObj.setParameter("id" + i, errorcodes[i]);
			}

			maxVal = (Integer) queryObj.getSingleResult();
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl::getCampaign based Contact Status" + str.toString());
			return 0;
		}
		return maxVal;
	}

	@Override
	public boolean insertSurveyContactDet(Map<String, Object> mapSurveyContact) {
		boolean insertionStatus = false;
		try {

			// Query queryObj =
			// firstEntityManager.createNativeQuery(CampaignQueryConstant.INSERT_SURVEY_CONTACT_DET);
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.INSERT_SURVEY_CONTACT_DET_CT);
			queryObj.setParameter("phone", mapSurveyContact.get("phone"));
			queryObj.setParameter("actionId", mapSurveyContact.get("actionid"));
			queryObj.setParameter("Survey_Lang", mapSurveyContact.get("Survey_Lang"));
			queryObj.setParameter("MainSkillset", mapSurveyContact.get("MainSkillset"));
			queryObj.setParameter("subSkillset", mapSurveyContact.get("SubSkillset"));
			queryObj.setParameter("call_status", "NEW");
			queryObj.setParameter("Agent_ID", mapSurveyContact.get("agentId"));
			String campaignEndDate = null;
			String campaignId = null;

			try {
				List<String> campDet = getCampaignEndDateAndId((String) mapSurveyContact.get("MainSkillset"));
				if (campDet != null && campDet.size() > 1) {
					campaignEndDate = campDet.get(0);
					campaignId = campDet.get(1);
				}
			} catch (Exception e) {

			}

			String campaignName = (String) mapSurveyContact.get("MainSkillset");
			if (campaignName != null && defaultcampaign != null && defaultcampaign.contains(campaignName)) {
				queryObj.setParameter("due_date", getEndDate());
			} else {
				queryObj.setParameter("due_date", campaignEndDate);
			}
			// queryObj.setParameter("due_date",
			// getCampaignEndDate((String)mapSurveyContact.get("MainSkillset"));
			queryObj.setParameter("campaign_id", campaignId);

			queryObj.executeUpdate();
			insertionStatus = true;
			logger.error("Inserted survey contact Status successfully ");
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl:: Insertion contact detail" + e);
		}
		return insertionStatus;
	}

	public String getEndDate() {
		String endDate = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			endDate = sdf.format(new Date());
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl:: Insertion contact detail" + e);
		}
		return endDate;
	}

	public boolean createDnc(DNCDetRequest DNCDetRequest) {
		GenericResponse response = new GenericResponse();
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.INSERT_dNS_DET);
			queryObj.setParameter("DNC_Name", DNCDetRequest.getDncName());
			queryObj.setParameter("description", DNCDetRequest.getDescription());

			int insertVal = queryObj.executeUpdate();
			return insertVal > 0;
		} catch (Exception e) {
			logger.error("Error occurred in CampaignDaoImpl::createDnc", e.getMessage());
			// throw new RuntimeException("Failed to create DNC!", e); // or handle
			// exception appropriately
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getdnsDet() {
		List<Object[]> resultList = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_dns_DET);
			resultList = queryObj.getResultList();
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getdnsDet" + e);
			return resultList;
		}
		return resultList;
	}

	@Override
	public boolean updateDns(DNCDetRequest DNCDetRequest) {
		boolean isupdated;
		int insertVal;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.UPDATE_DNS_DET);
			queryObj.setParameter("DNC_Name", DNCDetRequest.getDncName());
			queryObj.setParameter("description", DNCDetRequest.getDescription());
			queryObj.setParameter("DNCID", DNCDetRequest.getDNCID());
			insertVal = queryObj.executeUpdate();
			if (insertVal > 0) {
				return true;
			}
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::updateCampaign" + e);
			return false;
		}
		return false;
	}

	// @Override
	// public boolean createContactone(DncContactDto contactDetDto) {
	// try {
	// Query queryObj =
	// firstEntityManager.createNativeQuery(CampaignQueryConstant.INSERT_CONTACT_DET1);
	// // queryObj.setParameter("serialnumber", contactDetDto.getSerialnumber());
	// queryObj.setParameter("DNCID", contactDetDto.getDNCID());
	// queryObj.setParameter("contactNumber", contactDetDto.getContactNumber());
	// logger.error("Value : " + contactDetDto.getDNCID() + "" +
	// contactDetDto.getContactNumber());
	//
	// queryObj.executeUpdate();
	// } catch (Exception e) {
	// StringWriter str=new StringWriter();
	// e.printStackTrace(new PrintWriter(str));
	// logger.error("Error occured in CampaignDaoImpl::createContact" +
	// str.toString());
	// logger.error("serialnumber" + contactDetDto.getSerialnumber());
	// logger.error("DNCID", contactDetDto.getDNCID());
	// logger.error("contactNumber" + contactDetDto.getContactNumber());
	// logger.error("setFailureReason" + contactDetDto.getFailureReason());
	//
	// throw e;
	// }
	// return true;
	// }

	@Override
	public boolean createContactone(DncContactDto contactDetDto) {
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.INSERT_CONTACT_DET1);
			// queryObj.setParameter("serialnumber", contactDetDto.getSerialnumber());
			queryObj.setParameter("DNCID", contactDetDto.getDNCID());
			queryObj.setParameter("contactNumber", contactDetDto.getContactNumber());
			logger.info(
					"DNC Value : " + contactDetDto.getDNCID() + "and the Number" + contactDetDto.getContactNumber());
			int insertVal = queryObj.executeUpdate();
			if (insertVal > 0) {
				logger.info("DNC Insertion successful for the Number" + contactDetDto.getContactNumber());
				Query queryUpdatObj = firstEntityManager
						.createNativeQuery(CampaignQueryConstant.UPDATE_CAMPAIGN_STATUS_BASED_ON_DNC);
				queryUpdatObj.setParameter("contactNumber", contactDetDto.getContactNumber());
				queryUpdatObj.executeUpdate();
				logger.info("DNC Update successful for the Number" + contactDetDto.getContactNumber());
			}
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl::createContact" + str.toString());
			logger.error("serialnumber" + contactDetDto.getSerialnumber());
			logger.error("DNCID", contactDetDto.getDNCID());
			logger.error("contactNumber" + contactDetDto.getContactNumber());
			logger.error("setFailureReason" + contactDetDto.getFailureReason());

			// throw e;
		}
		return true;
	}

	@Override
	public boolean DeleteContact(DncContactDto contactDetDto) {

		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.DELETE_CONTACT_DET1);
			// queryObj.setParameter("serialnumber", contactDetDto.getSerialnumber());
			queryObj.setParameter("DNCID", contactDetDto.getDNCID());
			queryObj.setParameter("contactNumber", contactDetDto.getContactNumber());
			logger.info("DNC ID : " + contactDetDto.getDNCID() + "and Removal DNC Number"
					+ contactDetDto.getContactNumber() + " DNC Name :" + contactDetDto.getDncName());
			int delVal = queryObj.executeUpdate();
			if (delVal > 0) {
				logger.info("DNC Delete successful for the Number" + contactDetDto.getContactNumber());
				Query queryUpdatObj = firstEntityManager
						.createNativeQuery(CampaignQueryConstant.DELETE_DNC_ON_CAMPAIGN_DET);
				queryUpdatObj.setParameter("contactNumber", contactDetDto.getContactNumber());
				queryUpdatObj.executeUpdate();
				logger.info("DNC Update successful for the Number" + contactDetDto.getContactNumber());
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::DeleteContact" + e);
			logger.error("serialnumber" + contactDetDto.getSerialnumber());
			logger.error("DNCID", contactDetDto.getDNCID());
			logger.error("contactNumber" + contactDetDto.getContactNumber());
			logger.error("setFailureReason" + contactDetDto.getFailureReason());
			return false;
		}
	}

	@Override
	public Integer getCampBasedDNCSize(String campaignId) {
		Integer maxVal;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_DNC_CONTACT);
			queryObj.setParameter("campaign_id", campaignId);
			maxVal = (Integer) queryObj.getSingleResult();
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl::getCampaign based Contact Status" + str.toString());
			return 0;
		}
		return maxVal;
	}

	@Override
	public List<String> getCampaignBasedDNClist(String dncID) {
		List<String> resultList = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_DNC_CONTACT_DET);
			queryObj.setParameter("DNCID", dncID);
			resultList = queryObj.getResultList();
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getCampaignDet" + e);
			return resultList;
		}
		return resultList;
	}

	// List<Object[]> resultList;
	// Map<String, List<SurveyContactDetDto>> campaignDetMap = null;
	// try {
	// Query queryObj =
	// firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_SURVEY_CONTACT_DET);
	// resultList = queryObj.getResultList();
	// if (resultList != null && !resultList.isEmpty()) {
	// String preVal = "";
	// campaignDetMap = new LinkedHashMap<>();
	// for (Object[] obj : resultList) {
	// if (!preVal.equalsIgnoreCase(String.valueOf(obj[0]))) {
	// preVal = String.valueOf(obj[0]);
	// campaignDetMap.put(preVal, new ArrayList<SurveyContactDetDto>());
	// }
	// SurveyContactDetDto surveyConDto=new SurveyContactDetDto();
	// surveyConDto.setSubSkillset(preVal);
	// surveyConDto.setPhone(String.valueOf(obj[1]));
	// surveyConDto.setActionId(String.valueOf(obj[2]));
	// surveyConDto.setSurvey_Lang(String.valueOf(obj[3]));
	// campaignDetMap.get(preVal).add(surveyConDto);
	// }
	// }
	// } catch (Exception e) {
	// logger.error("Error occured in CampaignDaoImpl::getSurveyContactDet" + e);
	// return campaignDetMap;
	// }
	// return campaignDetMap;

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, List<SurveyContactDetDto>> getSurveyContactDet() {
		List<Object[]> resultList;
		Map<String, List<SurveyContactDetDto>> campaignDetMap = new LinkedHashMap<>();
		try {
			// Query queryObj =
			// firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_SURVEY_CONTACT_DET);
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_SURVEY_CONTACT_DET_CT);
			resultList = queryObj.getResultList();
			if (resultList != null && !resultList.isEmpty()) {
				String preVal = "";
				campaignDetMap = new LinkedHashMap<>();
				for (Object[] obj : resultList) {
					if (!preVal.equalsIgnoreCase(String.valueOf(obj[0]))) {
						preVal = String.valueOf(obj[0]);
						campaignDetMap.put(preVal, new ArrayList<SurveyContactDetDto>());
					}
					SurveyContactDetDto surveyConDto = new SurveyContactDetDto();
					// surveyConDto.setSubSkillset(preVal);
					// surveyConDto.setPhone(String.valueOf(obj[1]));
					// surveyConDto.setActionId(String.valueOf(obj[2]));
					// surveyConDto.setSurvey_Lang(String.valueOf(obj[3]));
					// surveyConDto.setMainSkillset(String.valueOf(obj[4]));
					// surveyConDto.setCall_status(String.valueOf(obj[5]));

					surveyConDto.setMainSkillset(preVal);
					surveyConDto.setPhone(String.valueOf(obj[1]));
					surveyConDto.setActionId(String.valueOf(obj[2]));
					surveyConDto.setSurvey_Lang(String.valueOf(obj[3]));
					surveyConDto.setSubSkillset(String.valueOf(obj[4]));
					surveyConDto.setCall_status(String.valueOf(obj[5]));
					surveyConDto.setRec_update_time(String.valueOf(obj[6]));
					try {
						surveyConDto.setRetryCount(String.valueOf(obj[7]));
					} catch (Exception e) {
						surveyConDto.setRetryCount("0");
					}
					surveyConDto.setLastFourDigits(String.valueOf(obj[8]));
					surveyConDto.setTotalDue(String.valueOf(obj[9]));
					surveyConDto.setMinPayment(String.valueOf(obj[10]));
					surveyConDto.setDueDate(String.valueOf(obj[11]));
					campaignDetMap.get(preVal).add(surveyConDto);
					// logger.info("TO DELETE --- Campaign Survey Contact Map:" + campaignDetMap);
				}
			}
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getSurveyContactDet" + e);
			return campaignDetMap;
		}
		return campaignDetMap;
	}

	@Override
	public List<DynamicContactDetDto> getDynamicContactDet(Map<String, String> mapDynamicFields, String campaign_id) {
		List<Object[]> resultList;
		List<DynamicContactDetDto> campaignDetlist = new ArrayList<>();
		try {
			// Query queryObj =
			// firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_SURVEY_CONTACT_DET);
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_DYANMIC_CONTACT_DET_CT);
			queryObj.setParameter("campaign_id", campaign_id);
			resultList = queryObj.getResultList();
			if (resultList != null && !resultList.isEmpty()) {
				String preVal = "";
				for (Object[] obj : resultList) {

					DynamicContactDetDto surveyConDto = new DynamicContactDetDto();
					surveyConDto.setCampaignId(String.valueOf(obj[0]));
					surveyConDto.setCampaignName(String.valueOf(obj[1]));
					surveyConDto.setCustomerMobileNumber(String.valueOf(obj[2]));
					surveyConDto.setActionId(String.valueOf(obj[3]));
					surveyConDto.setAgent_userid(String.valueOf(obj[4]));
					// surveyConDto.setCampaignId(preVal);
					Map<String, String> dynField = new LinkedHashMap<>();

					for (int i = 1; i <= mapDynamicFields.size(); i++) {
						String key = mapDynamicFields.get("reserve_" + i);
						String value = (String) obj[i + 4];
						dynField.put(key, value);
					}
					// surveyConDto.setMapDynamicFields(dynField);
					surveyConDto.setMapDynamicFields(dynField);
					campaignDetlist.add(surveyConDto);

					logger.info("list :: " + dynField);

					// logger.info("TO DELETE --- Campaign Survey Contact Map:" + campaignDetMap);
				}
			}
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getSurveyContactDet" + e);
			return campaignDetlist;
		}
		return campaignDetlist;
	}

	@Override
	public int getCountToCall(String productID) {
		Integer maxVal;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.COUNT_ACTIVE_CONTACT_DET);
			queryObj.setParameter("productId", productID);
			queryObj.setParameter("status", "HangUp");
			maxVal = (Integer) queryObj.getSingleResult();
		} catch (Exception e) {

			logger.error("Error occured in CampaignDaoImpl::getCampaign based Contact Status" + e);
			return 0;
		}
		return maxVal;
	}

	@Override
	public Integer getCompletedCountDetails(String campaignName, Integer campaigncount) throws Exception {
		Integer maxVal;
		try {
			// Query queryObj =
			// firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_COMPLETED_CALL_COUNT);
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_COMPLETED_CALL_COUNT_CT);
			queryObj.setParameter("campaigncount", campaigncount);
			queryObj.setParameter("campaignName", campaignName);
			maxVal = (Integer) queryObj.getSingleResult();
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error(
					"Error occured in CampaignDaoImpl::getCompletedCountDetails based Contact Status" + str.toString());
			return 0;
		}
		return maxVal;
	}
	// @Override
	// public boolean DeleteContact(DncContactDto contactDetDto) {
	// try {
	// Query queryObj =
	// firstEntityManager.createNativeQuery(CampaignQueryConstant.DELETE_CONTACT_DET1);
	// // queryObj.setParameter("serialnumber", contactDetDto.getSerialnumber());
	// queryObj.setParameter("DNCID", contactDetDto.getDNCID());
	// queryObj.setParameter("contactNumber", contactDetDto.getContactNumber());
	// logger.error("Value : " + contactDetDto.getDNCID() + "" +
	// contactDetDto.getContactNumber());
	//
	// queryObj.executeUpdate();
	// } catch (Exception e) {
	// logger.error("Error occured in CampaignDaoImpl::DeleteContact" + e);
	// logger.error("serialnumber" + contactDetDto.getSerialnumber());
	// logger.error("DNCID", contactDetDto.getDNCID());
	// logger.error("contactNumber" + contactDetDto.getContactNumber());
	// logger.error("setFailureReason" + contactDetDto.getFailureReason());
	//
	// throw e;
	// }
	// return true;
	// }

	@Override
	public boolean updateDeviceEvent(String state, String extn) {
		boolean updateStatus = false;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.UPDATE_AGENT_DEVICE_STATUS);
			queryObj.setParameter("state", state);
			queryObj.setParameter("device", extn);
			queryObj.executeUpdate();
			updateStatus = true;
			logger.error("Updated agent device status");
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl:: Updating agent device status" + e);
			throw e;
		}
		return updateStatus;
	}

	@Override
	public String getIdleAgentExtn() throws Exception {
		List<Object[]> resultList = null;
		String extn = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_AGENT_LOG_IDLETIME);
			resultList = queryObj.getResultList();
			if (resultList != null && !resultList.isEmpty()) {
				for (Object[] obj : resultList) {
					String userID = String.valueOf(obj[0]);
					extn = String.valueOf(obj[1]);
				}
			}

		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getCampaignDet" + e);
			return extn;
		}
		return extn;
	}

	@Override
	public boolean updateAgentLoginDetail(String state, String extn) throws Exception {
		String updateStatus = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.UPDATE_AGENT_LOGIN_STATUS);
			queryObj.setParameter("state", state);
			queryObj.setParameter("device", extn);
			queryObj.executeUpdate();

			// updateStatus=true;
			logger.error("Updated agent device status");
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl:: Updating agent login status" + str.toString());
			throw e;
		}
		return true;
	}

	@Override
	public boolean updateAgentLogoutDetail(String state, String extn) throws Exception {
		String updateStatus = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.UPDATE_AGENT_LOGOUT_STATUS);
			queryObj.setParameter("state", state);
			queryObj.setParameter("device", extn);
			queryObj.executeUpdate();

			// updateStatus=true;
			logger.error("Updated agent device status");
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl:: Updating agent logout status" + str.toString());
			throw e;
		}
		return true;
	}

	@Override
	public String getExtn() throws Exception {
		String phone = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_AVAIL_AGENT_FORCAMPAIGN);
			phone = (String) queryObj.getSingleResult();
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl:: get Extn " + str.toString());
			throw e;
		}
		return phone;
	}

	public List<String> getCampaignEndDateAndId(String name) throws Exception {
		String result = null;
		List<String> listDate = new ArrayList<>();
		List<Object[]> resultList = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String campaignId = null;
		Date date = null;
		Time time = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CAMPAIGN_END_DATE_ID);
			queryObj.setParameter("name", name);
			queryObj.getResultList();
			String formattedTime = null;
			String campaignEndDate = null;
			resultList = queryObj.getResultList();
			if (resultList != null && !resultList.isEmpty()) {
				for (Object[] obj : resultList) {
					date = (Date) obj[0];
					result = sdf.format(date);
					time = (Time) obj[1];
					campaignId = (String) obj[2];
					LocalTime localTime = time.toLocalTime();
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
					formattedTime = localTime.format(formatter);
					campaignEndDate = sdf.format(date);
					listDate.add(campaignEndDate + " " + formattedTime);
					listDate.add(campaignId);
				}
			}
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl:: getCampaignEndName" + e);
		}
		return listDate;
	}

	public String getCampaignEndDate(String name) throws Exception {
		String campaignEndDate = null;
		String formattedTime = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		List<Object[]> resultList = null;
		Date date = null;
		Time time = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CAMPAIGN_END_DATE);
			queryObj.setParameter("name", name);
			resultList = queryObj.getResultList();
			if (resultList != null && !resultList.isEmpty()) {
				for (Object[] obj : resultList) {
					date = (Date) obj[0];
					time = (Time) obj[1];
					LocalTime localTime = time.toLocalTime();
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
					formattedTime = localTime.format(formatter);
					campaignEndDate = sdf.format(date);
					logger.info("campaign end date : " + campaignEndDate);
					logger.info("campaign end time : " + formattedTime);
				}
			}
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl:: getCampaignEndName" + e);
		}
		return campaignEndDate + " " + formattedTime;
	}

	@Override
	public Map<String, Integer> getETC(String campaignId) throws Exception {
		Integer busyCount = 0;
		Integer answerCount = 0;
		Integer noanswerCount = 0;
		Integer answeredDuration = 0;

		Integer failedCount = 0;

		Map<String, Integer> map = new HashMap<String, Integer>();
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_BUSY_COUNT);
			queryObj.setParameter("campaign_id", campaignId);
			busyCount = (Integer) queryObj.getSingleResult();
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl:: getETC" + e);
		}
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_ANSWERED_COUNT);
			queryObj.setParameter("campaign_id", campaignId);
			answerCount = (Integer) queryObj.getSingleResult();
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl:: getETC" + e);
		}

		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_NO_ANSWER_COUNT);
			queryObj.setParameter("campaign_id", campaignId);
			noanswerCount = (Integer) queryObj.getSingleResult();
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl:: getETC" + e);
		}

		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_ANSWER_DURATION);
			queryObj.setParameter("campaign_id", campaignId);
			answeredDuration = (Integer) queryObj.getSingleResult();
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl:: getETC" + e);
		}
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_FAILED_COUNT);
			queryObj.setParameter("campaign_id", campaignId);
			failedCount = (Integer) queryObj.getSingleResult();
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl in failed count:: getETC" + e);
		}

		map.put("busycount", busyCount);
		map.put("answerCount", answerCount);
		map.put("noanswerCount", noanswerCount);
		map.put("answeredDuration", answeredDuration);

		map.put("failedCount",failedCount);


		return map;
	}

	// @Override
	// public String getCampaignCompletedTimeRT(String campaign_id) throws Exception
	// {
	// String campCompletedTime=null;
	// SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	// try {
	// logger.info("getCampaignCompletedTimeRT campaign Id : "+campaign_id);
	// Query queryObj =
	// firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CAMPAIGN_COMPLETED_TIME_RT);
	// queryObj.setParameter("campaign_id", campaign_id);
	// Date dateTime = (Date) queryObj.getSingleResult();
	// logger.info("getCampaignCompletedTimeRT date : "+dateTime);
	// campCompletedTime=sdf.format(dateTime);
	//
	//
	// }catch(Exception e) {
	// StringWriter str=new StringWriter();
	// e.printStackTrace(new PrintWriter(str));
	// logger.error("Error occured in CampaignDaoImpl:: getCampaignCompletedTimeRT "
	// + str.toString());
	// }
	// return campCompletedTime;
	// }

	@Override
	public String getCampaignCompletedTimeRT(String campaign_id) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String campCompletedTime = null;

		try {
			logger.info("getCampaignCompletedTimeRT campaign Id : " + campaign_id);
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CAMPAIGN_COMPLETED_TIME_RT);
			queryObj.setParameter("campaign_id", campaign_id);
			Date dateTime = (Date) queryObj.getSingleResult();
			campCompletedTime = sdf.format(dateTime);
		} catch (NoResultException e) {
			logger.info("No completion time found for campaign Id : " + campaign_id);
		} catch (Exception e) {
			logger.error("Error occurred in CampaignDaoImpl::getCampaignCompletedTimeRT", e);
		}

		return campCompletedTime;
	}

	@Override
	public String getDNCIDusingCampaignID(String campaignID) {
		String dncID = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_DNCID_BY_CAMPAIGN_ID);
			queryObj.setParameter("campaign_id", campaignID);
			dncID = (String) queryObj.getSingleResult();
			logger.error("Retrieved DNC ID successfully for the Campaign ID :" + campaignID);
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl:: getDNCID using Campaign ID" + str.toString());
		}
		return dncID;
	}

	@Override
	public String getCampIdbasedonactionId(String actionId) {
		String campid = null;
		try {
			Query queryObj = firstEntityManager
					.createNativeQuery(CampaignQueryConstant.GET_CAMP_RETRY_DET_BASED_ON_ACTIONID);
			queryObj.setParameter("actionId", actionId);
			campid = (String) queryObj.getSingleResult();
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl::get campaign Id based on action Id : " + str.toString());

		}
		return campid;
	}

	@Override
	public Integer getCampRetryCount(String campaignId) {
		Integer retryCount;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CAMP_RETRY_DET);
			queryObj.setParameter("campaign_id", campaignId);
			retryCount = (Integer) queryObj.getSingleResult();
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl::getCampRetryCount based campaign Id : " + str.toString());
			return 0;
		}
		return retryCount;
	}

	@Override
	public List<Object[]> getSmsReportDet(ReportRequest reportRequest) {
		List<Object[]> resultList = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_SMS_REPORT_DET);
			queryObj.setParameter("campaignId", reportRequest.getCampaignId());
			queryObj.setParameter("startDate", reportRequest.getStartDate());
			queryObj.setParameter("endDate", reportRequest.getEndDate());
			resultList = queryObj.getResultList();
			/*
			 * if (resultList != null && !resultList.isEmpty()) { for (Object[] row :
			 * resultList) { for (Object column : row) { logger.info("result list ::"
			 * +column+ "\t" ); } logger.info(""); } } else {
			 * logger.info("No campaign details found."); }
			 */

		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getSmsReportDet" + e);
			return resultList;
		}
		return resultList;
	}

	public boolean createDynamicContact(DynamicContactDetDto contactDetDto, Map<String, String> mapDynamicMapFields)
			throws Exception {
		boolean isUpdated = false;
		try {
			Map<String, String> mapDynamicFields = contactDetDto.getMapDynamicFields();
			StringBuilder queryBuilder = new StringBuilder(
					"insert into appointment_remainder.contact_det_new_1 (campaign_id,campaign_name,customer_mobile_number,subskill_set,language,call_status,upload_history_id,rec_upt_date,call_retry_count,actionId,campaign_stopstatus,");
			for (int j = 1; j <= mapDynamicMapFields.size(); j++) {
				queryBuilder.append("reserve_" + j);
				if (j < mapDynamicMapFields.size()) {
					queryBuilder.append(",");
				}
			}
			queryBuilder.append(
					") values (:campaignId,:campaignName,:customer_mobile_number,:subskill_set,:language,:callStatus,:historyId,getdate(),'0',:actionId,'NA',");
			for (int j = 1; j <= mapDynamicMapFields.size(); j++) {
				queryBuilder.append(":reserve_" + j);
				if (j < mapDynamicMapFields.size()) {
					queryBuilder.append(",");
				}
			}
			queryBuilder.append(")");

			logger.info("Map Dynamic Map Fields :" + mapDynamicMapFields);
			logger.info("Map Dynamic Fields :" + mapDynamicFields);

			logger.info("Final Query Builder for Dynamic Contact Insertion :" + queryBuilder);

			String queryData = queryBuilder.toString();
			Query queryObj = firstEntityManager.createNativeQuery(queryData);
			queryObj.setParameter("campaignId", contactDetDto.getCampaignId());
			queryObj.setParameter("campaignName", contactDetDto.getCampaignName());
			queryObj.setParameter("customer_mobile_number", contactDetDto.getCustomerMobileNumber());
			queryObj.setParameter("subskill_set", contactDetDto.getSubskill_set());
			queryObj.setParameter("language", "EN");
			queryObj.setParameter("historyId", contactDetDto.getHistoryId());
			queryObj.setParameter("callStatus", contactDetDto.getCallStatus());

			mapDynamicMapFields.forEach((key, value) -> {

				value = value != null ? value.trim() : value;
				String actualvalue = null;
				if (mapDynamicFields.containsKey(value)) {
					actualvalue = mapDynamicFields.get(value);
				} else {
					logger.info("Dynamic Field :" + value + " is not available in the Map" + mapDynamicFields);
				}
				logger.info("DB Key :" + key + "Map Key :" + value + " :: DB Value " + actualvalue);
				queryObj.setParameter(key, actualvalue);
			});

			logger.info(
					"campaign due date in insert contact det : " + getCampaignEndDate(contactDetDto.getCampaignName()));
			String seq = getActionSequence();
			logger.info("Action Sequence Value :" + seq);
			queryObj.setParameter("actionId", seq);
			queryObj.executeUpdate();
			isUpdated = true;
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl::createContact" + str.toString());
			isUpdated = false;
			throw e;
		}
		return isUpdated;
	}

	@Override
	public List<Object[]> getContMappingDet(String account) throws Exception {
		List<Object[]> resultlist = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CONT_MAP_DET);
			queryObj.setParameter("account", account);
			resultlist = queryObj.getResultList();
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl::getMappingDet" + str.toString());
		}
		return resultlist;
	}

	@Override
	public String getLanguageBasedonActionId(String actionId) {
		String responseLang = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_LANGUAGE_BASED_ON_ACTIONID);
			queryObj.setParameter("actionId", actionId);
			responseLang = (String) queryObj.getSingleResult();
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl::getLanguageBasedonActionId based campaign Id : "
					+ str.toString());
		}
		return responseLang;
	}

	@Override
	public boolean updateSMSStatus(String actionId) {
		boolean status = false;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.UPDATE_SMS_TRIGGERED);
			// queryObj.setParameter("SMS_Triggered", "YES");
			queryObj.setParameter("actionId", actionId);
			int insertVal = queryObj.executeUpdate();
			if (insertVal > 0) {
				logger.info("DNC Update successful for the Number" + actionId);
				status = true;
			} else {
				logger.info("No rows were updated for actionId: " + actionId);
			}
		} catch (Exception e) {
			logger.info("Error in Update SMS status" + e);
		}
		return status;
	}

	@Override
	public boolean updateContactDetail(String campaignId, String contactNo, String actionId, String callStatus) {
		boolean isUpdated = false;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(
					"UPDATE appointment_remainder.contact_det set call_status=:callStatus,rec_upt_date=getdate() where actionId =:actionId and customer_mobile_number=:contactNo and campaign_id=:campaignId");
			queryObj.setParameter("callStatus", callStatus);
			queryObj.setParameter("actionId", actionId);
			queryObj.setParameter("contactNo", contactNo);
			queryObj.setParameter("campaignId", campaignId);
			if (queryObj.executeUpdate() > 0) {
				isUpdated = true;
			} else {
				isUpdated = false;
			}
		} catch (Exception e) {
			logger.error("Error on updateContactDetail " + e.getMessage());
		}
		return isUpdated;
	}

	public synchronized boolean updateContactDetailToNOANSWER(String actionId, String retryCount) {
		boolean isUpdated = false;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(
					"UPDATE appointment_remainder.contact_det set call_status=:callStatus,rec_upt_date=getdate(),call_retry_count=:retryCount where actionId =:actionId");
			queryObj.setParameter("callStatus", "NO ANSWER");
			queryObj.setParameter("actionId", actionId);
			queryObj.setParameter("retryCount", retryCount);
			if (queryObj.executeUpdate() > 0) {
				isUpdated = true;
			} else {
				isUpdated = false;
			}
		} catch (Exception e) {
			logger.error("Error on updateContactDetail " + e.getMessage());
		}
		return isUpdated;
	}

	public int getinProgressCallCount(String campaignId) {
		int count = 0;
		try {
			// Note the typo fixes in the query and method name.
			Query queryObj = firstEntityManager.createNativeQuery(
					"SELECT COUNT(*) FROM appointment_remainder.contact_det WHERE call_status = :callStatus AND campaign_id = :campaignId");
			// Fixed parameter name to match the query placeholder.
			queryObj.setParameter("campaignId", campaignId);
			queryObj.setParameter("callStatus", "InProgress");
			Object result = queryObj.getSingleResult();
			count = Integer.parseInt(result.toString());
		} catch (Exception e) {
			logger.error("Error on getInProgressCallCount: " + e.getMessage(), e);
			return 0;
		}
		return count;
	}

	@Override
	public boolean checkContactIsHangUp(String actionId, String phone) {
		boolean isHangUp = false;
		int count = 0;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(
					"SELECT COUNT(*) FROM appointment_remainder.active_contact_det WHERE calluid = :contactId AND connectedlinenum = :customerMobileNumber");
			queryObj.setParameter("contactId", actionId);
			queryObj.setParameter("customerMobileNumber", phone);
			Object result = queryObj.getSingleResult();
			count = Integer.parseInt(result.toString());// Safe cast to Number then get int value
			if (count > 0) {
				isHangUp = true;
			} else {
				isHangUp = false;
			}
		} catch (Exception e) {
			logger.error("Error on checkContactIsHangUp: " + e.getMessage(), e);
			isHangUp = false;
		}
		return isHangUp;
	}

	@Override
	public boolean updateNTCStatus(String campaign_id) {
		boolean status = false;

		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.UPDATE_NTC_STATUS);
			// queryObj.setParameter("SMS_Triggered", "YES");
			queryObj.setParameter("campaign_id", campaign_id);
			int insertVal = queryObj.executeUpdate();
			if (insertVal > 0) {
				// Query queryObjs =
				// firstEntityManager.createNativeQuery(CampaignQueryConstant.UPDATE_NTC_STATUS_IN_RETRY_DET);
				// queryObj.setParameter("campaign_id", campaign_id);
				// queryObjs.executeUpdate();
				logger.info("NTC status Update successful for the campaign id " + campaign_id);
				status = true;
			} else {
				logger.info("NTC status Update failed for thr campaign id: " + campaign_id);
			}
		} catch (Exception e) {
			logger.info("Error in Update updateNTC status" + e);
		}
		return status;
	}

	@Override
	public boolean updateNTCStatusbyActionId(String actionid) {
		boolean status = false;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.UPDATE_NTC_STATUS_BY_ACTIONID);
			// queryObj.setParameter("SMS_Triggered", "YES");
			queryObj.setParameter("actionId", actionid);
			int insertVal = queryObj.executeUpdate();
			if (insertVal > 0) {
				logger.info("NTC status Update successful for thr actionid" + actionid);
				status = true;
			} else {
				logger.info("NTC status Update failed for thr actionid: " + actionid);
			}
		} catch (Exception e) {
			logger.info("Error in Update updateNTC status" + e);
		}
		return status;
	}

	public synchronized List<Object[]> makeInprogressintoNoAnswer(String campaignId) {
		boolean status = false;
		List<Object[]> resultList = new ArrayList<>();
		try {
			// Prepare and execute the select query
			Query selectQuery = firstEntityManager.createNativeQuery(CampaignQueryConstant.INPROGRESS_LIST);
			selectQuery.setParameter("campaign_id", campaignId);
			resultList = selectQuery.getResultList(); // 4
			if (!resultList.isEmpty()) {
				for (Object[] result : resultList) {
					logger.info("InProgress status data : " + Arrays.toString(result));
				}
				logger.info("Data retrieval successful for the campaignId " + campaignId);
			} else {
				logger.info("No InProgress Status found for the campaignId: " + campaignId);
			}
		} catch (Exception e) {
			logger.error("Error in retrieving Inprogress list", e);
		}
		return resultList;
	}

	public boolean updateNTCStatusinRetryDet(String campaign_id) {
		boolean status = false;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.UPDATE_NTC_STATUS_IN_RETRY_DET);
			// queryObj.setParameter("SMS_Triggered", "YES");
			queryObj.setParameter("campaign_id", campaign_id);
			int insertVal = queryObj.executeUpdate();
			if (insertVal > 0) {
				logger.info("NTC status Update successful for thr campaignId" + campaign_id);
				status = true;
			} else {
				logger.info("NTC status Update failed for thr campaignId: " + campaign_id);
			}
		} catch (Exception e) {
			logger.info("Error in Update updateNTC status" + e);
			status = false;
		}
		return status;
	}

	@Override
	public synchronized boolean saveRetryDetails(RetryDetailsDet retryDetailsDet, String campaignId, String actionID) {
		boolean isUpdated = false;
		try {

			int campRetry = getCampRetryCount(campaignId) + 1;
			int contactRetry = Integer.parseInt(retryDetailsDet.getRetryCount());

			logger.info("Campaign Retry : {} and Contact Retry Count : {} Action Id : {} ", campRetry, contactRetry,
					actionID);

			if (contactRetry == campRetry) {
				contactRetry = campRetry;
			} else if (contactRetry == 0 && contactRetry < campRetry) {
				contactRetry = 1;
			} else if (contactRetry == 1 && contactRetry < campRetry) {
				contactRetry = 2;
			} else if (contactRetry == 2 && contactRetry < campRetry) {
				contactRetry = 3;
			} else if (contactRetry == 3 && contactRetry < campRetry) {
				contactRetry = 4;
			} else if (contactRetry == 4 && contactRetry < campRetry) {
				contactRetry = 5;
			} else if (contactRetry == 5 && contactRetry < campRetry) {
				contactRetry = 6;
			} else if (contactRetry == 6 && contactRetry < campRetry) {
				contactRetry = 7;
			}

			Query queryObjRetry = firstEntityManager.createNativeQuery(CampaignQueryConstant.INSERT_CALL_RETRY_DET);
			queryObjRetry.setParameter("campaign_id", campaignId);
			queryObjRetry.setParameter("contactId", retryDetailsDet.getContactId());
			queryObjRetry.setParameter("callStatus", "NO ANSWER");
			queryObjRetry.setParameter("callDuration", retryDetailsDet.getCallDuration());
			queryObjRetry.setParameter("retryCount", String.valueOf(contactRetry));
			queryObjRetry.setParameter("contact_number", retryDetailsDet.getPhoneno());

			if (queryObjRetry.executeUpdate() > 0) {
				if (updateContactDetailToNOANSWER(actionID, String.valueOf(contactRetry))) {
					logger.info("Updated Inprogress into No Answer, actionId : " + actionID);
				}
				isUpdated = true;
			}
		} catch (NumberFormatException e) {
			logger.error("Error parsing retry count: {}", e.getMessage());
			return false;
		} catch (Exception e) {
			logger.error("Error occurring while updating data in call retry det for in progress: {}", e.getMessage());
			return false;
		}
		return isUpdated;
	}

	public Timestamp getRTCampaignStartDate(String campaign_id) {
		try {
			Query queryObj = firstEntityManager.createNativeQuery(
					"SELECT TOP 1 rec_add_dt FROM appointment_remainder.call_retry_det WHERE campaign_id = :campaign_id AND campaign_stopstatus != 'NTC' ORDER BY rec_add_dt ASC");
			queryObj.setParameter("campaign_id", campaign_id);
			Object responseLang = queryObj.getSingleResult();
			if (responseLang != null) {
				System.out.println(responseLang);
				return (Timestamp) responseLang;
			} else {
				System.out.println("No result found for the query.");
				return null;
			}
		} catch (NoResultException e) {
			System.out.println("No result found for the query.");
			logger.error("No result found for the query: " + e.getMessage());
		} catch (PersistenceException e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occurred in CampaignDaoImpl::getRTCampaignStartDate: " + str.toString());
			e.printStackTrace(); // Print stack trace for debugging
		}
		return null;
	}

	@Override
	public List<SurveyContactDetDto> getContactDetRetry(String campaignId, String retryCount) {
		List<Object[]> resultList;
		List<SurveyContactDetDto> contactDetDtoRetry = new ArrayList<>();
		;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CONTACT_DET_RETRY);
			queryObj.setParameter("campaignId", campaignId);
			queryObj.setParameter("retryCount", retryCount);
			resultList = queryObj.getResultList();
			if (resultList != null && !resultList.isEmpty()) {
				for (Object[] obj : resultList) {
					SurveyContactDetDto surveyConDto = new SurveyContactDetDto();
					// contactDetDto.(String.valueOf(obj[1]));
					// contactDetDto.s(String.valueOf(obj[1]));
					/*
					 * contactDetDto.setDoctorName(String.valueOf(obj[2]));
					 * contactDetDto.setPatientName(String.valueOf(obj[3]));
					 * contactDetDto.setContactNo(String.valueOf(obj[4]));
					 * contactDetDto.setAppointmentDate(String.valueOf(obj[5]));
					 */

					surveyConDto.setMainSkillset(String.valueOf(obj[0]));
					surveyConDto.setPhone(String.valueOf(obj[1]));
					surveyConDto.setActionId(String.valueOf(obj[2]));
					surveyConDto.setSurvey_Lang(String.valueOf(obj[3]));
					surveyConDto.setSubSkillset(String.valueOf(obj[4]));
					surveyConDto.setCall_status(String.valueOf(obj[5]));
					surveyConDto.setRec_update_time(String.valueOf(obj[6]));
					try {
						surveyConDto.setRetryCount(String.valueOf(obj[7]));
					} catch (Exception e) {
						surveyConDto.setRetryCount("0");
					}
					surveyConDto.setLastFourDigits(String.valueOf(obj[8]));
					surveyConDto.setTotalDue(String.valueOf(obj[9]));
					surveyConDto.setMinPayment(String.valueOf(obj[10]));
					surveyConDto.setDueDate(String.valueOf(obj[11]));
					// campaignDetMap.get(preVal).add(surveyConDto);
					// contactDetDto.setLastFourDigits(String.valueOf(obj[2]));
					// contactDetDto.setPhone(String.valueOf(obj[3]));
					// contactDetDto.setTotalDue(String.valueOf(obj[4]));
					// contactDetDto.setMinPayment(String.valueOf(obj[5]));
					// contactDetDto.setDueDate(String.valueOf(obj[6]));
					// contactDetDto.setSurvey_Lang(String.valueOf(obj[7]));
					// contactDetDto.setActionId(String.valueOf(obj[8]));
					// contactDetDto.setRetryCount(String.valueOf(obj[9]));
					// contactDetDto.setRec_update_time(String.valueOf(obj[10]));
					// contactDetDto.setCall_status(String.valueOf(obj[11]));
					// // contactDetDto.setProductID(String.valueOf(obj[12]));
					contactDetDtoRetry.add(surveyConDto);
				}
			}

		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getContactDet" + e);
			return contactDetDtoRetry;
		}
		return contactDetDtoRetry;
	}

	@Override
	public List<Object[]> getSmswebReportDet(String actionId) {
		List<Object[]> resultList = null;
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_SMS_WEB_REPORT);
			queryObj.setParameter("actionId", actionId);
			resultList = queryObj.getResultList();
			logger.info("resultList :" + resultList);
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getSmswebReportDet" + e);
			return resultList;
		}
		return resultList;
	}

	@Override
	public boolean updateAgentDynamicContact(String actionId, String agent_userid, String campaign_id,
			String customer_mobile_number) {
		boolean updateStatus;
		try {
			logger.info("Agent ID :" + agent_userid + " action ID :" + actionId + " :: Customer Mobile Number :"
					+ customer_mobile_number + " :: Campaign ID :" + campaign_id);
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.UPDATE_DYANMIC_CONTACT_DET_CT);
			queryObj.setParameter("actionId", actionId);
			queryObj.setParameter("customer_mobile_number", customer_mobile_number);
			queryObj.setParameter("agent_userid", agent_userid);
			queryObj.setParameter("campaign_id", campaign_id);
			queryObj.executeUpdate();
			updateStatus = true;
			logger.info("Updated Agent Id for Dynamic Contact Details for the action ID " + actionId);
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl:: Update Agent id in Dynamic Contact " + str.toString());
			throw e;
		}
		return updateStatus;
	}

	@Override
	public boolean updateAssignedAgentDynamicContact(String actionId, String agent_userid, String campaign_id,
			String customer_mobile_number) {
		boolean updateStatus;
		try {
			logger.info("Agent ID :" + agent_userid + " action ID :" + actionId + " :: Customer Mobile Number :"
					+ customer_mobile_number + " :: Campaign ID :" + campaign_id);
			Query queryObj = firstEntityManager
					.createNativeQuery(CampaignQueryConstant.UPDATE_ASSIGNED_DYANMIC_CONTACT_DET_CT);

			queryObj.setParameter("actionId", actionId);
			queryObj.setParameter("customer_mobile_number", customer_mobile_number);
			queryObj.setParameter("agent_userid", agent_userid);
			queryObj.setParameter("campaign_id", campaign_id);
			queryObj.executeUpdate();
			updateStatus = true;
			logger.info("Updated Agent Id for Dynamic Contact Details for the action ID " + actionId);
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Error occured in CampaignDaoImpl:: Update Agent id in Dynamic Contact " + str.toString());
			throw e;
		}
		return updateStatus;
	}

	/*
	 * @Override public List<DynamicContactDetDto>
	 * getPreviewAgentBasedContactDetail(Map<String,String> mapDynamicFields,String
	 * agentId) { List<Object[]> resultList; List<DynamicContactDetDto>
	 * campaignDetlist = new ArrayList<>(); try { // Query queryObj =
	 * firstEntityManager.createNativeQuery(CampaignQueryConstant.
	 * GET_SURVEY_CONTACT_DET); Query queryObj =
	 * firstEntityManager.createNativeQuery(CampaignQueryConstant.
	 * GET_AGENT_BASED_CONTACT_DET); // queryObj.setParameter("campaign_id",
	 * campaign_id); queryObj.setParameter("agent_userid", agentId);
	 * //logger.info("result list ::: "+ campaign_id + "  "+ agent_userid);
	 * //queryObj.executeUpdate(); resultList = queryObj.getResultList();
	 * logger.info("Result list "+ resultList); if (resultList != null &&
	 * !resultList.isEmpty()) { String preVal = ""; for (Object[] obj : resultList)
	 * { DynamicContactDetDto surveyConDto=new DynamicContactDetDto();
	 * surveyConDto.setCampaignId(String.valueOf(obj[0]));
	 * surveyConDto.setCampaignName(String.valueOf(obj[1]));
	 * surveyConDto.setCustomerMobileNumber(String.valueOf(obj[2]));
	 * surveyConDto.setAgent_userid(String.valueOf(obj[3]));
	 * 
	 * // surveyConDto.setCampaignId(preVal); Map<String,String> dynField=new
	 * LinkedHashMap<>();
	 * 
	 * int i=1; for(int j=3;j<=j+mapDynamicFields.size();j++) { String
	 * key=mapDynamicFields.get("reserve_"+i); i++; String value=(String) obj[j];
	 * dynField.put(key, value); }
	 * 
	 * for (int i = 1; i <= mapDynamicFields.size(); i++) { String key =
	 * mapDynamicFields.get("reserve_" + i); String value = (String) obj[i + 3];
	 * dynField.put(key, value); } surveyConDto.setMapDynamicFields(dynField);
	 * campaignDetlist.add(surveyConDto); } } else {
	 * logger.info("result list is empty "); } } catch (Exception e) { logger.
	 * error("Error occured in CampaignDaoImpl::getAsssignedContactAgentDetail" +
	 * e); return campaignDetlist; } return campaignDetlist; }
	 */


	@Override
	public Map<String, List<DynamicContactDetDto>> getcampBasedAssignedContactDetail(
			Map<String, String> mapDynamicFields, String campaign_id) {
		List<Object[]> resultList;
		List<DynamicContactDetDto> campaignDetlist = new ArrayList<>();
		Map<String, List<DynamicContactDetDto>> mapCampBasedAgCont = new LinkedHashMap<>();
		try {
			// Query queryObj =
			// firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_SURVEY_CONTACT_DET);
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CAMPAIGN_BASED_CONTACT_DET);
			queryObj.setParameter("campaign_id", campaign_id);
			// queryObj.executeUpdate();
			resultList = queryObj.getResultList();
			logger.info("Result list " + resultList);
			if (resultList != null && !resultList.isEmpty()) {
				String preVal = "";
				for (Object[] obj : resultList) {
					DynamicContactDetDto surveyConDto = new DynamicContactDetDto();
					surveyConDto.setCampaignId(String.valueOf(obj[0]));
					surveyConDto.setCampaignName(String.valueOf(obj[1]));
					surveyConDto.setCustomerMobileNumber(String.valueOf(obj[2]));
					surveyConDto.setAgent_userid(String.valueOf(obj[3]));
					String agentId = String.valueOf(obj[3]);
					surveyConDto.setActionId(String.valueOf(obj[4]));
					List<DynamicContactDetDto> listDy = null;
					if (mapCampBasedAgCont.containsKey(agentId)) {
						listDy = mapCampBasedAgCont.get(agentId);
					} else {
						listDy = new ArrayList<>();
					}
					// surveyConDto.setCampaignId(preVal);

					Map<String, String> dynField = new LinkedHashMap<>();
					/*
					 * int i=1; for(int j=3;j<=j+mapDynamicFields.size();j++) { String
					 * key=mapDynamicFields.get("reserve_"+i); i++; String value=(String) obj[j];
					 * dynField.put(key, value); }
					 */
					for (int i = 1; i <= mapDynamicFields.size(); i++) {
						String key = mapDynamicFields.get("reserve_" + i);
						String value = (String) obj[i + 3];
						dynField.put(key, value);
					}
					surveyConDto.setMapDynamicFields(dynField);
					listDy.add(surveyConDto);
					mapCampBasedAgCont.put(agentId, listDy);
				}
			} else {
				logger.info("result list is empty ");
			}
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getAsssignedContactAgentDetail" + e);
			return mapCampBasedAgCont;
		}
		return mapCampBasedAgCont;
	}

	@Override
	public Map<String, List<DynamicContactDetDto>> getPreviewAgentBasedContactDetail(Map<String, String> mapDynamicFields, String agent_userid) {

		List<Object[]> resultList;
		List<DynamicContactDetDto> campaignDetlist = new ArrayList<>();
		Map<String, List<DynamicContactDetDto>> mapCampBasedAgCont = new LinkedHashMap<>();
		try {
			// Query queryObj =
			// firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_SURVEY_CONTACT_DET);
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_AGENT_BASED_CONTACT_DET);
			queryObj.setParameter("agent_userid", agent_userid);
			// queryObj.executeUpdate();
			resultList = queryObj.getResultList();
			logger.info("Result list " + resultList);
			if (resultList != null && !resultList.isEmpty()) {
				String preVal = "";
				for (Object[] obj : resultList) {
					DynamicContactDetDto surveyConDto = new DynamicContactDetDto();
					surveyConDto.setCampaignId(String.valueOf(obj[0]));
					surveyConDto.setCampaignName(String.valueOf(obj[1]));
					surveyConDto.setCustomerMobileNumber(String.valueOf(obj[2]));
					surveyConDto.setAgent_userid(String.valueOf(obj[3]));
					String agentId = String.valueOf(obj[3]);
					surveyConDto.setActionId(String.valueOf(obj[4]));
					List<DynamicContactDetDto> listDy = null;
					if (mapCampBasedAgCont.containsKey(agentId)) {
						listDy = mapCampBasedAgCont.get(agentId);
					} else {
						listDy = new ArrayList<>();
					}
					// surveyConDto.setCampaignId(preVal);

					Map<String, String> dynField = new LinkedHashMap<>();
					/*
					 * int i=1; for(int j=3;j<=j+mapDynamicFields.size();j++) { String
					 * key=mapDynamicFields.get("reserve_"+i); i++; String value=(String) obj[j];
					 * dynField.put(key, value); }
					 */
					for (int i = 1; i <= mapDynamicFields.size(); i++) {
						String key = mapDynamicFields.get("reserve_" + i);
						String value = (String) obj[i + 3];
						dynField.put(key, value);
					}
					surveyConDto.setMapDynamicFields(dynField);
					listDy.add(surveyConDto);
					mapCampBasedAgCont.put(agentId, listDy);
				}
			} else {
				logger.info("result list is empty ");
			}
		} catch (Exception e) {
			logger.error("Error occured in CampaignDaoImpl::getAsssignedContactAgentDetail" + e);
			return mapCampBasedAgCont;
		}
		return mapCampBasedAgCont;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, List<DynamicContactDetDto>> getSupervisorAgentContactDet(Map<String, String> mapDynamicFields,
	        String supervisor) {
	    Map<String, List<DynamicContactDetDto>> supervisorContactDetails = new LinkedHashMap<>();
	    try {
	        Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_ASSIGNED_AGENT_BASED_ON_SUPERVISOR);
	        queryObj.setParameter("Supervisor", supervisor);
	        List<Object> resultList = queryObj.getResultList();

	        for (Object obj : resultList) {
	            String agent_userid = (String) obj;
	            Map<String, List<DynamicContactDetDto>> agentContactDetails = getAgentBasedContactDetailwithStatus(mapDynamicFields, agent_userid);
	            supervisorContactDetails.put(agent_userid, agentContactDetails.get(agent_userid));
	        }
	    } catch (Exception e) {
	        StringWriter str = new StringWriter();
	        e.printStackTrace(new PrintWriter(str));
	        logger.error("Error occurred in CampaignDaoImpl::getSupervisorAgentContactDet" + str.toString());
	    }
	    return supervisorContactDetails;
	}


	public DynamicContactDetDto getCustomerDetail(String customerNumber) {
		DynamicContactDetDto dynamicContactDetDto = new DynamicContactDetDto();
		List<DynamicContactDetDto> dynamicContactDetDtoList = new ArrayList<>();
		try {
			Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_CONTACT_DET_BY_CUSTOMERNUMBER);
			queryObj.setParameter("customerNumber", customerNumber);
			List<Object[]> resultList = queryObj.getResultList();  // Declare and initialize resultList
			logger.info("Result list: " + resultList);

			if (resultList != null && !resultList.isEmpty()) {
				for (Object[] obj : resultList) {
					DynamicContactDetDto surveyConDto = new DynamicContactDetDto();
					surveyConDto.setCampaignId(String.valueOf(obj[0]));
					surveyConDto.setCampaignName(String.valueOf(obj[1]));
					surveyConDto.setCustomerMobileNumber(String.valueOf(obj[2]));
					surveyConDto.setAgent_userid(String.valueOf(obj[3]));
					surveyConDto.setActionId(String.valueOf(obj[4]));
					surveyConDto.setCallStatus(String.valueOf(obj[5]));
					surveyConDto.setLanguage(String.valueOf(obj[6]));
					surveyConDto.setCallRetryCount(String.valueOf(obj[7]));
//					surveyConDto.setFailureReason(String.valueOf(obj[8]));
					surveyConDto.setHistoryId(BigInteger.valueOf(Integer.parseInt(String.valueOf(obj[8]))));
					surveyConDto.setUpdatedDate(String.valueOf(obj[9]));
					surveyConDto.setContactId(String.valueOf(obj[10]));
					surveyConDto.setSubskill_set(String.valueOf(obj[11]));
					dynamicContactDetDtoList.add(surveyConDto);
				}
			}
			if (!dynamicContactDetDtoList.isEmpty()) {
				dynamicContactDetDto = dynamicContactDetDtoList.get(0);
			}
		} catch (Exception e) {
			logger.error("Error occurring while getCustomerDetail :: CampaignDaoImpl " + e.getMessage());
		}
		return dynamicContactDetDto;
	}

	public Map<String, List<DynamicContactDetDto>> getAgentBasedContactDetailwithStatus(
	        Map<String, String> mapDynamicFields, String agent_userid) {
	    List<Object[]> resultList;
	    Map<String, List<DynamicContactDetDto>> mapCampBasedAgCont = new LinkedHashMap<>();
	    try {
	        Query queryObj = firstEntityManager.createNativeQuery(CampaignQueryConstant.GET_AGENT_BASED_CONTACT_STATUS_DET);
	        queryObj.setParameter("agent_userid", agent_userid);
	        resultList = queryObj.getResultList();
	        logger.info("Result list: " + resultList);
	        
	        List<DynamicContactDetDto> contactDetails = new ArrayList<>();
	        for (Object[] obj : resultList) {
	        	  DynamicContactDetDto surveyConDto = new DynamicContactDetDto();
		            surveyConDto.setCampaignId(String.valueOf(obj[0]));
		            surveyConDto.setCampaignName(String.valueOf(obj[1]));
		            surveyConDto.setCustomerMobileNumber(String.valueOf(obj[2]));
		            surveyConDto.setAgent_userid(String.valueOf(obj[3]));
		            String agentId = String.valueOf(obj[3]);
		            surveyConDto.setActionId(String.valueOf(obj[4]));
		            surveyConDto.setCallStatus(String.valueOf(obj[5]));
		            Map<String, String> dynField = new LinkedHashMap<>();
		            for (int i = 1; i <= mapDynamicFields.size(); i++) {
		                String key = mapDynamicFields.get("reserve_" + i);
		                String value = (String) obj[i + 3];
		                dynField.put(key, value);
		            }
		            surveyConDto.setMapDynamicFields(dynField);
		            mapCampBasedAgCont.computeIfAbsent(agentId, k -> new ArrayList<>()).add(surveyConDto);
	     //   mapCampBasedAgCont.put(agent_userid, contactDetails);
	    }} catch (Exception e) {
	        logger.error("Error occurred in CampaignDaoImpl::getAgentBasedContactDetailwithStatus" + e);
	    }
	    return mapCampBasedAgCont;
	}
}
