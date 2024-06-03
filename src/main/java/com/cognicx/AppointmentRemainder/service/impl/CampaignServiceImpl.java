package com.cognicx.AppointmentRemainder.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cognicx.AppointmentRemainder.Dto.ContactDetDto;
import com.cognicx.AppointmentRemainder.Dto.CustomerDataDto;
import com.cognicx.AppointmentRemainder.Dto.DncContactDto;
import com.cognicx.AppointmentRemainder.Dto.DynamicContactDetDto;
import com.cognicx.AppointmentRemainder.Dto.RetryCountDto;
import com.cognicx.AppointmentRemainder.Dto.RetryDetailsDet;
import com.cognicx.AppointmentRemainder.Dto.SurveyContactDetDto;
import com.cognicx.AppointmentRemainder.Dto.UploadHistoryDto;
import com.cognicx.AppointmentRemainder.Request.CampaignDetRequest;
import com.cognicx.AppointmentRemainder.Request.CampaignRealTimeDashboard;
import com.cognicx.AppointmentRemainder.Request.CampaignStatus;
import com.cognicx.AppointmentRemainder.Request.CampaignWeekDetRequest;
import com.cognicx.AppointmentRemainder.Request.DNCDetRequest;
import com.cognicx.AppointmentRemainder.Request.ReportRequest;
import com.cognicx.AppointmentRemainder.Request.UpdateCallDetRequest;
import com.cognicx.AppointmentRemainder.dao.CampaignDao;
import com.cognicx.AppointmentRemainder.response.GenericHeaderResponse;
import com.cognicx.AppointmentRemainder.response.GenericResponse;
import com.cognicx.AppointmentRemainder.response.GenericResponseReport;
import com.cognicx.AppointmentRemainder.service.CampaignService;
import com.cognicx.AppointmentRemainder.service.SMSAPIIntegration;
import com.cognicx.AppointmentRemainder.util.ExcelUtil;

@Service
public class CampaignServiceImpl implements CampaignService {

	@Autowired
	CampaignDao campaignDao;


	@Autowired
	SMSAPIIntegration smsAPIIntegration;

	@Value("${sms.enabled}")
	private String isSmsEnabled;

	@Value("${cont.errorcodes}")
	private String errorcodes;

	@Value("${defaultCampaign}")
	private String defaultcampaign;

	@Value("${account}")
	private String account;

	private static Logger logger = LoggerFactory.getLogger(CampaignServiceImpl.class);

