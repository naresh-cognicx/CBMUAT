package com.cognicx.AppointmentRemainder.controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;


import com.cognicx.AppointmentRemainder.Request.*;
import com.cognicx.AppointmentRemainder.service.AgentService;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cognicx.AppointmentRemainder.Dto.ContactDetDto;
import com.cognicx.AppointmentRemainder.Dto.DncContactDto;
import com.cognicx.AppointmentRemainder.Dto.DynamicContactDetDto;
import com.cognicx.AppointmentRemainder.Dto.SurveyContactDetDto;
import com.cognicx.AppointmentRemainder.Dto.UploadHistoryDto;

import com.cognicx.AppointmentRemainder.Request.CampaignDetRequest;
import com.cognicx.AppointmentRemainder.Request.CampaignRealTimeDashboard;
import com.cognicx.AppointmentRemainder.Request.CampaignStatus;
import com.cognicx.AppointmentRemainder.Request.CampaignWeekDetRequest;
import com.cognicx.AppointmentRemainder.Request.DNCDetRequest;
import com.cognicx.AppointmentRemainder.Request.ReportRequest;
import com.cognicx.AppointmentRemainder.Request.UpdateAutoCallRequest;
import com.cognicx.AppointmentRemainder.Request.UpdateCallDetRequest;
import com.cognicx.AppointmentRemainder.response.GenericResponse;
import com.cognicx.AppointmentRemainder.response.GenericResponseReport;
import com.cognicx.AppointmentRemainder.service.CampaignService;
import com.cognicx.AppointmentRemainder.util.LicenseUtil;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

@RestController
@CrossOrigin
@RequestMapping("/campaign")
public class CampaignController {

	@Autowired
	CampaignService campaignService;

	@Value("${app.isFTP}")
	private String isFTP;

	@Value("${app.fileDirectory}")
	private String fileDirectory;

	@Value("${call.apiurl.autocalls}")
	private String callApiAutoCall;

	@Value("${call.apiurl.SurveyApi}")
	private String SurveyApi;


	@Value("${call.apiurl.token.url}")
	private String tokenurl;

	@Value("${call.apiurl.token.username}")
	private String userName;

	@Value("${call.apiurl.token.password}")
	private String password;

	@Value("${defaultCampaign}")
	private String defaultcampaign;

	//	@Value("${license.key}")
	private String licenseKey;

	@Autowired
	private LicenseUtil licenseUtil;

	private LocalDate expireDate;

	private static final List<String> remListforRetry = new CopyOnWriteArrayList<>();
	private static final List<String> newRemList = new CopyOnWriteArrayList<>();


	@Value("${failure.filediectory}")
	private String failureDirectory;

	@Value("${staticFields}")
	private String staticFields;

	@Value("${account}")
	private String account;


	private static final Logger logger = LoggerFactory.getLogger(CampaignController.class);

