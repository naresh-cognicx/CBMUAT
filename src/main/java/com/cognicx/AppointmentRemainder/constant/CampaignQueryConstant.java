package com.cognicx.AppointmentRemainder.constant;

public class CampaignQueryConstant {

	public static final String INSERT_CAMPAIGN_DET = "INSERT INTO appointment_remainder.campaign_det(campaign_id,name,description,status,max_adv_time,retry_delay,retry_count,concurrent_call,start_date,start_time,end_date,end_time,ftp_location,ftp_credentials,file_name,dncId,groupname,DailingMode,Queue,dispositionID,Dailingoption)"
			+ "VALUES (:campaignId,:name,:desc,:status,:maxAdvTime,:retryDelay,:retryCount,:concurrentCall,:startDate,:startTime,:endDate,:endTime,:ftpLocation,:ftpCredentials,:fileName,:dncId,:groupname,:DailingMode,:Queue,:dispositionID,:Dailingoption)";
	public static final String INSERT_CAMPAIGN_WEEK_DET = "insert into appointment_remainder.campaign_week_det (campaign_id,day,status,start_time,end_time) values(:campaignId,:day,:status,:startTime,:endTime)";
	public static final String GET_CAMPAIGN_ID = "select count(*) from appointment_remainder.campaign_det";
	//    public static final String GET_CAMPAIGN_DET = "SELECT campaign_id,name,description,status,max_adv_time,retry_delay,retry_count,concurrent_call,start_date,start_time,campaign_det.end_date,end_time,ftp_location,ftp_credentials,file_name,call_before,dncId,groupname,DailingMode,dispositionID FROM appointment_remainder.campaign_det";

	//    public static final String GET_CAMPAIGN_DET = "SELECT campaign_id,name,description,status,max_adv_time,retry_delay,retry_count,concurrent_call,start_date,start_time,campaign_det.end_date,end_time,ftp_location,ftp_credentials,file_name,call_before,dncId,groupname,DailingMode,Queue,dispositionID FROM appointment_remainder.campaign_det";

	public static final String GET_CAMPAIGN_DET = "SELECT campaign_id,name,description,status,max_adv_time,retry_delay,retry_count,concurrent_call,start_date,start_time,campaign_det.end_date,end_time,ftp_location,ftp_credentials,file_name,dncId,groupname,DailingMode,Queue,dispositionID,Dailingoption FROM appointment_remainder.campaign_det  order by rec_add_dt desc";
	public static final String GET_CAMPAIGN_DET_BY_USERGROUP = "SELECT campaign_id,name,description,status,max_adv_time,retry_delay,retry_count,concurrent_call,start_date,start_time,campaign_det.end_date,end_time,ftp_location,ftp_credentials,file_name,dncId,groupname,DailingMode,Queue,dispositionID,Dailingoption FROM appointment_remainder.campaign_det where groupname=:groupName";



	//public static final String GET_CAMPAIGN_DET = "SELECT campaign_id,name,description,status,max_adv_time,retry_delay,retry_count,concurrent_call,start_date,start_time,campaign_det.end_date,end_time,ftp_location,ftp_credentials,file_name,call_before,dncId,groupname,DailingMode,Queue,dispositionID FROM appointment_remainder.campaign_det  order by campaign_id desc";
	// public static final String GET_CAMPAIGN_DET_BY_USERGROUP = "SELECT campaign_id,name,description,status,max_adv_time,retry_delay,retry_count,concurrent_call,start_date,start_time,campaign_det.end_date,end_time,ftp_location,ftp_credentials,file_name,call_before,dncId,groupname,DailingMode,Queue,dispositionID FROM appointment_remainder.campaign_det where groupname=:groupName";

	public static final String GET_CAMPAIGN_WEEK_DET = "SELECT campaign_week_id,campaign_id,day,status,start_time,end_time from appointment_remainder.campaign_week_det";
	// public static final String UPDATE_CAMPAIGN_DET = "UPDATE appointment_remainder.campaign_det SET name = :name,status = :status,max_adv_time = :maxAdvTime,retry_delay = :retryDelay,retry_count = :retryCount,concurrent_call = :concurrentCall,start_date = :startDate,start_time = :startTime,end_date = :endDate,end_time = :endTime,ftp_location = :ftpLocation,ftp_credentials = :ftpCredentials,rec_updt_dt = getdate(),call_before=:callBefore,file_name=:fileName, dncId=:dncId,groupname=:groupname, DailingMode=:DailingMode,Queue=:Queue,dispositionID=:dispositionID WHERE campaign_id = :campaignId";


	public static final String UPDATE_CAMPAIGN_DET = "UPDATE appointment_remainder.campaign_det SET name = :name,status = :status,max_adv_time = :maxAdvTime,retry_delay = :retryDelay,retry_count = :retryCount,concurrent_call = :concurrentCall,start_date = :startDate,start_time = :startTime,end_date = :endDate,end_time = :endTime,ftp_location = :ftpLocation,ftp_credentials = :ftpCredentials,rec_updt_dt = getdate(),file_name=:fileName, dncId=:dncId,groupname=:groupname, DailingMode=:DailingMode,Queue=:Queue,dispositionID=:dispositionID,Dailingoption=:Dailingoption WHERE campaign_id = :campaignId";


	public static final String UPDATE_CAMPAIGN_WEEK_DET = "UPDATE appointment_remainder.campaign_week_det SET day=:day, status=:status, start_time=:startTime, end_time=:endTime where campaign_week_id=:campaignWeekId";

	public static final String UPDATE_CALL_DET = "UPDATE appointment_remainder.contact_det SET caller_response=:callerResponse, call_status=:callStatus, call_duration=:callDuration, call_retry_count=:retryCount,rec_upt_date=getdate() where contact_id=:contactId";

	//public static final String INSERT_CONTACT_DET = "insert into appointment_remainder.contact_det (campaign_id,campaign_name,doctor_name,patient_name,contact_number,appointment_date,language,call_status,upload_history_id) values(:campaignId,:campaignName,:doctorName,:patientName,:contactNo,:appDate,:language,:callStatus,:historyId)";

