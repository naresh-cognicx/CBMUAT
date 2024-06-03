package com.cognicx.AppointmentRemainder.constant;

public class SkillsetQueryConstant {

//	public static final String INSERT_SKILLSET_DET = "INSERT INTO appointment_remainder.skillset_det(skillsetId,skillName,Language,TimeZone,ChannelType,ServiceLevelThreshold,ServiceLevelGoal,FirstCallResolution,AbandonedRateThreshold,ShortCallThreshold,ShortAbandonedThreshold,CountAbandonedSLA,Disposition) "
//			+ "VALUES (:skillsetId,:skillName,:Language,:TimeZone,:ChannelType,:ServiceLevelThreshold,:ServiceLevelGoal,:FirstCallResolution,:AbandonedRateThreshold,:ShortCallThreshold,:ShortAbandonedThreshold,:CountAbandonedSLA,:Disposition)";

	public static final String GET_SKILLSET_ID = "select max(SUBSTRING(skillsetId, 3, 100)) from appointment_remainder.skillset_det";

//	public static final String GET_SKILLSET_DET = "SELECT skillsetId,skillName,Language,TimeZone,ChannelType,ServiceLevelThreshold,ServiceLevelGoal,FirstCallResolution,AbandonedRateThreshold,ShortCallThreshold,ShortAbandonedThreshold,CountAbandonedSLA,Disposition FROM appointment_remainder.skillset_det";

//	public static final String UPDATE_SKILLSET_DET = "UPDATE appointment_remainder.skillset_det SET skillsetId = :skillsetId,skillName=:skillName,Language=:Language,TimeZone=:TimeZone,ChannelType=:ChannelType,ServiceLevelThreshold=:ServiceLevelThreshold,ServiceLevelGoal=:ServiceLevelGoal,FirstCallResolution=:FirstCallResolution,AbandonedRateThreshold=:AbandonedRateThreshold,ShortCallThreshold=:ShortCallThreshold,ShortAbandonedThreshold=:ShortAbandonedThreshold,CountAbandonedSLA=:CountAbandonedSLA,Disposition=:Disposition WHERE skillsetId = :skillsetId";

	public static final String VALIDATE_SKILLSET_NAME = "select count(1) from [appointment_remainder].[skillset_det] where skillsetId=:skillsetId";


	public static final String INSERT_SKILLSET_DET = "INSERT INTO appointment_remainder.skillset_det(skillsetId,skillName,Language,TimeZone,ChannelType,ServiceLevelThreshold,ServiceLevelGoal,FirstCallResolution,AbandonedRateThreshold,ShortCallThreshold,ShortAbandonedThreshold,CountAbandonedSLA,Disposition,ForceACW,ForceACWSec,AutoAnswer,AutoAnswerValue,VVDNQueueId,RoutingStrategy,NoAnswer) "
			+ "VALUES (:skillsetId,:skillName,:Language,:TimeZone,:ChannelType,:ServiceLevelThreshold,:ServiceLevelGoal,:FirstCallResolution,:AbandonedRateThreshold,:ShortCallThreshold,:ShortAbandonedThreshold,:CountAbandonedSLA,:Disposition,:forceACW,:forceACWSec,:autoanswer,:autoanswerValue,:VDNQueueId,:routingStrategy,:NoAnswer)";


	public static final String GET_SKILLSET_DET = "SELECT skillsetId,skillName,Language,TimeZone,ChannelType,ServiceLevelThreshold,ServiceLevelGoal,FirstCallResolution,AbandonedRateThreshold,ShortCallThreshold,ShortAbandonedThreshold,CountAbandonedSLA,Disposition,ForceACW,ForceACWSec,AutoAnswer,AutoAnswerValue,VDNQueueId,RoutingStrategy,NoAnswer FROM appointment_remainder.skillset_det";

	public static final String UPDATE_SKILLSET_DET = "UPDATE appointment_remainder.skillset_det SET skillsetId = :skillsetId,skillName=:skillName,Language=:Language,TimeZone=:TimeZone,ChannelType=:ChannelType,ServiceLevelThreshold=:ServiceLevelThreshold,ServiceLevelGoal=:ServiceLevelGoal,FirstCallResolution=:FirstCallResolution,AbandonedRateThreshold=:AbandonedRateThreshold,ShortCallThreshold=:ShortCallThreshold,ShortAbandonedThreshold=:ShortAbandonedThreshold,CountAbandonedSLA=:CountAbandonedSLA,Disposition=:Disposition,ForceACW=:forceACW,ForceACWSec=:forceACWSec,AutoAnswer=:autoanswer,AutoAnswerValue=:autoanswerValue,VVDNQueueId=:VDNQueueId,RoutingStrategy=:routingStrategy,NoAnswer=:NoAnswer WHERE skillsetId = :skillsetId";
	
}