	// @Scheduled(cron = "0 0/2 * * * *")
	@PostMapping("/uploadSftpContact")
	public void setupJsch() throws JSchException, SftpException, IOException {
		JSch jsch = new JSch();
		Session session = null;
		ChannelSftp sftpChannel = null;
		boolean isFileFound = true;
		try {
			String fileTimestamp = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
			List<CampaignDetRequest> campaignDetList = campaignService.getCampaignDetList();
			if (campaignDetList != null && !campaignDetList.isEmpty()) {
				for (CampaignDetRequest campaignDetRequest : campaignDetList) {
					isFileFound = true;
					String fileName = campaignDetRequest.getFileName();
					logger.info("****Getting SFTP session****");
					session = jsch.getSession(campaignDetRequest.getFtpUsername(), campaignDetRequest.getFtpLocation(),
							22);
					session.setConfig("StrictHostKeyChecking", "no");
					session.setPassword(campaignDetRequest.getFtpPassword());
					InputStream stream = null;
					if (isFTP != null && "true".equalsIgnoreCase(isFTP)) {
						session.connect();
						Channel channel = session.openChannel("sftp");
						channel.connect();
						sftpChannel = (ChannelSftp) channel;
						logger.info("****Got SFTP channel ****");
						// sftpChannel.cd("/www/eappzz.com/reminder1");
						logger.info(
								"****Getting '" + campaignDetRequest.getFileName() + "' file from SFTP channel ****");
						try {
							stream = sftpChannel.get(campaignDetRequest.getFileName());
						} catch (SftpException e) {
							logger.error("SftpException occurred in Retriving" + campaignDetRequest.getFileName()
									+ "file from SFTP");
							isFileFound = false;
						} catch (Exception e) {
							logger.error("Exception occurred in Retriving" + campaignDetRequest.getFileName()
									+ "file from SFTP");
							isFileFound = false;
						}
					} else {
						stream = null;
					}
					if (isFileFound) {


						BigInteger historyId = getUploadHistoryid(campaignDetRequest, fileName);

						List<ContactDetDto> contactDetList = csvToData(stream, historyId, isFTP,
								fileDirectory, fileTimestamp, failureDirectory, campaignDetList, new
										ArrayList<>(), "", "");
						logger.info("****Converted CSV DATA to Object****");
						if (stream != null) stream.close();
						logger.info("****Inserting contact details to DB Table****");
						for
						(ContactDetDto contactDetDto : contactDetList) {
							campaignService.createContact(contactDetDto);
						}


						String[] file = fileName.split("\\.");
						if (sftpChannel != null) {
							sftpChannel.rename(fileName, file[0] + "_" + fileTimestamp + "." + file[1]);
							sftpChannel.exit();
						}
					}
					session.disconnect();
				}
			}
		} catch (IOException io) {
			logger.error("IO Exception occurred file upload from SFTP server due to " + io.getMessage());
		} catch (JSchException e) {
			logger.error("JSchException occurred file upload from SFTP server due to " + e.getMessage());
			e.printStackTrace();
		} catch (SftpException e) {
			logger.error("SftpException occurred file upload from SFTP server due to " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Exception occurred during file upload from SFTP server due to " + e.getMessage());
		} finally {
			if (sftpChannel != null)
				sftpChannel.exit();
			session.disconnect();
		}
	}

	private BigInteger getUploadHistoryid(CampaignDetRequest campaignDetRequest, String fileName) {
		UploadHistoryDto uploadHistoryDto = new UploadHistoryDto();
		uploadHistoryDto.setCampaignId(campaignDetRequest.getCampaignId());
		uploadHistoryDto.setCampaignName(campaignDetRequest.getCampaignName());
		uploadHistoryDto.setFilename(fileName);
		BigInteger historyId = campaignService.insertUploadHistory(uploadHistoryDto);
		return historyId;
	}

	// @Scheduled(cron = "0 0/2 * * * *")
	@PostMapping("/uploadContact")
	public void uploadContact() {
		FTPClient client = new FTPClient();
		InputStream in;
		try {
			List<CampaignDetRequest> campaignDetList = campaignService.getCampaignDetList();
			if (campaignDetList != null && !campaignDetList.isEmpty()) {
				for (CampaignDetRequest campaignDetRequest : campaignDetList) {
					client.connect(campaignDetRequest.getFtpLocation(), 21);
					boolean isSuccess = client.login(campaignDetRequest.getFtpUsername(),
							campaignDetRequest.getFtpPassword());
					//					List<ContactDetDto> contactDetList = csvToData(null);
					//					for (ContactDetDto contactDetDto : contactDetList) {
					//						campaignService.createContact(contactDetDto);
					//					}
					if (isSuccess) {
						List<String> fileName = new ArrayList<>();
						// client.changeWorkingDirectory("/eappzz.com/reminder1");
						FTPFile[] files = client.listFiles();
						for (FTPFile file : files) {
							if (file.isFile()) {
								fileName.add(file.getName());
								logger.info("File Names file.getName()");
							}
						}
						if (fileName.contains(campaignDetRequest.getFileName())) {
							logger.info("Inside If condition");
							in = client.retrieveFileStream(campaignDetRequest.getFileName());
							// List<ContactDetDto> contactDetList = csvToData(in, null);
							boolean store = client.storeFile("tez.csv", in);
							in.close();
							String newFileName = "campaign_new.csv";
							boolean isRenamed = client.rename(campaignDetRequest.getFileName(), "test.csv");
							logger.info("Renamed Status:: " + isRenamed);
							client.disconnect();
							//							for (ContactDetDto contactDetDto : contactDetList) {
							//								campaignService.createContact(contactDetDto);
							//							}
						} else {
							logger.info("In ftp Fileupload:: expected file '" + campaignDetRequest.getFileName()
									+ "' is not there");
							client.disconnect();
						}

					}
				}
			}
			//			client.connect("eappzz.com", 21);
			//			boolean isSuccess = client.login("test1@eappzz.com", "2u42*(1t5#to");
			//			client.changeWorkingDirectory("/eappzz.com/reminder1");
			//
			//			String filename = "campaign.csv";
			//			InputStream in = client.retrieveFileStream(filename);

			// List<ContactDetDto> contactDetList = csvToData(in);
			// client.disconnect();
			// in.close();
			// for (ContactDetDto contactDetDto : contactDetList) {
			// campaignService.createContact(contactDetDto);
			// }
			// retrieveFile("/" + filename, in);
		} catch (Exception e) {
			logger.error("Error occured in FTP File upload:: " + e);
			e.printStackTrace();
		} finally {
			try {
				client.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// return null;
	}

	private static List<ContactDetDto> csvToData(InputStream is, BigInteger historyId, String isFTP,
												 String fileDirectory, String fileTimestamp, String failureDirectory,
												 List<CampaignDetRequest> campaignDetList, List<ContactDetDto> failureList, String campaignId, String campaignName) {
		List<ContactDetDto> contactList = null;
		CSVPrinter csvPrinter = null;
		CSVParser csvParser = null;
		BufferedReader fileReader = null;
		try {
			StringBuilder reason = null;
			if (isFTP != null && "true".equalsIgnoreCase(isFTP)) {
				fileReader = new BufferedReader(new InputStreamReader(is));
			} else {
				fileReader = new BufferedReader(new FileReader(fileDirectory));
			}
			csvParser = new CSVParser(fileReader,
					CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());

			contactList = new ArrayList<>();
			// failureList = new ArrayList<>();
			Iterable<CSVRecord> csvRecords = csvParser.getRecords();
			for (CSVRecord csvRecord : csvRecords) {
				reason = new StringBuilder();
				ContactDetDto contactDet = new ContactDetDto();
				contactDet.setCampaignId(campaignId);
				contactDet.setCampaignName(campaignName);
				contactDet.setSubskill_set(csvRecord.get("Sub Skillset"));
				contactDet.setLastFourDigits(csvRecord.get("Last 4 Digits"));
				contactDet.setCustomerMobileNumber(csvRecord.get("CUST_MOBILE_NUMBER"));
				contactDet.setTotalDue(csvRecord.get("Total Due"));
				contactDet.setMinPayment(csvRecord.get("Minimum Payment"));
				contactDet.setDueDate(csvRecord.get("Due Date"));
				// contactDet.setContactId(csvRecord.get("contact_id"));
				contactDet.setHistoryId(historyId);
				logger.info("Contact Details Data : " + contactDet.toString());
				if (validateFileData(csvRecord, reason, campaignDetList, contactDet)) {
					contactList.add(contactDet);
				} else {
					contactDet.setFailureReason(reason.toString());
					failureList.add(contactDet);
				}
			}
			csvParser.close();
			if (!failureList.isEmpty()) {
				// csvPrinter = failureCsvData(fileTimestamp, failureList, failureDirectory);
			}
		} catch (IOException e) {
			logger.error("fail to parse CSV file: " + e.getMessage());
		} catch (Exception e) {
			logger.error("fail to parse CSV file: " + e.getMessage());
		} finally {
			try {
				csvParser.close();
				if (csvPrinter != null)
					csvPrinter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return contactList;
	}


	public List<DynamicContactDetDto> dynamiccsvToData(InputStream is, BigInteger historyId, String isFTP,
													   String fileDirectory, String fileTimestamp, String failureDirectory,
													   List<CampaignDetRequest> campaignDetList, List<DynamicContactDetDto> failureList, String campaignId, String campaignName) {
		List<DynamicContactDetDto> dynamiccontactList = null;
		CSVPrinter csvPrinter = null;
		CSVParser csvParser = null;
		BufferedReader fileReader = null;
		try {
			StringBuilder reason = null;
			if (isFTP != null && "true".equalsIgnoreCase(isFTP)) {
				fileReader = new BufferedReader(new InputStreamReader(is));
			} else {
				fileReader = new BufferedReader(new FileReader(fileDirectory));
			}
			csvParser = new CSVParser(fileReader,
					CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());

			dynamiccontactList = new ArrayList<>();
			// failureList = new ArrayList<>();
			Iterable<CSVRecord> csvRecords = csvParser.getRecords();
			logger.info("Header Names :" + csvParser.getHeaderMap());
			List<String> headerNames = csvParser.getHeaderNames();

			String[] arrstaticFields = staticFields.split("\\|");
			List<String> listStaticFields = null;

			if (arrstaticFields != null) {
				listStaticFields = Arrays.asList(arrstaticFields);
			}


			for (CSVRecord csvRecord : csvRecords) {
				reason = new StringBuilder();
				DynamicContactDetDto dynacontactDet = new DynamicContactDetDto();
				dynacontactDet.setCampaignId(campaignId);
				dynacontactDet.setCampaignName(campaignName);
				//dynacontactDet.setSubskill_set(csvRecord.get("Sub Skillset"));
				dynacontactDet.setCustomerMobileNumber(csvRecord.get("CUST_MOBILE_NUMBER"));

				//logger.info("Sub Skill Set : "+csvRecord.get("Sub Skillset"));
				logger.info("static field : " + staticFields);
				logger.info("dynamic contact det :: " + dynacontactDet.toString());
				//	Iterator<String> it=csvRecord.iterator();
				Map<String, String> mapDynamicValues = new LinkedHashMap();

				for (String header : headerNames) {
					if (listStaticFields == null || !listStaticFields.contains(header)) {
						mapDynamicValues.put(header, csvRecord.get(header));
					}
				}
				dynacontactDet.setMapDynamicFields(mapDynamicValues);
				// contactDet.setContactId(csvRecord.get("contact_id"));
				dynacontactDet.setHistoryId(historyId);
				logger.info("Dynamic Contacts :" + dynacontactDet.getMapDynamicFields() + " :: Campaign ID " + dynacontactDet.getCampaignId());
				dynamiccontactList.add(dynacontactDet);
				/*
				 * if (validateFileData(csvRecord, reason, campaignDetList, dynacontactDet)) {
				 * dynamiccontactList.add(dynacontactDet); } else {
				 * dynacontactDet.setFailureReason(reason.toString());
				 * failureList.add(dynacontactDet); }
				 */
			}
			csvParser.close();
			if (!failureList.isEmpty()) {
				// csvPrinter = failureCsvData(fileTimestamp, failureList, failureDirectory);
			}
		} catch (IOException e) {
			logger.error("fail to parse CSV file: " + e.getMessage());
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace();
			logger.error("fail to parse CSV file: " + str.toString());
		} finally {
			try {
				csvParser.close();
				if (csvPrinter != null)
					csvPrinter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return dynamiccontactList;
	}


	private static CSVPrinter failureCsvData(String fileTimestamp, List<ContactDetDto> failureList,
											 String failureDirectory) throws IOException {
		CSVPrinter csvPrinter;
		List<String> headerlist = new ArrayList<>(Arrays.asList("campaign id", "campaign name",
				"CUST_MOBILE_NUMBER", "reason"));
		//        List<String> headerlist = new ArrayList<>(Arrays.asList("campaign id", "campaign name", "subskill_set",
		//                "language", "customer_mobile_number", "reason"));
		final CSVFormat format = CSVFormat.DEFAULT.withHeader(headerlist.toArray(new String[0]));
		Writer writer = Files.newBufferedWriter(Paths.get(failureDirectory + fileTimestamp + ".csv"));
		csvPrinter = new CSVPrinter(writer, format);

		for (ContactDetDto contactDet : failureList) {
			csvPrinter
					.printRecord(new ArrayList<>(Arrays.asList(contactDet.getCampaignId(), contactDet.getCampaignName(),
							contactDet.getLastFourDigits(), contactDet.getCustomerMobileNumber(), contactDet.getTotalDue(),
							contactDet.getMinPayment(), contactDet.getDueDate(), contactDet.getFailureReason())));
		}
		csvPrinter.flush();
		return csvPrinter;
	}


	/*
	 * private static boolean validateFileData(CSVRecord csvRecord, StringBuilder
	 * reason, List<CampaignDetRequest> campaignDetList, ContactDetDto contactDet) {
	 * boolean isValid = true; if (csvRecord.get("campaign id") == null ||
	 * csvRecord.get("campaign id").isEmpty()) {
	 * reason.append("Campaign ID is missing;"); isValid = false; } else {
	 * CampaignDetRequest commonDetail = campaignDetList.stream() .filter(x ->
	 * csvRecord.get("campaign id").equalsIgnoreCase(x.getCampaignId())).findAny()
	 * .orElse(null); if (commonDetail == null) {
	 * reason.append("Campaign Id is Incorrect;"); isValid = false; } } if
	 * (csvRecord.get("campaign name") == null ||
	 * csvRecord.get("campaign name").isEmpty()) {
	 * reason.append("Campaign name is missing;"); isValid = false; } if
	 * (csvRecord.get("doctor name") == null ||
	 * csvRecord.get("doctor name").isEmpty()) {
	 * reason.append("Doctor name is missing;"); isValid = false; } if
	 * (csvRecord.get("Patient name") == null ||
	 * csvRecord.get("Patient name").isEmpty()) {
	 * reason.append("Patient name is missing;"); isValid = false; } if
	 * (csvRecord.get("contact number") == null ||
	 * csvRecord.get("contact number").isEmpty()) {
	 * reason.append("Contact name is missing;"); isValid = false; } if
	 * (csvRecord.get("language") == null || csvRecord.get("language").isEmpty()) {
	 * reason.append("language is missing;"); isValid = false; } if
	 * (csvRecord.get("appointment date") == null &&
	 * csvRecord.get("appointment date").isEmpty()) {
	 * reason.append("Appointment date is missing;"); isValid = false; } else { try
	 * { new SimpleDateFormat("yyyy-mm-dd HH:mm:ss").parse(csvRecord.
	 * get("appointment date")); } catch (Exception e) {
	 * reason.append("Appointment date format is incorrect;"); isValid = false; } }
	 * return isValid; }
	 */

	private static boolean validateFileData(CSVRecord csvRecord, StringBuilder reason,
											List<CampaignDetRequest> campaignDetList, ContactDetDto contactDet) {
		boolean isValid = true;
		/*
		 * if (csvRecord.get("Last 4 Digits") == null ||
		 * csvRecord.get("Last 4 Digits").isEmpty()) {
		 * reason.append("Last 4 Digits is missing;"); isValid = false; }
		 */
		if (csvRecord.get("CUST_MOBILE_NUMBER") == null || csvRecord.get("CUST_MOBILE_NUMBER").isEmpty()) {
			reason.append("Customer Mobile Number is missing;");
			isValid = false;
		}
		/*
		 * if (csvRecord.get("Total Due") == null ||
		 * csvRecord.get("Total Due").isEmpty()) {
		 * reason.append("Total Due is missing;"); isValid = false; } if
		 * (csvRecord.get("Minimum Payment") == null ||
		 * csvRecord.get("Minimum Payment").isEmpty()) {
		 * reason.append("Minimum Payment is missing;"); isValid = false; } if
		 * (csvRecord.get("Due Date") == null && csvRecord.get("Due Date").isEmpty()) {
		 * reason.append("Due Date is missing;"); isValid = false; }
		 */
		else {
			try {
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(csvRecord.get("Due Date"));
			} catch (Exception e) {
				reason.append(" Date format is incorrect;");
				isValid = false;
			}
		}


		if (!isValid)
			logger.info("validateFileData : " + reason);
		return isValid;
	}


	//	@Scheduled(cron = "0 0/30 * * * *")
	//	@PostMapping("/httpurl")
	//	public void executeFailure() {
	//
	//		try {
	//			int concurrent;
	//			long timeDifference;
	//			long timeDifference1;
	//			long retryDifference;
	//			boolean isMaxAdvTime = true;
	//			Date currentDate = new Date();
	//			Date weekStartDate = null;
	//			Date weekEndDate = null;
	//			DateFormat dateTimeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	//			DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
	//			DateFormat time = new SimpleDateFormat("hh:mm a");
	//			DateFormat WeekDaytimeFormat = new SimpleDateFormat("HH:mm:ss");
	//			DateFormat weekDayFormat = new SimpleDateFormat("EEEE");
	//			String weekDay = String.valueOf(weekDayFormat.format(currentDate));
	//			logger.info("**** Scheduler Started ****");
	//			List<CampaignDetRequest> campaignDetList = campaignService.getCampaignDetList();
	//			Map<String, List<ContactDetDto>> contactDetMap = campaignService.getContactDet();
	//			logger.info("**** Campaign and Contact details fetched ****");
	//			if (campaignDetList != null && !campaignDetList.isEmpty()) {
	//				logger.info("**** Campaign details not empty ****");
	//				for (CampaignDetRequest campaignDetRequest : campaignDetList) {
	//					//currentDate = new Date();
	//
	//					logger.info("New :: Contact Details Keys :"+contactDetMap.keySet().toString());
	//					logger.info("New :: Campaign ID :"+campaignDetRequest.getCampaignId());
	//
	//					//Added on 05/02/2024
	//					CampaignStatus campaignStatus=new CampaignStatus();
	//					campaignStatus.setCampaignId(campaignDetRequest.getCampaignId());
	//					boolean campStatus=campaignService.getCampaignStatus(campaignStatus);
	//					logger.info("New :: Campaign status :"+campStatus);
	//					if (campStatus) {
	//						logger.info("Campaign status is enabled . Hence Campaaign scheduler is called");
	//						if (contactDetMap != null && contactDetMap.containsKey(campaignDetRequest.getCampaignId())) {
	//							logger.info("**** Contact details contain campaign key ****");
	//							if (campaignDetRequest.getConcurrentCall() != null
	//									&& !campaignDetRequest.getConcurrentCall().isEmpty())
	//								concurrent = Integer.parseInt(campaignDetRequest.getConcurrentCall());
	//							else
	//								concurrent = 5;
	//							String campaignDateStr = campaignDetRequest.getStartDate() + " "
	//									+ campaignDetRequest.getStartTime();
	//							Date campaignStartdate = dateTimeformat.parse(campaignDateStr);
	//							Date campaignEndDate = dateTimeformat
	//									.parse(campaignDetRequest.getEndDate() + " " + campaignDetRequest.getEndTime());
	//							for (CampaignWeekDetRequest campaignWeekDetRequest : campaignDetRequest.getWeekDaysTime()) {
	//								if (weekDay.equalsIgnoreCase(campaignWeekDetRequest.getDay())) {
	//									weekStartDate = WeekDaytimeFormat.parse(campaignWeekDetRequest.getStartTime());
	//									weekEndDate = WeekDaytimeFormat.parse(campaignWeekDetRequest.getEndTime());
	//								}
	//							}
	//							List<ContactDetDto> contactDetList = contactDetMap.get(campaignDetRequest.getCampaignId());
	//							if (contactDetList != null && !contactDetList.isEmpty()) {
	//								logger.info("**** Contact details condition  ****");
	//								int i = 1, j = 1;
	//								for (ContactDetDto contactDetDto : contactDetList) {
	//									logger.info("**** inside contact details loop  ****");
	//									logger.info("**** Contact ID : ****"+contactDetDto.getContactId());
	//									isMaxAdvTime = true;
	//
	//									// Added on 13th March
	//									String dncID=campaignDetRequest.getDncId();
	//									List<String> dncContacts=campaignService.getDNSDetList(dncID);
	//									String contactNumber=contactDetDto.getContactNo();
	//									if(!(contactNumber!=null && dncContacts!=null && dncContacts.contains(contactNumber))) {
	//										Date appdate = dateTimeformat.parse(contactDetDto.getDueDate());
	//										Date appdateCallBefore = dateformat.parse(contactDetDto.getDueDate());
	//										logger.info("CampaignId: " + campaignDetRequest.getCampaignId() + "   **** outside date condition  **** "+ "  appdate:"+ appdate + " campaignStartdate :"+ campaignStartdate+ "  campaignEndDate: "+ campaignEndDate);
	//
	//										if (appdate.after(campaignStartdate) && appdate.before(campaignEndDate)) {
	//											logger.info("CampaignId: " + campaignDetRequest.getCampaignId() + "   **** inside contact details loop  **** "+"customer mobile number:"+contactDetDto.getCustomerMobileNumber()+ "  appdate:"+ appdate + " campaignStartdate :"+ campaignStartdate+ "  campaignEndDate: "+ campaignEndDate);
	//											timeDifference = appdate.getTime() - currentDate.getTime();
	//											timeDifference1 = appdateCallBefore.getDate() - currentDate.getDate();
	//
	//
	//											LocalDate start = LocalDate.of(currentDate.getYear(), (currentDate.getMonth()+1), currentDate.getDate());
	//											LocalDate end = LocalDate.of(appdateCallBefore.getYear(), (appdateCallBefore.getMonth()+1), appdateCallBefore.getDate());
	//											long dayDifference = start.until(end, ChronoUnit.DAYS);
	//
	//											logger.info("**** currentDate "+ " Year :" +currentDate.getYear()+ " Month :" + currentDate.getMonth()+ "Date :" + currentDate.getDate());
	//											logger.info("**** DueDate "+ " Year :" +appdateCallBefore.getYear()+ " Month :" + appdateCallBefore.getMonth()+ "Date :" + appdateCallBefore.getDate());
	//
	//
	//											logger.info("**** inside contact details loop  ****timeDifference: " + timeDifference +  "  Time difference1:  " + timeDifference1 + "  dayDifference:" + dayDifference + "  currentDate: " + currentDate  +
	//													"**** inside contact details loop  ****  CampaignId:  " + campaignDetRequest.getCampaignId() + "  CallBefore:" + campaignDetRequest.getCallBefore() + "  CampaignName: " + campaignDetRequest.getCampaignName()  );
	//											logger.info("New ::  call Before ="+campaignDetRequest.getCallBefore());
	//
	//											if (dayDifference == Integer.parseInt(campaignDetRequest.getCallBefore())) {
	//												if ("0".equalsIgnoreCase(campaignDetRequest.getCallBefore())) {
	//													long minutesDifference = TimeUnit.MILLISECONDS.toMinutes(timeDifference);
	//													String[] hourMin = campaignDetRequest.getMaxAdvNotice().split(":");
	//													long minutes = (Integer.parseInt(hourMin[0]) * 60)
	//															+ Integer.parseInt(hourMin[1]);
	//													logger.info("CampaignId: " + campaignDetRequest.getCampaignId() + "   **** minutesDifference :"+  minutesDifference + " contact details loop  **** minutes:"+ minutes);
	//													if (minutesDifference < minutes) {
	//														isMaxAdvTime = false;
	//													}
	//												}
	//												logger.info("CampaignId: " + campaignDetRequest.getCampaignId() + "   **** isMaxAdvTime :"+  isMaxAdvTime + " contact details loop  **** appdate:"+ appdate + " campaignStartdate :"+ campaignStartdate+ "  campaignEndDate: "+ campaignEndDate);
	//												if (isMaxAdvTime) {
	//													currentDate = new Date(); //Code added by SK Praveen Kumar for bug fix
	//
	//													logger.info("New :: Week Start Date :"+weekStartDate);
	//													logger.info("New :: Week End Date :"+weekEndDate);
	//													logger.info("New :: Currend Date :"+currentDate);
	//													if (WeekDaytimeFormat.parse(WeekDaytimeFormat.format(currentDate))
	//															.after(weekStartDate)
	//															&& WeekDaytimeFormat.parse(WeekDaytimeFormat.format(currentDate))
	//															.before(weekEndDate)) {
	//														logger.info("CampaignId: " + campaignDetRequest.getCampaignId() + "    **** WeekDaytimeFormat weekEndDate:"+  weekEndDate + " contact details loop  **** appdate:"+ appdate + " campaignStartdate :"+ campaignStartdate+ "  campaignEndDate: "+ campaignEndDate);
	//														if (contactDetDto.getCallRetryCount() != null && (Integer
	//																.parseInt(contactDetDto.getCallRetryCount()) <= Integer
	//																.parseInt(campaignDetRequest.getRetryCount()))) {
	//															Date updateddate = dateTimeformat
	//																	.parse(contactDetDto.getUpdatedDate());
	//															retryDifference = TimeUnit.MILLISECONDS
	//																	.toMinutes(currentDate.getTime() - updateddate.getTime());
	//															if ("New".equalsIgnoreCase(contactDetDto.getCallStatus())
	//																	|| retryDifference > Integer
	//																	.parseInt(campaignDetRequest.getRetryDelay())) {
	//																logger.info(
	//																		"**** All Conditions are satisfied going to make call For the contact ID : "+contactDetDto.getContactId()+"****");
	//																Date dueDate = dateTimeformat.parse(contactDetDto.getDueDate());
	//																Long dueUnixTime = dueDate.getTime() / 1000;
	//																Runnable obj1 = () -> {
	//
	//																	logger.info(
	//																			"**** Inside Thread API Thread API request****" );
	//
	//
	//																	Unirest.setTimeouts(0, 0);
	//																	try {
	//																		ContactDetDto dummycontact = new ContactDetDto();
	//																		dummycontact.setCampaignId(contactDetDto.getCampaignId());
	//																		dummycontact.setCampaignName(contactDetDto.getCustomerMobileNumber());
	//																		dummycontact.setContactNo(Long.toString(dueUnixTime));
	//																		dummycontact.setAppointmentDate(contactDetDto.getDueDate());
	//																		dummycontact.setCallStatus(contactDetDto.getContactId());
	//																		campaignService.createDummyContact(dummycontact);
	//
	//																		logger.info("*************Dummy Contact*********");
	//																		logger.info("*************Contact ID : *********"+contactDetDto.getContactId());
	//																		logger.info("campaignId"+dummycontact.getCampaignId());
	//																		logger.info("getCustomerMobileNumber"+ dummycontact.getCampaignName());
	//																		logger.info("language"+dummycontact.getLanguage());
	//																		logger.info("UnixTime"+ dummycontact.getContactNo());
	//																		logger.info("DueDate"+ dummycontact.getAppointmentDate());
	//																		logger.info("ContactId"+ dummycontact.getCallStatus());
	//
	//
	//
	//																		String request = "{\r\n    \"outcallerid\": \"044288407\",\r\n    \"siptrunk\": \"Avaya\",\r\n  "
	//																				+ "  \"phone\": \""+contactDetDto.getCustomerMobileNumber()+"\",\r\n   "
	//																				+ "  \"productid\": \""+contactDetDto.getProductID()+"\",\r\n   "
	//																				+ " \"language\": \""+contactDetDto.getMinPayment()+"\",\r\n    "
	//																				+"\",\r\n    \"dialplan\": \"\"nas-neuro\",\r\n "
	//																				+ " \"actionid\": \""+contactDetDto.getContactId()+"\"\r\n}";
	//																		logger.info(request);
	//
	//
	//
	//																		HttpResponse<String> response = Unirest.post(callApiAutoCall)
	//																				.header("Content-Type", "application/json")
	//																				.body("{\r\n    \"outcallerid\": \"044288407\",\r\n    \"siptrunk\": \"Avaya\",\r\n  "
	//																						+ "  \"phone\": \""+contactDetDto.getCustomerMobileNumber()+"\",\r\n   "
	//																						+ "  \"productid\": \""+contactDetDto.getProductID()+"\",\r\n   "
	//																						+ " \"language\": \""+contactDetDto.getMinPayment()+"\",\r\n    "
	//																						+ "  \"unixtime\": \""+dueUnixTime+"\",\r\n    \"timezone\": \"GST\",\r\n    \"dialplan\": \"nas-neuro\",\r\n   "
	//																						+ " \"actionid\": \""+contactDetDto.getContactId()+"\"\r\n}")
	//																				.asString();
	//																		logger.info(
	//																				"**** Inside Thread API Thread API response****" );
	//																		logger.info(response.getBody());
	//
	//																	} catch (UnirestException e1) {
	//																		logger.info(
	//																				"**** Inside Exception clause API Thread API ****" );
	//																		logger.error(e1.getMessage());
	//																		// TODO Auto-generated catch block
	//																		e1.printStackTrace();
	//																	}
	//
	//
	//																	logger.info("Request Success");
	//																	CloseableHttpClient httpclient = HttpClients
	//																			.createDefault();
	//																	HttpPost httppost = new HttpPost(callApiAutoCall);
	//																	List<NameValuePair> nvps = new ArrayList<NameValuePair>();
	//																	nvps.add(new BasicNameValuePair("outcallerid", "044288407"));
	//																	nvps.add(new BasicNameValuePair("siptrunk", "Avaya"));
	//																	nvps.add(new BasicNameValuePair("phone", contactDetDto.getCustomerMobileNumber()));
	//																	nvps.add(new BasicNameValuePair("language", contactDetDto.getLanguage()));
	//																	nvps.add(new BasicNameValuePair("productID", contactDetDto.getProductID()));
	//																	nvps.add(new BasicNameValuePair("unixtime", "1695454737"));
	//																	nvps.add(new BasicNameValuePair("timezone", "GST"));
	//																	nvps.add(new BasicNameValuePair("dialplan", "nas-neuro"));
	//																	long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
	//																	nvps.add(new BasicNameValuePair("actionid", contactDetDto.getContactId()));
	//																	for (NameValuePair name : nvps) {
	//																		logger.info("**** Call Request Parameters executeFailureAutoCalls****");
	//																		logger.info("Name value pair :"+ name.getValue());
	//																	}
	//
	//																	try {
	//																		httppost.setEntity(
	//																				new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
	//																	} catch (UnsupportedEncodingException e) {
	//																		e.printStackTrace();
	//																	}
	//																	/*
	//																	 * HttpResponse httpresponse; try { httpresponse =
	//																	 * httpclient.execute(httppost); logger.
	//																	 * info("**** Call made successfully for below details executeFailureAutoCalls****"
	//																	 * ); logger.info("custphone===== " +
	//																	 * contactDetDto.getCampaignName());
	//																	 * logger.info("docname===== " +
	//																	 * contactDetDto.getDueDate()); logger.info("docname===== "
	//																	 * + contactDetDto.getLastFourDigits());
	//																	 * logger.info("docname===== " +
	//																	 * contactDetDto.getMinPayment());
	//																	 * logger.info("docname===== " +
	//																	 * contactDetDto.getCustomerMobileNumber());
	//																	 * logger.info("docname===== " +
	//																	 * contactDetDto.getCallStatus());
	//																	 * logger.info("docname===== " +
	//																	 * contactDetDto.getTotalDue()); logger.info(
	//																	 * "Time for " + System.currentTimeMillis() + " : " +
	//																	 * contactDetDto.getCustomerMobileNumber()); Scanner sc =
	//																	 * new Scanner( httpresponse.getEntity().getContent());
	//																	 * logger.info(httpresponse.getEntity().getContent()
	//																	 * .toString()); while (sc.hasNext()) {
	//																	 * logger.info("***Call response***");
	//																	 * logger.info(sc.nextLine()); } } catch (IOException e) {
	//																	 * e.printStackTrace(); }
	//																	 *
	//																	 * try { httpclient.close(); } catch (IOException e) {
	//																	 * e.printStackTrace(); }
	//																	 */
	//																};
	//																//														updateCallDet(i, contactDetDto.getContactId(),
	//																//																contactDetDto.getCallRetryCount());
	//
	//																Thread t = new Thread(obj1);
	//																logger.info("New :: Thread Started :");
	//																logger.info("New :: Concurrent Value :"+concurrent);
	//																t.start();
	//																if (j > concurrent) {
	//																	logger.info("New :: Thread is going to Sleep");
	//																	Thread.sleep(25000 * concurrent);
	//																	logger.info("New :: Thread Resumed");
	//																	j = 0;
	//																}
	//																i++;
	//																j++;
	//															}else {
	//																logger.info("New :: contact details status is either not new or retry difference is greater than campaing retry difference for the contact ID : "+contactDetDto.getContactId());
	//																logger.info("New :: contact details status :"+contactDetDto.getCallStatus()+" :: Campaign Retry  :"+ campaignDetRequest.getRetryCount()+" Retry Difference :"+retryDifference);
	//															}
	//														}
	//													}
	//												}
	//											}
	//										}
	//									}else {
	//										logger.info("Contact is in DNC list, Hence not invoking campaign API");
	//									}
	//								}
	//							}
	//						}
	//					}
	//					else
	//					{
	//						logger.info("New :: Campaign Status is not true. Hence campaign API scheduler is not invoked");
	//					}
	//				}
	//			}
	//		}
	//		//		catch (MalformedURLException e) {
	//		//			e.printStackTrace();
	//		//		} catch (IOException e) {
	//		//			e.printStackTrace();
	//		//		}
	//		catch (Exception e) {
	//			logger.info("Error Occured in call Making due to : " + e.getMessage());
	//			e.printStackTrace();
	//		}
	//		// return null;
	//	}

	//    @Scheduled(cron = "0 0/2 * * * *")

	//    @Scheduled(cron = "0/5 * * * * *")
	//    public void test(){
	//        List<String> list = new ArrayList<>();
	//
	//        // Add elements to the list
	//        list.add("1");
	//        list.add("2");
	//        list.add("3");
	//
	//        // Output a message to the console
	//        Iterator<String> it = list.iterator();
	//        while (it.hasNext()) {
	//            String value = it.next();
	//            if ("2".equals(value)) {
	//                // Direct removal using iterator is not supported in CopyOnWriteArrayList
	//                list.remove("2");
	//            }
	//        }
	//
	//        // Print the list and a message
	//        System.out.println(list); // Should not contain "2"
	//        System.out.println("Modified list in scheduler using CopyOnWriteArrayList");
	//    }


	@Scheduled(cron = "${scheduling.job.cron}")
	@PostMapping("/httpurl")
	public void scheduledAPIInvoker() {
		//        if (expireDate != null || !expireDate.toString().isEmpty()) {
		//            if (expireDate.equals(LocalDate.now()) || expireDate.isBefore(LocalDate.now())) {
		//                logger.info("License Key has expired, Date: " + expireDate);
		//            } else {
		try {
			int concurrent;
			int val;
			long timeDifference = 0;
			long timeDifference1 = 0;
			long retryDifference = 0;
			boolean isMaxAdvTime = true;
			Date currentDate = new Date();
			Date weekStartDate = null;
			Date weekEndDate = null;
			Date weekStartDate1 = null;
			Date weekEndDate1 = null;
			DateFormat dateTimeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
			DateFormat time = new SimpleDateFormat("hh:mm a");
			DateFormat WeekDaytimeFormat = new SimpleDateFormat("HH:mm:ss");
			DateFormat weekDayFormat = new SimpleDateFormat("EEEE");
			String weekDay = String.valueOf(weekDayFormat.format(currentDate));
			logger.info("**** Scheduler Started ****");
			List<CampaignDetRequest> campaignDetList = campaignService.getCampaignDetList();
			Map<String, List<SurveyContactDetDto>> surveyContactDet = campaignService.getSurveyContDet();
			logger.info("**** Campaign and Contact details fetched ****");
			if (campaignDetList != null && !campaignDetList.isEmpty()) {
				logger.info("**** Campaign details not empty ****");
				for (CampaignDetRequest campaignDetRequest : campaignDetList) {
					logger.info("New :: Contact Details Keys :" + surveyContactDet.keySet().toString());
					CampaignStatus campaignStatus = new CampaignStatus();
					campaignStatus.setCampaignId(campaignDetRequest.getCampaignId());
					boolean campStatus = campaignService.getCampaignStatus(campaignStatus);
					//                    logger.info("New :: Campaign status :" + campStatus);
					logger.info("New :: Campaign ID : {} , Campaign Status : {}", campaignDetRequest.getCampaignId(), campStatus);
					if (campStatus) {
						logger.info("Campaign status is enabled for this campaign Name : " + campaignDetRequest.getCampaignName() + ". Hence Campaign scheduler is called");

						logger.info("**** Contact details contain campaign key ****");
						if (campaignDetRequest.getConcurrentCall() != null && !campaignDetRequest.getConcurrentCall().isEmpty()) {
							concurrent = Integer.parseInt(campaignDetRequest.getConcurrentCall());
						} else {
							concurrent = 5;
						}

						String campaignDateStr = campaignDetRequest.getStartDate() + " "
								+ campaignDetRequest.getStartTime();
						Date campaignStartdate = dateTimeformat.parse(campaignDateStr);
						Date campaignEndDate = dateTimeformat
								.parse(campaignDetRequest.getEndDate() + " " + campaignDetRequest.getEndTime());
						//                            logger.info("Campaign DET Weeks Days Time :" + campaignDetRequest.getWeekDaysTime().toString());
						String strCurrentDat = dateTimeformat.format(new Date());
						Date currentDat = dateTimeformat.parse(strCurrentDat);
						//Default campaign check

						logger.info("Default campaign set to property is :: " + defaultcampaign + " and Campaign name::" + campaignDetRequest.getCampaignName());
						if (((currentDat.after(campaignStartdate) || currentDat.equals(campaignStartdate)) &&
								(currentDat.before(campaignEndDate) || currentDat.equals(campaignEndDate))) ||
								defaultcampaign.contains(campaignDetRequest.getCampaignName())) {
							// campaignDetRequest.getCampaignName().contains(defaultcampaign)){
							logger.info("Success -default campaign list inside start and end date check :: " + defaultcampaign.contains(campaignDetRequest.getCampaignName()));
							if (campaignDetRequest.getWeekDaysTime() != null) {
								for (CampaignWeekDetRequest campaignWeekDetRequest : campaignDetRequest.getWeekDaysTime()) {
									if (weekDay.equalsIgnoreCase(campaignWeekDetRequest.getDay())) {
										weekStartDate = WeekDaytimeFormat.parse(campaignWeekDetRequest.getStartTime());
										weekEndDate = WeekDaytimeFormat.parse(campaignWeekDetRequest.getEndTime());
									}
								}
								logger.info("Campaign ID : " + campaignDetRequest.getCampaignId() + ", Week Start Date :" + weekStartDate + ", Week End Date :" + weekEndDate);
							}
							//                                int countOfDialNo = surveyContDetList.size();

							if (campaignService.makeInprogressintoNoAnswer(campaignDetRequest.getCampaignId())) {
								logger.info("Inprogress for this campaignId {} ", campaignDetRequest.getCampaignId(), " change to NO ANSWER");
							} else {
								logger.info("No InProgress call status to change");
							}
							//
							List<SurveyContactDetDto> contactDetListRetry = campaignService.getContactDetRetry(campaignDetRequest.getCampaignId(), campaignDetRequest.getRetryCount());
							logger.info("For this Campaign Id : {}  and for retry contact Count: {}", campaignDetRequest.getCampaignId(), contactDetListRetry.size());
							int i = 0;
							int isValidCount = 0;
							int retryCallCount = 0;


							try {
								if (contactDetListRetry != null && !contactDetListRetry.isEmpty()) {
									for (SurveyContactDetDto surveyContactDetDto : contactDetListRetry) {
										logger.info("Remove list for Retry list : " + remListforRetry + " ,Current ActionID : " + surveyContactDetDto.getActionId());
										if (!remListforRetry.contains(surveyContactDetDto.getActionId())) {
											boolean isValid = validContact(campaignStartdate, campaignEndDate, weekStartDate, weekEndDate, weekDay, dateformat, dateTimeformat, currentDate, weekDayFormat, time, WeekDaytimeFormat, isMaxAdvTime, surveyContactDetDto, campaignDetRequest, retryDifference);
											//                                        logger.info("ValidContact is checked in retry list");
											retryCallCount = campaignService.getinProgressCallCount(campaignDetRequest.getCampaignId());
											if (isValid) {
												if (retryCallCount < concurrent) {
													try {
														logger.info("Current ActionID : " + surveyContactDetDto.getActionId() + ", In Retry Count : " + retryCallCount);
														String response = processCallApi(surveyContactDetDto, campaignDetRequest.getCampaignName(), campaignDetRequest, campaignDetRequest.getQueue(), dateTimeformat);
														if (response.contains("Success")) {
															remListforRetry.add(surveyContactDetDto.getActionId());
															if (campaignService.updateContactDetail(campaignDetRequest.getCampaignId(), surveyContactDetDto.getPhone(), surveyContactDetDto.getActionId(), "InProgress")) {
																retryCallCount++;
															}
														}
														logger.info("InProgress Count for Retry list : " + retryCallCount);

													} catch (Exception e) {
														logger.error("Error on process call api " + e.getMessage());
													}
												} else {
													logger.info("Reached the maximum concurrent in-progress calls for retry list");
													break;
												}
											} else {
												logger.info("Action id is not valid conditions");
											}
										}
										logger.info("Current Action ID is already in Inprogress : " + surveyContactDetDto.getActionId());
									}
								} else {
									logger.info("Retry Contact list is empty for this campaign ID" + campaignDetRequest.getCampaignId());
									retryCallCount = 0;
								}
							} catch (Exception e) {
								logger.error("Error in retry api process list " + e.getMessage());
							}


							try {
								if (surveyContactDet != null && surveyContactDet.containsKey(campaignDetRequest.getCampaignName())) {
									List<SurveyContactDetDto> surveyContDetList = surveyContactDet.get(campaignDetRequest.getCampaignName());
									logger.info("**** To Delete Survey Contact List for the campaign : " + campaignDetRequest.getCampaignName() + " and the contacts :" + surveyContDetList.toString());
									if (surveyContDetList != null && !surveyContDetList.isEmpty()) {
										logger.info("Total Contact: " + surveyContDetList.size());
										for (SurveyContactDetDto surveyContactDetDto : surveyContDetList) {
											if (!newRemList.contains(surveyContactDetDto.getActionId())) {
												logger.info("Inside the loop of list of new surveyContactDetDto  and Current Action Id  ; " + surveyContactDetDto.getActionId());
												if (surveyContactDetDto == null) {
													logger.info("Contact Detail is null");
												} else {
													boolean isValid = validContact(campaignStartdate, campaignEndDate, weekStartDate, weekEndDate, weekDay, dateformat, dateTimeformat, currentDate, weekDayFormat, time, WeekDaytimeFormat, isMaxAdvTime, surveyContactDetDto, campaignDetRequest, retryDifference);
													isValidCount = campaignService.getinProgressCallCount(campaignDetRequest.getCampaignId());
													if (isValid) {
														if (isValidCount != concurrent && isValidCount < concurrent) {
															logger.info("All Conditions Satisfied for this ContactId : " + surveyContactDetDto.getActionId() + ", Current Call In : " + isValidCount);
															try {
																String response = processCallApi(surveyContactDetDto, campaignDetRequest.getCampaignName(), campaignDetRequest, campaignDetRequest.getQueue(), dateTimeformat);
																if (response.contains("Success")) {
																	newRemList.add(surveyContactDetDto.getActionId());
																	if (campaignService.updateContactDetail(campaignDetRequest.getCampaignId(), surveyContactDetDto.getPhone(), surveyContactDetDto.getActionId(), "InProgress")) {
																		isValidCount++;
																		//                                                                if (campaignService.checkContactIsHangUp(surveyContactDetDto.getActionId(), surveyContactDetDto.getPhone())) {
																	}

																	logger.info("InProgress Count for New list : " + isValidCount);


																}
																//                                                        }
															} catch (Exception e) {
																logger.error("Error on process call api " + e.getMessage());
															}
														} else {
															logger.info("Reached the maximum concurrent in-progress calls. for new list");
															break;
														}
													}
												}
											} else {
												logger.info("Current Action Id is already in progress " + surveyContactDetDto.getActionId());
											}
										}
									} else {
										logger.info("Contact List is Null or Empty");
									}
								} else {
									logger.info("Survey contact Det is Null , No contact for this campaign Name : " + campaignDetRequest.getCampaignName());
								}
							} catch (Exception e) {
								logger.error("Error in New api process list : " + e.getMessage());
							}
						} else {
							logger.info("Current Date ::" + currentDat + " Not falls between Campaign Start date :" + campaignStartdate + " and End Date" + campaignEndDate);
						}

					} else {
						logger.info("New :: Campaign Status is not true. Hence campaign API scheduler is not invoked");
					}
				}
			} else {
				logger.info("Campaign List Is Null ");
			}
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
	}


	private boolean validContact(Date campaignStartdate, Date campaignEndDate, Date weekStartDate, Date
			weekEndDate, String weekDay, DateFormat dateformat, DateFormat dateTimeformat, Date currentDate, DateFormat
										 weekDayFormat, DateFormat time, DateFormat WeekDaytimeFormat, boolean isMaxAdvTime, SurveyContactDetDto surveycontDetDto, CampaignDetRequest
										 campaignDetRequest, long retryDifference) {
		boolean isValid = false;
		logger.info("Action Id : " + surveycontDetDto.getActionId() + " : call_status :: " + surveycontDetDto.getCall_status() + ", retry COunt :: " + surveycontDetDto.getRetryCount() + "Campaign Retry count : " + campaignDetRequest.getRetryCount() + ", Contact last retry time : " + surveycontDetDto.getRec_update_time() + ", Current time : " + currentDate);
		try {
			String call_status = surveycontDetDto.getCall_status();
			//            logger.info("Call status is :: " + call_status);
			if (surveycontDetDto.getRetryCount() != null && Integer.parseInt(surveycontDetDto.getRetryCount()) > Integer.parseInt(campaignDetRequest.getRetryCount())) {
				return false;
			}
			isMaxAdvTime = true;
			String dncID = campaignDetRequest.getDncId();
			List<String> dncContacts = new ArrayList<>();
			if (dncID != null) {
				dncContacts = campaignService.getDNSDetList(dncID);
			}
			String contactNumber = surveycontDetDto.getPhone();
			logger.info("contact number :" + contactNumber);
			logger.info("Dnc Contacts list for campaignID : " + campaignDetRequest.getCampaignId() + " : " + dncContacts.toString());
			if (contactNumber != null && dncContacts != null && !dncContacts.contains(contactNumber)) {
				//                logger.info("call_status :: " + call_status + "Survey contact retry count is  ::" + surveycontDetDto.getRetryCount() + "Campaign contact retry count :: " + campaignDetRequest.getRetryCount());
				if (surveycontDetDto.getRetryCount() != null &&
						(Integer.parseInt(surveycontDetDto.getRetryCount()) <= Integer.parseInt(campaignDetRequest.getRetryCount()))) {
					Date updateddate = dateTimeformat.parse(surveycontDetDto.getRec_update_time());
					retryDifference = TimeUnit.MILLISECONDS.toMinutes(currentDate.getTime() - updateddate.getTime());
					//
					//Added on 27
					if ("New".equalsIgnoreCase(call_status) || retryDifference >= Integer.parseInt(campaignDetRequest.getRetryDelay())) {
						String productID = surveycontDetDto.getSurvey_Lang() + "_" + surveycontDetDto.getSubSkillset();
						String Queue = campaignDetRequest.getQueue();
						logger.info("Queue : " + Queue);

						if (isMaxAdvTime) {
							currentDate = new Date(); //Code added by SK Praveen Kumar for bug fix
							//                            logger.info("New :: Week Start Date :" + weekStartDate);
							//                            logger.info("New :: Week Start Date :" + weekStartDate+", "+"New :: Week End Date :" + weekEndDate+", New :: Current Date :" + currentDate+", Current Time :" + currentDate.getTime());
							//                            logger.info("New :: Currend Date :" + currentDate);
							if (weekStartDate != null && (WeekDaytimeFormat.parse(WeekDaytimeFormat.format(currentDate))
									.after(weekStartDate))
									&& weekEndDate != null && (WeekDaytimeFormat.parse(WeekDaytimeFormat.format(currentDate))
									.before(weekEndDate))) {
								isValid = true;
								logger.info(
										"**** All Conditions are satisfied going to make call For the Action ID : " + surveycontDetDto.getActionId() + "****");
							} else {
								logger.info("If Condition False  in : New :: Week Start Date :" + weekStartDate + ", " + "New :: Week End Date :" + weekEndDate + ", New :: Current Date :" + currentDate + ", Current Time :" + currentDate.getTime());
								logger.info("Week Start is not after current date or Week end date is not before current date");
								return false;
							}
						} else {
							logger.info("Is MaxAdv Time is false");
							return false;
						}

					} else {
						logger.info("If Condition False in : call_status :: " + call_status + ", retryDifference :: " + retryDifference + "Campaign Retry delay :: " + campaignDetRequest.getRetryDelay());
						return false;
					}
				} else {
					//                    logger.info("Contact status is Neither NULL nor NEW, Hence not invoking Campaign API");
					logger.info("If Condition False in : Survey contact retry count is  ::" + surveycontDetDto.getRetryCount() + "Campaign contact retry count :: " + campaignDetRequest.getRetryCount());
					return false;
				}
			} else {
				logger.info("Contact is in DNC list, Hence not invoking campaign API");
				return false;
			}
		} catch (Exception e) {

			logger.error("Error occur on the valid contact " + e.getMessage());
			return false;
		}
		return isValid;
	}


	private String processCallApi(SurveyContactDetDto surveycontDetDto, String campaignName, CampaignDetRequest
			campaignDetRequest, String Queue, DateFormat dateTimeformat) {
		logger.info(
				"****Inside API Processing****");
		String responseValue = null;
		Unirest.setTimeouts(0, 0);
		try {
			CampaignStatus campaignStatus = new CampaignStatus();
			campaignStatus.setCampaignId(campaignDetRequest.getCampaignId());
			boolean campStatus = campaignService.getCampaignStatus(campaignStatus);
			String frontCampStatus = null;
			String dialingMode = campaignDetRequest.getDailingMode();
			String dialingOption = campaignDetRequest.getDailingoption();
			try {
				frontCampStatus = campaignService.getFrontCampStatus(campaignStatus);
			} catch (Exception e) {
				e.printStackTrace();
			}
			logger.info("New :: Campaign status :" + campStatus);
			if (campStatus) {
				if (frontCampStatus != null && frontCampStatus.equalsIgnoreCase("running")) {
					String request = null;
					if (dialingOption != null && dialingOption.equalsIgnoreCase("agentbased")) {
						String agentExtn = campaignService.getExtn();
						request = getAgentBasedString(surveycontDetDto, campaignName, Queue, dateTimeformat, agentExtn);
					} else {
						request = getString(surveycontDetDto, campaignName, Queue, dateTimeformat);
					}

					String actionId = surveycontDetDto.getActionId();
					logger.info("Campaign Name :" + campaignName + "It's Request :" + request);

					logger.info("Campaign Name :" + campaignName + "It's Request :" + request);
					HttpResponse<String> response = Unirest.post(callApiAutoCall)
							.header("Content-Type", "application/json")
							.body(request)
							.asString();
					logger.info("**** Inside Thread API Thread API response****");
					logger.info("OutBound API Response :" + response.getBody());
					responseValue = response.getBody();
				} else {
					logger.info("Front Camp Status is in " + frontCampStatus + " state, hence not invoking API");
				}
			} else {
				logger.info("Campaign status is disabled..");
			}
		} catch (UnirestException e1) {
			logger.info("**** Inside Exception clause API Thread API ****");
			logger.error(e1.getMessage());
			e1.printStackTrace();
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		return responseValue;
	}


	//    private void processCallApi(SurveyContactDetDto surveycontDetDto, String campaignName, String Queue) {
	//        logger.info(
	//                "**** Inside Thread API Thread API request****");
	//        Unirest.setTimeouts(0, 0);
	//        try {
	//            String request = getString(surveycontDetDto, campaignName, Queue);
	//            String actionId = surveycontDetDto.getActionId();
	//            logger.info("Request :" + request);
	//            HttpResponse<String> response = Unirest.post(callApiAutoCall)
	//                    .header("Content-Type", "application/json")
	//                    .body(request)
	//                    .asString();
	//            listOfActionIdStore.add(actionId);
	//            if (response.getBody().contains(actionId) && response.getBody().contains("success")) {
	//                if (listOfActionIdStore.contains(actionId)) {
	//                    logger.info("Response for this : " + surveycontDetDto.getActionId() + " " + surveycontDetDto.getPhone() + " : call done..");
	//                }
	//            } else if (response.getBody().contains("error")) {
	//                logger.info("Response : " + response.getBody());
	//            }
	//            logger.info("**** Inside Thread API Thread API response****");
	//            logger.info("OutBound API Response :" + response.getBody());
	//        } catch (UnirestException e1) {
	//            logger.info("**** Inside Exception clause API Thread API ****");
	//            logger.error(e1.getMessage());
	//            e1.printStackTrace();
	//        }
	//    }

	private static String getString(SurveyContactDetDto surveycontDetDto, String campaignName, String Queue, DateFormat dateTimeformat) throws ParseException {

		String productID = surveycontDetDto.getSurvey_Lang() + "_" + surveycontDetDto.getSubSkillset();

		/*
		 * "{\r\n    \"outcallerid\": \"044288407\",\r\n    \"siptrunk\": \"Avaya\",\r\n  "
		 * + "  \"phone\": \""+ surveycontDetDto.getPhone()+"\",\r\n   " +
		 * "  \"productid\": \""+productID+"\",\r\n   " +
		 * "  \"campaingnname\": \""+campaignName+"\",\r\n   " + " \"language\": \""+
		 * surveycontDetDto.getSurvey_Lang()
		 * +"\",\r\n    \"dialplan\": \"nas-neuro\",\r\n " + " \"actionid\": \""+
		 * surveycontDetDto.getActionId()+"\"\r\n}";
		 */
		//
		//    return "{\r\n    \"outcallerid\": \"044288407\",\r\n    \"siptrunk\": \"Avaya\",\r\n  "
		//            + "  \"phone\": \"" + surveycontDetDto.getPhone() + "\",\r\n   "
		//            + "  \"productid\": \"" + productID + "\",\r\n"
		//            + " \"language\": \"" + surveycontDetDto.getSurvey_Lang() + "\",\r\n  "
		//            + " \"campaingnname\": \"" + campaignName + "\",\r\n"
		//            + "\r\n    \"dialplan\": \"" + Queue + "\",\r\n "
		//            + " \"actionid\": \"" + surveycontDetDto.getActionId() + "\"\r\n}";


		Date dueDate = dateTimeformat.parse(surveycontDetDto.getDueDate());
		Long dueUnixTime = dueDate.getTime() / 1000;

		return "{\r\n    \"outcallerid\": \"044288407\",\r\n    \"siptrunk\": \"Avaya\",\r\n  "
				+ "  \"phone\": \"" + surveycontDetDto.getPhone() + "\",\r\n   "
				+ "\"language\": \"" + surveycontDetDto.getSurvey_Lang() + "\",\r\n "
				+ "  \"productid\": \"" + productID + "\",\r\n   "
				+ " \"amount\": \"" + surveycontDetDto.getMinPayment() + "\",\r\n    "
				+ " \"last4digit\": \"" + surveycontDetDto.getLastFourDigits() + "\",\r\n "
				+ "   \"duedate\": \"" + surveycontDetDto.getDueDate() + "\",\r\n   "
				+ " \"campaingnname\": \"" + campaignName + "\",\r\n"
				+ "  \"unixtime\": \"" + dueUnixTime + "\",\r\n    \"timezone\": \"GST\",\r\n    \"dialplan\": \"" + Queue + "\",\r\n   "
				+ " \"actionid\": \"" + surveycontDetDto.getActionId() + "\"\r\n}";

	}


	private static String getAgentBasedString(SurveyContactDetDto surveycontDetDto, String campaignName, String Queue, DateFormat dateTimeformat, String extn) throws ParseException {
		String productID = surveycontDetDto.getSurvey_Lang() + "_" + surveycontDetDto.getSubSkillset();
		Date dueDate = dateTimeformat.parse(surveycontDetDto.getDueDate());
		Long dueUnixTime = dueDate.getTime() / 1000;

		String data = "{\r\n \"actionid\":\"" + surveycontDetDto.getActionId() + "\",\r\n"
				+ "\"outcallerid\":\"044288407\",\r\n"
				+ "\"siptrunk\":\"Avaya\",\r\n"
				+ "\"trunktype\":\"pjsip\",\r\n"
				+ "\"phone\":\"" + surveycontDetDto.getPhone() + "\",\r\n"
				+ "\"dialplan\":\"direct-customer\",\r\n"
				+ "\"agent\":\"" + extn + "\",\r\n"
				+ "\"callmode\":\"progressive\",\r\n"
				+ "\"campaingnname\":\"progressive_data\",\r\n"
				+ "}";

		return data;
	}

	@PostMapping("/createCampaign")
	public ResponseEntity<GenericResponse> createCampaign(@RequestBody CampaignDetRequest campaignDetRequest)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		return campaignService.createCampaign(campaignDetRequest);
	}

	@GetMapping("/getCampaignDetail")
	public ResponseEntity<GenericResponse> getCampaignDetail(@RequestParam String userGroup)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		logger.info("Invoking Get Campaign Detail for the User Group ID :" + userGroup);
		return campaignService.getCampaignDetail(userGroup);
	}

	@GetMapping("/getCampaignDetailAll")
	public ResponseEntity<GenericResponse> getCampaignDetailAll()
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		GenericResponse genericResponse = new GenericResponse();
		List<CampaignDetRequest> campaignDetList = null;
		try {
			campaignDetList = campaignService.getCampaignDetList();
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

	@PostMapping("/updateCampaign")
	public ResponseEntity<GenericResponse> updateCampaign(@RequestBody CampaignDetRequest campaignDetRequest)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		logger.info("Updating Campaign Detail");
		return campaignService.updateCampaign(campaignDetRequest);
	}


	//    @PostMapping("/updateCallDetail")
	//    public ResponseEntity<GenericResponse> updateCallDetail(@RequestBody UpdateAutoCallRequest updateAutoCallRequest)
	//            throws ParseException, JsonParseException, JsonMappingException, IOException {
	//        logger.info("**********UPDATE CALL DETAILS INPUT**********");
	//        logger.info("getActionid: " + updateAutoCallRequest.getActionid());
	//        logger.info("getPhone: " + updateAutoCallRequest.getPhone());
	//        logger.info("getCallstart: " + updateAutoCallRequest.getCallstart());
	//        logger.info("getCallanswer: " + updateAutoCallRequest.getCallanswer());
	//        logger.info("getCallend: " + updateAutoCallRequest.getCallend());
	//        logger.info("getCalltalktime: " + updateAutoCallRequest.getCalltalktime());
	//        logger.info("getCallduration: " + updateAutoCallRequest.getCallduration());
	//        logger.info("getDisposition: " + updateAutoCallRequest.getDisposition());
	//        logger.info("getDialstatus: " + updateAutoCallRequest.getDialstatus());
	//        logger.info("getHangupcode: " + updateAutoCallRequest.getHangupcode());
	//        logger.info("getHangupreason: " + updateAutoCallRequest.getHangupreason());
	//        logger.info("getHanguptext: " + updateAutoCallRequest.getHanguptext());
	//
	//
	//        UpdateCallDetRequest updateCallDetRequest = new UpdateCallDetRequest();
	//        updateCallDetRequest.setCallDuration(updateAutoCallRequest.getCallduration());
	//        updateCallDetRequest.setContactId(updateAutoCallRequest.getActionid());
	//        updateCallDetRequest.setCallerResponse("0");
	//        //Added on 28022024
	//        if (updateAutoCallRequest.getDisposition() != null)
	//            updateCallDetRequest.setCallStatus(updateAutoCallRequest.getDisposition());
	//
	//        else if (updateAutoCallRequest.getDialstatus() != null)
	//            updateCallDetRequest.setCallStatus(updateAutoCallRequest.getDialstatus());
	//            //
	//
	//            //	if("ANSWER".equalsIgnoreCase(updateAutoCallRequest.getDialstatus()))
	//            //	updateCallDetRequest.setCallStatus("ANSWERED");
	//        else
	//            updateCallDetRequest.setCallStatus(updateAutoCallRequest.getDialstatus());
	//        updateCallDetRequest.setHangupcode(updateAutoCallRequest.getHangupcode());
	//        return campaignService.updateCallDetail(updateCallDetRequest);
	//    }
	@PostMapping("/updateCallDetail")
	public ResponseEntity<GenericResponse> updateCallDetail(@RequestBody UpdateAutoCallRequest
																	updateAutoCallRequest)
			throws ParseException, JsonParseException, JsonMappingException, IOException {

		logger.info("**********UPDATE CALL DETAILS INPUT**********");
		UpdateCallDetRequest updateCallDetRequest = new UpdateCallDetRequest();
		updateCallDetRequest.setCallduration(String.valueOf(updateAutoCallRequest.getCallduration()));
		updateCallDetRequest.setActionid(updateAutoCallRequest.getActionid());
		updateCallDetRequest.setPhone(updateAutoCallRequest.getPhone());
		updateCallDetRequest.setDisposition(updateAutoCallRequest.getDisposition());
		updateCallDetRequest.setDialstatus(updateAutoCallRequest.getDialstatus());
		updateCallDetRequest.setHangupcode(updateAutoCallRequest.getHangupcode());
		updateCallDetRequest.setSurveyrating(updateAutoCallRequest.getSurveyrating());
		updateCallDetRequest.setCallStartTime(updateAutoCallRequest.getCallstart());
		updateCallDetRequest.setCallEndTime(updateAutoCallRequest.getCallend());
		updateCallDetRequest.setHangupreason(updateAutoCallRequest.getHangupreason());
		updateCallDetRequest.setHanguptext(updateAutoCallRequest.getHanguptext());
		updateCallDetRequest.setCallduration(updateAutoCallRequest.getCallduration());
		updateCallDetRequest.setCallanswer(updateAutoCallRequest.getCallanswer());
		updateCallDetRequest.setSurveyrating(updateCallDetRequest.getSurveyrating());
		updateCallDetRequest.setCalltalktime(updateAutoCallRequest.getCalltalktime());
		String actionID = updateAutoCallRequest.getActionid();
		if (remListforRetry.contains(actionID)) {
			remListforRetry.remove(actionID);
			logger.info("This ActionId : {} is updated in database and remove from rem List for Retry", actionID);
		} else {
			logger.info("This Action Id not in retry contact list : {}", actionID);
		}

		if (newRemList.contains(actionID)) {
			newRemList.remove(actionID);
			logger.info("This ActionId : {} is updated in database and remove from remove List for New", actionID);
		} else {
			logger.info("This Action Id not in New contact list : {}", actionID);
		}

		return campaignService.updateCallDetail(updateCallDetRequest);
	}

	@PostMapping("/validateCampaignName")
	public ResponseEntity<GenericResponse> validateCampaignName(@RequestBody CampaignDetRequest campaignDetRequest)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		return campaignService.validateCampaignName(campaignDetRequest);
	}

	@PostMapping("/updateContact")
	public ResponseEntity<GenericResponse> updateContact(@RequestBody DncContactDto contactDetDto)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		logger.info("updateContact Campaign Detail");
		return campaignService.updateContact(contactDetDto);
	}

	@PostMapping("/DeleteContact")
	public ResponseEntity<GenericResponse> DeleteContact(@RequestBody DncContactDto contactDetDto)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		logger.info("DeleteContact ****** Campaign Detail");
		return campaignService.DeleteContact(contactDetDto);
	}


	// Report 4
	@PostMapping("/summaryReport")
	public ResponseEntity<GenericResponseReport> summaryReport(@RequestBody ReportRequest reportRequest)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		return campaignService.summaryReport(reportRequest);
	}

	@PostMapping("/smsReport")
	public ResponseEntity<GenericResponseReport> smsReport(@RequestBody ReportRequest reportRequest)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		return campaignService.smsReport(reportRequest);
	}

	@PostMapping("/downloadsmsReport")
	public ResponseEntity<InputStreamResource>
	downloadsmsReport(@RequestBody ReportRequest reportRequest) throws
			ParseException, JsonParseException, JsonMappingException, IOException {
		return campaignService.downloadsmsReport(reportRequest);
	}


	// Report 5
	@PostMapping("/detailReport")
	public ResponseEntity<GenericResponseReport> detailReport(@RequestBody ReportRequest reportRequest)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		return campaignService.detailReport(reportRequest);
	}

	@PostMapping("/downloadSummaryReport")
	public ResponseEntity<InputStreamResource> downloadSummaryReport(@RequestBody ReportRequest reportRequest)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		return campaignService.downloadSummaryReport(reportRequest);
	}