	@Override
	public ResponseEntity<GenericResponse> createCampaign(CampaignDetRequest campaignDetRequest) {
		GenericResponse genericResponse = new GenericResponse();
		try {
			String campaignId = campaignDao.createCampaign(campaignDetRequest);
			if (campaignId != null) {
				genericResponse.setStatus(200);
				genericResponse.setValue("Success");
				genericResponse.setMessage("Campaign created successfully, Campaign Id: " + campaignId);
			} else {
				genericResponse.setStatus(400);
				genericResponse.setValue("Failure");
				genericResponse.setMessage("Error occured while creating Campaign");
			}
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::createCampaign " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("Error occured while creating Campaign");
		}

		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<GenericResponse> getCampaignDetail(String userGroup) {
		GenericResponse genericResponse = new GenericResponse();
		List<CampaignDetRequest> campaignDetList = null;
		try {
			campaignDetList = getCampaignDetList(userGroup);
			genericResponse.setStatus(200);
			genericResponse.setValue(campaignDetList);
			genericResponse.setMessage("Success");
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::createCampaign " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("No data Found");
		}

		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}

	@Override
	public List<CampaignDetRequest> getCampaignDetList(String userGroup) {
		List<CampaignDetRequest> campaignDetList;
		campaignDetList = new ArrayList<>();
		CampaignStatus campaignStatus = null;
		List<Object[]> campainDetObjList = campaignDao.getCampaignDet(userGroup);
		Map<String, List<CampaignWeekDetRequest>> campainWeekDetList = campaignDao.getCampaignWeekDet();
		if (campainDetObjList != null && !campainDetObjList.isEmpty()) {
			for (Object[] obj : campainDetObjList) {
				CampaignDetRequest campaignDetRequest = new CampaignDetRequest();
				campaignDetRequest.setCampaignId(String.valueOf(obj[0]));
				campaignDetRequest.setCampaignName(String.valueOf(obj[1]));
				campaignDetRequest.setCampaignActive(String.valueOf(obj[3]));
				campaignDetRequest.setMaxAdvNotice(String.valueOf(obj[4]));
				campaignDetRequest.setRetryDelay(String.valueOf(obj[5]));
				campaignDetRequest.setRetryCount(String.valueOf(obj[6]));
				campaignDetRequest.setConcurrentCall(String.valueOf(obj[7]));
				campaignDetRequest.setStartDate(String.valueOf(obj[8]));
				campaignDetRequest.setStartTime(String.valueOf(obj[9]));
				campaignDetRequest.setEndDate(String.valueOf(obj[10]));
				campaignDetRequest.setEndTime(String.valueOf(obj[11]));
				campaignDetRequest.setFtpLocation(String.valueOf(obj[12]));
				if (obj[13] != null && !";".equalsIgnoreCase(String.valueOf(obj[13]))) {
					String[] ftpCredendials = String.valueOf(obj[13]).split(";");
					campaignDetRequest.setFtpUsername(ftpCredendials[0]);
					campaignDetRequest.setFtpPassword(ftpCredendials[1]);
				}
				campaignDetRequest.setFileName(String.valueOf(obj[14]));
				//campaignDetRequest.setCallBefore(String.valueOf(obj[15]));
				campaignDetRequest.setDncId(String.valueOf(obj[15]));
				campaignDetRequest.setUserGroup(String.valueOf(obj[16]));
				campaignDetRequest.setDailingMode(String.valueOf(obj[17]));
				;
				campaignDetRequest.setQueue(String.valueOf(obj[18]));
				campaignDetRequest.setDispositionID(String.valueOf(obj[19]));
				campaignDetRequest.setDailingoption(String.valueOf(obj[20]));
				if (campainWeekDetList != null && campainWeekDetList.containsKey(campaignDetRequest.getCampaignId()))
					campaignDetRequest.setWeekDaysTime(campainWeekDetList.get(campaignDetRequest.getCampaignId()));
				campaignDetList.add(campaignDetRequest);
				campaignStatus = new CampaignStatus();
				campaignStatus.setCampaignId(campaignDetRequest.getCampaignId());
				campaignDetRequest.setSchedulerEnabled(campaignDao.getCampaignStatus(campaignStatus));
				campaignDetRequest.setFrontstatus(campaignDao.getFrontCampaignStatus(campaignStatus));
				logger.info("Campaign Details :" + campaignDetRequest.toString());
			}
		}
		return campaignDetList;
	}


	@Override
	public List<CampaignDetRequest> getCampaignDetList() {
		List<CampaignDetRequest> campaignDetList;
		campaignDetList = new ArrayList<>();
		CampaignStatus campaignStatus = null;
		List<Object[]> campainDetObjList = campaignDao.getCampaignDet();
		Map<String, List<CampaignWeekDetRequest>> campainWeekDetList = campaignDao.getCampaignWeekDet();
		if (campainDetObjList != null && !campainDetObjList.isEmpty()) {
			for (Object[] obj : campainDetObjList) {
				CampaignDetRequest campaignDetRequest = new CampaignDetRequest();
				campaignDetRequest.setCampaignId(String.valueOf(obj[0]));
				campaignDetRequest.setCampaignName(String.valueOf(obj[1]));
				campaignDetRequest.setCampaignActive(String.valueOf(obj[3]));
				campaignDetRequest.setMaxAdvNotice(String.valueOf(obj[4]));
				campaignDetRequest.setRetryDelay(String.valueOf(obj[5]));
				campaignDetRequest.setRetryCount(String.valueOf(obj[6]));
				campaignDetRequest.setConcurrentCall(String.valueOf(obj[7]));
				campaignDetRequest.setStartDate(String.valueOf(obj[8]));
				campaignDetRequest.setStartTime(String.valueOf(obj[9]));
				campaignDetRequest.setEndDate(String.valueOf(obj[10]));
				campaignDetRequest.setEndTime(String.valueOf(obj[11]));
				campaignDetRequest.setFtpLocation(String.valueOf(obj[12]));
				if (obj[13] != null && !";".equalsIgnoreCase(String.valueOf(obj[13]))) {
					String[] ftpCredendials = String.valueOf(obj[13]).split(";");
					campaignDetRequest.setFtpUsername(ftpCredendials[0]);
					campaignDetRequest.setFtpPassword(ftpCredendials[1]);
				}
				campaignDetRequest.setFileName(String.valueOf(obj[14]));
				//campaignDetRequest.setCallBefore(String.valueOf(obj[15]));
				campaignDetRequest.setDncId(String.valueOf(obj[15]));
				campaignDetRequest.setUserGroup(String.valueOf(obj[16]));
				campaignDetRequest.setDailingMode(String.valueOf(obj[17]));
				;
				campaignDetRequest.setQueue(String.valueOf(obj[18]));
				campaignDetRequest.setDispositionID(String.valueOf(obj[19]));
				campaignDetRequest.setDailingoption(String.valueOf(obj[20]));
				if (campainWeekDetList != null && campainWeekDetList.containsKey(campaignDetRequest.getCampaignId()))
					campaignDetRequest.setWeekDaysTime(campainWeekDetList.get(campaignDetRequest.getCampaignId()));
				campaignDetList.add(campaignDetRequest);
				campaignStatus = new CampaignStatus();
				campaignStatus.setCampaignId(campaignDetRequest.getCampaignId());
				campaignDetRequest.setSchedulerEnabled(campaignDao.getCampaignStatus(campaignStatus));
				campaignDetRequest.setFrontstatus(campaignDao.getFrontCampaignStatus(campaignStatus));
				logger.info("Campaign Details :" + campaignDetRequest.toString());
			}
		}
		return campaignDetList;
	}

	@Override
	public ResponseEntity<GenericResponse> updateCampaign(CampaignDetRequest campaignDetRequest) {
		GenericResponse genericResponse = new GenericResponse();
		try {
			boolean isUpdated = campaignDao.updateCampaign(campaignDetRequest);
			if (isUpdated) {
				genericResponse.setStatus(200);
				genericResponse.setValue("Success");
				genericResponse.setMessage("Campaign updated successfully");
			} else {
				genericResponse.setStatus(400);
				genericResponse.setValue("Failure");
				genericResponse.setMessage("Error occured while updating Campaign");
			}
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::updateCampaign " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("Error occured while updating Campaign");
		}

		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<GenericResponse> startResumeCampaignStatus(String campaginId, String frontStatus) {
		GenericResponse genericResponse = new GenericResponse();
		try {
			boolean isUpdated = campaignDao.startResumeCampaignStatus(campaginId, frontStatus);
			if (isUpdated) {
				genericResponse.setStatus(200);
				genericResponse.setValue("Success");
				genericResponse.setMessage("Campaign updated successfully");
			} else {
				genericResponse.setStatus(400);
				genericResponse.setValue("Failure");
				genericResponse.setMessage("Error occured while updating Campaign");
			}
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::updateCampaign " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("Error occured while updating Campaign");
		}

		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<GenericResponse> stopPauseCampaignStatus(String campaginId, String frontStatus) {
		GenericResponse genericResponse = new GenericResponse();
		try {
			boolean isUpdated = campaignDao.stopPauseCampaignStatus(campaginId, frontStatus);
			if (isUpdated) {
				genericResponse.setStatus(200);
				genericResponse.setValue("Success");
				genericResponse.setMessage("Campaign updated successfully");
			} else {
				genericResponse.setStatus(400);
				genericResponse.setValue("Failure");
				genericResponse.setMessage("Error occured while updating Campaign");
			}
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::updateCampaign " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("Error occured while updating Campaign");
		}

		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<GenericResponse> updateCallDetail(UpdateCallDetRequest updateCallDetRequest) {
		GenericResponse genericResponse = new GenericResponse();
		try {
			logger.info("**********UPDATE CALL DETAILS INPUT**********");
			logger.info("Phone : " + updateCallDetRequest.getPhone());
			logger.info("Call Duration: " + updateCallDetRequest.getCallduration());
			logger.info("Action Id: " + updateCallDetRequest.getActionid());
			logger.info("Survey Rating: " + updateCallDetRequest.getSurveyrating());
			logger.info("Call start time " + updateCallDetRequest.getCallStartTime());
			logger.info("Call end time " + updateCallDetRequest.getCallEndTime());
			logger.info("Dial Status " + updateCallDetRequest.getDialstatus());
			logger.info("Disposition : " + updateCallDetRequest.getDisposition()); //
			logger.info("HangUp Code : " + updateCallDetRequest.getHangupcode());
			logger.info("HangUp Reason : " + updateCallDetRequest.getHangupreason());
			logger.info("Call Answer: " + updateCallDetRequest.getCallanswer());
			logger.info("Call TalkTime: " + updateCallDetRequest.getCalltalktime());
			logger.info("Hangup Text: " + updateCallDetRequest.getHanguptext());
			logger.info("Survey Rating : " + updateCallDetRequest.getSurveyrating());

			//	logger.info("Hangupcode: " + updateCallDetRequest.getHangupcode());
			boolean isUpdated = campaignDao.updateCallDetail(updateCallDetRequest);
			if (isUpdated) {
				String campaignID = campaignDao.getCampIdbasedonactionId(updateCallDetRequest.getActionid());
				int campaignRetryCount = campaignDao.getCampRetryCount(campaignID) + 1;
				logger.info("Campaign Retry Count + 1 : " + campaignRetryCount);
				//                if (updateCallDetRequest.getRetryCount() == campaignRetryCount || updateCallDetRequest.getDisposition().equalsIgnoreCase("ANSWERED")) {
				//                    campaignDao.updateNTCStatusbyActionId(updateCallDetRequest.getActionid());
				//                } else {
				//                    logger.info("Update NTC is not set for this action Id  : " + updateCallDetRequest.getActionid());
				//                }
				int retrycount = updateCallDetRequest.getRetryCount();

				if (campaignID == null || campaignID.isEmpty()) {
					logger.info("Not able to fetch Campaign ID based on Action ID :" + updateCallDetRequest.getActionid());
					logger.info("Hence SMS is not triggered");
				} else {
					logger.info("Campaign ID for the Action ID :" + updateCallDetRequest.getActionid() + " :: " + campaignID);
					if (updateCallDetRequest.getDisposition() == null || !updateCallDetRequest.getDisposition().equalsIgnoreCase("ANSWERED")) {
						logger.info("Campaign Retry Count :" + campaignRetryCount);
						retrycount = retrycount + 1;
						if (retrycount >= campaignRetryCount) {
							if (isSmsEnabled != null && isSmsEnabled.equalsIgnoreCase("true")) {
								String language = campaignDao.getLanguageBasedonActionId(updateCallDetRequest.getActionid());
								logger.info("SMS Service class Lang received is :" + language);
								String status = smsAPIIntegration.sendSMS(updateCallDetRequest.getPhone(), language, updateCallDetRequest.getActionid());
								logger.info("SMS Service class Status received is :" + status);
								if (status != null && status.equalsIgnoreCase("SUCCESS")) {
									logger.info("SMS trigerred successfully");
									campaignDao.updateSMSStatus(updateCallDetRequest.getActionid());
								} else {
									logger.info("Failed to trigger SMS");
								}
							} else {
								logger.info("SMS featured is disabled...");
							}
						} else {
							logger.info("Retry count is :" + retrycount + "which is less than or equal to Campaign Max Count" + campaignRetryCount + "Hence not invoking SMS");
						}
					} else {
						logger.info("Caller Status is Answered Hence not invoking SMS");
					}
				}
				genericResponse.setStatus(200);
				genericResponse.setValue("Success");
				genericResponse.setMessage("Call Details updated successfully");
			} else {
				logger.info("Update Call Detail failed for the campaign ID " + updateCallDetRequest.getActionid());
				genericResponse.setStatus(400);
				genericResponse.setValue("Failure");
				genericResponse.setMessage("Error occured while updating Call Details");
			}
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::update Call Detail " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("Error occured while updating Call Details");
		}
		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}

	@Override
	public boolean createContact(ContactDetDto contactDetDto) {
		boolean isCreated;
		try {
			isCreated = campaignDao.createContact(contactDetDto);
			logger.info("Is Created or not : " + isCreated);
		} catch (Exception e) {
			return false;
		}
		return isCreated;
	}

	@Override
	public Map<String, List<ContactDetDto>> getContactDet() {
		return campaignDao.getContactDet();
	}

	@Override
	public ResponseEntity<GenericResponse> validateCampaignName(CampaignDetRequest campaignDetRequest) {
		GenericResponse genericResponse = new GenericResponse();
		try {
			boolean isValidated = campaignDao.validateCampaignName(campaignDetRequest);
			genericResponse.setStatus(200);
			genericResponse.setValue(isValidated);
			genericResponse.setMessage("Validation done");
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::updateCampaign " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue(true);
			genericResponse.setMessage("Error occured Validating Details");
		}

		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}


	@Override
	public ResponseEntity<GenericResponseReport> summaryReport(ReportRequest reportRequest) {
		GenericResponseReport genericResponse = new GenericResponseReport();
		List<GenericHeaderResponse> headerlist = null;
		List<GenericHeaderResponse> subHeaderlist = null;
		List<Map<Object, Object>> valueList = null;
		try {
			List<Object[]> resultList = campaignDao.getSummaryReportDet(reportRequest);
			if (resultList != null && !resultList.isEmpty()) {
				headerlist = new ArrayList<GenericHeaderResponse>();
				subHeaderlist = new ArrayList<GenericHeaderResponse>();
				valueList = new ArrayList<Map<Object, Object>>();
				subHeaderlist.add(new GenericHeaderResponse("Campaign Name", "campaignName"));
				subHeaderlist.add(new GenericHeaderResponse("Campaign Date", "date"));
				subHeaderlist.add(new GenericHeaderResponse("List Length", "totalContact"));
				//				subHeaderlist.add(new GenericHeaderResponse("Contacts Called", "contactCalled"));
				subHeaderlist.add(new GenericHeaderResponse("Answered", "contactConnected"));
				subHeaderlist.add(new GenericHeaderResponse("Ringing No Answer", "answered"));
				subHeaderlist.add(new GenericHeaderResponse("Busy", "busy"));
				subHeaderlist.add(new GenericHeaderResponse("Not Reachable", "notreachable"));
				//				subHeaderlist.add(new GenericHeaderResponse("Invalid No", "invalidno"));
				subHeaderlist.add(new GenericHeaderResponse("DNC", "dnc"));
				subHeaderlist.add(new GenericHeaderResponse("Others", "others"));
				/*
				 * subHeaderlist.add(new GenericHeaderResponse("Confirmed", "confirmed"));
				 * subHeaderlist.add(new GenericHeaderResponse("Cancelled", "canceleld"));
				 * subHeaderlist.add(new GenericHeaderResponse("Rescheduled", "rescheduled"));
				 * subHeaderlist.add(new GenericHeaderResponse("No Response", "noResponse"));
				 *
				 */

				String campaignId = reportRequest.getCampaignId();
				int dncCount = getDNSDetList(getDNCIDusingCampaignID(campaignId)).size();
				logger.info("Campaign Id:" + campaignId + ", DNC Count and ID " + dncCount + "DNC ID : " + getDNCIDusingCampaignID(campaignId));
				headerlist.add(new GenericHeaderResponse("Campaign Summary Report", "", subHeaderlist));
				for (Object[] obj : resultList) {
					Map<Object, Object> valueMap = new LinkedHashMap<>();
					valueMap.put("campaignName", obj[0]);
					valueMap.put("date", obj[1]);
					valueMap.put("totalContact", obj[2]);
					valueMap.put("contactCalled", obj[3]);
					valueMap.put("contactConnected", obj[4]);
					valueMap.put("answered", obj[5]);
					valueMap.put("busy", obj[6]);
					valueMap.put("notreachable", obj[7]);
					//					valueMap.put("invalidno", obj[8]);
					valueMap.put("dnc", obj[8]);
					valueMap.put("others", obj[10]);
					/*
					 * valueMap.put("confirmed", obj[7]); valueMap.put("canceleld", obj[8]);
					 * valueMap.put("rescheduled", obj[9]); valueMap.put("noResponse", obj[10]);
					 */
					valueList.add(valueMap);
				}
				genericResponse.setStatus(200);
				genericResponse.setHeader(headerlist);
				genericResponse.setValue(valueList);
				genericResponse.setMessage("Data fetched sucessfully");
			} else {
				genericResponse.setStatus(200);
				genericResponse.setValue(null);
				genericResponse.setMessage("No data found");
			}
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::summaryReport " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue(null);
			genericResponse.setMessage("Error occured generating report");
		}
		return new ResponseEntity<GenericResponseReport>(new GenericResponseReport(genericResponse), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<GenericResponseReport> detailReport(ReportRequest reportRequest) {
		GenericResponseReport genericResponse = new GenericResponseReport();
		List<GenericHeaderResponse> headerlist = null;
		List<GenericHeaderResponse> subHeaderlist = null;
		List<Map<Object, Object>> valueList = null;
		List<String> contactIdList = null;
		Map<String, List<Map<Object, Object>>> callRetryDetMap = null;
		Map<String, String> callerChoice = new LinkedHashMap<>();
		callerChoice.put("1", "Confirmed");
		callerChoice.put("2", "Cancelled");
		callerChoice.put("3", "Reschedule");
		callerChoice.put("0", "No Response");
		try {
			List<Object[]> resultList = campaignDao.getContactDetailReport(reportRequest);
			if (resultList != null && !resultList.isEmpty()) {
				contactIdList = new ArrayList<String>();
				for (Object[] obj : resultList) {
					contactIdList.add(String.valueOf(obj[0]));
				}
				callRetryDetMap = campaignDao.getCallRetryDetail(contactIdList);
				headerlist = new ArrayList<GenericHeaderResponse>();
				subHeaderlist = new ArrayList<GenericHeaderResponse>();
				valueList = new ArrayList<Map<Object, Object>>();
				subHeaderlist.add(new GenericHeaderResponse("Contact Id", "Contact Id"));
				subHeaderlist.add(new GenericHeaderResponse("Customer Mobile Number", "Customer Mobile Number"));
				subHeaderlist.add(new GenericHeaderResponse("Campaign name", "campaignName"));
				/*
				 * subHeaderlist.add(new GenericHeaderResponse("Last 4 digits",
				 * "Last 4 digits")); subHeaderlist.add(new GenericHeaderResponse("Total Due",
				 * "Total Due")); subHeaderlist.add(new GenericHeaderResponse("Minimum Payment",
				 * "Minimum Payment"));
				 */
				subHeaderlist.add(new GenericHeaderResponse("Due Date", "Due Date"));
				subHeaderlist.add(new GenericHeaderResponse("Call Status", "callStatus"));
				//	subHeaderlist.add(new GenericHeaderResponse("Caller Choice", "callerChoice"));
				headerlist.add(new GenericHeaderResponse("Call Detail Report", "", subHeaderlist));
				for (Object[] obj : resultList) {
					Map<Object, Object> valueMap = new LinkedHashMap<>();
					valueMap.put("Contact Id", obj[0]);
					valueMap.put("Customer Mobile Number", obj[4]);
					String campaignName = String.valueOf(obj[2]);
					valueMap.put("campaignName", campaignName);
					/*
					 * valueMap.put("Last 4 digits", obj[3]); valueMap.put("Total Due", obj[5]);
					 * valueMap.put("Minimum Payment", obj[6]);
					 */

					if (campaignName != null && !defaultcampaign.contains(campaignName)) {
						valueMap.put("Due Date", obj[7]);
					} else {
						valueMap.put("Due Date", "");
					}

					valueMap.put("callStatus", obj[9]);
					//	valueMap.put("callerChoice", obj[8] != null ? callerChoice.get(obj[7]) : null);
					Map<Object, Object> callRetryDetail = new LinkedHashMap<>();
					callRetryDetail.put("lastRetryStatus", obj[9]);
					callRetryDetail.put("retryCount", obj[11]);
					if (callRetryDetMap != null && callRetryDetMap.containsKey(String.valueOf(obj[0])))
						callRetryDetail.put("retryHistory", callRetryDetMap.get(String.valueOf(obj[0])));
					else
						callRetryDetail.put("retryHistory", null);
					valueMap.put("callRetryDetail", callRetryDetail);
					valueList.add(valueMap);
				}
				genericResponse.setStatus(200);
				genericResponse.setHeader(headerlist);
				genericResponse.setValue(valueList);
				genericResponse.setMessage("Data fetched sucessfully");
			} else {
				genericResponse.setStatus(200);
				genericResponse.setValue(null);
				genericResponse.setMessage("No data found");
			}
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::detailReport " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue(null);
			genericResponse.setMessage("Error occured generating report");
		}
		return new ResponseEntity<GenericResponseReport>(new GenericResponseReport(genericResponse), HttpStatus.OK);
	}

	public ResponseEntity<InputStreamResource> downloadDetailReport(ReportRequest reportRequest) {
		// String previousVal = null;
		Map<String, List<Map<Object, Object>>> callRetryDetMap = null;
		List<String> contactIdList = null;
		int maxHistoryCount = 0;
		Workbook workbook = new XSSFWorkbook();
		try {
			String currentDirectory = System.getProperty("user.dir");
			Sheet sheet1 = null;
			final String fileName = currentDirectory + "\\Detail_Report.xlsx";
			sheet1 = workbook.createSheet("Detail Report");
			sheet1.setDefaultColumnWidth(12);
			CellStyle style = ExcelUtil.getCellStyleForHeader(workbook);
			CellStyle styleContent = ExcelUtil.getCellStyleForContent(workbook);
			List<Object[]> contactDetList = campaignDao.getContactDetailReport(reportRequest);
			if (contactDetList != null && !contactDetList.isEmpty()) {
				contactIdList = new ArrayList<String>();
				for (Object[] obj : contactDetList) {
					contactIdList.add(String.valueOf(obj[0]));
				}
			}
			callRetryDetMap = campaignDao.getCallRetryDetail(contactIdList);
			for (Map.Entry<String, List<Map<Object, Object>>> entry : callRetryDetMap.entrySet()) {
				if (maxHistoryCount < entry.getValue().size())
					maxHistoryCount = entry.getValue().size();
			}
			int row = 0;
			// Summary View Sheet

			row++;
			Row searchHeaderRow = sheet1.createRow(row);
			ExcelUtil.setCellValue(style, searchHeaderRow, 2, "Report Input Criteria");
			ExcelUtil.frameMerged(new CellRangeAddress(row, row, 2, 3), sheet1, workbook);

			row++;
			searchHeaderRow = sheet1.createRow(row);
			ExcelUtil.setCellValue(style, searchHeaderRow, 2, "Campaign Name");
			ExcelUtil.setCellValue(styleContent, searchHeaderRow, 3, reportRequest.getCampaignName());

			row++;
			searchHeaderRow = sheet1.createRow(row);
			ExcelUtil.setCellValue(style, searchHeaderRow, 2, "Start Date");
			ExcelUtil.setCellValue(styleContent, searchHeaderRow, 3, reportRequest.getStartDate());

			row++;
			searchHeaderRow = sheet1.createRow(row);
			ExcelUtil.setCellValue(style, searchHeaderRow, 2, "End Date");
			ExcelUtil.setCellValue(styleContent, searchHeaderRow, 3, reportRequest.getEndDate());

			/*
			 * row++; searchHeaderRow = sheet1.createRow(row); ExcelUtil.setCellValue(style,
			 * searchHeaderRow, 2, "Customer Name"); ExcelUtil.setCellValue(styleContent,
			 * searchHeaderRow, 3, reportRequest.getDoctorName() == null ? "" : "");
			 */

			row++;
			searchHeaderRow = sheet1.createRow(row);
			ExcelUtil.setCellValue(style, searchHeaderRow, 2, "Contact No.");
			ExcelUtil.setCellValue(styleContent, searchHeaderRow, 3,
					reportRequest.getContactNo() == null ? "" : reportRequest.getContactNo());
			ExcelUtil.setRegionBorderWithMedium(new CellRangeAddress(2, row, 2, 3), sheet1, workbook);

			row = row + 3;
			int firstRow = row;
			Row headerRow = sheet1.createRow(row);
			ExcelUtil.setCellValue(style, headerRow, 0, "Call Detail Report");
			ExcelUtil.frameMerged(new CellRangeAddress(row, row, 0, 4), sheet1, workbook);
			int reportCell = 5;
			for (int i = 1; i <= maxHistoryCount; i++) {
				ExcelUtil.setCellValue(style, headerRow, reportCell, "Retry-" + i);
				ExcelUtil.frameMerged(new CellRangeAddress(row, row, reportCell, reportCell + 1), sheet1, workbook);
				reportCell = reportCell + 2;
			}

			row++;


			Row subHeaderRow = sheet1.createRow(row);
			//ExcelUtil.setCellValue(style, subHeaderRow, 0, "Customer Name");
			ExcelUtil.setCellValue(style, subHeaderRow, 0, "Campaign name");
			//			ExcelUtil.setCellValue(style, subHeaderRow, 2, "Last 4 digits");
			ExcelUtil.setCellValue(style, subHeaderRow, 1, "Customer Mobile Number");
			ExcelUtil.setCellValue(style, subHeaderRow, 2, "Call Status");
			//ExcelUtil.setCellValue(style, subHeaderRow, 3, "Caller Choice");
			ExcelUtil.setCellValue(style, subHeaderRow, 3, "Due Date");
			ExcelUtil.setCellValue(style, subHeaderRow, 4, "Retry Count");
			int column = 4;
			for (int i = 1; i <= maxHistoryCount; i++) {
				ExcelUtil.setCellValue(style, subHeaderRow, column + 1, "Called On");
				ExcelUtil.setCellValue(style, subHeaderRow, column + 2, "Call Status");
				column = column + 2;
			}

			try {
				row++;
				if (contactDetList != null && !contactDetList.isEmpty()) {
					for (Object[] obj : contactDetList) {
						Row subHeaderValue = sheet1.createRow(row);
						//ExcelUtil.setCellValue(styleContent, subHeaderValue, 0, "");

						String campaignName = String.valueOf(obj[2]);
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 0, String.valueOf(obj[2]));
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 1, String.valueOf(obj[4]));
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 2, String.valueOf(obj[9]));
						//						ExcelUtil.setCellValue(styleContent, subHeaderValue, 5, String.valueOf(obj[9]));
						/*
						 * ExcelUtil.setCellValue(styleContent, subHeaderValue, 3, obj[7] != null ?
						 * AppointmentReminderUtil.getCallerChoice(String.valueOf(obj[8])) : null);
						 */

						if (campaignName != null && !defaultcampaign.contains(campaignName)) {
							ExcelUtil.setCellValue(styleContent, subHeaderValue, 3, String.valueOf(obj[7]));
						} else {
							ExcelUtil.setCellValue(styleContent, subHeaderValue, 3, "");
						}

						ExcelUtil.setCellValue(styleContent, subHeaderValue, 4, String.valueOf(obj[11]));
						column = 4;
						if (callRetryDetMap != null && callRetryDetMap.containsKey(String.valueOf(obj[0]))) {
							for (Map<Object, Object> retryHistory : callRetryDetMap.get(String.valueOf(obj[0]))) {
								ExcelUtil.setCellValue(styleContent, subHeaderValue, column + 1,
										String.valueOf(retryHistory.get("date")));
								ExcelUtil.setCellValue(styleContent, subHeaderValue, column + 2,
										(String) retryHistory.get("callStatus"));
								column = column + 2;
							}
						}
						row++;
					}
				}
				ExcelUtil.setRegionBorderWithMedium(
						new CellRangeAddress(firstRow, row - 1, 0, 4 + (maxHistoryCount * 2)), sheet1, workbook);
				sheet1.autoSizeColumn(2, false);
				sheet1.autoSizeColumn(1, true);
				sheet1.autoSizeColumn(3, true);
				sheet1.autoSizeColumn(5, true);
				sheet1.autoSizeColumn(7, true);
				//sheet1.autoSizeColumn(5, false);
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				FileOutputStream outputStream = new FileOutputStream(fileName);
				workbook.write(outputStream);
				workbook.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			File file = new File(fileName);
			InputStreamResource resource1 = null;
			try {
				resource1 = new InputStreamResource(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
					.contentType(MediaType.parseMediaType("application/octet-stream")).contentLength(file.length())
					.body(resource1);

		} catch (Exception e) {
		}
		return null;
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadSummaryReport(ReportRequest reportRequest) {
		// String previousVal = null;
		Workbook workbook = new XSSFWorkbook();
		try {
			String currentDirectory = System.getProperty("user.dir");
			Sheet sheet1 = null;
			final String fileName = currentDirectory + "\\Summary_Report.xlsx";
			sheet1 = workbook.createSheet("Summary Report");
			sheet1.setDefaultColumnWidth(10);
			CellStyle style = ExcelUtil.getCellStyleForHeader(workbook);
			CellStyle styleContent = ExcelUtil.getCellStyleForContent(workbook);
			List<Object[]> contactSummaryList = campaignDao.getSummaryReportDet(reportRequest);

			int row = 0;
			// Summary View Sheet
			row++;
			Row searchHeaderRow = sheet1.createRow(row);
			ExcelUtil.setCellValue(style, searchHeaderRow, 2, "Report Input Criteria");
			ExcelUtil.frameMerged(new CellRangeAddress(row, row, 2, 3), sheet1, workbook);

			row++;
			searchHeaderRow = sheet1.createRow(row);
			ExcelUtil.setCellValue(style, searchHeaderRow, 2, "Campaign Name");
			ExcelUtil.setCellValue(styleContent, searchHeaderRow, 3, reportRequest.getCampaignName());

			row++;
			searchHeaderRow = sheet1.createRow(row);
			ExcelUtil.setCellValue(style, searchHeaderRow, 2, "Start Date");
			ExcelUtil.setCellValue(styleContent, searchHeaderRow, 3, reportRequest.getStartDate());

			row++;
			searchHeaderRow = sheet1.createRow(row);
			ExcelUtil.setCellValue(style, searchHeaderRow, 2, "End Date");
			ExcelUtil.setCellValue(styleContent, searchHeaderRow, 3, reportRequest.getEndDate());
			ExcelUtil.setRegionBorderWithMedium(new CellRangeAddress(2, row, 2, 3), sheet1, workbook);

			row++;
			row++;
			row++;
			Row headerRow = sheet1.createRow(row);
			int firstRow = row;
			ExcelUtil.setCellValue(style, headerRow, 0, "Call Summary Report");
			ExcelUtil.frameMerged(new CellRangeAddress(row, row, 0, 8), sheet1, workbook);
			row++;

			Row subHeaderRow = sheet1.createRow(row);
			ExcelUtil.setCellValue(style, subHeaderRow, 0, "Campaign Name");
			ExcelUtil.setCellValue(style, subHeaderRow, 1, "Appointment Date");
			ExcelUtil.setCellValue(style, subHeaderRow, 2, "List Length");
			//			ExcelUtil.setCellValue(style, subHeaderRow, 3, "Contacts Called");
			ExcelUtil.setCellValue(style, subHeaderRow, 3, "Answered");
			ExcelUtil.setCellValue(style, subHeaderRow, 4, "Ringing No Answer");
			ExcelUtil.setCellValue(style, subHeaderRow, 5, "Busy");
			ExcelUtil.setCellValue(style, subHeaderRow, 6, "Not Reachable");
			//			ExcelUtil.setCellValue(style, subHeaderRow, 7, "Invalid No");
			ExcelUtil.setCellValue(style, subHeaderRow, 7, "DNC");
			ExcelUtil.setCellValue(style, subHeaderRow, 8, "Others");
			//			ExcelUtil.setCellValue(style, subHeaderRow, 10, "Confirmed");
			//			ExcelUtil.setCellValue(style, subHeaderRow, 11, "Cancelled");
			//			ExcelUtil.setCellValue(style, subHeaderRow, 12, "Rescheduled");
			//			ExcelUtil.setCellValue(style, subHeaderRow, 13, "No Response");

			String campaignId = reportRequest.getCampaignId();
			int dncCount = getDNSDetList(getDNCIDusingCampaignID(campaignId)).size();

			try {
				row++;
				if (contactSummaryList != null && !contactSummaryList.isEmpty()) {
					for (Object[] obj : contactSummaryList) {
						Row subHeaderValue = sheet1.createRow(row);
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 0, String.valueOf(obj[0]));
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 1, String.valueOf(obj[1]));
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 2, String.valueOf(obj[2]));
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 3, String.valueOf(obj[4]));
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 4, String.valueOf(obj[5]));
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 5, String.valueOf(obj[6]));
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 6, String.valueOf(obj[7]));
						//						ExcelUtil.setCellValue(styleContent, subHeaderValue, 7, String.valueOf(obj[8]));
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 7, String.valueOf(obj[8]));
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 8, String.valueOf(obj[10]));
						//						ExcelUtil.setCellValue(styleContent, subHeaderValue, 10, String.valueOf(obj[7]));
						//						ExcelUtil.setCellValue(styleContent, subHeaderValue, 11, String.valueOf(obj[8]));
						//						ExcelUtil.setCellValue(styleContent, subHeaderValue, 12, String.valueOf(obj[9]));
						//						ExcelUtil.setCellValue(styleContent, subHeaderValue, 13, String.valueOf(obj[10]));
						row++;
					}
				}
				ExcelUtil.setRegionBorderWithMedium(new CellRangeAddress(firstRow, row - 1, 0, 8), sheet1, workbook);
				sheet1.autoSizeColumn(2, false);
				sheet1.autoSizeColumn(3, false);
				sheet1.autoSizeColumn(6, false);
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				FileOutputStream outputStream = new FileOutputStream(fileName);
				workbook.write(outputStream);
				workbook.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			File file = new File(fileName);
			InputStreamResource resource1 = null;
			try {
				resource1 = new InputStreamResource(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
					.contentType(MediaType.parseMediaType("application/octet-stream")).contentLength(file.length())
					.body(resource1);

		} catch (Exception e) {
		}
		return null;
	}

	@Override
	public ResponseEntity<GenericResponse> getUploadHistory(ReportRequest reportRequest) {
		GenericResponse genericResponse = new GenericResponse();
		List<Object[]> resultList = null;
		List<UploadHistoryDto> uploadHistoryList = null;
		try {
			resultList = campaignDao.getUploadHistory(reportRequest);
			if (resultList != null && !resultList.isEmpty()) {
				uploadHistoryList = new ArrayList<>();
				for (Object[] obj : resultList) {
					UploadHistoryDto uploadHistoryDto = new UploadHistoryDto();
					uploadHistoryDto.setUploadHistoryId(String.valueOf(obj[0]));
					uploadHistoryDto.setCampaignId(String.valueOf(obj[1]));
					uploadHistoryDto.setCampaignName(String.valueOf(obj[2]));
					uploadHistoryDto.setUploadedOn(String.valueOf(obj[3]));
					uploadHistoryDto.setFilename(String.valueOf(obj[4]));
					uploadHistoryDto
					.setContactUploaded(campaignDao.getTotalContactNo(uploadHistoryDto.getUploadHistoryId()));
					uploadHistoryList.add(uploadHistoryDto);
				}
			}
			genericResponse.setStatus(200);
			genericResponse.setValue(uploadHistoryList);
			genericResponse.setMessage("Success");
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::createCampaign " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("No data Found");
		}

		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<GenericResponse> deleteContactByHistory(UpdateCallDetRequest updateCallDetRequest) {
		GenericResponse genericResponse = new GenericResponse();
		try {
			boolean isDeleted = campaignDao.deleteContactByHistory(updateCallDetRequest);
			if (isDeleted) {
				genericResponse.setStatus(200);
				genericResponse.setValue("Success");
				genericResponse.setMessage("Contact deleted successfully");
			} else {
				genericResponse.setStatus(400);
				genericResponse.setValue("Failure");
				genericResponse.setMessage("Error occured while deleting contact details");
			}
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::deleteContactByHistory " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("Error occured deleting contact details");
		}

		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}

	@Override
	public BigInteger insertUploadHistory(UploadHistoryDto uploadHistoryDto) {
		BigInteger historyId = null;
		try {
			historyId = campaignDao.insertUploadHistory(uploadHistoryDto);
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::deleteContactByHistory " + e);
			return historyId;
		}
		return historyId;
	}

	@Override
	public List<CustomerDataDto> getCustomerData() {
		return campaignDao.getCustomerData();
	}

	@Override
	public ResponseEntity<GenericResponse> getRetryReport(ReportRequest reportRequest) {
		GenericResponse genericResponse = new GenericResponse();
		try {
			RetryCountDto retryCountDto = campaignDao.getRetryReport(reportRequest);

			if (retryCountDto != null) {
				genericResponse.setStatus(200);
				genericResponse.setValue(retryCountDto);
				genericResponse.setMessage("Contact fetched successfully");
			}

		} catch (Exception e) {
			logger.error("Error occured in CampaignServiceImpl::getRetryReport" + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("Error occured while fetching the report details");
		}
		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<GenericResponse> getRetryReport(ReportRequest reportRequest, String userGroup) throws Exception {
		GenericResponse genericResponse = new GenericResponse();
		try {
			RetryCountDto retryCountDto = campaignDao.getRetryReport(reportRequest, userGroup);

			if (retryCountDto != null) {
				genericResponse.setStatus(200);
				genericResponse.setValue(retryCountDto);
				genericResponse.setMessage("Contact fetched successfully");
			}

		} catch (Exception e) {
			logger.error("Error occured in CampaignServiceImpl::getRetryReport" + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("Error occured while fetching the report details");
		}
		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<GenericResponseReport> getLeadWiseSummary(ReportRequest reportRequest) {
		GenericResponseReport genericResponse = new GenericResponseReport();
		List<GenericHeaderResponse> headerlist = null;
		List<GenericHeaderResponse> subHeaderlist = null;
		List<Map<Object, Object>> valueList = null;
		try {
			List<Object[]> resultList = campaignDao.getLeadWiseSummary(reportRequest);
			if (resultList != null && !resultList.isEmpty()) {
				headerlist = new ArrayList<GenericHeaderResponse>();
				subHeaderlist = new ArrayList<GenericHeaderResponse>();
				valueList = new ArrayList<Map<Object, Object>>();
				subHeaderlist.add(new GenericHeaderResponse("Total Contact", "totalContact"));
				subHeaderlist.add(new GenericHeaderResponse("Contacts Called", "contactCalled"));
				//subHeaderlist.add(new GenericHeaderResponse("Contacts Connected", "contactConnected"));
				subHeaderlist.add(new GenericHeaderResponse("Ring No Answered", "answered"));
				subHeaderlist.add(new GenericHeaderResponse("Busy", "busy"));
				subHeaderlist.add(new GenericHeaderResponse("Not Answered", "notanswered"));
				subHeaderlist.add(new GenericHeaderResponse("Others", "others"));
				/*
				 * subHeaderlist.add(new GenericHeaderResponse("Confirmed", "confirmed"));
				 * subHeaderlist.add(new GenericHeaderResponse("Cancelled", "canceleld"));
				 * subHeaderlist.add(new GenericHeaderResponse("Rescheduled", "rescheduled"));
				 * subHeaderlist.add(new GenericHeaderResponse("No Response", "noResponse"));
				 */
				headerlist.add(new GenericHeaderResponse("Lead Wise Summary Report", "", subHeaderlist));
				for (Object[] obj : resultList) {
					Map<Object, Object> valueMap = new LinkedHashMap<>();
					valueMap.put("totalContact", obj[0]);
					valueMap.put("contactCalled", obj[1]);
					//	valueMap.put("contactConnected", obj[2]);
					valueMap.put("answered", obj[3]);
					valueMap.put("busy", obj[4]);
					valueMap.put("notanswered", obj[5]);
					valueMap.put("others", obj[10]);
					/*
					 * valueMap.put("confirmed", obj[7]); valueMap.put("canceleld", obj[8]);
					 * valueMap.put("rescheduled", obj[9]); valueMap.put("noResponse", obj[10]);
					 */
					valueList.add(valueMap);
				}
				genericResponse.setStatus(200);
				genericResponse.setHeader(headerlist);
				genericResponse.setValue(valueList);
				genericResponse.setMessage("Data fetched sucessfully");
			} else {
				genericResponse.setStatus(200);
				genericResponse.setValue(null);
				genericResponse.setMessage("No data found");
			}
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::summaryReport " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue(null);
			genericResponse.setMessage("Error occured generating report");
		}
		return new ResponseEntity<GenericResponseReport>(new GenericResponseReport(genericResponse), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<GenericResponseReport> getLeadWiseSummary(ReportRequest reportRequest, String userGroup) {
		GenericResponseReport genericResponse = new GenericResponseReport();
		List<GenericHeaderResponse> headerlist = null;
		List<GenericHeaderResponse> subHeaderlist = null;
		List<Map<Object, Object>> valueList = null;
		try {
			List<Object[]> resultList = campaignDao.getLeadWiseSummary(reportRequest, userGroup);
			if (resultList != null && !resultList.isEmpty()) {
				headerlist = new ArrayList<GenericHeaderResponse>();
				subHeaderlist = new ArrayList<GenericHeaderResponse>();
				valueList = new ArrayList<Map<Object, Object>>();
				subHeaderlist.add(new GenericHeaderResponse("Total Contact", "totalContact"));
				subHeaderlist.add(new GenericHeaderResponse("Contacts Called", "contactCalled"));
				//subHeaderlist.add(new GenericHeaderResponse("Contacts Connected", "contactConnected"));
				subHeaderlist.add(new GenericHeaderResponse("Ring No Answered", "answered"));
				subHeaderlist.add(new GenericHeaderResponse("Busy", "busy"));
				subHeaderlist.add(new GenericHeaderResponse("Not Answered", "notanswered"));
				subHeaderlist.add(new GenericHeaderResponse("Others", "others"));
				/*
				 * subHeaderlist.add(new GenericHeaderResponse("Confirmed", "confirmed"));
				 * subHeaderlist.add(new GenericHeaderResponse("Cancelled", "canceleld"));
				 * subHeaderlist.add(new GenericHeaderResponse("Rescheduled", "rescheduled"));
				 * subHeaderlist.add(new GenericHeaderResponse("No Response", "noResponse"));
				 */
				headerlist.add(new GenericHeaderResponse("Lead Wise Summary Report", "", subHeaderlist));
				for (Object[] obj : resultList) {
					Map<Object, Object> valueMap = new LinkedHashMap<>();
					valueMap.put("totalContact", obj[0]);
					valueMap.put("contactCalled", obj[1]);
					//	valueMap.put("contactConnected", obj[2]);
					valueMap.put("answered", obj[3]);
					valueMap.put("busy", obj[4]);
					valueMap.put("notanswered", obj[5]);
					valueMap.put("others", obj[10]);
					/*
					 * valueMap.put("confirmed", obj[7]); valueMap.put("canceleld", obj[8]);
					 * valueMap.put("rescheduled", obj[9]); valueMap.put("noResponse", obj[10]);
					 */
					valueList.add(valueMap);
				}
				genericResponse.setStatus(200);
				genericResponse.setHeader(headerlist);
				genericResponse.setValue(valueList);
				genericResponse.setMessage("Data fetched sucessfully");
			} else {
				genericResponse.setStatus(200);
				genericResponse.setValue(null);
				genericResponse.setMessage("No data found");
			}
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::summaryReport " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue(null);
			genericResponse.setMessage("Error occured generating report");
		}
		return new ResponseEntity<GenericResponseReport>(new GenericResponseReport(genericResponse), HttpStatus.OK);
	}


	@Override
	public ResponseEntity<GenericResponseReport> getCallVolumeReport(ReportRequest reportRequest) {
		GenericResponseReport genericResponse = new GenericResponseReport();
		List<GenericHeaderResponse> headerlist = null;
		List<GenericHeaderResponse> subHeaderlist = null;
		List<Map<Object, Object>> valueList = null;
		try {
			List<Object[]> resultList = campaignDao.getCallVolumeReport(reportRequest);
			if (resultList != null && !resultList.isEmpty()) {
				headerlist = new ArrayList<GenericHeaderResponse>();
				subHeaderlist = new ArrayList<GenericHeaderResponse>();
				valueList = new ArrayList<Map<Object, Object>>();
				subHeaderlist.add(new GenericHeaderResponse("Campaign Date", "date"));
				subHeaderlist.add(new GenericHeaderResponse("Total Contact", "totalContact"));
				subHeaderlist.add(new GenericHeaderResponse("Contacts Called", "contactCalled"));
				subHeaderlist.add(new GenericHeaderResponse("Contacts Connected", "contactConnected"));
				subHeaderlist.add(new GenericHeaderResponse("Ring No Answered", "answered"));
				subHeaderlist.add(new GenericHeaderResponse("Busy", "busy"));
				subHeaderlist.add(new GenericHeaderResponse("Not Answered", "notanswered"));
				subHeaderlist.add(new GenericHeaderResponse("Others", "others"));
				/*
				 * subHeaderlist.add(new GenericHeaderResponse("Confirmed", "confirmed"));
				 * subHeaderlist.add(new GenericHeaderResponse("Cancelled", "canceleld"));
				 * subHeaderlist.add(new GenericHeaderResponse("Rescheduled", "rescheduled"));
				 * subHeaderlist.add(new GenericHeaderResponse("No Response", "noResponse"));
				 */
				headerlist.add(new GenericHeaderResponse("Lead Wise Summary Report", "", subHeaderlist));
				for (Object[] obj : resultList) {
					Map<Object, Object> valueMap = new LinkedHashMap<>();
					valueMap.put("date", obj[0]);
					valueMap.put("totalContact", obj[1]);
					valueMap.put("contactCalled", obj[2]);
					valueMap.put("contactConnected", obj[3]);
					valueMap.put("answered", obj[4]);
					valueMap.put("busy", obj[5]);
					valueMap.put("notanswered", obj[6]);
					valueMap.put("others", obj[11]);
					/*
					 * valueMap.put("confirmed", obj[7]); valueMap.put("canceleld", obj[8]);
					 * valueMap.put("rescheduled", obj[9]); valueMap.put("noResponse", obj[10]);
					 */
					valueList.add(valueMap);
				}
				genericResponse.setStatus(200);
				genericResponse.setHeader(headerlist);
				genericResponse.setValue(valueList);
				genericResponse.setMessage("Data fetched sucessfully");
			} else {
				genericResponse.setStatus(200);
				genericResponse.setValue(null);
				genericResponse.setMessage("No data found");
			}
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::summaryReport " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue(null);
			genericResponse.setMessage("Error occured generating report");
		}
		return new ResponseEntity<GenericResponseReport>(new GenericResponseReport(genericResponse), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<GenericResponseReport> getCallVolumeReport(ReportRequest reportRequest, String userGroup) {
		GenericResponseReport genericResponse = new GenericResponseReport();
		List<GenericHeaderResponse> headerlist = null;
		List<GenericHeaderResponse> subHeaderlist = null;
		List<Map<Object, Object>> valueList = null;
		try {
			List<Object[]> resultList = campaignDao.getCallVolumeReport(reportRequest, userGroup);
			if (resultList != null && !resultList.isEmpty()) {
				headerlist = new ArrayList<GenericHeaderResponse>();
				subHeaderlist = new ArrayList<GenericHeaderResponse>();
				valueList = new ArrayList<Map<Object, Object>>();
				subHeaderlist.add(new GenericHeaderResponse("Campaign Date", "date"));
				subHeaderlist.add(new GenericHeaderResponse("Total Contact", "totalContact"));
				subHeaderlist.add(new GenericHeaderResponse("Contacts Called", "contactCalled"));
				subHeaderlist.add(new GenericHeaderResponse("Contacts Connected", "contactConnected"));
				subHeaderlist.add(new GenericHeaderResponse("Ring No Answered", "answered"));
				subHeaderlist.add(new GenericHeaderResponse("Busy", "busy"));
				subHeaderlist.add(new GenericHeaderResponse("Not Answered", "notanswered"));
				subHeaderlist.add(new GenericHeaderResponse("Others", "others"));
				/*
				 * subHeaderlist.add(new GenericHeaderResponse("Confirmed", "confirmed"));
				 * subHeaderlist.add(new GenericHeaderResponse("Cancelled", "canceleld"));
				 * subHeaderlist.add(new GenericHeaderResponse("Rescheduled", "rescheduled"));
				 * subHeaderlist.add(new GenericHeaderResponse("No Response", "noResponse"));
				 */
				headerlist.add(new GenericHeaderResponse("Lead Wise Summary Report", "", subHeaderlist));
				for (Object[] obj : resultList) {
					Map<Object, Object> valueMap = new LinkedHashMap<>();
					valueMap.put("date", obj[0]);
					valueMap.put("totalContact", obj[1]);
					valueMap.put("contactCalled", obj[2]);
					valueMap.put("contactConnected", obj[3]);
					valueMap.put("answered", obj[4]);
					valueMap.put("busy", obj[5]);
					valueMap.put("notanswered", obj[6]);
					valueMap.put("others", obj[11]);
					/*
					 * valueMap.put("confirmed", obj[7]); valueMap.put("canceleld", obj[8]);
					 * valueMap.put("rescheduled", obj[9]); valueMap.put("noResponse", obj[10]);
					 */
					valueList.add(valueMap);
				}
				genericResponse.setStatus(200);
				genericResponse.setHeader(headerlist);
				genericResponse.setValue(valueList);
				genericResponse.setMessage("Data fetched sucessfully");
			} else {
				genericResponse.setStatus(200);
				genericResponse.setValue(null);
				genericResponse.setMessage("No data found");
			}
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::summaryReport " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue(null);
			genericResponse.setMessage("Error occured generating report");
		}
		return new ResponseEntity<GenericResponseReport>(new GenericResponseReport(genericResponse), HttpStatus.OK);

	}


	@Override
	public boolean createDummyContact(ContactDetDto contactDetDto) {
		boolean isCreated;
		try {
			isCreated = campaignDao.createDummyContact(contactDetDto);
		} catch (Exception e) {
			return false;
		}
		return isCreated;
	}

	@Override
	public boolean getCampaignStatus(CampaignStatus campaignStatus) {
		boolean status = false;
		try {
			status = campaignDao.getCampaignStatus(campaignStatus);
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::getCampaignStatus " + e);

		}
		return status;
	}

	@Override
	public boolean getCampaignActiveStatus(CampaignDetRequest campaignActive) {
		boolean status = false;
		try {
			status = campaignDao.getCampaignActiveStatus(campaignActive);
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::getCampaignActiveStatus " + e);

		}
		return status;
	}


	@Override
	public ResponseEntity<GenericResponse> getRealTimeDashboard(String userGroup) { //UG
		GenericResponse genericResponse = new GenericResponse();
		List<CampaignRealTimeDashboard> campaignRTList = null;
		try {
			campaignRTList = getRealTimeData(userGroup);
			genericResponse.setStatus(200);
			genericResponse.setValue(campaignRTList);
			genericResponse.setMessage("Success");
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::createCampaign " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("No data Found");
		}

		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}

	//	@Override
	//	public List<CampaignRealTimeDashboard> getRealTimeData() throws Exception {
	//		List<CampaignRealTimeDashboard> campaignRTList=null;
	//		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	//		SimpleDateFormat sdfTime=new SimpleDateFormat("HH:mm:ss");
	//		try {
	//			campaignRTList = new ArrayList<>();
	//			List<Object[]> campainDetObjList = campaignDao.getCampaignDetForRT();
	//			if (campainDetObjList != null && !campainDetObjList.isEmpty()) {
	//				for (Object[] obj : campainDetObjList) {
	//
	//					CampaignRealTimeDashboard campaignRT=new CampaignRealTimeDashboard();
	//					String campaignID=String.valueOf(obj[0]);
	//					campaignRT.setCampaignId(String.valueOf(obj[0]));
	//					campaignRT.setCampaignName(String.valueOf(obj[1]));
	//					campaignRT.setCampaignStatus(String.valueOf(obj[2]));
	//					String status=null;
	//					String startDate=String.valueOf(obj[3]);
	//					String EndDate=String.valueOf(obj[4]);
	//					String startTime=String.valueOf(obj[5]);
	//					String endTime=String.valueOf(obj[6]);
	//					String conCall=String.valueOf(obj[7]);
	//					String dncID=String.valueOf(obj[8]);
	//
	//					Integer intConcall=Integer.parseInt(conCall);
	//					String currDate=sdf.format(new Date());
	//					String currTime=sdfTime.format(new Date());
	//					logger.info("Start Date : "+startDate+ " :: End Date"+EndDate);
	//					logger.info("Start Time : "+startTime+ " :: End Time"+endTime);
	//					logger.info("Current Date : "+currDate+ " :: Current Time"+currTime);
	//					campaignRT.setStartDate(startDate+" "+startTime);
	//					campaignRT.setEndDate(EndDate+" "+endTime);
	//					Integer activeCall=campaignDao.getActiveContDetails(campaignID);
	//					campaignRT.setOncall(activeCall);
	//					campaignRT.setTotalline(intConcall-activeCall);
	//
	//					Date startDat=sdf.parse(startDate);
	//					Date endDat=sdf.parse(EndDate);
	//					Date currDat=sdf.parse(currDate);
	//					Date startTim=sdfTime.parse(startTime);
	//					Date endTim=sdfTime.parse(endTime);
	//					Date currTim=sdfTime.parse(currTime);
	//					if((currDat.after(startDat) || currDat.equals(startDat)) && (currDat.before(endDat) || currDat.equals(endDat))) {
	//						if((currTim.after(startTim)||currTim.equals(startTim)) && (currTim.before(endTim))){
	//							status="Running";
	//						}else {
	//							status="Paused";
	//						}
	//					}else if(currDat.before(startDat)){
	//						status="Ready";
	//					}else if(currDat.after(endDat)) {
	//						status="Completed";
	//					}
	//					campaignRT.setCampaignStatus(status);
	//					Integer contactLength=campaignDao.getCampaignBasedContactCount(campaignID);
	//					campaignRT.setListLength(String.valueOf(contactLength));
	//					Integer callpending=campaignDao.getCampaginBasedContactStatus(campaignID,"NEW");
	//					campaignRT.setPending(callpending);
	//					Integer callcompleted=contactLength-callpending;
	//					campaignRT.setCompleted(callcompleted);
	//					//
	//					Integer answeredCount=campaignDao.getCampaginBasedContactStatus(campaignID,"ANSWERED");
	//					campaignRT.setAnswered(answeredCount);
	//					Integer busyCount=campaignDao.getCampaginBasedContactStatus(campaignID,"BUSY");
	//					campaignRT.setLinebusy(busyCount);
	//					Integer NoAnswerCount=campaignDao.getCampaginBasedContactStatus(campaignID,"NOANSWER");
	//					campaignRT.setNoanswer(NoAnswerCount);
	//					String[] errorCodes=errorcodes.split(",");
	//					Integer errorCounts=campaignDao.getActiveContErrorDetails(campaignID, errorCodes);
	//					campaignRT.setError(errorCounts);
	//
	//					campaignRT.setETC(getETC(campaignID, startTim, currTim, endTim,contactLength, callcompleted, callpending,startDat,endDat,currDat));
	//
	//					campaignRT.setDND(campaignDao.getCampBasedDNCSize(dncID));
	//
	//
	//					campaignRTList.add(campaignRT);
	//
	//
	//
	//				}
	//			}
	//		}catch(Exception e) {
	//			logger.error("Error in CampaignServiceImpl::getRealTimeData " + e);
	//		}
	//		return campaignRTList;
	//	}

	//	@Override
	//	public List<CampaignRealTimeDashboard> getRealTimeData(String userGroup) throws Exception { //UG
	//		List<CampaignRealTimeDashboard> campaignRTList=null;
	//		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	//		SimpleDateFormat sdfTime=new SimpleDateFormat("HH:mm:ss");
	//		try {
	//			campaignRTList = new ArrayList<>();
	//			List<Object[]> campainDetObjList = campaignDao.getCampaignDetForRT(userGroup);
	//			if (campainDetObjList != null && !campainDetObjList.isEmpty()) {
	//				for (Object[] obj : campainDetObjList) {
	//
	//					CampaignRealTimeDashboard campaignRT=new CampaignRealTimeDashboard();
	//					String campaignID=String.valueOf(obj[0]);
	//					campaignRT.setCampaignId(String.valueOf(obj[0]));
	//					CampaignStatus campaignRTStatus=new CampaignStatus();
	//					campaignRTStatus.setCampaignId(campaignID);
	//					String frontStatus=getFrontCampStatus(campaignRTStatus);
	//					if(frontStatus!=null && !(frontStatus.equalsIgnoreCase("notready")|| frontStatus.equalsIgnoreCase("start"))){
	//						campaignRT.setCampaignId(String.valueOf(obj[0]));
	//						campaignRT.setCampaignName(String.valueOf(obj[1]));
	//						campaignRT.setCampaignStatus(String.valueOf(obj[2]));
	//						String status=null;
	//						String startDate=String.valueOf(obj[3]);
	//						String EndDate=String.valueOf(obj[4]);
	//						String startTime=String.valueOf(obj[5]);
	//						String endTime=String.valueOf(obj[6]);
	//						String conCall=String.valueOf(obj[7]);
	//						String dncID=String.valueOf(obj[8]);
	//						Integer camRetryCount=Integer.parseInt(String.valueOf(obj[9]));
	//
	//						Integer intConcall=Integer.parseInt(conCall);
	//						String currDate=sdf.format(new Date());
	//						String currTime=sdfTime.format(new Date());
	//						logger.info("Start Date : "+startDate+ " :: End Date"+EndDate);
	//						logger.info("Start Time : "+startTime+ " :: End Time"+endTime);
	//						logger.info("Current Date : "+currDate+ " :: Current Time"+currTime);
	//						String campaignName=String.valueOf(obj[1]);
	//						logger.info("Campaign Name : "+campaignName);
	//						campaignRT.setStartDate(startDate+" "+startTime);
	//						//campaignRT.setEndDate(EndDate+" "+endTime);
	//						Integer activeCall=campaignDao.getActiveContDetails(campaignName);
	//						campaignRT.setOncall(activeCall);
	//						campaignRT.setTotalline(intConcall-activeCall);
	//
	//						Date startDat=sdf.parse(startDate);
	//						Date endDat=sdf.parse(EndDate);
	//						Date currDat=sdf.parse(currDate);
	//						Date startTim=sdfTime.parse(startTime);
	//						Date endTim=sdfTime.parse(endTime);
	//						Date currTim=sdfTime.parse(currTime);
	//						/*
	//						 * if((currDat.after(startDat) || currDat.equals(startDat)) &&
	//						 * (currDat.before(endDat) || currDat.equals(endDat))) {
	//						 * if((currTim.after(startTim)||currTim.equals(startTim)) &&
	//						 * (currTim.before(endTim))){ status="Running"; }else { status="Paused"; } }else
	//						 * if(currDat.before(startDat)){ status="Ready"; }else if(currDat.after(endDat))
	//						 * { status="Completed"; }
	//						 */
	//
	//						CampaignStatus campaignStatus=new CampaignStatus();
	//						campaignStatus.setCampaignId(campaignID);
	//						status=campaignDao.getFrontCampaignStatus(campaignStatus);
	//
	//						campaignRT.setCampaignStatus(status);
	//						Integer contactLength=campaignDao.getCampaignBasedContactCount(campaignName);
	//						campaignRT.setListLength(String.valueOf(contactLength));
	//						//Integer callpending=campaignDao.getCampaginBasedContactStatus(campaignName,"NEW");
	//						//Integer callcompleted=contactLength-callpending;
	//						Integer answeredCount=campaignDao.getCampaginBasedContactStatus(campaignName,"ANSWERED");
	//						campaignRT.setAnswered(answeredCount);
	//						Integer callcompletedNew=campaignDao.getCompletedCountDetails(campaignName,camRetryCount);
	//						callcompletedNew=callcompletedNew+answeredCount;
	//						logger.info("callcompletedNew :: "+callcompletedNew + "answeredCount :: "+answeredCount);
	//						campaignRT.setCompleted(callcompletedNew);
	//						Integer dncCount=campaignDao.getCampBasedDNCSize(dncID);
	//						campaignRT.setDND(campaignDao.getCampBasedDNCSize(dncID));
	//						logger.info("DNC count based on campaign :: "+dncCount);
	//						//Integer intPending = null;
	//						//Integer callpending=null;
	//						/*
	//						 * if(dncCount>0) { intPending =callcompletedNew-dncCount;
	//						 * callpending=contactLength-intPending;
	//						 * logger.info("Pending count excluding dnc count:: "+ callpending); } else {
	//						 * callpending=contactLength-callcompletedNew;
	//						 * logger.info("Pending count without DNC count :: "+ callpending); }
	//						 */
	//						Integer callpending=contactLength-callcompletedNew;
	//						logger.info("Pending count :: "+ callpending);
	//						campaignRT.setPending(callpending);
	//						Integer busyCount=campaignDao.getCampaginBasedContactStatus(campaignName,"BUSY");
	//						campaignRT.setLinebusy(busyCount);
	//						Integer NoAnswerCount=campaignDao.getCampaginBasedContactStatus(campaignName,"NO ANSWER");
	//						campaignRT.setNoanswer(NoAnswerCount);
	//						String[] errorCodes=errorcodes.split(",");
	//						Integer errorCounts=campaignDao.getActiveContErrorDetails(campaignName, errorCodes);
	//						campaignRT.setError(errorCounts);
	//						if(contactLength==callcompletedNew)
	//						{
	//							String campComTime=campaignDao.getCampaignCompletedTimeRT(campaignID);
	//							campaignRT.setEndDate(campComTime);
	//						}
	//						//	campaignRT.setETC(getETC(campaignID, startTim, currTim, endTim,contactLength, callcompletedNew, callpending,startDat,endDat,currDat));
	//						Map<String,Integer> etcMap=getETC(campaignID);
	//						if(etcMap!=null) {
	//							campaignRT.setExecutedCount(etcMap.get("TotalCount"));
	//							campaignRT.setExecutedDuration(etcMap.get("Totalduration"));
	//						}
	//
	//						campaignRTList.add(campaignRT);
	//						logger.info("Campaign RealTime List data :: "+campaignRTList);
	//					}
	//					}
	//				}
	//			}catch(Exception e) {
	//				logger.error("Error in CampaignServiceImpl::getRealTimeData " + e);
	//			}
	//			return campaignRTList;
	//		}
	//
	//
	//		@Override
	//		public List<CampaignRealTimeDashboard> getRealTimeDashboard() { //UG
	//			List<CampaignRealTimeDashboard> campaignRTList=null;
	//			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	//			SimpleDateFormat sdfTime=new SimpleDateFormat("HH:mm:ss");
	//			try {
	//				campaignRTList = new ArrayList<>();
	//				List<Object[]> campainDetObjList = campaignDao.getCampaignDetForRT();
	//				if (campainDetObjList != null && !campainDetObjList.isEmpty()) {
	//					for (Object[] obj : campainDetObjList) {
	//						CampaignRealTimeDashboard campaignRT=new CampaignRealTimeDashboard();
	//						String campaignID=String.valueOf(obj[0]);
	//						CampaignStatus campaignRTStatus=new CampaignStatus();
	//						campaignRTStatus.setCampaignId(campaignID);
	//						String frontStatus=getFrontCampStatus(campaignRTStatus);
	//						if(frontStatus!=null && !(frontStatus.equalsIgnoreCase("notready")|| frontStatus.equalsIgnoreCase("start"))){
	//
	//							campaignRT.setCampaignId(String.valueOf(obj[0]));
	//							campaignRT.setCampaignName(String.valueOf(obj[1]));
	//							campaignRT.setCampaignStatus(String.valueOf(obj[2]));
	//							String status=null;
	//							String startDate=String.valueOf(obj[3]);
	//							String EndDate=String.valueOf(obj[4]);
	//							String startTime=String.valueOf(obj[5]);
	//							String endTime=String.valueOf(obj[6]);
	//							String conCall=String.valueOf(obj[7]);
	//							String dncID=String.valueOf(obj[8]);
	//							Integer camRetryCount=Integer.parseInt(String.valueOf(obj[9]));
	//							if(conCall==null) {
	//								conCall="0";
	//							}
	//							Integer intConcall=Integer.parseInt(conCall);
	//							String currDate=sdf.format(new Date());
	//							String currTime=sdfTime.format(new Date());
	//							logger.info("Start Date : "+startDate+ " :: End Date"+EndDate);
	//							logger.info("Start Time : "+startTime+ " :: End Time"+endTime);
	//							logger.info("Current Date : "+currDate+ " :: Current Time"+currTime);
	//							String campaignName=String.valueOf(obj[1]);
	//							logger.info("Campaign Name : "+campaignName);
	//							campaignRT.setStartDate(startDate+" "+startTime);
	//							//campaignRT.setEndDate(EndDate+" "+endTime);
	//							Integer activeCall=campaignDao.getActiveContDetails(campaignName);
	//							campaignRT.setOncall(activeCall);
	//							campaignRT.setTotalline(intConcall-activeCall);
	//
	//							Date startDat=sdf.parse(startDate);
	//							Date endDat=sdf.parse(EndDate);
	//							Date currDat=sdf.parse(currDate);
	//							Date startTim=sdfTime.parse(startTime);
	//							Date endTim=sdfTime.parse(endTime);
	//							Date currTim=sdfTime.parse(currTime);
	//							/*
	//							 * if((currDat.after(startDat) || currDat.equals(startDat)) &&
	//							 * (currDat.before(endDat) || currDat.equals(endDat))) {
	//							 * if((currTim.after(startTim)||currTim.equals(startTim)) &&
	//							 * (currTim.before(endTim))){ status="Running"; }else { status="Paused"; } }else
	//							 * if(currDat.before(startDat)){ status="Ready"; }else if(currDat.after(endDat))
	//							 * { status="Completed"; }
	//							 */
	//
	//							CampaignStatus campaignStatus=new CampaignStatus();
	//							campaignStatus.setCampaignId(campaignID);
	//							status=campaignDao.getFrontCampaignStatus(campaignStatus);
	//							campaignRT.setCampaignStatus(status);
	//							Integer contactLength=campaignDao.getCampaignBasedContactCount(campaignName);
	//							campaignRT.setListLength(String.valueOf(contactLength));
	//							Integer answeredCount=campaignDao.getCampaginBasedContactStatus(campaignName,"ANSWERED");
	//							campaignRT.setAnswered(answeredCount);
	//							Integer callcompletedNew=campaignDao.getCompletedCountDetails(campaignName,camRetryCount);
	//							callcompletedNew=callcompletedNew+answeredCount;
	//							campaignRT.setCompleted(callcompletedNew);
	//							logger.info("callcompletedNew :: "+callcompletedNew + "answeredCount :: "+answeredCount);
	//							Integer dncCount=campaignDao.getCampBasedDNCSize(dncID);
	//							campaignRT.setDND(campaignDao.getCampBasedDNCSize(dncID));
	//							logger.info("DNC count based on campaign :: "+dncCount);
	//							Integer callpending=contactLength-callcompletedNew;
	//							logger.info("Pending count :: "+ callpending);
	//							//	Integer callpending=contactLength-callcompletedNew;
	//							campaignRT.setPending(callpending);
	//							Integer busyCount=campaignDao.getCampaginBasedContactStatus(campaignName,"BUSY");
	//							campaignRT.setLinebusy(busyCount);
	//							Integer NoAnswerCount=campaignDao.getCampaginBasedContactStatus(campaignName,"NO ANSWER");
	//							campaignRT.setNoanswer(NoAnswerCount);
	//							String[] errorCodes=errorcodes.split(",");
	//							Integer errorCounts=campaignDao.getActiveContErrorDetails(campaignName, errorCodes);
	//							campaignRT.setError(errorCounts);
	//							if(contactLength==callcompletedNew)
	//							{
	//								String campComTime=campaignDao.getCampaignCompletedTimeRT(campaignID);
	//								campaignRT.setEndDate(campComTime);
	//							}
	//							//	campaignRT.setETC(getETC(campaignID, startTim, currTim, endTim,contactLength, callcompletedNew, callpending,startDat,endDat,currDat));
	//
	//							Map<String,Integer> etcMap=getETC(campaignID);
	//							if(etcMap!=null) {
	//								campaignRT.setExecutedCount(etcMap.get("TotalCount"));
	//								Integer intExeDur=etcMap.get("Totalduration");
	//								Integer intExeDurInMin;
	//								if(intExeDur==null) {
	//									intExeDur=0;
	//									intExeDurInMin=0;
	//								}else {
	//									intExeDurInMin=intExeDur/60;
	//								}
	//								campaignRT.setExecutedDuration(intExeDurInMin);
	//							}
	//							campaignRTList.add(campaignRT);
	//							logger.info("Campaign RealTime List data :: "+campaignRTList);
	//
	//						}
	//					}
	//				}
	//			}catch(Exception e) {
	//				StringWriter str=new StringWriter();
	//				e.printStackTrace(new PrintWriter(str));
	//				logger.error("Error in CampaignServiceImpl::getRealTimeData " + str.toString());
	//			}
	//			return campaignRTList;
	//		}

	@Override
	public List<CampaignRealTimeDashboard> getRealTimeData(String userGroup) throws Exception { //UG
		List<CampaignRealTimeDashboard> campaignRTList = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
		try {
			campaignRTList = new ArrayList<>();
			List<Object[]> campainDetObjList = campaignDao.getCampaignDetForRT(userGroup);
			if (campainDetObjList != null && !campainDetObjList.isEmpty()) {
				for (Object[] obj : campainDetObjList) {

					CampaignRealTimeDashboard campaignRT = new CampaignRealTimeDashboard();
					String campaignID = String.valueOf(obj[0]);
					campaignRT.setCampaignId(String.valueOf(obj[0]));
					CampaignStatus campaignRTStatus = new CampaignStatus();
					campaignRTStatus.setCampaignId(campaignID);
					String frontStatus = getFrontCampStatus(campaignRTStatus);
					String campaignName = String.valueOf(obj[1]);
					logger.info("Campaign Name : " + campaignName);
					logger.info("default campaign from property :: " + defaultcampaign);
					//Default campaign change

					if (frontStatus != null && !(frontStatus.equalsIgnoreCase("notready") || frontStatus.equalsIgnoreCase("start"))) {
						campaignRT.setCampaignId(String.valueOf(obj[0]));
						String campaignId = String.valueOf(obj[0]);
						campaignRT.setCampaignName(String.valueOf(obj[1]));
						campaignRT.setCampaignStatus(String.valueOf(obj[2]));
						String status;
						// String startDate = String.valueOf(obj[3])
						//                        Timestamp startTimeStamp=campaignDao.getRTCampaignStartDate(campaignID);
						//                        SimpleDateFormat sdfTimeSt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						//                        String startDate = (startTimeStamp != null) ? sdfTimeSt.format(startTimeStamp) : "";

						String startDate = String.valueOf(obj[3]);
						//                        try {
						//                            Timestamp startTimeStamp =  campaignDao.getRTCampaignStartDate(campaignID);
						//                            SimpleDateFormat sdfTimeSt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						//                            startDate = (startTimeStamp != null) ? sdfTimeSt.format(startTimeStamp) : "";
						//
						//                        } catch (Exception e) {
						//                            logger.error("Error on getting start time : " + e.getMessage());
						//                        }

						String EndDate = String.valueOf(obj[4]);
						String startTime = String.valueOf(obj[5]);
						String endTime = String.valueOf(obj[6]);
						String conCall = String.valueOf(obj[7]);
						String dncID = String.valueOf(obj[8]);
						Integer camRetryCount = Integer.parseInt(String.valueOf(obj[9])) + 1;
						//Plus 1 for first customer no answer call , then we need to give one more


						Integer intConcall = Integer.parseInt(conCall);
						String currDate = sdf.format(new Date());
						String currTime = sdfTime.format(new Date());
						logger.info("Start Date : " + startDate + " :: End Date" + EndDate);
						logger.info("Start Time : " + startTime + " :: End Time" + endTime);
						logger.info("Current Date : " + currDate + " :: Current Time" + currTime);
						//String campaignId=String.valueOf(obj[0]);
						campaignName = String.valueOf(obj[1]);
						campaignRT.setStartDate(startDate + " " + startTime);

						//Added on may13th
						Timestamp campaignStartTime = campaignDao.getRTCampaignStartDate(campaignID);
						if (campaignStartTime != null) {
							String formattedStartTime = sdf.format(campaignStartTime) + " " + sdfTime.format(campaignStartTime);
							campaignRT.setStartDate(formattedStartTime);
						}
						else
						{
							campaignRT.setStartDate(startDate + " " + startTime);
						}

						//campaignRT.setEndDate(EndDate+" "+endTime);
						Integer activeCall = campaignDao.getActiveContDetails(campaignName);
						Integer inprogressCount = campaignDao.getCampaginBasedContactStatus(campaignName, "InProgress");
						campaignRT.setOncall(activeCall);
						campaignRT.setTotalline(intConcall - inprogressCount);
						campaignRT.setInprogress(inprogressCount);

						Date startDat = sdf.parse(startDate);
						Date endDat = sdf.parse(EndDate);
						Date currDat = sdf.parse(currDate);
						Date startTim = sdfTime.parse(startTime);
						Date endTim = sdfTime.parse(endTime);
						Date currTim = sdfTime.parse(currTime);
						/*
						 * if((currDat.after(startDat) || currDat.equals(startDat)) &&
						 * (currDat.before(endDat) || currDat.equals(endDat))) {
						 * if((currTim.after(startTim)||currTim.equals(startTim)) &&
						 * (currTim.before(endTim))){ status="Running"; }else { status="Paused"; } }else
						 * if(currDat.before(startDat)){ status="Ready"; }else if(currDat.after(endDat))
						 * { status="Completed"; }
						 */

						CampaignStatus campaignStatus = new CampaignStatus();
						campaignStatus.setCampaignId(campaignID);
						status = campaignDao.getFrontCampaignStatus(campaignStatus);

						campaignRT.setCampaignStatus(status);
						Integer contactLength = campaignDao.getCampaignBasedContactCount(campaignName);
						campaignRT.setListLength(String.valueOf(contactLength));
						//Integer callpending=campaignDao.getCampaginBasedContactStatus(campaignName,"NEW");
						//Integer callcompleted=contactLength-callpending;
						Integer answeredCount = campaignDao.getCampaginBasedContactStatus(campaignName, "ANSWERED");
						campaignRT.setAnswered(answeredCount);
						Integer callcompletedNew = campaignDao.getCompletedCountDetails(campaignName, camRetryCount);
						callcompletedNew = callcompletedNew + answeredCount;
						logger.info("callcompletedNew :: " + callcompletedNew + "answeredCount :: " + answeredCount);
						campaignRT.setCompleted(callcompletedNew);
						Integer dncCount = campaignDao.getCampBasedDNCSize(campaignID);
						campaignRT.setDND(dncCount);
						logger.info("DNC count based on campaign :: " + dncCount);

						Integer callpending = contactLength - callcompletedNew - dncCount;
						logger.info("Pending count :: " + callpending);
						campaignRT.setPending(callpending);
						Integer busyCount = campaignDao.getCampaginBasedContactStatus(campaignName, "BUSY");
						campaignRT.setLinebusy(busyCount);
						Integer NoAnswerCount = campaignDao.getCampaginBasedContactStatus(campaignName, "NO ANSWER");
						campaignRT.setNoanswer(NoAnswerCount);
						Integer notReachable = campaignDao.getCampaginBasedContactStatus(campaignName, "FAILED");
						campaignRT.setNotReachable(notReachable);
						String[] errorCodes = errorcodes.split(",");
						Integer errorCounts = campaignDao.getActiveContErrorDetails(campaignName, errorCodes);
						campaignRT.setError(errorCounts);
						if (contactLength == (callcompletedNew + dncCount)) {
							String campComTime = campaignDao.getCampaignCompletedTimeRT(campaignID);
							campaignRT.setEndDate(campComTime);
							campaignRT.setCampaignStatus("Completed");
							if (defaultcampaign == null || !defaultcampaign.contains(campaignName)) {
								campaignDao.updateCampaignstatusOnComplete(campaignID);
							} else {
								logger.info("Default Campaign, Hence not updating the completed status");
							}
						}
						//	campaignRT.setETC(getETC(campaignID, startTim, currTim, endTim,contactLength, callcompletedNew, callpending,startDat,endDat,currDat));
						Map<String, Integer> etcMap = getETC(campaignID);
						if (etcMap != null) {
							campaignRT.setExecutedCount(etcMap.get("TotalCount"));
							Integer intExeDur = etcMap.get("Totalduration");
							Integer intExeDurInMin;
							if (intExeDur == null) {
								intExeDur = 0;
								intExeDurInMin = 0;
							} else {
								intExeDurInMin = intExeDur / 60;
							}
							campaignRT.setExecutedDuration(intExeDurInMin);

						}

						campaignRTList.add(campaignRT);
						logger.info("Campaign RealTime List data :: " + campaignRTList);
					}

					if (defaultcampaign != null && defaultcampaign.contains(campaignName)) {
						campaignRT.setEndDate("NA");
						campaignRT.setCampaignStatus("Running");
						campaignRT.setETC("NA");
					}

				}
			}
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::getRealTimeData " + e);
		}
		return campaignRTList;
	}


	@Override
	public List<CampaignRealTimeDashboard> getRealTimeDashboard() { //UG
		List<CampaignRealTimeDashboard> campaignRTList = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
		try {
			campaignRTList = new ArrayList<>();
			List<Object[]> campainDetObjList = campaignDao.getCampaignDetForRT();
			if (campainDetObjList != null && !campainDetObjList.isEmpty()) {
				for (Object[] obj : campainDetObjList) {

					CampaignRealTimeDashboard campaignRT = new CampaignRealTimeDashboard();
					String campaignID = String.valueOf(obj[0]);
					campaignRT.setCampaignId(String.valueOf(obj[0]));
					CampaignStatus campaignRTStatus = new CampaignStatus();
					campaignRTStatus.setCampaignId(campaignID);
					String frontStatus = getFrontCampStatus(campaignRTStatus);
					String campaignName = String.valueOf(obj[1]);
					logger.info("Campaign Name : " + campaignName);
					logger.info("default campaign from property :: " + defaultcampaign);
					//Default campaign change

					if (frontStatus != null && !(frontStatus.equalsIgnoreCase("notready") || frontStatus.equalsIgnoreCase("start"))) {
						campaignRT.setCampaignId(String.valueOf(obj[0]));
						String campaignId = String.valueOf(obj[0]);
						campaignRT.setCampaignName(String.valueOf(obj[1]));
						campaignRT.setCampaignStatus(String.valueOf(obj[2]));
						String status;
						// String startDate = String.valueOf(obj[3])
						//                        Timestamp startTimeStamp=campaignDao.getRTCampaignStartDate(campaignID);
						//                        SimpleDateFormat sdfTimeSt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						//                        String startDate = (startTimeStamp != null) ? sdfTimeSt.format(startTimeStamp) : "";

						String startDate = String.valueOf(obj[3]);
						//                        try {
						//                            Timestamp startTimeStamp =  campaignDao.getRTCampaignStartDate(campaignID);
						//                            SimpleDateFormat sdfTimeSt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						//                            startDate = (startTimeStamp != null) ? sdfTimeSt.format(startTimeStamp) : "";
						//
						//                        } catch (Exception e) {
						//                            logger.error("Error on getting start time : " + e.getMessage());
						//                        }

						String EndDate = String.valueOf(obj[4]);
						String startTime = String.valueOf(obj[5]);
						String endTime = String.valueOf(obj[6]);
						String conCall = String.valueOf(obj[7]);
						String dncID = String.valueOf(obj[8]);
						Integer camRetryCount = Integer.parseInt(String.valueOf(obj[9])) + 1;
						//Plus 1 for first customer no answer call , then we need to give one more


						Integer intConcall = Integer.parseInt(conCall);
						String currDate = sdf.format(new Date());
						String currTime = sdfTime.format(new Date());
						logger.info("Start Date : " + startDate + " :: End Date" + EndDate);
						logger.info("Start Time : " + startTime + " :: End Time" + endTime);
						logger.info("Current Date : " + currDate + " :: Current Time" + currTime);
						//String campaignId=String.valueOf(obj[0]);
						campaignName = String.valueOf(obj[1]);
						campaignRT.setStartDate(startDate + " " + startTime);

						//Added on may13th
						Timestamp campaignStartTime = campaignDao.getRTCampaignStartDate(campaignID);
						if (campaignStartTime != null) {
							String formattedStartTime = sdf.format(campaignStartTime) + " " + sdfTime.format(campaignStartTime);
							campaignRT.setStartDate(formattedStartTime);
						}
						else
						{
							campaignRT.setStartDate(startDate + " " + startTime);
						}

						//campaignRT.setEndDate(EndDate+" "+endTime);
						Integer activeCall = campaignDao.getActiveContDetails(campaignName);
						Integer inprogressCount = campaignDao.getCampaginBasedContactStatus(campaignName, "InProgress");
						campaignRT.setOncall(activeCall);
						campaignRT.setTotalline(intConcall - inprogressCount);
						campaignRT.setInprogress(inprogressCount);

						Date startDat = sdf.parse(startDate);
						Date endDat = sdf.parse(EndDate);
						Date currDat = sdf.parse(currDate);
						Date startTim = sdfTime.parse(startTime);
						Date endTim = sdfTime.parse(endTime);
						Date currTim = sdfTime.parse(currTime);
						/*
						 * if((currDat.after(startDat) || currDat.equals(startDat)) &&
						 * (currDat.before(endDat) || currDat.equals(endDat))) {
						 * if((currTim.after(startTim)||currTim.equals(startTim)) &&
						 * (currTim.before(endTim))){ status="Running"; }else { status="Paused"; } }else
						 * if(currDat.before(startDat)){ status="Ready"; }else if(currDat.after(endDat))
						 * { status="Completed"; }
						 */

						CampaignStatus campaignStatus = new CampaignStatus();
						campaignStatus.setCampaignId(campaignID);
						status = campaignDao.getFrontCampaignStatus(campaignStatus);

						campaignRT.setCampaignStatus(status);
						Integer contactLength = campaignDao.getCampaignBasedContactCount(campaignName);
						campaignRT.setListLength(String.valueOf(contactLength));
						//Integer callpending=campaignDao.getCampaginBasedContactStatus(campaignName,"NEW");
						//Integer callcompleted=contactLength-callpending;
						Integer answeredCount = campaignDao.getCampaginBasedContactStatus(campaignName, "ANSWERED");
						campaignRT.setAnswered(answeredCount);
						Integer callcompletedNew = campaignDao.getCompletedCountDetails(campaignName, camRetryCount);
						callcompletedNew = callcompletedNew + answeredCount;
						logger.info("callcompletedNew :: " + callcompletedNew + "answeredCount :: " + answeredCount);
						campaignRT.setCompleted(callcompletedNew);
						Integer dncCount = campaignDao.getCampBasedDNCSize(campaignID);
						campaignRT.setDND(dncCount);
						logger.info("DNC count based on campaign :: " + dncCount);

						Integer callpending = contactLength - callcompletedNew - dncCount;
						logger.info("Pending count :: " + callpending);
						campaignRT.setPending(callpending);
						Integer busyCount = campaignDao.getCampaginBasedContactStatus(campaignName, "BUSY");
						campaignRT.setLinebusy(busyCount);
						Integer NoAnswerCount = campaignDao.getCampaginBasedContactStatus(campaignName, "NO ANSWER");
						campaignRT.setNoanswer(NoAnswerCount);
						Integer notReachable = campaignDao.getCampaginBasedContactStatus(campaignName, "FAILED");
						campaignRT.setNotReachable(notReachable);
						String[] errorCodes = errorcodes.split(",");
						Integer errorCounts = campaignDao.getActiveContErrorDetails(campaignName, errorCodes);
						campaignRT.setError(errorCounts);
						if (contactLength == (callcompletedNew + dncCount)) {
							String campComTime = campaignDao.getCampaignCompletedTimeRT(campaignID);
							campaignRT.setEndDate(campComTime);
							campaignRT.setCampaignStatus("Completed");
							if (defaultcampaign == null || !defaultcampaign.contains(campaignName)) {
								campaignDao.updateCampaignstatusOnComplete(campaignID);
							} else {
								logger.info("Default Campaign, Hence not updating the completed status");
							}
						}
						//	campaignRT.setETC(getETC(campaignID, startTim, currTim, endTim,contactLength, callcompletedNew, callpending,startDat,endDat,currDat));
						Map<String, Integer> etcMap = getETC(campaignID);
						if (etcMap != null) {
							campaignRT.setExecutedCount(etcMap.get("TotalCount"));
							Integer intExeDur = etcMap.get("Totalduration");
							Integer intExeDurInMin;
							if (intExeDur == null) {
								intExeDur = 0;
								intExeDurInMin = 0;
							} else {
								intExeDurInMin = intExeDur / 60;
							}
							campaignRT.setExecutedDuration(intExeDurInMin);

						}

						campaignRTList.add(campaignRT);
						logger.info("Campaign RealTime List data :: " + campaignRTList);
					}

					if (defaultcampaign != null && defaultcampaign.contains(campaignName)) {
						campaignRT.setEndDate("NA");
						campaignRT.setCampaignStatus("Running");
						campaignRT.setETC("NA");
					}

				}
			}
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::getRealTimeData " + e);
		}
		return campaignRTList;
	}

	private String getETC(String campaignID, Date campaingStartTime, Date CurrTime, Date campaignEndTime, int TotalContacts, int completedCalls, int pendingCalls, Date camStartdate, Date camEndDate, Date currDate) {
		String remDuration = null;
		Date ectDate = null;
		try {

			long differenceMillis = Math.abs(camStartdate.getTime() - currDate.getTime());
			long differenceDays = TimeUnit.DAYS.convert(differenceMillis, TimeUnit.MILLISECONDS);
			logger.info("Diff in Days :" + differenceDays + "for the Campaign ID :" + campaignID);


			long campDurationinMillis = Math.abs(campaingStartTime.getTime() - campaignEndTime.getTime());
			long campRunDuration = TimeUnit.HOURS.convert(campDurationinMillis, TimeUnit.MILLISECONDS);
			logger.info("Daily Camp Running Time :" + campRunDuration);

			long TotalRunnintTime = differenceDays * campRunDuration;
			long totalRuntilyes = TotalRunnintTime * 60 * 60;
			logger.info("Total Running Duration in Hours :" + TotalRunnintTime);

			Calendar calCampStart = Calendar.getInstance();
			calCampStart.setTime(campaingStartTime);
			Calendar calcurrTime = Calendar.getInstance();
			calcurrTime.setTime(CurrTime);
			Calendar calcampEnd = Calendar.getInstance();
			calcampEnd.setTime(campaignEndTime);


			Calendar calEstTime = Calendar.getInstance();


			long longdiffinMIlls = 0;
			if (calcurrTime.getTimeInMillis() > calcampEnd.getTimeInMillis()) {
				longdiffinMIlls = calcampEnd.getTimeInMillis() - calCampStart.getTimeInMillis();
			} else {
				longdiffinMIlls = calcurrTime.getTimeInMillis() - calCampStart.getTimeInMillis();
			}

			long longdiffinSec = TimeUnit.MILLISECONDS.toSeconds(longdiffinMIlls);


			logger.info("Diff in Sec :" + longdiffinSec);

			double totalRunDuraininSec = totalRuntilyes + longdiffinSec;
			if (completedCalls > 0) {
				double avgCallDuratin = totalRunDuraininSec / completedCalls;
				double remainngDuration = pendingCalls * avgCallDuratin;
				logger.info("Average Call Duration :" + remainngDuration);
				int intRemDuration = (int) remainngDuration;
				int inthours = intRemDuration / 3600;
				int intminutes = (intRemDuration % 3600) / 60;
				int intseconds = intRemDuration % 60;
				remDuration = getWholeValue(inthours) + ":" + getWholeValue(intminutes) + ":" + getWholeValue(intseconds);
				logger.info("Rem Duration :" + remDuration);
				calEstTime.add(Calendar.SECOND, intRemDuration);
				ectDate = calEstTime.getTime();
				logger.info("ECT DATE :" + ectDate);
			} else {
				logger.info("Completed Call is less than 1, Hence setting it as 'NA'");
				remDuration = "NA";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return remDuration;
	}


	private String getWholeValue(int data) {
		return data > 9 ? String.valueOf(data) : "0" + String.valueOf(data);
	}

	public static void main(String[] args) {
		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			String startTime = "09:00:00";
			String currTime = "11:00:00";
			String EndTime = "17:00:00";
			String startDate = "2024-03-07";
			String EndDate = "2024-03-10";
			String CurrDate = "2024-03-08";
			Date startTim = sdfTime.parse(startTime);
			Date currTim = sdfTime.parse(currTime);
			Date EndTim = sdfTime.parse(EndTime);
			Date StartDat = sdf.parse(startDate);
			Date EndDat = sdf.parse(EndDate);
			Date CurrDat = sdf.parse(CurrDate);
			logger.info("Start Time :" + startTim);
			logger.info("Curr Time :" + currTim);
			//getETC("C_01", startTim, currTim, EndTim,13000, 1000, 300,StartDat,EndDat,CurrDat);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/*
	 * @Override public boolean insertSurveyContactDet(List<Map<String,Object>>
	 * listSurveyContact) { boolean insertionStatus=false; try {
	 * for(Map<String,Object> mapSurveyContact:listSurveyContact ) { insertionStatus
	 * = campaignDao.insertSurveyContactDet(mapSurveyContact); } } catch (Exception
	 * e) { logger.error("Error in ::insertSurveyContactDet " + e); } return
	 * insertionStatus; }
	 */
	@Override
	public boolean insertSurveyContactDet(List<Map<String, Object>> listSurveyContact) {
		boolean insertionStatus = false;
		try {
			logger.info("Retrieved Survey Contact Size :" + listSurveyContact.size());
			for (Map<String, Object> mapSurveyContact : listSurveyContact) {
				logger.info("Inserting DAO for the contact :" + mapSurveyContact.get("phone"));
				try {
					insertionStatus = campaignDao.insertSurveyContactDet(mapSurveyContact);
				} catch (Exception e) {
					logger.error("Error in ::insertSurveyContactDet DAO Invoked " + e);
				}
				logger.info("Insertion Status for the phone number :" + mapSurveyContact.get("phone") + " :: " + insertionStatus);
			}
		} catch (Exception e) {
			logger.error("Error in ::insertSurveyContactDet " + e);
		}
		return insertionStatus;
	}


	/*	@Override
	public boolean insertSurveyContactDet(List<Map<String,Object>> listSurveyContact) {
	    boolean overallInsertionStatus = true; // Initialize overallInsertionStatus to true
	    try {
	        for(Map<String,Object> mapSurveyContact : listSurveyContact) {
	            boolean insertionStatus = campaignDao.insertSurveyContactDet(mapSurveyContact);
	            // If any insertion fails, set overallInsertionStatus to false
	            if (!insertionStatus) {
	                overallInsertionStatus = false;
	            }
	        }
	    } catch (Exception e) {
	        logger.error("Error in ::insertSurveyContactDet " + e);
	        overallInsertionStatus = false; // Set overallInsertionStatus to false in case of exception
	    }
	    return overallInsertionStatus; // Return the overall insertion status
	}*/


	@Override
	public String getDummySurveyResponse() {
		String response = null;
		try {
			response = "{\"phone\":\"9876098987\",\"actionId\":\"123456789\",\"Survey_Lang\":\"EN\",\"MainSkillset\":\"NAS\",\"subSkillset\":\"NasMember\"}";

		} catch (Exception e) {
			logger.error("Error in :: Dummy Survey Response " + e);
		}
		return response;
	}

	@Override
	public ResponseEntity<GenericResponse> createDnc(DNCDetRequest DNCDetRequest) {

		GenericResponse genericResponse = new GenericResponse();
		try {
			boolean done = campaignDao.createDnc(DNCDetRequest);
			if (done) {
				genericResponse.setStatus(200);
				genericResponse.setValue("Success");
				genericResponse.setMessage("create Dnc successfully");
			} else {
				genericResponse.setStatus(400);
				genericResponse.setValue("Failure");
				genericResponse.setMessage("Error occured while createDnc details");
			}
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl:: createDnc " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("Error occured createDnc details");
		}
		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<GenericResponse> getDnsDetail() {
		GenericResponse genericResponse = new GenericResponse();
		List<DNCDetRequest> campaignDetList = null;
		try {
			campaignDetList = getDNSDetailList();
			genericResponse.setStatus(200);
			genericResponse.setValue(campaignDetList);
			genericResponse.setMessage("Success");
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::getDnsDetail " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("No data Found");
		}

		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}

	public List<DNCDetRequest> getDNSDetailList() {

		List<DNCDetRequest> campaignDetList;
		campaignDetList = new ArrayList<>();
		List<Object[]> campainDetObjList = campaignDao.getdnsDet();
		if (campainDetObjList != null && !campainDetObjList.isEmpty()) {
			for (Object[] obj : campainDetObjList) {
				DNCDetRequest DNCDetRequest = new DNCDetRequest();
				DNCDetRequest.setDNCID(String.valueOf(obj[0]));
				DNCDetRequest.setDncName(String.valueOf(obj[1]));
				DNCDetRequest.setDescription(String.valueOf(obj[2]));
				campaignDetList.add(DNCDetRequest);
				//	logger.info("dnsDetails :"+DNCDetRequest.toString());
			}
		}
		return campaignDetList;
	}

	@Override
	public ResponseEntity<GenericResponse> updateDns(DNCDetRequest DNCDetRequest) {
		GenericResponse genericResponse = new GenericResponse();
		try {
			boolean isUpdated = campaignDao.updateDns(DNCDetRequest);
			if (isUpdated) {
				genericResponse.setStatus(200);
				genericResponse.setValue("Success");
				genericResponse.setMessage("DNC details updated successfully");
			} else {
				genericResponse.setStatus(400);
				genericResponse.setValue("Failure");
				genericResponse.setMessage("Error occured while updating DNC details");
			}
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::updating DNC details " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("Error occured while updating DNC details");
		}

		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}

	@Override
	public List<String> getDNSDetList(String dncID) {

		List<String> campaignDetList = new ArrayList<>();
		List<String> campainDetObjList = campaignDao.getCampaignBasedDNClist(dncID);
		if (campainDetObjList != null && !campainDetObjList.isEmpty()) {

			if (campainDetObjList.size() > 1) {
				for (String contact : campainDetObjList) {
					campaignDetList.add(contact);
					logger.info("dnsDetails :" + campaignDetList.toString());
				}
			}
		}
		return campaignDetList;
	}


	@Override
	public Map<String, List<SurveyContactDetDto>> getSurveyContDet() {
		return campaignDao.getSurveyContactDet();
	}



	@Override
	public int getCountToCall(String productID) {
		GenericResponse genericResponse = new GenericResponse();
		int count = 0;
		try {
			count = campaignDao.getCountToCall(productID);
			genericResponse.setStatus(200);
			genericResponse.setValue(count);
			genericResponse.setMessage("Success");
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::getCountToCall " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("No data Found");
		}

		return count;
	}

	@Override
	public ResponseEntity<GenericResponse> updateContact(DncContactDto contactDetDto) {
		GenericResponse genericResponse = new GenericResponse();
		try {
			boolean isUpdated = campaignDao.createContactone(contactDetDto);
			if (isUpdated) {
				genericResponse.setStatus(200);
				genericResponse.setValue("Success");
				genericResponse.setMessage("Number updated successfully");
			} else {
				genericResponse.setStatus(400);
				genericResponse.setValue("Failure");
				genericResponse.setMessage("Error occured while Inserting the number");
			}
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::updateContact " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("Error occured while updateContact");
		}

		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}

	@Override
	public boolean createContactone(DncContactDto contactDetDto) {
		boolean isCreated;
		try {
			isCreated = campaignDao.createContactone(contactDetDto);
		} catch (Exception e) {
			return false;
		}
		return isCreated;
	}

	public ResponseEntity<GenericResponse> DeleteContact(DncContactDto contactDetDto) {
		GenericResponse genericResponse = new GenericResponse();
		try {
			boolean isUpdated = campaignDao.DeleteContact(contactDetDto);
			if (isUpdated) {
				genericResponse.setStatus(200);
				genericResponse.setValue("Success");
				genericResponse.setMessage("Deleted the number successfully");
			} else {
				genericResponse.setStatus(400);
				genericResponse.setValue("Failure");
				genericResponse.setMessage("Error occured while Deleted the number ");
			}
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::DeleteContact " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("Error occured while DeleteContact");
		}

		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}

	@Override
	public boolean updateCampaignStatusUploadContact(String campaignId) throws Exception {
		boolean isUploaded = false;
		try {
			isUploaded = campaignDao.updateCampaignStatusUploadContact(campaignId);
		} catch (Exception e) {
			logger.error("Error occured while updateCampaignStatusUploadContact");
		}
		return isUploaded;
	}

	@Override
	public String getFrontCampStatus(CampaignStatus campaignStatus) throws Exception {
		String frontCampStatus = null;
		try {
			frontCampStatus = campaignDao.getFrontCampaignStatus(campaignStatus);
		} catch (Exception e) {
			logger.error("Error occured while getFrontCampStatus");
		}
		return frontCampStatus;
	}

	@Override
	public Map<String, Integer> getETC(String campaignId) throws Exception {
		Map<String, Integer> map = null;
		Map<String, Integer> mapETCDuration = new HashMap<>();
		try {
			map = campaignDao.getETC(campaignId);
			Integer busyCount = map.get("busycount");
			Integer answerCount = map.get("answerCount");
			Integer noAnswerCount = map.get("noanswerCount");
			Integer answerduration = map.get("answeredDuration");
			Integer failedCount = map.get("failedCount");
			if (answerduration == null) {
				answerduration = 0;
			}
			Integer Totalcount = busyCount + answerCount + noAnswerCount + failedCount;
			Integer Totalduration = (busyCount * 7) + (noAnswerCount * 15) + answerduration + (failedCount * 30);
			mapETCDuration.put("TotalCount", Totalcount);
			mapETCDuration.put("Totalduration", Totalduration);
		} catch (Exception e) {
			logger.error("Error occured while getETC serviceImpl");
		}
		return mapETCDuration;
	}

	@Override
	public String getDNCIDusingCampaignID(String campaignID) {
		String dncID = null;
		try {
			dncID = campaignDao.getDNCIDusingCampaignID(campaignID);
		} catch (Exception e) {
			logger.error("Error occured while get DNC ID " + e);
		}
		return dncID;
	}

	@Override
	public synchronized boolean updateContactDetail(String campaignId, String phone, String actionId, String inProgress) {
		return campaignDao.updateContactDetail(campaignId, phone, actionId, inProgress);
	}

	@Override
	public int getinProgressCallCount(String campaignId) {
		return campaignDao.getinProgressCallCount(campaignId);
	}

	@Override
	public boolean checkContactIsHangUp(String actionId, String phone) {
		return campaignDao.checkContactIsHangUp(actionId, phone);
	}

	@Override
	public List<SurveyContactDetDto> getContactDetRetry(String campaignName, String retryCount) {
		return campaignDao.getContactDetRetry(campaignName, retryCount);
	}

	public boolean updateNTCStatus(String campaignId) {
		if (campaignDao.updateNTCStatusinRetryDet(campaignId)) {
			logger.info("NTC Updated Successfully in call retry table");
		} else {
			logger.info("NTC NOT Updated");
		}
		return campaignDao.updateNTCStatus(campaignId);
	}

	@Override
	public boolean makeInprogressintoNoAnswer(String campaignId) {
		boolean isUpdated = false;
		List<Object[]> updatedRecords = campaignDao.makeInprogressintoNoAnswer(campaignId);
		if (updatedRecords != null && !updatedRecords.isEmpty()) {
			try {
				for (Object[] record : updatedRecords) {
					RetryDetailsDet retryDetailsDet = new RetryDetailsDet();
					retryDetailsDet.setContactId(String.valueOf(record[0]));
					retryDetailsDet.setPhoneno(String.valueOf(record[1]));
					retryDetailsDet.setRetryCount(String.valueOf(record[2]));
					retryDetailsDet.setCallDuration("0");
					String actionID = String.valueOf(record[3]);
					if (campaignDao.saveRetryDetails(retryDetailsDet, campaignId, actionID)) {
						logger.info("Updated in Call Retry Detail table and In Contact Det");
					} else {
						logger.info("Record Not Updated in Call Retry Detail table and In Contact Det : " + updatedRecords.size());
					}

				}
				isUpdated = true;
			} catch (Exception e) {
				logger.error("Error processing the updated records", e);
			}
		}
		return isUpdated;
	}


	@Override
	public ResponseEntity<GenericResponse> stopCampaignStatus(String campaginId, String frontStatus) {
		GenericResponse genericResponse = new GenericResponse();
		try {
			boolean isUpdated = campaignDao.stopPauseCampaignStatus(campaginId, frontStatus);
			if (isUpdated) {
				//campaignDao.updateNTCStatus(campaginId);
				genericResponse.setStatus(200);
				genericResponse.setValue("Success");
				genericResponse.setMessage("Campaign updated successfully");
			} else {
				genericResponse.setStatus(400);
				genericResponse.setValue("Failure");
				genericResponse.setMessage("Error occured while updating Campaign");
			}
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::updateCampaign " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("Error occured while updating Campaign");
		}

		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}

	public ResponseEntity<GenericResponseReport> smsReport(ReportRequest reportRequest) {
		GenericResponseReport genericResponse = new GenericResponseReport();
		List<GenericHeaderResponse> headerlist = null;
		List<GenericHeaderResponse> subHeaderlist = null;
		List<Map<Object, Object>> valueList = null;
		try {
			List<Object[]> resultList = campaignDao.getSmsReportDet(reportRequest);

			if (resultList != null && !resultList.isEmpty()) {
				headerlist = new ArrayList<GenericHeaderResponse>();
				subHeaderlist = new ArrayList<GenericHeaderResponse>();
				valueList = new ArrayList<Map<Object, Object>>();
				subHeaderlist.add(new GenericHeaderResponse("Campaign ID", "campaignID"));
				subHeaderlist.add(new GenericHeaderResponse("Agent ID", "agentID"));
				subHeaderlist.add(new GenericHeaderResponse("Customer No", "customerNum"));
				// subHeaderlist.add(new GenericHeaderResponse("Contacts Called",
				// "contactCalled"));
				subHeaderlist.add(new GenericHeaderResponse("Contacted", "contactConnected"));
				subHeaderlist.add(new GenericHeaderResponse("Survey", "survey"));
				subHeaderlist.add(new GenericHeaderResponse("Channel", "channel"));
				subHeaderlist.add(new GenericHeaderResponse("Q1 Response", "surveyResponse"));
				subHeaderlist.add(new GenericHeaderResponse("Start Time", "startTime"));
				subHeaderlist.add(new GenericHeaderResponse("End Time", "endTime"));
				subHeaderlist.add(new GenericHeaderResponse("Retries Count", "retriesCount"));
				subHeaderlist.add(new GenericHeaderResponse("SMS Triggered", "smstriggered"));
				subHeaderlist.add(new GenericHeaderResponse("SMS Triggered Date Time", "smstriggeredDate"));
				subHeaderlist.add(new GenericHeaderResponse("Web Q Response 1", "QResponse3"));
				subHeaderlist.add(new GenericHeaderResponse("Web Q Response 2", "QResponse4"));
				/*
				 * subHeaderlist.add(new GenericHeaderResponse("Confirmed", "confirmed"));
				 * subHeaderlist.add(new GenericHeaderResponse("Cancelled", "canceleld"));
				 * subHeaderlist.add(new GenericHeaderResponse("Rescheduled", "rescheduled"));
				 * subHeaderlist.add(new GenericHeaderResponse("No Response", "noResponse"));
				 */

				headerlist.add(new GenericHeaderResponse("Campaign sms Report", "", subHeaderlist));
				for (Object[] obj : resultList) {
					logger.info("sms report obj length :" + obj.length);
					Map<Object, Object> valueMap = new LinkedHashMap<>();
					valueMap.put("campaignID", obj[0]);
					valueMap.put("agentID", obj[1]);
					valueMap.put("customerNum", obj[2]);

					String callStatus = (String) obj[3];
					if (callStatus != null && !callStatus.equalsIgnoreCase("NEW")) {
						callStatus = "YES";
						obj[3] = callStatus;
						valueMap.put("contactConnected", obj[3]);
					} else {
						callStatus = "NO";
						obj[3] = callStatus;
						valueMap.put("contactConnected", obj[3]);
					}
					String surveyRating = (String) obj[4];
					if (surveyRating != null && !surveyRating.isEmpty()) {
						obj[4] = surveyRating;
						valueMap.put("surveyResponse", obj[4]);
						String repondedStatus = "RESPONDED";
						// obj[5]=repondedStatus;
						String strchannel = "IVR";
						valueMap.put("survey", repondedStatus);
						valueMap.put("channel", strchannel);
						if (repondedStatus != null && repondedStatus.equalsIgnoreCase("RESPONDED")) {
							if (obj[5] == null) {
								logger.info("Start Time is NULL, Hence setting empty");
								valueMap.put("startTime", "");
							} else {
								Timestamp startTimeStamp = (Timestamp) obj[5];
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								String formattedStartTime = (startTimeStamp != null) ? sdf.format(startTimeStamp) : "";
								valueMap.put("startTime", formattedStartTime);
							}

							if (obj[6] == null) {
								logger.info("End Time is NULL, Hence setting empty");
								valueMap.put("endTime", "");
							} else {
								Timestamp endTimeStamp = (Timestamp) obj[6];
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								String formattedEndTime = (endTimeStamp != null) ? sdf.format(endTimeStamp) : "";
								valueMap.put("endTime", formattedEndTime);
							}
							/*
							 *
							 * Timestamp startTimeStamp = (Timestamp) obj[5]; Timestamp endTimeStamp =
							 * (Timestamp) obj[6]; SimpleDateFormat sdf = new
							 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); String formattedStartTime =
							 * (startTimeStamp != null) ? sdf.format(startTimeStamp) : ""; String
							 * formattedEndTime = (endTimeStamp != null) ? sdf.format(endTimeStamp) : "";
							 * valueMap.put("startTime", formattedStartTime); valueMap.put("endTime",
							 * formattedEndTime);
							 */
						} else {
							valueMap.put("startTime", "");
							valueMap.put("endTime", "");
						}
					} else {
						obj[4] = surveyRating;
						valueMap.put("surveyResponse", obj[4]);
						String repondedStatus = "NOT RESPONDED";
						valueMap.put("survey", repondedStatus);
						valueMap.put("startTime", "");
						valueMap.put("endTime", "");

					}
					/*
					 * if (callStatus == null || callStatus.equalsIgnoreCase("null") ||
					 * callStatus.equalsIgnoreCase("NEW")) { valueMap.put("startTime", "");
					 * valueMap.put("endTime", ""); }else { //String startTime= (String)obj[5];
					 * if(callStatus.equalsIgnoreCase("ANSWERED")) { Timestamp startTimeStamp =
					 * (Timestamp) obj[5]; SimpleDateFormat sdf = new
					 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); String formattedStartTime =
					 * sdf.format(startTimeStamp); valueMap.put("startTime", formattedStartTime);
					 * Timestamp endTimeStamp = (Timestamp) obj[6]; SimpleDateFormat sdfe = new
					 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); String formattedEndTime =
					 * sdfe.format(endTimeStamp); valueMap.put("endTime", formattedEndTime); } else
					 * { valueMap.put("startTime", ""); valueMap.put("endTime", "");
					 *
					 * } if(obj[5]==null) { logger.info("Start Time is NULL, Hence setting empty");
					 * valueMap.put("startTime", ""); }else {
					 *
					 * Timestamp startTimeStamp = (Timestamp) obj[5]; SimpleDateFormat sdf = new
					 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); String formattedStartTime =
					 * sdf.format(startTimeStamp); valueMap.put("startTime", formattedStartTime);
					 *
					 * valueMap.put("startTime", ""); }
					 *
					 * if(obj[6]==null) { logger.info("End Time is NULL, Hence setting empty");
					 * valueMap.put("endTime", ""); }else {
					 *
					 * Timestamp endTimeStamp = (Timestamp) obj[6]; SimpleDateFormat sdf = new
					 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); String formattedEndTime =
					 * sdf.format(endTimeStamp); valueMap.put("endTime", formattedEndTime);
					 *
					 * valueMap.put("endTime", ""); } }
					 */
					/*
					 * if ("ANSWERED".equalsIgnoreCase(callStatus)) { Timestamp startTimeStamp =
					 * (Timestamp) obj[5]; Timestamp endTimeStamp = (Timestamp) obj[6];
					 * SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); String
					 * formattedStartTime = (startTimeStamp != null) ? sdf.format(startTimeStamp) :
					 * ""; String formattedEndTime = (endTimeStamp != null) ?
					 * sdf.format(endTimeStamp) : ""; valueMap.put("startTime", formattedStartTime);
					 * valueMap.put("endTime", formattedEndTime); } else { valueMap.put("startTime",
					 * ""); valueMap.put("endTime", ""); }
					 */

					// Set empty string if startTime or endTime is null
					/*
					 * if (obj[5] == null) { logger.info("Start Time is NULL, Hence setting empty");
					 * valueMap.put("startTime", ""); }
					 *
					 * if (obj[6] == null) { logger.info("End Time is NULL, Hence setting empty");
					 * valueMap.put("endTime", ""); }
					 */


					valueMap.put("retriesCount", obj[7]);
					valueMap.put("smstriggered", obj[8]);
					// Timestamp SMSstartTimeStamp = (Timestamp) obj[9];
					// SimpleDateFormat ssdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					// String formattedsmssentime = sdf.format(SMSstartTimeStamp);
					// Timestamp SMSstartTimeStamp = (Timestamp) obj[9];
					if (obj[9] != null) {
						Timestamp smsTriggeredTimestamp = (Timestamp) obj[9];
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String formattedSmsTriggeredDate = sdf.format(smsTriggeredTimestamp);
						valueMap.put("smstriggeredDate", formattedSmsTriggeredDate);
						// valueMap.put("smstriggeredDate", obj[9]);
					} else {
						valueMap.put("smstriggeredDate", "");
					}

					String smsTriggered = (String) obj[8];
					if (smsTriggered != null && smsTriggered.equalsIgnoreCase("YES")) {
						logger.info("SMS Triggered is coming as YES");
						List<Object> listWebResponse = getWebResponse((String) obj[10]);
						if (listWebResponse != null && !listWebResponse.isEmpty()) {
							logger.info("List Web Response Size :" + listWebResponse.size());
							String smsTriggeredTime = (String) listWebResponse.get(0);
							String smsresponeTime = (String) listWebResponse.get(1);
							valueMap.put("startTime", smsTriggeredTime);
							valueMap.put("endTime", smsresponeTime);
							String strchannel = "Web";
							valueMap.put("channel", strchannel);
							String repondedStatus = "RESPONDED";
							valueMap.put("survey", repondedStatus);
							for (int i = 2; i < listWebResponse.size(); i++) {
								valueMap.put("QResponse" + (i + 1), listWebResponse.get(i));

							}
						} else {
							logger.info("List Response NULL");
						}
					}

					// valueMap.put("q1reponse", obj[9]);
					// valueMap.put("q2reponse", obj[10]);
					/*
					 * valueMap.put("confirmed", obj[7]); valueMap.put("canceleld", obj[8]);
					 * valueMap.put("rescheduled", obj[9]); valueMap.put("noResponse", obj[10]);
					 */

					logger.info("Value Map :" + valueMap);
					valueList.add(valueMap);
				}
				genericResponse.setStatus(200);
				genericResponse.setHeader(headerlist);
				genericResponse.setValue(valueList);
				genericResponse.setMessage("Data fetched sucessfully");
			} else {
				genericResponse.setStatus(200);
				genericResponse.setValue(null);
				genericResponse.setMessage("No data found");
			}
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Exception :" + str.toString());
			logger.error("Error in CampaignServiceImpl::smsReport " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue(null);
			genericResponse.setMessage("Error occured generating sms report");
		}
		return new ResponseEntity<GenericResponseReport>(new GenericResponseReport(genericResponse), HttpStatus.OK);
	}

	private List<Object> getWebResponse(String actionID) {
		List<Object> listResponse = new ArrayList<>();
		try {
			logger.info("invoking  action ID  " + actionID);
			List<Object[]> responselist = campaignDao.getSmswebReportDet(actionID);
			String smsresponseTime = null;
			String smsresponseEndTime = null;
			String response = null;

			if (responselist == null || responselist.isEmpty()) {
				logger.error("Web Response is null.");
				return listResponse; // Return empty list
			}

			for (Object[] obj : responselist) {
				smsresponseTime = (String) obj[0];
				smsresponseEndTime = (String) obj[1];
				response = (String) obj[2];
			}

			listResponse.add(smsresponseTime);
			listResponse.add(smsresponseEndTime);

			JSONParser parser = new JSONParser();
			JSONArray jsonArray = null;
			if (response != null) {
				jsonArray = (JSONArray) parser.parse(response);
			} else {
				logger.error("Web Response is null.");
				// You might want to return an empty list or handle it differently based on your requirements
			}
			//JSONArray jsonArray = (JSONArray) parser.parse(response);

			for (Object dbResponse : jsonArray) {
				JSONObject jsonResponse = (JSONObject) dbResponse;
				Long questionID = null;
				String rating = null;
				if (jsonResponse.containsKey("question_id")) {
					logger.info("Question ID Available");
					questionID = (Long) jsonResponse.get("question_id");
				}
				if (jsonResponse.containsKey("answers")) {
					JSONArray arrAnswer = (JSONArray) jsonResponse.get("answers");
					for (Object obj2 : arrAnswer) {
						JSONObject jsonarrObj = (JSONObject) obj2;
						if (jsonarrObj.containsKey("chosen_rating")) {
							logger.info("Choosen Rating Available");
							rating = (String) jsonarrObj.get("chosen_rating");
							listResponse.add(rating);
						}
					}
				}
			}
			logger.info("List Response Size :" + listResponse.size());

		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Exception :" + str.toString());
		}
		return listResponse;
	}

	@Override
	public ResponseEntity<InputStreamResource> downloadsmsReport(ReportRequest reportRequest) {
		// String previousVal = null;
		Workbook workbook = new XSSFWorkbook();
		try {
			String currentDirectory = System.getProperty("user.dir");
			Sheet sheet1 = null;
			final String fileName = currentDirectory + "\\SMS_Report.xlsx";
			sheet1 = workbook.createSheet("SMS Report");
			sheet1.setDefaultColumnWidth(12);
			CellStyle style = ExcelUtil.getCellStyleForHeader(workbook);
			CellStyle styleContent = ExcelUtil.getCellStyleForContent(workbook);
			List<Object[]> contactSummaryList = campaignDao.getSmsReportDet(reportRequest);

			int row = 0;
			// Summary View Sheet
			row++;
			Row searchHeaderRow = sheet1.createRow(row);
			ExcelUtil.setCellValue(style, searchHeaderRow, 2, "Report Input Criteria");
			ExcelUtil.frameMerged(new CellRangeAddress(row, row, 2, 3), sheet1, workbook);

			row++;
			searchHeaderRow = sheet1.createRow(row);
			ExcelUtil.setCellValue(style, searchHeaderRow, 2, "Campaign Name");
			ExcelUtil.setCellValue(styleContent, searchHeaderRow, 3, reportRequest.getCampaignName());

			row++;
			searchHeaderRow = sheet1.createRow(row);
			ExcelUtil.setCellValue(style, searchHeaderRow, 2, "Start Date");
			ExcelUtil.setCellValue(styleContent, searchHeaderRow, 3, reportRequest.getStartDate());

			row++;
			searchHeaderRow = sheet1.createRow(row);
			ExcelUtil.setCellValue(style, searchHeaderRow, 2, "End Date");
			ExcelUtil.setCellValue(styleContent, searchHeaderRow, 3, reportRequest.getEndDate());
			ExcelUtil.setRegionBorderWithMedium(new CellRangeAddress(2, row, 2, 3), sheet1, workbook);

			sheet1.autoSizeColumn(2, true);
			sheet1.autoSizeColumn(3, true);

			row++;
			row++;
			row++;
			Row headerRow = sheet1.createRow(row);
			int firstRow = row;
			ExcelUtil.setCellValue(style, headerRow, 0, "SMS Summary Report");
			ExcelUtil.frameMerged(new CellRangeAddress(row, row, 0, 13), sheet1, workbook);
			row++;

			Row subHeaderRow = sheet1.createRow(row);
			ExcelUtil.setCellValue(style, subHeaderRow, 0, "Campaign Name");
			ExcelUtil.setCellValue(style, subHeaderRow, 1, "Agent ID");
			ExcelUtil.setCellValue(style, subHeaderRow, 2, "Customer No");
			ExcelUtil.setCellValue(style, subHeaderRow, 3, "Contacts Called");
			ExcelUtil.setCellValue(style, subHeaderRow, 4, "Survey");
			ExcelUtil.setCellValue(style, subHeaderRow, 5, "Channel");
			ExcelUtil.setCellValue(style, subHeaderRow, 6, "Q1 Response");
			ExcelUtil.setCellValue(style, subHeaderRow, 7, "Start Time");
			ExcelUtil.setCellValue(style, subHeaderRow, 8, "End Time");
			ExcelUtil.setCellValue(style, subHeaderRow, 9, "Retries Count");
			ExcelUtil.setCellValue(style, subHeaderRow, 10, "SMS Triggered");
			ExcelUtil.setCellValue(style, subHeaderRow, 11, "SMS Triggered DateTime");
			ExcelUtil.setCellValue(style, subHeaderRow, 12, "Web Q Response 1");
			ExcelUtil.setCellValue(style, subHeaderRow, 13, "Web Q Response 2");

			// ExcelUtil.setCellValue(style, subHeaderRow, 11, "No Response");

			try {
				row++;
				if (contactSummaryList != null && !contactSummaryList.isEmpty()) {
					for (Object[] obj : contactSummaryList) {

						//
						Row subHeaderValue = sheet1.createRow(row);
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 0,
								returnEmptyForNULL(String.valueOf(obj[0])));
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 1,
								returnEmptyForNULL(String.valueOf(obj[1])));
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 2,
								returnEmptyForNULL(String.valueOf(obj[2])));

						String callStatus = (String) obj[3];
						if (callStatus != null && !callStatus.equalsIgnoreCase("NEW")) {
							callStatus = "YES";
						} else {
							callStatus = "NO";
						}
						String surveyRating = (String) obj[4];
						String repondedStatus = null;
						String channel = null;
						if (surveyRating != null && !surveyRating.isEmpty()) {
							repondedStatus = "RESPONDED";
							channel = "IVR";
							ExcelUtil.setCellValue(styleContent, subHeaderValue, 5, channel);
							String startTime = returnEmptyForNULL(String.valueOf(obj[5]));
							String endTime = returnEmptyForNULL(String.valueOf(obj[6]));
							ExcelUtil.setCellValue(styleContent, subHeaderValue, 7, startTime);
							ExcelUtil.setCellValue(styleContent, subHeaderValue, 8, endTime);

						} else {
							repondedStatus = "NOT RESPONDED";

						}
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 3, callStatus);
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 4, repondedStatus);
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 5, channel);
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 6,
								returnEmptyForNULL(String.valueOf(obj[4])));
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 7,
								returnEmptyForNULL(String.valueOf(obj[5])));
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 8,
								returnEmptyForNULL(String.valueOf(obj[6])));
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 9,
								returnEmptyForNULL(String.valueOf(obj[7])));
						ExcelUtil.setCellValue(styleContent, subHeaderValue, 10,
								returnEmptyForNULL(String.valueOf(obj[8])));

						String smsTriggered = String.valueOf(obj[8]);
						if (smsTriggered != null && smsTriggered.equalsIgnoreCase("YES")) {
							//channel = "WEB";
							ExcelUtil.setCellValue(styleContent, subHeaderValue, 11, String.valueOf(obj[9]));
							int j = 12;

							List<Object> listWebResponse = getWebResponse((String) obj[10]);

							if (listWebResponse != null && !listWebResponse.isEmpty()) {
								logger.info("download sms report :: "+listWebResponse.toString());
								String smsTriggeredTime = (String) listWebResponse.get(0);
								String smsresponeTime = (String) listWebResponse.get(1);
								String webResponse1 = returnEmptyForNULL((String) listWebResponse.get(2)); 
								String webResponse2 = returnEmptyForNULL((String) listWebResponse.get(3));
								channel = "WEB";
								repondedStatus = "RESPONDED";
								ExcelUtil.setCellValue(styleContent, subHeaderValue, 7, smsTriggeredTime);
								ExcelUtil.setCellValue(styleContent, subHeaderValue, 8, smsresponeTime);
								ExcelUtil.setCellValue(styleContent, subHeaderValue, 12, webResponse1); 
								ExcelUtil.setCellValue(styleContent, subHeaderValue, 13, webResponse2);
							} else {
								logger.info("List Response NULL");
								ExcelUtil.setCellValue(styleContent, subHeaderValue, 7,
										returnEmptyForNULL(String.valueOf(obj[5])));
								ExcelUtil.setCellValue(styleContent, subHeaderValue, 8,
										returnEmptyForNULL(String.valueOf(obj[6])));
								ExcelUtil.setCellValue(styleContent, subHeaderValue, 12,"");
								ExcelUtil.setCellValue(styleContent, subHeaderValue, 13,"");
							}

							/*
							 * for (int i = 0; i < listWebResponse.size(); i++) {
							 * 
							 * // if (listWebResponse.get(i) instanceof Timestamp) { // Timestamp
							 * startTimeStamp = (Timestamp) obj[5]; // SimpleDateFormat sdf = new
							 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // String formattedStartTime =
							 * sdf.format(startTimeStamp); // ExcelUtil.setCellValue(styleContent,
							 * subHeaderValue, j, formattedStartTime); // } else {
							 * ExcelUtil.setCellValue(styleContent, subHeaderValue, j, (String)
							 * listWebResponse.get(i)); // } j++; }
							 */


							ExcelUtil.setCellValue(styleContent, subHeaderValue, 5, channel);
							ExcelUtil.setCellValue(styleContent, subHeaderValue, 4, repondedStatus);
						} else {
							logger.info("List Response NULL");
						}

						// ExcelUtil.setCellValue(styleContent, subHeaderValue, 10,
						// String.valueOf(obj[10]));
						// ExcelUtil.setCellValue(styleContent, subHeaderValue, 11,
						// String.valueOf(obj[10]));
						row++;
					}
				}
				ExcelUtil.setRegionBorderWithMedium(new CellRangeAddress(firstRow, row - 1, 0, 13), sheet1, workbook);
				sheet1.autoSizeColumn(10, true);
				sheet1.autoSizeColumn(11, true);
				sheet1.autoSizeColumn(12, true);
				sheet1.autoSizeColumn(6, true);
				sheet1.autoSizeColumn(7, true);
				sheet1.autoSizeColumn(8, true);
				sheet1.autoSizeColumn(9, true);
				sheet1.autoSizeColumn(6, false);
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				FileOutputStream outputStream = new FileOutputStream(fileName);
				workbook.write(outputStream);
				workbook.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			File file = new File(fileName);
			InputStreamResource resource1 = null;
			try {
				resource1 = new InputStreamResource(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
					.contentType(MediaType.parseMediaType("application/octet-stream")).contentLength(file.length())
					.body(resource1);

		} catch (Exception e) {
		}
		return null;
	}

	public String returnEmptyForNULL(String data) {
		return data != null && !data.equalsIgnoreCase("null") ? data : "";
	}

	@Override
	public Map<String, String> getContMappDetail() {
		Map<String, String> mapDynamicCont=null;
		try {
			mapDynamicCont=new LinkedHashMap<>();
			List<Object[]> resultList=campaignDao.getContMappingDet(account);
			if (resultList != null && !resultList.isEmpty()) {
				for (Object[] obj : resultList) {
					int numberofColumns=Integer.parseInt((String)obj[0]);
					for(int i=1;i<=numberofColumns;i++) {
						String value=String.valueOf(obj[i]);
						mapDynamicCont.put("reserve_"+i,value);
					}
				}
			}
			logger.info("Get Contact Mapping Data :"+mapDynamicCont);
		}catch(Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Exception :" + str.toString());
		}
		return mapDynamicCont;
	}

	@Override
	public boolean createDynamicContact(DynamicContactDetDto dynContactDetDto) {
		boolean isCreated;
		try {
			Map<String,String> mapDynamicMapField=getContMappDetail();
			isCreated = campaignDao.createDynamicContact(dynContactDetDto,mapDynamicMapField);
			logger.info("Is Created or not : " + isCreated);
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Exception :" + str.toString());
			return false;
		}
		return isCreated;
	}

	public List<DynamicContactDetDto> getDynamicContactDet(String campaign_id) {
		List<DynamicContactDetDto> dynamicContList=null;
		try {
			Map<String,String> mapDynamicMapField=getContMappDetail();
			dynamicContList=campaignDao.getDynamicContactDet(mapDynamicMapField,campaign_id);
		}catch(Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Exception :" + str.toString());
		}
		return dynamicContList;
	}

	@Override
	public ResponseEntity<GenericResponse> updateAgentDynamicContact(List<DynamicContactDetDto> dynamiccontactDetList) {
		GenericResponse genericResponse = new GenericResponse();
		boolean isUpdated =false;
		try {
			for(DynamicContactDetDto dynamicContact:dynamiccontactDetList)
			{

				String ActionId=dynamicContact.getActionId();
				String Agent_userid=dynamicContact.getAgent_userid();
				String campaignId=dynamicContact.getCampaignId();
				String mobileNumber=dynamicContact.getCustomerMobileNumber();
				isUpdated = campaignDao.updateAgentDynamicContact(ActionId,Agent_userid,campaignId,mobileNumber);
				logger.info("Assigned agent and contact : "+isUpdated );
				isUpdated=true;
			}

			if (isUpdated) {
				genericResponse.setStatus(200);
				genericResponse.setValue("Success");
				genericResponse.setMessage("Updating Agent- contact details successfully");
			} else {
				genericResponse.setStatus(400);
				genericResponse.setValue("Failure");
				genericResponse.setMessage("Error occured while updating Agent- contact details");
			}
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::updating Agent- contact details " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("Error occured while updating Agent- contact details");
		}

		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}


	@Override
	public ResponseEntity<GenericResponse> updateAssignedAgentDynamicContact(List<DynamicContactDetDto> dynamiccontactDetList) {
		GenericResponse genericResponse = new GenericResponse();
		boolean isUpdated =false;
		try {
			for(DynamicContactDetDto dynamicContact:dynamiccontactDetList)
			{

				String ActionId=dynamicContact.getActionId();
				String Agent_userid=dynamicContact.getAgent_userid();
				String campaignId=dynamicContact.getCampaignId();
				String mobileNumber=dynamicContact.getCustomerMobileNumber();
				isUpdated = campaignDao.updateAgentDynamicContact(ActionId,Agent_userid,campaignId,mobileNumber);
				logger.info("Assigned agent and contact : "+isUpdated );
				isUpdated=true;
			}

			if (isUpdated) {
				genericResponse.setStatus(200);
				genericResponse.setValue("Success");
				genericResponse.setMessage("Updating Agent- contact details successfully");
			} else {
				genericResponse.setStatus(400);
				genericResponse.setValue("Failure");
				genericResponse.setMessage("Error occured while updating Agent- contact details");
			}
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::updating Agent- contact details " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("Error occured while updating Agent- contact details");
		}

		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}

	//Map<String,List<DynamicContactDetDto>> 
	@Override
	public  Map<String,List<DynamicContactDetDto>> getAgentBasedContactDetail(String campaign_id) {
		Map<String,List<DynamicContactDetDto>> dynamicContListMap=null;
		try {
			Map<String,String> mapDynamicMapField=getContMappDetail();
			dynamicContListMap=campaignDao.getcampBasedAssignedContactDetail(mapDynamicMapField,campaign_id);
		}catch(Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Exception :" + str.toString());
		}
		return dynamicContListMap;
	}

	@Override
	public  Map<String,List<DynamicContactDetDto>> getPreviewAgentBasedContactDetail(String agent_userid) {
		Map<String,List<DynamicContactDetDto>> dynamicContListMap=null;
		try {
			Map<String,String> mapDynamicMapField=getContMappDetail();
			dynamicContListMap=campaignDao.getPreviewAgentBasedContactDetail(mapDynamicMapField,agent_userid);
		}catch(Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Exception :" + str.toString());
		}
		return dynamicContListMap;
	}


	@Override
	public  Map<String,List<DynamicContactDetDto>> getSupervisorAgentContactDet(String Supervisor) {
		Map<String,List<DynamicContactDetDto>> dynamicContListMap=null;
		try {
			Map<String,String> mapDynamicMapField=getContMappDetail();
			dynamicContListMap=campaignDao.getSupervisorAgentContactDet(mapDynamicMapField,Supervisor);
		}catch(Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Exception :" + str.toString());
		}
		return dynamicContListMap;
	}

	@Override
	public String getExtn(){
		try {
			return campaignDao.getExtn();
		}catch(Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Exception :" + str.toString());
		}
		return null;
	}

	@Override
	public DynamicContactDetDto getCustomerDetail(String customerNumber) {
		return campaignDao.getCustomerDetail(customerNumber);
	}

}