	public static final String INSERT_CONTACT_DET = "insert into appointment_remainder.contact_det (campaign_id,campaign_name,last_four_digits,customer_mobile_number,total_due,minimum_payment,due_date,language,call_status,upload_history_id) values(:campaignId,:campaignName,:last_four_digits,:customer_mobile_number,:total_due,:minimum_payment,:due_date,:language,:callStatus,:historyId)";
	//    public static final String INSERT_CONTACT_DET_CT = "insert into appointment_remainder.contact_det (campaign_id,campaign_name,subskill_set,customer_mobile_number,language,call_status,upload_history_id,rec_upt_date,call_retry_count,due_date,actionId) values(:campaignId,:campaignName,:subskill_set,:customer_mobile_number,:language,:callStatus,:historyId,getdate(),'0',:due_date,:actionId)";

	public static final String INSERT_CONTACT_DET_CT = "insert into appointment_remainder.contact_det (campaign_id,campaign_name,last_four_digits,customer_mobile_number,total_due,minimum_payment,subskill_set,language,call_status,upload_history_id,rec_upt_date,call_retry_count,due_date,actionId,campaign_stopstatus) values(:campaignId,:campaignName,:last_four_digits,:customer_mobile_number,:total_due,:minimum_payment,:subskill_set,:language,:callStatus,:historyId,getdate(),'0',:due_date,:actionId,'NA')";
	//public static final String GET_CONTACT_DET = "select campaign_id,campaign_name,doctor_name,patient_name,contact_number,appointment_date,language,contact_id,call_retry_count,rec_upt_date,call_status from appointment_remainder.contact_det where call_status !='ANSWERED' order by appointment_date asc";

	public static final String GET_CONTACT_DET = "select campaign_id,campaign_name,last_four_digits,customer_mobile_number,total_due,minimum_payment,due_date,language,contact_id,call_retry_count,rec_upt_date,call_status from appointment_remainder.contact_det where call_status !='ANSWERED' order by appointment_date asc";


	public static final String VALIDATE_CAMPAIGN_NAME = "select count(1) from [appointment_remainder].[campaign_det] where name=:name";

	//    public static final String GET_SUMMARY_REPORT_DET = "select campaign_name,format(due_date,'dd-MMM-yyyy'),count(contact_id) as totalContact,SUM(CASE WHEN call_status!='New' THEN 1 ELSE 0 END) as contactCalled,"
	//            + "  SUM(CASE WHEN call_status in ('ANSWERED','BUSY','NOANSWER','NOT ANSWERED')  THEN 1 ELSE 0 END) as connectedCall,"
	//            + "  SUM(CASE WHEN call_status='NOT ANSWERED'  THEN 1 ELSE 0 END) as answered,"
	//            + "  SUM(CASE WHEN call_status='BUSY'  THEN 1 ELSE 0 END) as busy,"
	//            + "  SUM(CASE WHEN caller_response='1'  THEN 1 ELSE 0 END) as confirmed,"
	//            + "  SUM(CASE WHEN caller_response='2'  THEN 1 ELSE 0 END) as cancelled,"
	//            + "  SUM(CASE WHEN caller_response='3'  THEN 1 ELSE 0 END) as rescheduled,"
	//            + "  SUM(CASE WHEN caller_response='0'  THEN 1 ELSE 0 END) as noresponse,"
	//            + "  SUM(CASE WHEN call_status not in ('ANSWERED','BUSY','NOANSWER','NOT ANSWERED')  THEN 1 ELSE 0 END) as others"
	//            + "  from appointment_remainder.contact_det"
	//            + "  where cast(due_date as date) between :startDate and :endDate and campaign_id =:name"
	//            + "  group by campaign_name,format(due_date,'dd-MMM-yyyy')";
	/*
	 * public static final String GET_SUMMARY_REPORT_DET =
	 * "select campaign_name,format(due_date,'dd-MMM-yyyy'),count(contact_id) as totalContact,SUM(CASE WHEN call_status!='New' THEN 1 ELSE 0 END) as contactCalled,"
	 * + "  SUM(CASE WHEN call_status='ANSWERED'  THEN 1 ELSE 0 END) as answered," +
	 * "  SUM(CASE WHEN call_status='NO ANSWER'  THEN 1 ELSE 0 END) as notanswered,"
	 * + "  SUM(CASE WHEN call_status='BUSY'  THEN 1 ELSE 0 END) as busy," +
	 * "  SUM(CASE WHEN call_status='FAILED'  THEN 1 ELSE 0 END) as notreachable," +
	 * "  SUM(CASE WHEN caller_response='1'  THEN 1 ELSE 0 END) as confirmed," +
	 * "  SUM(CASE WHEN caller_response='2'  THEN 1 ELSE 0 END) as cancelled," +
	 * "  SUM(CASE WHEN caller_response='3'  THEN 1 ELSE 0 END) as rescheduled," +
	 * "  SUM(CASE WHEN caller_response='0'  THEN 1 ELSE 0 END) as noresponse," +
	 * "  SUM(CASE WHEN call_status not in ('ANSWERED','BUSY','NO ANSWER','FAILED')  THEN 1 ELSE 0 END) as others"
	 * + "  from appointment_remainder.contact_det" +
	 * "  where cast(due_date as date) between :startDate and :endDate and campaign_id =:name"
	 * + "  group by campaign_name,format(due_date,'dd-MMM-yyyy')";
	 */