	@PostMapping("/downloadDetailReport")
	public ResponseEntity<InputStreamResource> downloadDetailReport(@RequestBody ReportRequest reportRequest)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		return campaignService.downloadDetailReport(reportRequest);
	}

	@PostMapping("/getUploadhistory")
	public ResponseEntity<GenericResponse> getUploadHistory(@RequestBody ReportRequest reportRequest)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		return campaignService.getUploadHistory(reportRequest);
	}

	@PostMapping("/deleteContactByHistory")
	public ResponseEntity<GenericResponse> deleteContactByHistory(
			@RequestBody UpdateCallDetRequest updateCallDetRequest)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		return campaignService.deleteContactByHistory(updateCallDetRequest);
	}

	@PostMapping("/uploadContactDetail")
	public ResponseEntity<GenericResponse> uploadContactDetail(@RequestParam("file") MultipartFile file,
															   @RequestParam(name = "campaignId", required = false) String campaignId,
															   @RequestParam(name = "campaignName", required = false) String campaignName)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		String message = null;
		CSVPrinter csvPrinter = null;
		boolean isUploaded = true;
		List<ContactDetDto> failureList = null;
		if ("text/csv".equalsIgnoreCase(file.getContentType()) || file.getOriginalFilename().endsWith(".csv")) {
			try {
				failureList = new ArrayList<>();
				CampaignDetRequest campaignDetRequest = new CampaignDetRequest();
				campaignDetRequest.setCampaignId(campaignId);
				campaignDetRequest.setCampaignName(campaignName);
				List<CampaignDetRequest> campaignDetList = campaignService.getCampaignDetList("default");
				BigInteger historyId = getUploadHistoryid(campaignDetRequest, file.getOriginalFilename());
				List<ContactDetDto> contactDetList = csvToData(file.getInputStream(), historyId, isFTP, fileDirectory,
						new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()), failureDirectory, campaignDetList,
						failureList, campaignId, campaignName);
				ContactDetDto commonDetail = contactDetList.stream()
						.filter(x -> campaignId.equalsIgnoreCase(x.getCampaignId())).findAny().orElse(null);
				logger.info("****Converted CSV DATA to Object****");
				if (contactDetList.isEmpty()) {
					message = "Upload failed! Invalid data or Contact details already exist for same Appointment date and time ";
					return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
							.body(new GenericResponse(message, "Failed"));
				}
				if (commonDetail != null) {
					List<String> dncList = null;
					String dncID = campaignService.getDNCIDusingCampaignID(campaignId);
					logger.info("Dnc ID : " + dncID);
					if (dncID != null) {
						dncList = campaignService.getDNSDetList(dncID);
						logger.info("Dnc list : " + dncList);
					}
					logger.info("****Inserting contact details to DB Table****");

					CampaignStatus campaignStatus = new CampaignStatus();
					campaignStatus.setCampaignId(campaignDetRequest.getCampaignId());
					String status = campaignService.getFrontCampStatus(campaignStatus);
					if (status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Stop")) {
						campaignService.updateNTCStatus(campaignDetRequest.getCampaignId());
					} else {
						logger.info("NO UpdateNTC status is required");
					}
					for (ContactDetDto contactDetDto : contactDetList) {
						if (dncList != null && dncList.contains(contactDetDto.getCustomerMobileNumber())) {
							contactDetDto.setCallStatus("DNC");
//							isUploaded = campaignService.createContact(contactDetDto);
							logger.info("**** This contact is in DNC List :" + contactDetDto.getCustomerMobileNumber() + "Hence setting status as DNC****");
						} else {
							contactDetDto.setCallStatus("NEW");
						}

						isUploaded = campaignService.createContact(contactDetDto);

						if (!isUploaded) {
							contactDetDto.setFailureReason(
									"Contact details already exist for same Appointment Date and Time");
							failureList.add(contactDetDto);
						}

					}
					message = "Uploaded the file successfully: " + file.getOriginalFilename();
//				if (commonDetail != null) {
//					logger.info("****Inserting contact details to DB Table****");
//					for (ContactDetDto contactDetDto : contactDetList) {
////						if (contactDetDto!=null) {
////							logger.info("Contact : "+contactDetDto.getCustomerMobileNumber());
//							isUploaded = campaignService.createContact(contactDetDto);
////						}else {
////							logger.info("It's seems the upload list has some null values ...");
////							continue;
////						}
//						if (!isUploaded) {
//							contactDetDto.setFailureReason(
//									"Contact details already exist for same Appointment Date and Time");
//							failureList.add(contactDetDto);
//						}
//					}
//					message = "Uploaded the file successfully: " + file.getOriginalFilename();
					campaignService.updateCampaignStatusUploadContact(campaignId);
					logger.info(message);
					logger.info(failureList.toString());
					if (!failureList.isEmpty()) {
						csvPrinter = failureCsvData(new SimpleDateFormat("yyyy-MM-dd-hhmmss").format(new Date()),
								failureList, failureDirectory);
						message = "One or more Contacts not uploaded due to some invalid data!";
						logger.info(message);
					}
					logger.info("2");
					return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse(message, "Success"));
				} else if (commonDetail == null && !contactDetList.isEmpty()) {
					message = "Upload failed. Identified incorrect Campaign id, expected Id is " + campaignId;
					logger.info(message);
					return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
							.body(new GenericResponse(message, "Failed"));
				}
			} catch (Exception e) {
				message = "Could not upload the file: " + file.getOriginalFilename() + "!";
				logger.error("Error occured in uploadContactDetail:: " + e.getMessage());
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
						.body(new GenericResponse(message, "Failed"));
			} finally {
				if (csvPrinter != null)
					csvPrinter.close();
			}
		}

		message = "Please upload a csv file!";
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).

				body(new GenericResponse(message, "Failed"));
	}


	@PostMapping("/dynamicuploadContactDetail")
	public ResponseEntity<GenericResponse> dynamicuploadContactDetail(@RequestParam("file") MultipartFile file,
																	  @RequestParam(name = "campaignId", required = false) String campaignId,
																	  @RequestParam(name = "campaignName", required = false) String campaignName)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		String message = null;
		CSVPrinter csvPrinter = null;
		boolean isUploaded = true;
		List<DynamicContactDetDto> failureList = null;
		if ("text/csv".equalsIgnoreCase(file.getContentType()) || file.getOriginalFilename().endsWith(".csv")) {
			try {
				failureList = new ArrayList<>();
				CampaignDetRequest campaignDetRequest = new CampaignDetRequest();
				campaignDetRequest.setCampaignId(campaignId);
				campaignDetRequest.setCampaignName(campaignName);
				List<CampaignDetRequest> campaignDetList = campaignService.getCampaignDetList("default");
				BigInteger historyId = getUploadHistoryid(campaignDetRequest, file.getOriginalFilename());
				/*
				 * List<ContactDetDto> contactDetList = csvToData(file.getInputStream(),
				 * historyId, isFTP, fileDirectory, new
				 * SimpleDateFormat("yyyyMMddhhmmss").format(new Date()), failureDirectory,
				 * campaignDetList, failureList, campaignId, campaignName); ContactDetDto
				 * commonDetail = contactDetList.stream() .filter(x ->
				 * campaignId.equalsIgnoreCase(x.getCampaignId())).findAny().orElse(null);
				 */
				List<DynamicContactDetDto> dynamiccontactDetList = dynamiccsvToData(file.getInputStream(), historyId, isFTP, fileDirectory,
						new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()), failureDirectory, campaignDetList,
						failureList, campaignId, campaignName);
				DynamicContactDetDto commonDetail = dynamiccontactDetList.stream()
						.filter(x -> campaignId.equalsIgnoreCase(x.getCampaignId())).findAny().orElse(null);


				logger.info("****Converted CSV DATA to Object****");
				if (dynamiccontactDetList.isEmpty()) {
					message = "Upload failed! Invalid data or Contact details already exist for same Appointment date and time ";
					return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
							.body(new GenericResponse(message, "Failed"));
				}
				if (commonDetail != null) {
					List<String> dncList = null;
					String dncID = campaignService.getDNCIDusingCampaignID(campaignId);
					logger.info("Dnc ID : " + dncID);
					if (dncID != null) {
						dncList = campaignService.getDNSDetList(dncID);
						logger.info("Dnc list : " + dncList);
					}
					logger.info("****Inserting contact details to DB Table****");

					CampaignStatus campaignStatus = new CampaignStatus();
					campaignStatus.setCampaignId(campaignDetRequest.getCampaignId());
					String status = campaignService.getFrontCampStatus(campaignStatus);
					if (status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Stop")) {
						campaignService.updateNTCStatus(campaignDetRequest.getCampaignId());
					} else {
						logger.info("NO UpdateNTC status is required");
					}
					for (DynamicContactDetDto contactDetDto : dynamiccontactDetList) {
						if (dncList != null && dncList.contains(contactDetDto.getCustomerMobileNumber())) {
							contactDetDto.setCallStatus("DNC");
							//							isUploaded = campaignService.createContact(contactDetDto);
							logger.info("**** This contact is in DNC List :" + contactDetDto.getCustomerMobileNumber() + "Hence setting status as DNC****");
						} else {
							contactDetDto.setCallStatus("NEW");
						}

						isUploaded = campaignService.createDynamicContact(contactDetDto);

						if (!isUploaded) {
							contactDetDto.setFailureReason(
									"Contact details already exist for same Appointment Date and Time");
							failureList.add(contactDetDto);
						}

					}
					message = "Uploaded the file successfully: " + file.getOriginalFilename();
					//				if (commonDetail != null) {
					//					logger.info("****Inserting contact details to DB Table****");
					//					for (ContactDetDto contactDetDto : contactDetList) {
					////						if (contactDetDto!=null) {
					////							logger.info("Contact : "+contactDetDto.getCustomerMobileNumber());
					//							isUploaded = campaignService.createContact(contactDetDto);
					////						}else {
					////							logger.info("It's seems the upload list has some null values ...");
					////							continue;
					////						}
					//						if (!isUploaded) {
					//							contactDetDto.setFailureReason(
					//									"Contact details already exist for same Appointment Date and Time");
					//							failureList.add(contactDetDto);
					//						}
					//					}
					//					message = "Uploaded the file successfully: " + file.getOriginalFilename();
					campaignService.updateCampaignStatusUploadContact(campaignId);
					logger.info(message);
					logger.info(failureList.toString());
					/*
					 * if (!failureList.isEmpty()) { csvPrinter = failureCsvData(new
					 * SimpleDateFormat("yyyy-MM-dd-hhmmss").format(new Date()), failureList,
					 * failureDirectory); message =
					 * "One or more Contacts not uploaded due to some invalid data!";
					 * logger.info(message); }
					 */
					logger.info("2");
					return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse(message, "Success"));
				} else if (commonDetail == null && !dynamiccontactDetList.isEmpty()) {
					message = "Upload failed. Identified incorrect Campaign id, expected Id is " + campaignId;
					logger.info(message);
					return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
							.body(new GenericResponse(message, "Failed"));
				}
			} catch (Exception e) {
				message = "Could not upload the file: " + file.getOriginalFilename() + "!";
				logger.error("Error occured in uploadContactDetail:: " + e.getMessage());
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
						.body(new GenericResponse(message, "Failed"));
			} finally {
				if (csvPrinter != null)
					csvPrinter.close();
			}
		}

		message = "Please upload a csv file!";
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).

				body(new GenericResponse(message, "Failed"));
	}

	//    private void updateCallDet(int i, String contactId, String C) {
	//        UpdateCallDetRequest UpdateCallDetRequest = new UpdateCallDetRequest();
	//        UpdateCallDetRequest.setContactId(contactId);
	//        UpdateCallDetRequest.setRetryCount(Integer.parseInt(contactId));
	//        if (i == 0) {
	//            UpdateCallDetRequest.setCallStatus("Failed");
	//        } else if (i % 3 == 0) {
	//            UpdateCallDetRequest.setCallStatus("ANSWERED");
	//            UpdateCallDetRequest.setCallerResponse("2");
	//            UpdateCallDetRequest.setCallDuration("20");
	//        } else if (i % 5 == 0) {
	//            UpdateCallDetRequest.setCallStatus("Failed");
	//        } else if (i % 7 == 0) {
	//            UpdateCallDetRequest.setCallStatus("ANSWERED");
	//            UpdateCallDetRequest.setCallerResponse("3");
	//            UpdateCallDetRequest.setCallDuration("20");
	//        } else if (i % 2 == 0) {
	//            UpdateCallDetRequest.setCallStatus("ANSWERED");
	//            UpdateCallDetRequest.setCallerResponse("1");
	//            UpdateCallDetRequest.setCallDuration("20");
	//        } else {
	//            UpdateCallDetRequest.setCallStatus("ANSWERED");
	//            UpdateCallDetRequest.setCallDuration("5");
	//        }
	//        campaignService.updateCallDetail(UpdateCallDetRequest);
	//    }

	private static void pause(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			logger.error("IOException: %s%n", e);
		}
	}


	/*
	 * @PostMapping("/httpurlautocalls") public void executeFailureAutoCalls() {
	 *
	 * logger.info("***Call executeFailureAutoCalls started***");
	 *
	 * try { List<CustomerDataDto> customerList = campaignService.getCustomerData();
	 *
	 * for (CustomerDataDto customerDataDto : customerList) {
	 *
	 * logger.
	 * info("**** All Conditions are satisfied going to make call For executeFailureAutoCalls **** "
	 * ); Runnable obj1 = () -> { logger.info("Request Success");
	 * CloseableHttpClient httpclient = HttpClients.createDefault(); HttpPost
	 * httppost = new HttpPost(callApiAutoCall); List<NameValuePair> nvps = new
	 * ArrayList<NameValuePair>(); nvps.add(new BasicNameValuePair("outcallerid",
	 * "044288407")); nvps.add(new BasicNameValuePair("siptrunk", "Avaya"));
	 * nvps.add(new BasicNameValuePair("phone", customerDataDto.getMobileNumber()));
	 * nvps.add(new BasicNameValuePair("amount", customerDataDto.getTotalDue()));
	 * nvps.add(new BasicNameValuePair("last4digit",
	 * customerDataDto.getLastFourDigits())); nvps.add(new
	 * BasicNameValuePair("duedate", customerDataDto.getDueDate())); nvps.add(new
	 * BasicNameValuePair("unixtime", "1695454737")); nvps.add(new
	 * BasicNameValuePair("timezone", "IST")); nvps.add(new
	 * BasicNameValuePair("dialplan", "autodial")); nvps.add(new
	 * BasicNameValuePair("actionid", "1234567890")); try { httppost.setEntity(new
	 * UrlEncodedFormEntity(nvps, HTTP.UTF_8)); } catch
	 * (UnsupportedEncodingException e) { e.printStackTrace(); } HttpResponse
	 * httpresponse; try { httpresponse = httpclient.execute(httppost); logger.
	 * info("**** Call made successfully for below details executeFailureAutoCalls****"
	 * ); logger.info("custphone===== " + customerDataDto.getCampaignName());
	 * logger.info("custname===== " + customerDataDto.getCutomerDataId());
	 * logger.info("docname===== " + customerDataDto.getDueDate());
	 * logger.info("docname===== " + customerDataDto.getLastFourDigits());
	 * logger.info("docname===== " + customerDataDto.getMinimumPayment());
	 * logger.info("docname===== " + customerDataDto.getMobileNumber());
	 * logger.info("docname===== " + customerDataDto.getMobileNumber());
	 * logger.info("docname===== " + customerDataDto.getStatus());
	 * logger.info("docname===== " + customerDataDto.getTotalDue());
	 *
	 * Scanner sc = new Scanner(httpresponse.getEntity().getContent());
	 * logger.info(httpresponse.getEntity().getContent().toString()); while
	 * (sc.hasNext()) { logger.info("***Call response executeFailureAutoCalls***");
	 * logger.info(sc.nextLine()); } } catch (IOException e) { e.printStackTrace();
	 * }
	 *
	 * try { httpclient.close(); } catch (IOException e) { e.printStackTrace(); } };
	 *
	 * Thread t = new Thread(obj1); t.start(); } }
	 *
	 * catch (Exception e) { logger.info("Error Occured in call Making due to : " +
	 * e.getMessage()); e.printStackTrace(); } // return null; }
	 */
	// Report 1
	@PostMapping("/getRetryReport")
	public ResponseEntity<GenericResponse> getRetryReportAll(@RequestBody ReportRequest reportRequest)
			throws Exception {
		return campaignService.getRetryReport(reportRequest);
	}

	// Report 2
	@PostMapping("/getLeadWiseSummary")
	public ResponseEntity<GenericResponseReport> getLeadWiseSummaryAll(@RequestBody ReportRequest reportRequest)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		return campaignService.getLeadWiseSummary(reportRequest);
	}

	// Report 3
	@PostMapping("/getCallVolumeReport")
	public ResponseEntity<GenericResponseReport> getCallVolumeReportAll(@RequestBody ReportRequest reportRequest)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		return campaignService.getCallVolumeReport(reportRequest);
	}

	@GetMapping("/getRealTimeDashboard")
	public ResponseEntity<GenericResponse> getRealTimeData(@RequestParam String userGroup)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		return campaignService.getRealTimeDashboard(userGroup);
	}

	//
	@GetMapping("/getRealTimeDashboardAll")
	public ResponseEntity<GenericResponse> getRealTimeDataAll()
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		GenericResponse genericResponse = new GenericResponse();
		List<CampaignRealTimeDashboard> campaignDetList = null;
		try {
			campaignDetList = campaignService.getRealTimeDashboard();
			genericResponse.setStatus(200);
			genericResponse.setValue(campaignDetList);
			genericResponse.setMessage("Success");
		} catch (Exception e) {
			logger.error("Error in CampaignServiceImpl::getRealTimeDashboardAll " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("No data Found");
		}

		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}

	@PostMapping("/createDnc")
	public ResponseEntity<GenericResponse> createDnc(@RequestBody DNCDetRequest DNCDetRequest) {
		return campaignService.createDnc(DNCDetRequest);

	}

	@GetMapping("/getdnsValue")
	public ResponseEntity<GenericResponse> getdnsdetails() {
		return campaignService.getDnsDetail();
	}

	@PostMapping("/updateDNS")
	public ResponseEntity<GenericResponse> updateDns(@RequestBody DNCDetRequest DNCDetRequest)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		logger.info("Updating DNC Detail");
		return campaignService.updateDns(DNCDetRequest);
	}

	private BigInteger getUploadHistoryidDNc(DNCDetRequest dncDetRequest, String fileName) {
		UploadHistoryDto uploadHistoryDto = new UploadHistoryDto();
		uploadHistoryDto.setCampaignId("DncId:" + dncDetRequest.getDNCID());
		uploadHistoryDto.setCampaignName("DncName:" + dncDetRequest.getDncName());
		uploadHistoryDto.setFilename(fileName);
		return campaignService.insertUploadHistory(uploadHistoryDto);
	}

	@PostMapping("/uploadDncDetail")
	public ResponseEntity<GenericResponse> uploadDNCDetail(@RequestParam("file") MultipartFile file,
														   @RequestParam(name = "dncid", required = false) String dncId,
														   @RequestParam(name = "dncName", required = false) String dncName)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		GenericResponse response = new GenericResponse();

		CSVPrinter csvPrinters = null;
		boolean isUploaded = true;
		List<DncContactDto> failureList = null;
		if ("text/csv".equalsIgnoreCase(file.getContentType()) || file.getOriginalFilename().endsWith(".csv")) {
			try {
				failureList = new ArrayList<>();
				DNCDetRequest dncDetRequest = new DNCDetRequest();
				dncDetRequest.setDNCID(dncId);
				dncDetRequest.setDncName(dncName);
				List<DNCDetRequest> dncDetRequestList = campaignService.getDNSDetailList();
				BigInteger historyId = null;
				List<DncContactDto> contactDetList = csvToDataconverter(dncDetRequest, file.getInputStream(), historyId, failureList, dncDetRequestList, dncId, dncName);
				DncContactDto contactDto = contactDetList.stream()
						.filter(x -> dncId.equalsIgnoreCase(x.getDNCID())).findAny().orElse(null);
				logger.info("****Converted CSV DATA to Object****");
				if (contactDetList.isEmpty()) {
					response.setError(null);
					response.setValue("Failed");
					response.setStatus(404);
					response.setPath("/campaign/uploadDncDetail");
					response.setMessage("Upload failed ");
					return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
							.body(response);
				}
				if (contactDto != null) {
					logger.info("****Inserting contact details to DB Table****");
					for (DncContactDto contactDetDto : contactDetList) {
						isUploaded = campaignService.createContactone(contactDetDto);
						if (!isUploaded) {
							contactDetDto.setFailureReason(
									"dnc details already exist ");
							failureList.add(contactDetDto);
						}
					}
					response.setError(null);
					response.setValue("Success");
					response.setStatus(200);
					response.setPath("/campaign/uploadDncDetail");
					response.setMessage("Uploaded the file successfully: " + file.getOriginalFilename());
					return ResponseEntity.status(HttpStatus.OK).body(response);
				} else if (contactDto == null && !contactDetList.isEmpty()) {
					response.setError("incorrect dnc id");
					response.setValue("Failed");
					response.setStatus(404);
					response.setPath("/campaign/uploadDncDetail");
					response.setMessage("Upload failed. Identified incorrect dnc id, expected Id is " + dncId);
					return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
							.body(response);
				}
			} catch (Exception e) {
				logger.error("Error occured in upload dnsDetail:: " + e.getMessage());
				response.setError("Error occured in upload dnsDetail:: " + e.getMessage());
				response.setValue("Failed");
				response.setStatus(404);
				response.setPath("/campaign/uploadDncDetail");
				response.setMessage("Could not upload the file: " + file.getOriginalFilename() + "!");
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
						.body(response);
			}
		}
		response.setError("BAD_REQUEST");
		response.setValue("Failed");
		response.setStatus(400);
		response.setPath("/campaign/uploadDncDetail");
		response.setMessage("Please upload a csv file!");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	private List<DncContactDto> csvToDataconverter(DNCDetRequest dncDetRequest, InputStream inputStream, BigInteger
			historyId, List<DncContactDto> failureList, List<DNCDetRequest> dncDetRequestList, String dncId, String
														   dncName) {
		List<DncContactDto> contactList = new ArrayList<>();
		CSVPrinter csvPrinter = null;
		CSVParser csvParser = null;
		BufferedReader fileReader = null;
		try {
			StringBuilder reason = null;
			fileReader = new BufferedReader(new InputStreamReader(inputStream));

			csvParser = new CSVParser(fileReader,
					CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());

			//            BufferedReader filereader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			//
			//            csvParser = new CSVParser(filereader,
			//                    CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());

			for (CSVRecord csvRecord : csvParser) {
				DncContactDto contactDto = new DncContactDto();
				contactDto.setDNCID(dncId);
				contactDto.setDncName(dncName);
				contactDto.setSerialnumber(csvRecord.get("serialNumber"));
				contactDto.setContactNumber(csvRecord.get("contactNumber"));
				if (validateFileDataDNC(csvRecord, reason, dncDetRequestList, contactDto)) {
					contactList.add(contactDto);
				} else {
					contactDto.setFailureReason(reason.toString());
					failureList.add(contactDto);
				}
			}
			csvParser.close();
		} catch (IOException e) {
			logger.error("Fail to parse CSV file: " + e.getMessage());
		} finally {
			if (csvParser != null) {
				try {
					csvParser.close();
				} catch (IOException e) {
					logger.error("Error closing CSVParser: " + e.getMessage());
				}
			}
		}
		return contactList;
	}

	private static boolean validateFileDataDNC(CSVRecord csvRecord, StringBuilder reason,
											   List<DNCDetRequest> campaignDetList, DncContactDto contactDet) {
		boolean isValid = true;

		if (csvRecord.get("contactNumber") == null || csvRecord.get("contactNumber").isEmpty()) {
			reason.append("CcontactNumber is missing;");
			isValid = false;
		}
		if (!isValid)
			logger.info("validateFileData : " + reason);
		return isValid;
	}


	@GetMapping("/getSurveyContactDet")
	public String getSurveyContactDet()
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		return campaignService.getDummySurveyResponse();
	}

	@GetMapping("/getDynamicContactDet")
	public ResponseEntity<GenericResponse> getDynamicContactDet(@RequestParam String campaignId)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		logger.info("Get dynamic contact det");
		GenericResponse genericResponse = new GenericResponse();
		List<DynamicContactDetDto> campaignDetList = null;
		try {
			campaignDetList = campaignService.getDynamicContactDet(campaignId);
			genericResponse.setStatus(200);
			genericResponse.setValue(campaignDetList);
			genericResponse.setMessage("Success");
		} catch (Exception e) {
			logger.error("Error in getDynamicContactDet controller " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("No data Found");
		}

		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}


	@SuppressWarnings("unchecked")
	//Disabled  for Aafaq Onlu
	@Scheduled(cron = "0 0/2 * * * *") // 15 minutes (15 * 60 * 1000 milliseconds)
	public void fetchData() {
		try {
			String token = getTokenData();
			Map<String, Object> surveyContMap = null;
			if (token != null && !token.equalsIgnoreCase("Error")) {
				HttpResponse<String> response = Unirest.get(SurveyApi)
						.header("accept", "application/json")
						.header("Authorization", "Bearer " + token)
						.asString();
				if (response != null) {
					int status = response.getStatus();
					if (status == 200) {
						String responseBody = response.getBody();
						//                        logger.info("Survey Contact Detail Response :" + responseBody);
						ObjectMapper mapper = new ObjectMapper();
						try {
							surveyContMap = mapper.readValue(responseBody, Map.class);
							if (surveyContMap.containsKey("data")) {
								List<Map<String, Object>> surveyContList = (List<Map<String, Object>>) surveyContMap.get("data");
								campaignService.insertSurveyContactDet(surveyContList);
							}
						} catch (Exception e) {
							StringWriter str = new StringWriter();
							e.printStackTrace(new PrintWriter(str));
							logger.error("Exception while Inserting Survey Contact Details API :" + str.toString());
						}
					} else {
						logger.info("Survey Response Status is Not 200 and the Failed Response Code is :" + status);
					}
				} else {
					logger.info("Survey Response is NULL");
				}
			} else {
				logger.info("Token is NULL, Hence not able to invoke Survey Contact Details API");
			}
		} catch (UnirestException e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Exception while Fetching Survey Contact Details API :" + str.toString());
		}
	}


	public String getTokenData() {
		String token = null;
		try {
			StringBuilder sb = new StringBuilder("grant_type=password");
			sb.append("&username=").append(URLEncoder.encode(userName, "UTF-8"));
			sb.append("&password=").append(URLEncoder.encode(password, "UTF-8"));
			String body = sb.toString();
			HttpResponse<String> tokenresponse = Unirest.post(tokenurl)
					.header("Content-Type", "application/x-www-form-urlencoded")
					.body(body)
					.asString();
			int responseCode = tokenresponse.getStatus();
			if (responseCode == 200) {
				JSONObject json = new JSONObject(tokenresponse.getBody());
				if (json.has("access_token")) {
					token = (String) json.get("access_token");
				} else {
					logger.info("Access Token not available in the response");
					token = "Error";
				}
			} else {
				token = "Error";
				logger.info("Access Token Error Response code :" + responseCode);
			}
		} catch (Exception e) {
			StringWriter str = new StringWriter();
			e.printStackTrace(new PrintWriter(str));
			logger.error("Exception in Get Token API :" + str.toString());
		}
		return token;
	}


	public static String getToken() {
		String token = null;
		try {
			StringBuilder sb = new StringBuilder("grant_type=password");
			sb.append("&username=").append(URLEncoder.encode("admin", "UTF-8"));
			sb.append("&password=").append(URLEncoder.encode("admin", "UTF-8"));
			String body = sb.toString();
			HttpResponse<String> tokenresponse = Unirest.post("http://192.168.45.59:82/OutboundDialer/token")
					.header("Content-Type", "application/x-www-form-urlencoded")
					.body(body)
					.asString();
			logger.info("Token API Response Code :" + tokenresponse.getStatus());
			logger.info(tokenresponse.getBody());
			JSONObject json = new JSONObject(tokenresponse.getBody());
			String accessToken = (String) json.get("access_token");

			HttpResponse<String> response = Unirest.get("http://192.168.45.59:82/OutboundDialer/api/SurveyOB/GetSurveyDetails?actionid=0")
					.header("accept", "application/json")
					.header("Authorization", "Bearer " + accessToken)
					.asString();

			if (response != null) {
				int status = response.getStatus();
				if (status == 200) {
					String responseBody = response.getBody();
					logger.info("Survey Contact Detail Response :" + responseBody);
					ObjectMapper mapper = new ObjectMapper();
					try {
						Map<String, Object> surveyContMap = mapper.readValue(responseBody, Map.class);
						if (surveyContMap.containsKey("data")) {
							List<Map<String, String>> surveyContList = (List<Map<String, String>>) surveyContMap.get("data");

						}
						logger.info("Response Map :" + surveyContMap);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return token;
	}


	public static void main(String[] args) {
		String campaignName = "CallBack Campaign - AL";
		String configCampaignName = "CallBack Campaign - AB|CallBack Campaign - AL";
		if (configCampaignName.contains(campaignName)) {
			logger.info("Matching");
		} else {
			logger.info("Not Matching");
		}
	}

	//    @Scheduled(cron = "0 0 0 * * *")  // Run daily at 12.00Am
	@GetMapping("/expireDate")
	public void licenseKey() {
		try {
			String value = null;
			if (licenseKey == null || licenseKey.isEmpty()) {
				logger.info("License key is null or empty");
			} else {
				value = licenseUtil.decrypt(licenseKey);
			}
			if (value != null) {
				LocalDate date = LocalDate.parse(Objects.requireNonNull(extractDate(value)));
				expireDate = date;
				if (date.equals(LocalDate.now()) || date.isBefore(LocalDate.now())) {
					logger.info("License Key has expired, Date: " + date);
				}
			}
		} catch (NullPointerException e) {
			logger.error("NullPointerException occurred: " + e.getMessage());
		} catch (DateTimeParseException e) {
			logger.error("Error parsing date: " + e.getMessage());
		} catch (Exception e) {
			logger.error("An unexpected error occurred: " + e.getMessage());
		}
	}


	public static String extractDate(String input) {
		int startIndex = input.indexOf("ExpireDate(");
		int endIndex = input.indexOf(")", startIndex);
		if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
			return input.substring(startIndex + 11, endIndex);
		}
		return null;
	}

	//    @Scheduled(cron = "0/10 * * * * *")  // Run daily at 12.00Am
	public void checkExpireDateisnullornot() {
		try {
			if (expireDate == null || expireDate.toString().isEmpty()) {
				logger.info("Expire Date is Null or Empty : " + expireDate);
				licenseKey();
			}
		} catch (NullPointerException e) {
			logger.error("NullPointerException occurred while checking expireDate: " + e.getMessage());
		}
	}

	@PostMapping("/startCampaign")
	public ResponseEntity<GenericResponse> startCampaign(@RequestParam String campaignId)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		return campaignService.startResumeCampaignStatus(campaignId, "Running");
	}

	@PostMapping("/resumeCampaign")
	public ResponseEntity<GenericResponse> resumeCampaign(@RequestParam String campaignId)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		return campaignService.startResumeCampaignStatus(campaignId, "Running");
	}

	//    @PostMapping("/stopCampaign")
	//    public ResponseEntity<GenericResponse> stopCampaign(@RequestParam String campaignId)
	//            throws ParseException, JsonParseException, JsonMappingException, IOException {
	//        return campaignService.stopPauseCampaignStatus(campaignId, "Stop");
	//    }

	@PostMapping("/stopCampaign")
	public ResponseEntity<GenericResponse> stopCampaign(@RequestParam String campaignId)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		return campaignService.stopCampaignStatus(campaignId, "Stop");
	}

	@PostMapping("/pauseCampaign")
	public ResponseEntity<GenericResponse> pauseCampaign(@RequestParam String campaignId)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		return campaignService.stopPauseCampaignStatus(campaignId, "Pause");
	}

	@PostMapping("/updateAgentDynamicContact")
	public ResponseEntity<GenericResponse> updateAgentDynamicContact(@RequestBody List<DynamicContactDetDto> dynamiccontactDet)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		logger.info("Updating Agent ans contact Detail");
		return campaignService.updateAgentDynamicContact(dynamiccontactDet);
	}

	@PostMapping("/updateAssignedAgentDynamicContact")
	public ResponseEntity<GenericResponse> updateAssignedAgentDynamicContact(@RequestBody List<DynamicContactDetDto> dynamiccontactDet)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		logger.info("Updating Agent ans contact Detail");
		return campaignService.updateAssignedAgentDynamicContact(dynamiccontactDet);
	}


	@GetMapping("/getAgentBasedContactDetail")
	public ResponseEntity<GenericResponse> getAgentBasedContactDetail(@RequestParam String campaignId)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		logger.info("Get agent based dynamic contact detail");
		GenericResponse genericResponse = new GenericResponse();
		Map<String, List<DynamicContactDetDto>> mapcampaignDetList = null;
		List<DynamicContactDetDto> campaignDetList = null;
		try {
			mapcampaignDetList = campaignService.getAgentBasedContactDetail(campaignId);
			genericResponse.setStatus(200);
			genericResponse.setValue(mapcampaignDetList);
			genericResponse.setMessage("Success");
		} catch (Exception e) {
			logger.error("Error in getDynamicContactDet controller " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("No data Found");
		}

		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}

	/*
	 * @GetMapping("/getPreviewAgentBasedContactDetail") public
	 * ResponseEntity<GenericResponse>
	 * getPreviewAgentBasedContactDetail(@RequestParam String agent_userid) throws
	 * ParseException, JsonParseException, JsonMappingException, IOException {
	 * logger.info("Get agent based dynamic contact detail"); GenericResponse
	 * genericResponse = new GenericResponse();
	 * Map<String,List<DynamicContactDetDto>> mapcampaignDetList=null;
	 * List<DynamicContactDetDto> campaignDetList = null; try { campaignDetList =
	 * campaignService.getPreviewAgentBasedContactDetail(agent_userid);
	 * genericResponse.setStatus(200); genericResponse.setValue(mapcampaignDetList);
	 * genericResponse.setMessage("Success"); } catch (Exception e) {
	 * logger.error("Error in getPreviewAgentBasedContactDetail controller " + e);
	 * genericResponse.setStatus(400); genericResponse.setValue("Failure");
	 * genericResponse.setMessage("No data Found"); } return new
	 * ResponseEntity<GenericResponse>(new GenericResponse(genericResponse),
	 * HttpStatus.OK); }
	 */
	@GetMapping("/getPreviewAgentBasedContactDetail")
	public ResponseEntity<GenericResponse> getPreviewAgentBasedContactDetail(@RequestParam String agent_userid)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		logger.info("Get agent based dynamic contact detail");
		GenericResponse genericResponse = new GenericResponse();
		Map<String, List<DynamicContactDetDto>> mapcampaignDetList = null;
		List<DynamicContactDetDto> campaignDetList = null;
		try {
			mapcampaignDetList = campaignService.getPreviewAgentBasedContactDetail(agent_userid);
			genericResponse.setStatus(200);
			genericResponse.setValue(mapcampaignDetList);
			genericResponse.setMessage("Success");
		} catch (Exception e) {
			logger.error("Error in getDynamicContactDet controller " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("No data Found");
		}

		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}


	@GetMapping("/getCustomerDetail")
	public ResponseEntity<GenericResponse> getCustomerDetail(@RequestParam String customerNumber)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		logger.info("Get agent based dynamic contact detail");
		GenericResponse genericResponse = new GenericResponse();
