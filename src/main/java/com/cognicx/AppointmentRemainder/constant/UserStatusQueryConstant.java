package com.cognicx.AppointmentRemainder.constant;

public class UserStatusQueryConstant {
    public static final String GET_USER_ID = "select max(id) from appointment_remainder.agent_update_status";

    public static final String  INSERT_USER_STATUS = "insert into appointment_remainder.user_status(userStatusId,status_name,status_desc,enabled,status_code) values (:userStatusId,:status_name,:status_desc,:enabled,:status_code)";

    public static final String GET_AGENT_DET = "SELECT status_id, status_name FROM appointment_remainder.agent_status_list WHERE not_ready_code_id IS NULL ORDER BY status_id;";

    public static final String GET_NOT_READY ="SELECT  not_ready_code_id, not_ready_code_description FROM appointment_remainder.agent_status_list WHERE status_name = 'Not Ready' AND not_ready_code_id IS NOT NULL ORDER BY not_ready_code_id";

    public static  final String INSERT_AGENT_UPDATE_STATUS = "insert into appointment_remainder.agent_update_status(id,agent_id,status,updated_date_time) values(:id,:agent_id,:status,:updated_date_time)";
}