	public static final String GET_SUMMARY_REPORT_DET = "select campaign_name,format(due_date,'dd-MMM-yyyy'),count(contact_id) as totalContact,SUM(CASE WHEN call_status!='New' THEN 1 ELSE 0 END) as contactCalled,"
			+ "  SUM(CASE WHEN call_status='ANSWERED'  THEN 1 ELSE 0 END) as answered,"
			+ "  SUM(CASE WHEN call_status='NO ANSWER'  THEN 1 ELSE 0 END) as notanswered,"
			+ "  SUM(CASE WHEN call_status='BUSY'  THEN 1 ELSE 0 END) as busy,"
			+ "  SUM(CASE WHEN call_status='FAILED'  THEN 1 ELSE 0 END) as notreachable,"
			+ "  SUM(CASE WHEN call_status='DNC'  THEN 1 ELSE 0 END) as dnc,"
			+ "  SUM(CASE WHEN caller_response='1'  THEN 1 ELSE 0 END) as confirmed,"
			+ "  SUM(CASE WHEN caller_response='2'  THEN 1 ELSE 0 END) as cancelled,"
			+ "  SUM(CASE WHEN caller_response='3'  THEN 1 ELSE 0 END) as rescheduled,"
			+ "  SUM(CASE WHEN caller_response='0'  THEN 1 ELSE 0 END) as noresponse,"
			+ "  SUM(CASE WHEN call_status not in ('ANSWERED','BUSY','NO ANSWER','FAILED')  THEN 1 ELSE 0 END) as others"
			+ "  from appointment_remainder.contact_det"
			+ "  where cast(due_date as date) between :startDate and :endDate and campaign_id =:name"
			+ "  group by campaign_name,format(due_date,'dd-MMM-yyyy')";


	//public static final String GET_CONTACT_DET_REPORT = "SELECT contact_id,campaign_id,campaign_name,doctor_name,patient_name,contact_number,format(appointment_date,'dd-MMM-yyyy hh:mm:ss tt'),caller_response ,call_status ,call_duration ,call_retry_count FROM appointment_remainder .contact_det where";

	public static final String GET_CONTACT_DET_REPORT = "SELECT contact_id,campaign_id,campaign_name,last_four_digits,customer_mobile_number,total_due,minimum_payment,format(due_date,'dd-MMM-yyyy hh:mm:ss tt'),caller_response ,call_status ,call_duration ,call_retry_count FROM appointment_remainder .contact_det where";

	public static final String GET_CONTACT_DET_SURVEY_DET = "";
	public static final String GET_CALL_RETRY_DET = "select contact_id,call_status,format(rec_add_dt,'dd-MMM-yyyy hh:mm:ss tt') from appointment_remainder.call_retry_det where contact_id in (:contactIdList) order by contact_id asc,rec_add_dt asc";

	public static final String GET_CALL_RETRY_SURVEY_DET = "select contact_id,call_status,format(rec_add_dt,'dd-MMM-yyyy hh:mm:ss tt') from appointment_remainder.call_retry_det where contact_id in (:contactIdList) order by contact_id asc";

	public static final String INSERT_CALL_RETRY_DET = "insert into appointment_remainder.call_retry_det (contact_id,call_status,retry_count,call_duration,contact_number,rec_add_dt,campaign_id,campaign_stopstatus) values (:contactId,:callStatus,:retryCount,:callDuration,:contact_number,getdate(),:campaign_id,'NA')";

	public static final String GET_UPLOAD_HISTORY_DETIALS = "select upload_history_id,campaign_id,campaign_name,uploaded_on,file_name from appointment_remainder.upload_history_det where status=1 and cast(uploaded_on as date) between :startDate and :endDate";

	public static final String DELETE_CONTACT_BY_HISTORY = "delete from appointment_remainder.contact_det where upload_history_id=:historyId";

	public static final String GET_CUSTOMER_DATA = " select customer_data_id,last_four_digits,customer_mobile_number,total_due,minimum_payment,due_date,status from appointment_remainder.customer_data where status = 'Active' ";
	public static final String GET_CALL_RETRY_COUNT = " select call_retry_count from appointment_remainder.contact_det where contact_id = :contact_id ";

	public static final String GET_CONTACT_DET_BY_DATE_RANGE = "select campaign_id,campaign_name,last_four_digits,customer_mobile_number,total_due,minimum_payment,due_date,language,contact_id,call_retry_count,rec_upt_date,call_status from appointment_remainder.contact_det where cast(due_date as date) between :startDate and :endDate ";
	public static final String GET_CONTACT_DET_BY_DATE_RANGE_SURVEY = "select campaign_id,campaign_name,last_four_digits,customer_mobile_number,total_due,minimum_payment,due_date,language,contact_id,call_retry_count,rec_upt_date,call_status from appointment_remainder.contact_det where cast(due_date as date) between :startDate and :endDate ";
	public static final String GET_CONTACT_DET_BY_DATE_RANGE_BY_USER_GROUP = "select campaign_id,campaign_name,last_four_digits,customer_mobile_number,total_due,minimum_payment,due_date,language,contact_id,call_retry_count,rec_upt_date,call_status from appointment_remainder.contact_det where cast(due_date as date) between :startDate and :endDate AND groupname=:userGroup";
	public static final String GET_CALL_RETRY_DETAILS = " select call_retry_id,contact_id,call_status,rec_add_dt,retry_count,call_duration from appointment_remainder.call_retry_det where contact_id = :contact_id ";
	public static final String GET_LEAD_WISE_SUMMARY_REPORT_DET = "select count(contact_id) as totalContact,SUM(CASE WHEN call_status!='New' THEN 1 ELSE 0 END) as contactCalled,"
			+ "  SUM(CASE WHEN call_status in ('ANSWERED','BUSY','NOANSWER','NOT ANSWERED')  THEN 1 ELSE 0 END) as connectedCall,"
			+ "  SUM(CASE WHEN call_status='ANSWERED'  THEN 1 ELSE 0 END) as answered,"
			+ "  SUM(CASE WHEN call_status='BUSY'  THEN 1 ELSE 0 END) as busy,"
			+ "  SUM(CASE WHEN call_status='NOANSWER'  THEN 1 ELSE 0 END) as notanswered,"
			+ "  SUM(CASE WHEN caller_response='1'  THEN 1 ELSE 0 END) as confirmed,"
			+ "  SUM(CASE WHEN caller_response='2'  THEN 1 ELSE 0 END) as cancelled,"
			+ "  SUM(CASE WHEN caller_response='3'  THEN 1 ELSE 0 END) as rescheduled,"
			+ "  SUM(CASE WHEN caller_response='0'  THEN 1 ELSE 0 END) as noresponse,"
			+ "  SUM(CASE WHEN call_status not in ('ANSWERED','BUSY','NOANSWER','NOT ANSWERED')  THEN 1 ELSE 0 END) as others"
			+ "  from appointment_remainder.contact_det"
			+ "  where cast(due_date as date) between :startDate and :endDate ";

