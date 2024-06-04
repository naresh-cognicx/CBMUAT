package com.cognicx.AppointmentRemainder.constant;

public class ApplicationConstant {
    public static final String SPRING_BASE_CLASS_PROPERTYSOURCE = "classpath:application.properties";
    public static final String LOGGER_PROPERTYSOURCE = "classpath:log4j.properties";
    // Hibernate Configuration
    public static final String HIBERNATE_DIALECT = "hibernate.dialect";
    public static final String HIBERNATE_SHOW_SQL = "hibernate.show_sql";
    public static final String HIBERNATE_FORMAT_SQL = "hibernate.format_sql";

    /* Data Source Names */
    public static final String FIRST_DATA_SOURCE_BEAN_NAME = "firstDataSource";

    public static final String TENANT_DATA_SOURCE_BEAN_NAME = "tenantDataSource";
    public static final String FIRST_ENTITY_MANAGER = "firstEntityManager";

    public static final String TENANT_ENTITY_MANAGER = "tenantEntityManager";
    public static final String FIRST_TRANSACTION_MANAGER = "firstTransactionManager";

    public static final String TENANT_TRANSACTION_MANAGER = "tenantTransactionManager";
    public static final String FIRST_JDBC_TEMPLATE = "firstJdbcTemplate";

    public static final String TENANT_JDBC_TEMPLATE = "tenantJdbcTemplate";
    public static final String FIRST_MODAL_PACKAGE = "com.cognicx.AppointmentRemainder.model";
    public static final String FIRST_PERSISTENCE_UNIT_NAME = "first";
    public static final String TENANT_PERSISTENCE_UNIT_NAME = "tenant";
    public static final String WILDFLY_STR = "WildFly";
    public static final String COMPONENT_SCAN = "com.cognicx";
    public static final String ENTITY_SCAN = "com.cognicx.AppointmentRemainder.model";
    public static final String EMPTY_STR = "";
    public static final String BADCREDENTIALS_EXCEPTION = "Please provide a valid login credentials or if you have forgotten the password please use the ‘Forget password’ to set a new password";
    public static final String USERNAMENOTFOUNDEXCEPTION = "User account does not exists. Please contact your system administrator.";
    public static final String SUPER_ADMIN_ROLE = "Super Admin";
    public static final String DEFAULT_DOMAIN_ID = "1";
    public static final String DEFAULT_BU_ID = "1";
    public static final String DEFAULT_DOMAIN = "Banking";
    public static final String DEFAULT_BU = "Retail";
    public static final String ACTIVE_STATUS = "ACTIVE";
    public static final String MOBILE_NUMBER_NOT_FOUND = "Please Provide Valid Mobile Number";
    public static final String MOBILE_NUMBER_IS_REQUIRED= "Mobile Number is Required";
    public static final String FAILED_TO_FETCH_DATA = "Failed To Fetch Data, Please check the data";
    public static final String CUSTOMER_DETAILS_NOT_AVAILABLE = "Customer Details not found";
    public static final String FAILED_TO_SAVE_CUSTOME_DETAILS_PLEASE_CONTACT_YOUR_SYSTEM_ADMIN = "Failed To Savecustomer details, please contact your system admin";
    public static final String FAILED_TO_SAVE_AGENT_INTERACTION_PLEASE_CONTACT_YOUR_SYSTEM_ADMIN = "Failed To Save agent interaction, please contact your system admin";

    public static final String FAILED_TO_UPDATE_DATA = "Failed To update Data, Please check the data";
    public static final String FAILED_TO_SAVE_CUSTOMER_DETAILS = "Failed To Save cust";

    public static final String BAD_CREDENTIALS = "Bad Credentials";

    public static final String FAILED_TO_LIST_USER_STATUS_PLEASE_CONTACT_YOUR_SYSTEM_ADMIN = "Failed To List User Status, please contact your system admin";

    public static final String FAILED_TO_LIST_USER_STATUS = "Failed To List Agent Status";

    public static final String FAILED_TO_SAVE_USER_STATUS_PLEASE_CONTACT_YOUR_SYSTEM_ADMIN = "Failed To Save User Status, please contact your system admin";

    public static final String FAILED_TO_SAVE_USER_STATUS = "Failed To Save user Status";

    public static final String USER_STATUS_IS_SAVED_SUCCESSFULLY = "agent Status is Saved Successfully";
    public static final String USER_STATUS_NAME_IS_REQUIRED = "User Status Name is Required";

    public static final String USER_STATUS_NOT_FOUND = "User Status not found";
    public static final String USER_STATUS_NAME_ALREADY_EXISTS = "User Status Name Already Exists";
    public static final String FAILED_TO_UPDATE = "Failed To Update";

    public static final String ENABLED_FLAG_UPDATED_SUCCESSFULLY = "Enabled Flag Updated Successfully";

    public static final String FAILED_TO_LIST_SKILLS_PLEASE_CONTACT_YOUR_SYSTEM_ADMIN = "Failed To List Skills, please contact your system admin";
    public static final String FAILED_TO_UPDATE_PLEASE_CONTACT_YOUR_SYSTEM_ADMIN = "Failed To Update, please contact your system admin";
    public static final String STATUS_NAME = "statusName";
    public static final String NO_USER_STATUS_FOUND = "No User Status Found";
    public static final String USER_STATUS_ID_IS_REQUIRED = "User Status Id is Required";
    public static final String ENABLED = "enabled";
    public static final String USER_STATUS_ID = "userStatusId";
    public static final String MODIFIED_BY = "modifiedBy";
    public static final String LAST_MODIFIED_DATE = "lastModifiedDate";
    public static final String TENANT_ID_IS_REQUIRED = "Tenant id is required";
    public static final String USER_DISPOSITION_IS_SAVED_SUCCESSFULLY = "User disposition is Saved Successfully";
    public static final String FAILED_TO_SAVE_USER_DISPOSITION = "Failed To Save user disposition";

    public static final String USER_DISPOSITION_NOT_FOUND = "User disposition not found";

    public static final String USER_DISPOSITION_NAME_IS_REQUIRED = "User disposition Name is Required";
    public static final String FAILED_TO_SAVE_DISPOSITION_STATUS = "Failed To Save disposition Status";
    public static final String FAILED_TO_SAVE_DISPOSITION_STATUS_PLEASE_CONTACT_YOUR_SYSTEM_ADMIN = "Failed To Save disposition Status, please contact your system admin";
    public static final String DISPOSITION_STATUS_ID_IS_REQUIRED = "Disposition Status Id is Required";
    public static final String USER_DISPOSITION_ID = "userDispositionId";
    public static final String DISPOSITION_NAME = "dispositionName";
    public static final String NO_USER_DISPOSITION_FOUND = "No User Disposition Found";
    public static final String FAILED_TO_LIST_USER_DISPOSITION = "Failed To List User Disposition";

    public static final String FAILED_TO_LIST_USER_DISPOSITION_PLEASE_CONTACT_YOUR_SYSTEM_ADMIN = "Failed To list User Disposition, please contact your system admin";
    public static final String USER_DISPOSITION_NAME_ALREADY_EXISTS = "User disposition Name Already Exists";
    public static final String AGENT_INTERACTION_IS_ALREADY_EXISTS = "Agent Interaction Record Already Exists";

    public static final String CUSTOMER_MOBILE_NUMBER_ALREADY_EXISTS = "Custome Mobile Number Already Exists";
    public static final String AGENT_ANI_NUMBER_AND_ID_IS_REQUIRED = "Agent ANI number and Agentid is Required";

    public static final String SEARCH_RESULT_NOT_FOUND = "search result not found";

}