//		Map<String,List<DynamicContactDetDto>> mapcampaignDetList=null;
//		List<DynamicContactDetDto> campaignDetList = null;
		try {
			DynamicContactDetDto mapcampaignDetList = campaignService.getCustomerDetail(customerNumber);
			genericResponse.setStatus(200);
			genericResponse.setValue(mapcampaignDetList);
			genericResponse.setMessage("Success");
		} catch (Exception e) {
			logger.error("Error in getCustomerDetail controller " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("No data Found");
		}

		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}

	@GetMapping("/getSupervisorAgentContactDet")
	public ResponseEntity<GenericResponse> getPreviewSupervisorBasedContactDetail(@RequestParam String Supervisor)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		logger.info("Get agent based dynamic contact detail");
		GenericResponse genericResponse = new GenericResponse();
		Map<String, List<DynamicContactDetDto>> mapcampaignDetList = null;
		List<DynamicContactDetDto> campaignDetList = null;
		try {
			mapcampaignDetList = campaignService.getSupervisorAgentContactDet(Supervisor);
			genericResponse.setStatus(200);
			genericResponse.setValue(mapcampaignDetList);
			genericResponse.setMessage("Success");
		} catch (Exception e) {
			logger.error("Error in getDynamicContactDet controller " + e);
			genericResponse.setStatus(400);
			genericResponse.setValue("Failure");
			genericResponse.setMessage("No data Found");
		}

		return new ResponseEntity<GenericResponse>(new GenericResponse(genericResponse), HttpStatus.OK);
	}


}