	public static final String GET_LEAD_WISE_SUMMARY_REPORT_DET_SURVEY = "select count(actionId) as totalContact,SUM(CASE WHEN call_status!='New' THEN 1 ELSE 0 END) as contactCalled,"
			+ "  SUM(CASE WHEN call_status in ('ANSWERED','BUSY','NOANSWER','NOT ANSWERED')  THEN 1 ELSE 0 END) as connectedCall,"
			+ "  SUM(CASE WHEN call_status='ANSWERED'  THEN 1 ELSE 0 END) as answered,"
			+ "  SUM(CASE WHEN call_status='BUSY'  THEN 1 ELSE 0 END) as busy,"
			+ "  SUM(CASE WHEN call_status='NOANSWER'  THEN 1 ELSE 0 END) as notanswered,"
			+ "  SUM(CASE WHEN caller_response='1'  THEN 1 ELSE 0 END) as confirmed,"
			+ "  SUM(CASE WHEN caller_response='2'  THEN 1 ELSE 0 END) as cancelled,"
			+ "  SUM(CASE WHEN caller_response='3'  THEN 1 ELSE 0 END) as rescheduled,"
			+ "  SUM(CASE WHEN caller_response='0'  THEN 1 ELSE 0 END) as noresponse,"
			+ "  SUM(CASE WHEN call_status not in ('ANSWERED','BUSY','NOANSWER','NOT ANSWERED')  THEN 1 ELSE 0 END) as others"
			+ "  from appointment_remainder.survey_contact"
			+ "  where cast(due_date as date) between :startDate and :endDate ";

	public static final String GET_LEAD_WISE_SUMMARY_REPORT_DET_BY_USER_GROUP = "select count(contact_id) as totalContact,SUM(CASE WHEN call_status!='New' THEN 1 ELSE 0 END) as contactCalled,"
			+ "  SUM(CASE WHEN call_status in ('ANSWERED','BUSY','NOANSWER','NOT ANSWERED')  THEN 1 ELSE 0 END) as connectedCall,"
			+ "  SUM(CASE WHEN call_status='ANSWERED'  THEN 1 ELSE 0 END) as answered,"
			+ "  SUM(CASE WHEN call_status='BUSY'  THEN 1 ELSE 0 END) as busy,"
			+ "  SUM(CASE WHEN call_status='NOANSWER'  THEN 1 ELSE 0 END) as notanswered,"
			+ "  SUM(CASE WHEN caller_response='1'  THEN 1 ELSE 0 END) as confirmed,"
			+ "  SUM(CASE WHEN caller_response='2'  THEN 1 ELSE 0 END) as cancelled,"
			+ "  SUM(CASE WHEN caller_response='3'  THEN 1 ELSE 0 END) as rescheduled,"
			+ "  SUM(CASE WHEN caller_response='0'  THEN 1 ELSE 0 END) as noresponse,"
			+ "  SUM(CASE WHEN call_status not in ('ANSWERED','BUSY','NOANSWER','NOT ANSWERED')  THEN 1 ELSE 0 END) as others"
			+ "  from appointment_remainder.contact_det"
			+ "  where cast(due_date as date) between :startDate and :endDate AND groupname=:userGroup";

	public static final String GET_CALL_VOLUME_REPORT_DET = "select format(due_date,'dd-MMM-yyyy'),count(contact_id) as totalContact,SUM(CASE WHEN call_status!='New' THEN 1 ELSE 0 END) as contactCalled,"
			+ "  SUM(CASE WHEN call_status in ('ANSWERED','BUSY','NOANSWER','NOT ANSWERED')  THEN 1 ELSE 0 END) as connectedCall,"
			+ "  SUM(CASE WHEN call_status='ANSWERED'  THEN 1 ELSE 0 END) as answered,"
			+ "  SUM(CASE WHEN call_status='BUSY'  THEN 1 ELSE 0 END) as busy,"
			+ "  SUM(CASE WHEN call_status='NOANSWER'  THEN 1 ELSE 0 END) as notanswered,"
			+ "  SUM(CASE WHEN caller_response='1'  THEN 1 ELSE 0 END) as confirmed,"
			+ "  SUM(CASE WHEN caller_response='2'  THEN 1 ELSE 0 END) as cancelled,"
			+ "  SUM(CASE WHEN caller_response='3'  THEN 1 ELSE 0 END) as rescheduled,"
			+ "  SUM(CASE WHEN caller_response='0'  THEN 1 ELSE 0 END) as noresponse,"
			+ "  SUM(CASE WHEN call_status not in ('ANSWERED','BUSY','NOANSWER','NOT ANSWERED')  THEN 1 ELSE 0 END) as others"
			+ "  from appointment_remainder.contact_det"
			+ "  where cast(due_date as date) between :startDate and :endDate "
			+ "  group by format(due_date,'dd-MMM-yyyy')";

