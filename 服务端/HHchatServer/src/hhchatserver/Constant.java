/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hhchatserver;

/**
 *
 * @author Administrator
 */
public class Constant {
    
    public static final String STR_LOGIN = "00";
    public static final String STR_LOGOUT = "01";
    public static final String STR_REGISTER = "02";
    public static final String STR_MESSAGE = "20";
    public static final String STR_HEARTBEAT = "10";
    public static final String STR_USERS = "11";
    public static final String STR_SET_NICKNAME = "12";
    
    public static final int RSLT_DB_CONN_ERROR = -3;
    public static final int RSLT_DB_SQL_ERROR = -2;            
    public static final int RSLT_LOGIN_OK = 1;
    public static final int RSLT_LOGIN_WRONG_PWD = 0;
    public static final int RSLT_LOGIN_NO_PHONE = -1;
    public static final int RSLT_REG_OK = 1;
    public static final int RSLT_REG_EXIST_PHONE = 0;
    public static final int RSLT_SETNAME_OK = 1;
    public static final int RSLT_SETNAME_FAIL = 0;
    
    public static final int TIME_MAX_HEARTBEAT_INTVL = 10;
    
}