	public static final String GET_CALL_VOLUME_REPORT_DET_SURVEY = "select format(due_date,'dd-MMM-yyyy'),count(actionId) as totalContact,SUM(CASE WHEN call_status!='New' THEN 1 ELSE 0 END) as contactCalled,"
			+ "  SUM(CASE WHEN call_status in ('ANSWERED','BUSY','NOANSWER','NOT ANSWERED')  THEN 1 ELSE 0 END) as connectedCall,"
			+ "  SUM(CASE WHEN call_status='ANSWERED'  THEN 1 ELSE 0 END) as answered,"
			+ "  SUM(CASE WHEN call_status='BUSY'  THEN 1 ELSE 0 END) as busy,"
			+ "  SUM(CASE WHEN call_status='NOANSWER'  THEN 1 ELSE 0 END) as notanswered,"
			+ "  SUM(CASE WHEN caller_response='1'  THEN 1 ELSE 0 END) as confirmed,"
			+ "  SUM(CASE WHEN caller_response='2'  THEN 1 ELSE 0 END) as cancelled,"
			+ "  SUM(CASE WHEN caller_response='3'  THEN 1 ELSE 0 END) as rescheduled,"
			+ "  SUM(CASE WHEN caller_response='0'  THEN 1 ELSE 0 END) as noresponse,"
			+ "  SUM(CASE WHEN call_status not in ('ANSWERED','BUSY','NOANSWER','NOT ANSWERED')  THEN 1 ELSE 0 END) as others"
			+ "  from appointment_remainder.survey_contact"
			+ "  where cast(due_date as date) between :startDate and :endDate "
			+ "  group by format(due_date,'dd-MMM-yyyy')";
	public static final String GET_CALL_VOLUME_REPORT_DET_BY_USER_GROUP = "select format(due_date,'dd-MMM-yyyy'),count(contact_id) as totalContact,SUM(CASE WHEN call_status!='New' THEN 1 ELSE 0 END) as contactCalled,"
			+ "  SUM(CASE WHEN call_status in ('ANSWERED','BUSY','NOANSWER','NOT ANSWERED')  THEN 1 ELSE 0 END) as connectedCall,"
			+ "  SUM(CASE WHEN call_status='ANSWERED'  THEN 1 ELSE 0 END) as answered,"
			+ "  SUM(CASE WHEN call_status='BUSY'  THEN 1 ELSE 0 END) as busy,"
			+ "  SUM(CASE WHEN call_status='NOANSWER'  THEN 1 ELSE 0 END) as notanswered,"
			+ "  SUM(CASE WHEN caller_response='1'  THEN 1 ELSE 0 END) as confirmed,"
			+ "  SUM(CASE WHEN caller_response='2'  THEN 1 ELSE 0 END) as cancelled,"
			+ "  SUM(CASE WHEN caller_response='3'  THEN 1 ELSE 0 END) as rescheduled,"
			+ "  SUM(CASE WHEN caller_response='0'  THEN 1 ELSE 0 END) as noresponse,"
			+ "  SUM(CASE WHEN call_status not in ('ANSWERED','BUSY','NOANSWER','NOT ANSWERED')  THEN 1 ELSE 0 END) as others"
			+ "  from appointment_remainder.contact_det"
			+ "  where cast(due_date as date) between :startDate and :endDate "
			+ "  group by format(due_date,'dd-MMM-yyyy') AND groupname=:userGroup";


	public static final String INSERT_DUMMY_CONTACT_DET = "insert into appointment_remainder.dbo.dummy_det (campaign_id,mobile_number,unix_time,due_date,contact_id) values(:campaign_id,:mobile_number,:unix_time,:due_date,:contact_id)";

	//Added on 05/02/2024
	public static final String GET_CAMPAIGN_STATUS = "select campaign_status from [appointment_remainder].[campaign_status] where campaign_id=:campaign_id";
	public static final String GET_CAMPAIGN_FRONT_STATUS = "select campaign_frontendstatus from [appointment_remainder].[campaign_status] where campaign_id=:campaign_id";
	public static final String GET_CAMPAIGN_ACTIVE_STATUS = "select status from [appointment_remainder].[campaign_det] where campaign_id=:campaign_id";

	//Added on 14/02/2024
	public static final String INSERT_CAMPAIGN_STATUS = "insert into appointment_remainder.campaign_status (campaign_id,campaign_status,campaign_frontendstatus) values (:campaignId,:campaign_status,:campaign_frontendstatus)";
	public static final String INSERT_CAMPAIGN_STATUS_NEW = "insert into appointment_remainder.campaign_status (campaign_id,campaign_status,campaign_frontendstatus) values (:campaignId,:campaign_status,:campaign_frontendstatus)";


	public static final String UPDATE_CAMPAIGN_STATUS = "update appointment_remainder.campaign_status SET campaign_status=:campaign_status where campaign_id=:campaignId";
	public static final String UPDATE_CAMPAIGN_FRONT_STATUS = "update appointment_remainder.campaign_status SET campaign_status=:campaign_status,campaign_frontendstatus=:campaign_frontendstatus where campaign_id=:campaignId";

	public static final String UPDATE_CAMPAIGN_STATUS_ON_UPLOADCONTACT = "update appointment_remainder.campaign_status SET campaign_frontendstatus=:campaign_frontendstatus where campaign_id=:campaignId";

	public static final String GET_CAMPAIGN_DET_RT = "select campaign_id,name,status,start_date,end_date,start_time,end_time,concurrent_call,dncId,retry_count FROM appointment_remainder.campaign_det where status=1";

	public static final String GET_CAMPAIGN_BASED_COUNT = "select COUNT(*) from appointment_remainder.survey_contact_det where MainSkillset=:campaignname";

	public static final String GET_CAMPAIGN_BASED_CONT_STATUS = "select COUNT(*) from appointment_remainder.survey_contact_det where MainSkillset=:campaignname AND call_status=:disposition";

	//    public static final String INSERT_ACTIVE_CONTACT_DET = "insert into appointment_remainder.active_contact_det (calluid,productid,connectedlinenum,status) values (:calluid,:productid,:connectedlinenum,:status)";

	//    public static final String UPDATE_ACTIVE_CONTACT_DET = "update appointment_remainder.active_contact_det SET status=:status,errorcode=:errorcode where calluid=:calluid and productid=:productid and connectedlinenum=:connectedlinenum";

	//    public static final String GET_ACTIVE_CONTACT_DET = "select COUNT(*) from appointment_remainder.active_contact_det where productid=:campaignId AND status=:status";

	public static final String GET_ACTIVE_CONTACT_ERR_DET = "select COUNT(*) from appointment_remainder.active_contact_det where productid=:campaignname AND errorcode in (";

	//    public static final String INSERT_SURVEY_CONTACT_DET = "insert into appointment_remainder.survey_contact_det (phone,actionId,Survey_Lang,MainSkillset,subSkillset) values (:phone,:actionId,:Survey_Lang,:MainSkillset,:subSkillset)";

	//    public static final String GET_SURVEY_CONTACT_DET = "select subSkillset,phone,actionId, Survey_Lang,MainSkillset from appointment_remainder.survey_contact_det";


	public static final String INSERT_dNS_DET = "INSERT INTO appointment_remainder.DNC_Details(DNC_Name,description) VALUES (:DNC_Name,:description)";
	public static final String GET_dns_DET = "Select DNCID,DNC_Name,description from appointment_remainder.DNC_Details";
	public static final String UPDATE_DNS_DET = "UPDATE appointment_remainder.DNC_Details SET DNC_Name=:DNC_Name,description = :description where DNCID=:DNCID";
	public static final String INSERT_CONTACT_DET1 = "insert into appointment_remainder.DNC_Contact (dncId,contactNumber) values(:DNCID,:contactNumber)";

	public static final String GET_SURVEY_CALL_RETRY_COUNT = "select call_retry_count from appointment_remainder.survey_contact_det where actionId = :actionId ";
	public static final String GET_DNC_CONTACT = "select count(*) from appointment_remainder.contact_det where call_status='DNC' and campaign_id =:campaign_id and campaign_stopstatus!='NTC'";
	public static final String GET_DNC_CONTACT_DET = "select contactNumber from appointment_remainder.DNC_Contact where dncId=:DNCID";

	public static final String UPADTE_SURVEY_CALL_DET = "UPDATE appointment_remainder.survey_contact_det SET  call_status=:callStatus, call_duration=:callDuration, call_retry_count=:retryCount,rec_upt_date=getdate() where actionId=:actionId";
	//    public static final String UPADTE_SURVEY_CALL_DET = "UPDATE appointment_remainder.survey_contact_det SET caller_response=:callerResponse, call_status=:callStatus, call_duration=:callDuration, call_retry_count=:retryCount,rec_upt_date=getdate() where contact_id=:contactId";
	public static final String GET_DNCID_BY_CAMPAIGN_ID = "select dncId from appointment_remainder.campaign_det where campaign_id=:campaign_id";
	public static final String UPDATE_CAMPAIGN_STATUS_ON_COMPLETE = "update appointment_remainder.campaign_status SET campaign_frontendstatus=:campaign_frontendstatus where campaign_id=:campaignId";

	//public static final String COUNT_ACTIVE_CONTACT_DET = "SELECT COUNT(*) FROM appointment_remainder.active_contact_det WHERE productid=:productid and status=:status";

	public static final String COUNT_ACTIVE_CONTACT_DET = "SELECT COUNT(*) FROM appointment_remainder.active_contact_det WHERE productid=:productId AND status=:status and connectTime >= DATEADD(second, -20, GETDATE())";

	//public static final String INSERT_ACTIVE_CONTACT_DET = "insert into appointment_remainder.active_contact_det (calluid,productid,connectedlinenum,status,connectTime) values (:calluid,:productid,:connectedlinenum,:status,GETDATE())";

	public static final String INSERT_SURVEY_CONTACT_DET = "insert into appointment_remainder.survey_contact_det (phone,actionId,Survey_Lang,MainSkillset,subSkillset,call_status,rec_upt_date,call_retry_count) values (:phone,:actionId,:Survey_Lang,:MainSkillset,:subSkillset,:call_status,getdate(),'0')";
	//    public static final String GET_SURVEY_CONTACT_DET = "select subSkillset,phone,actionId, Survey_Lang,MainSkillset,call_status from appointment_remainder.survey_contact_det where call_status !='ANSWERED'";
	public static final String GET_SURVEY_CONTACT_DET = "select MainSkillset,phone,actionId, Survey_Lang,subSkillset,call_status,rec_upt_date,call_retry_count from appointment_remainder.survey_contact_det where call_status !='ANSWERED'";

	public static final String INSERT_ACTIVE_CONTACT_DET = "insert into appointment_remainder.active_contact_det (calluid,productid,connectedlinenum,status,campaignname,connectTime) values (:calluid,:productid,:connectedlinenum,:status,:campaignname,GETDATE())";

	public static final String UPDATE_ACTIVE_CONTACT_DET = "update appointment_remainder.active_contact_det SET status=:status,errorcode=:errorcode,connectTime=GETDATE() where calluid=:calluid and campaignname=:campaignname and connectedlinenum=:connectedlinenum";
	public static final String GET_ACTIVE_CONTACT_DET = "select COUNT(*) from appointment_remainder.active_contact_det where campaignname=:campaignname AND status=:status";
	public static final String GET_COMPLETED_CALL_COUNT = "select count(*) from appointment_remainder.survey_contact_det where call_status != 'ANSWERED' and call_retry_count>=:campaigncount";

	public static final String GET_CAMPAIGN_DET_RT_BY_USERGROUP = "select campaign_id,name,status,start_date,end_date,start_time,end_time,concurrent_call,dncId,retry_count FROM appointment_remainder.campaign_det where  groupname=:userGroup";
	public static final String DELETE_CONTACT_DET1 = "Delete from appointment_remainder.DNC_Contact where dncId=:DNCID and contactNumber=:contactNumber";

	public static final String INSERT_SURVEY_CONTACT_DET_CT = "insert into appointment_remainder.contact_det (customer_mobile_number,language,campaign_name,subskill_set,call_status,rec_upt_date,call_retry_count,due_date,campaign_id,actionId,Agent_ID,campaign_stopstatus) values (:phone,:Survey_Lang,:MainSkillset,:subSkillset,:call_status,getdate(),'0',:due_date,:campaign_id,:actionId,:Agent_ID,'NA')";
	public static final String GET_COMPLETED_CALL_COUNT_CT = "select count(*) from appointment_remainder.contact_det where call_status != 'ANSWERED' and call_retry_count>=:campaigncount and campaign_name=:campaignName and campaign_stopstatus!='NTC'";

	//    public static final String GET_SURVEY_CONTACT_DET_CT = "select campaign_name,customer_mobile_number,actionId, language,subskill_set,call_status,rec_upt_date,call_retry_count from appointment_remainder.contact_det where call_status !='ANSWERED'";


	public static final String GET_SURVEY_CONTACT_DET_CT = "select campaign_name,customer_mobile_number,actionId, language,subskill_set,call_status,rec_upt_date,call_retry_count,last_four_digits,total_due,minimum_payment,due_date from appointment_remainder.contact_det where call_status = 'NEW' and campaign_stopstatus!='NTC' order by rec_upt_date asc";


	public static final String UPADTE_SURVEY_CALL_DET_CT = "UPDATE appointment_remainder.contact_det SET caller_response=:callerResponse, call_status=:callStatus, call_duration=:callDuration, call_retry_count=:retryCount,rec_upt_date=getdate(),survey_rating=:survey_rating,SMS_Triggered=:SMS_Triggered,call_start_time=:call_start_time,call_end_time=:call_end_time  where actionId=:actionId";
	public static final String GET_SURVEY_CALL_RETRY_COUNT_CT = "select call_retry_count from appointment_remainder.contact_det where actionId = :actionId";
	public static final String GET_CAMPAIGN_BASED_COUNT_CT = "select COUNT(*) from appointment_remainder.contact_det where campaign_name=:campaignname and campaign_stopstatus!='NTC'";
	public static final String GET_CAMPAIGN_BASED_CONT_STATUS_CT = "select COUNT(*) from appointment_remainder.contact_det where campaign_name=:campaignname AND call_status=:disposition and campaign_stopstatus!='NTC'";
	public static final String UPDATE_AGENT_DEVICE_STATUS = "update appointment_remainder.agent_status_det SET  state=:state, rec_upt_date=getdate() where device=:device";

	public static final String GET_AGENT_LOG_IDLETIME = "SELECT TOP 1 userId,Device from appointment_remainder.agent_status_det where State='NOT_INUSE' ORDER BY rec_upt_date";

	public static final String UPDATE_AGENT_LOGIN_STATUS = "update appointment_remainder.agent_status_det SET  rec_upt_date=getdate(),rec_login_date=getdate(),logstatus=1,state=:state where device=:device";

	public static final String UPDATE_AGENT_LOGOUT_STATUS = "update appointment_remainder.agent_status_det SET  rec_upt_date=getdate(),rec_logout_date=getdate(),logstatus=0,state=:state where device=:device";

	public static final String GET_AVAIL_AGENT_FORCAMPAIGN = "SELECT TOP 1 * FROM[appointment_remainder].[agent_status_det] where State='Reachable' ORDER BY [rec_upt_date]";

	public static final String GET_CAMPAIGN_END_DATE_ID = "SELECT TOP 1 end_date,end_time,campaign_id FROM[appointment_remainder].[campaign_det] where name=:name";

	public static final String GET_CAMPAIGN_END_DATE = "SELECT TOP 1 end_date,end_time FROM[appointment_remainder].[campaign_det] where name=:name";

	public static final String UPDATE_CAMPAIGN_STATUS_BASED_ON_DNC = "update appointment_remainder.contact_det set call_status='DNC' where [customer_mobile_number]=:contactNumber and call_status='NEW'";

	public static final String DELETE_DNC_ON_CAMPAIGN_DET = "update appointment_remainder.contact_det set call_status='NEW' where [customer_mobile_number]=:contactNumber and call_status='DNC'";


	public static final String GET_ACTIONID_SEQUENCE = "SELECT NEXT VALUE FOR ActionIDSequence;";

	public static final String GET_BUSY_COUNT = "select count(*) from appointment_remainder.call_retry_det where call_status = 'BUSY' and campaign_id=:campaign_id and campaign_stopstatus!='NTC'";
	public static final String GET_NO_ANSWER_COUNT = "select count(*) from appointment_remainder.call_retry_det where call_status = 'NO ANSWER' and campaign_id=:campaign_id and campaign_stopstatus!='NTC'";
	public static final String GET_ANSWERED_COUNT = "select count(*) from appointment_remainder.call_retry_det where call_status = 'ANSWERED' and campaign_id=:campaign_id and campaign_stopstatus!='NTC'";
	public static final String GET_ANSWER_DURATION = "select sum(call_duration) from [appointment_remainder].[call_retry_det]where call_status = 'ANSWERED' and campaign_id=:campaign_id and campaign_stopstatus!='NTC'";

	public static final String GET_FAILED_COUNT = "select count(*) from appointment_remainder.call_retry_det where call_status = 'FAILED' and campaign_id=:campaign_id and campaign_stopstatus!='NTC'";

	public static final String GET_CAMPAIGNID_FOR_RETRY_DET = "select campaign_id,customer_mobile_number,contact_id from appointment_remainder.contact_det where actionId = :actionId";

	public static final String CHECK_CONTACT_NUMBER = "select count(*) from appointment_remainder.contact_det where customer_mobile_number =:customer_mobile_number and campaign_id =:campaign_id";

	public static final String GET_CAMPAIGN_COMPLETED_TIME_RT = "SELECT top 1 rec_add_dt FROM [appointment_remainder].[call_retry_det] where campaign_id =:campaign_id ORDER BY rec_add_dt DESC";

	public static final String GET_CAMP_RETRY_DET_BASED_ON_ACTIONID = "SELECT top 1 campaign_id  FROM [appointment_remainder].[contact_det] where actionId =:actionId";

	public static final String GET_CAMP_RETRY_DET = "SELECT top 1 retry_count FROM [appointment_remainder].[campaign_det] where campaign_id =:campaign_id";

	public static final String GET_SMS_REPORT_DET = "SELECT campaign_id,Agent_ID,customer_mobile_number,call_status,survey_rating,call_start_time,call_end_time, call_retry_count,SMS_Triggered,SMS_Triggered_DateTime,actionId FROM appointment_remainder .contact_det where campaign_id=:campaignId and cast(rec_upt_date as date) between :startDate and :endDate ORDER BY call_start_time DESC";

	public static final String GET_LANGUAGE_BASED_ON_ACTIONID = "SELECT language FROM [appointment_remainder].[contact_det] where actionId =:actionId";

	//    public static final String UPDATE_SMS_TRIGGERED = "update appointment_remainder.contact_det set SMS_Triggered='YES' where actionId =:actionId ";

	public static final String GET_CONTACT_DET_RETRY = "select campaign_name,customer_mobile_number,actionId, language,subskill_set,call_status,rec_upt_date,call_retry_count,last_four_digits,total_due,minimum_payment,due_date FROM appointment_remainder.contact_det WHERE campaign_id = :campaignId AND call_retry_count <= :retryCount AND call_status!='New' AND call_status!='ANSWERED' AND call_status!='InProgress' AND call_status!='INVALID NO' and call_status!='DNC' and campaign_stopstatus!='NTC'";

	public static final String UPDATE_SMS_TRIGGERED = "update appointment_remainder.contact_det set SMS_Triggered='YES',SMS_Triggered_DateTime=getdate() where actionId =:actionId ";

	public static final String GET_SMS_WEB_REPORT = "SELECT top 1 surveylinkstarttime,surveylinkendtime,report FROM [dbo].[survey_reports] where actionId =:actionId";
	public static final String UPDATE_NTC_STATUS = "update appointment_remainder.contact_det set campaign_stopstatus='NTC' where campaign_id =:campaign_id";

	public static final String UPDATE_NTC_STATUS_BY_ACTIONID = "update appointment_remainder.contact_det set campaign_stopstatus='NTC' where actionId =:actionId";
	public static final String UPDATE_NTC_STATUS_IN_RETRY_DET = "update appointment_remainder.call_retry_det set campaign_stopstatus='NTC' where campaign_id =:campaign_id";
	public static final String INPROGRESS_LIST = "select contact_id,customer_mobile_number,call_retry_count,actionId from appointment_remainder.contact_det where campaign_id=:campaign_id and call_status='InProgress' and rec_upt_date < DATEADD(minute, -2, GETDATE())";
	public static final String GET_CAMPAIGN_START_TIME_RT = "SELECT TOP 1 rec_add_dt FROM [appointment_remainder].[call_retry_det] WHERE campaign_id = :campaign_id AND campaign_stopstatus != 'NTC' ORDER BY rec_add_dt ASC";
	public static final String GET_CONT_MAP_DET = "SELECT [number_of_field],[reserve_1],[reserve_2],[reserve_3],[reserve_4],[reserve_5],[reserve_6],[reserve_7],[reserve_8],[reserve_9],[reserve_10],[reserve_11] FROM [appointment_remainder_db].[appointment_remainder].[contact_mapping_det] where account=:account";
	public static final String GET_DYANMIC_CONTACT_DET_CT = "select campaign_id,campaign_name,customer_mobile_number,actionId,agent_userid,[reserve_1],[reserve_2],[reserve_3],[reserve_4],[reserve_5],[reserve_6],[reserve_7],[reserve_8],[reserve_9],[reserve_10],[reserve_11] FROM [appointment_remainder_db].[appointment_remainder].[contact_det_new_1] where campaign_id=:campaign_id ";
	public static final String UPDATE_DYANMIC_CONTACT_DET_CT ="update appointment_remainder.contact_det_new_1 set agent_userid=:agent_userid where actionId =:actionId and customer_mobile_number=:customer_mobile_number and campaign_id=:campaign_id";

	public static final String UPDATE_ASSIGNED_DYANMIC_CONTACT_DET_CT ="update appointment_remainder.contact_det_new_1 set agent_userid as NULL where actionId =:actionId and customer_mobile_number=:customer_mobile_number and campaign_id=:campaign_id";

	
	public static final String GET_AGENT_BASED_CONTACT_DET = "select campaign_id,campaign_name,customer_mobile_number,agent_userid,actionId,[reserve_1],[reserve_2],[reserve_3],[reserve_4],[reserve_5],[reserve_6],[reserve_7],[reserve_8],[reserve_9],[reserve_10],[reserve_11] FROM [appointment_remainder_db].[appointment_remainder].[contact_det_new_1] where agent_userid=:agent_userid";
	//public static final String GET_CONTACT_ASSIGNED_TO_AGENT = "select count(*) from appointment_remainder.contact_det where customer_mobile_number =:customer_mobile_number and campaign_id =:campaign_id";
	public static final String GET_CAMPAIGN_BASED_CONTACT_DET = "select campaign_id,campaign_name,customer_mobile_number,agent_userid,actionId,[reserve_1],[reserve_2],[reserve_3],[reserve_4],[reserve_5],[reserve_6],[reserve_7],[reserve_8],[reserve_9],[reserve_10],[reserve_11] FROM [appointment_remainder_db].[appointment_remainder].[contact_det_new_1] where campaign_id=:campaign_id";
	

	public static final String GET_ASSIGNED_AGENT_BASED_ON_SUPERVISOR = "select Agent FROM [appointment_remainder_db].[appointment_remainder].[agent_sup_mapping] where Supervisor=:Supervisor";
	
	public static final String GET_AGENT_BASED_CONTACT_STATUS_DET = "select campaign_id,campaign_name,customer_mobile_number,agent_userid,actionId,call_status,[reserve_1],[reserve_2],[reserve_3],[reserve_4],[reserve_5],[reserve_6],[reserve_7],[reserve_8],[reserve_9],[reserve_10],[reserve_11] FROM [appointment_remainder_db].[appointment_remainder].[contact_det_new_1] where agent_userid=:agent_userid";
	public static final String GET_AGENT_SKILSET="select SkillSet from [appointment_remainder].[usermanagement_det] where UserId=:UserId";
	public static final String GET_CONTACT_DET_BY_CUSTOMERNUMBER = "select campaign_id,campaign_name,customer_mobile_number,agent_userid,actionId,call_status,[language],call_retry_count,upload_history_id,rec_upt_date,contact_id,subskill_set,[reserve_1],[reserve_2],[reserve_3],[reserve_4],[reserve_5],[reserve_6],[reserve_7],[reserve_8],[reserve_9],[reserve_10],[reserve_11] FROM [appointment_remainder].[contact_det_new_1] where customer_mobile_number=:customerNumber";

}

