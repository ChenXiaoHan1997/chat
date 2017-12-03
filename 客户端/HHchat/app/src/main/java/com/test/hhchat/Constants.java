package com.test.hhchat;

import android.graphics.Color;

/**
 * Created by Administrator on 2017/11/22 0022.
 */

public class Constants {

    public static final String STR_LOGIN = "00";
    public static final String STR_LOGOUT = "01";
    public static final String STR_REGISTER = "02";
    public static final String STR_MESSAGE = "20";
    public static final String STR_HEARTBEAT = "10";
    public static final String STR_USERS = "11";
    public static final String STR_SET_NICKNAME = "12";
    public static final String STR_SET_PASSWORD = "13";

    public static final String SP_HOST = "host";
    public static final String SP_PORT = "port";
    public static final String SP_PHONE = "phone";
    public static final String SP_NICKNAME = "nickname";
    public static final String SP_PASSWORD = "password";

    public static final String INTNT_HOST = "host";
    public static final String INTN_PORT = "port";
    public static final String ITNT_MY_PHONE = "my_phone";
    public static final String ITNT_MY_NICKNAME = "my_nickname";
    public static final String ITNT_OTHER_PHONE = "other_phone";
    public static final String ITNT_OTHER_NICKNAME = "other_nickname";

    public static final int RQCD_SERVERPREF = 1;
    public static final int RQCD_REGISTER = 2;
    public static final int RQCD_SETNICKNAME = 3;

    public static final int COLOR_ONLINE = Color.rgb(255, 165, 0);
    public static final int COLOR_OFFLINE = Color.rgb(181, 181, 181);

    public static final int TIMES_MAX_WAIT_ACK = 5;
    public static final long TIME_MAX_WAIT_USERS = 3000;
    public static final long TIME_SPLASH = 500;
    public static final long TIME_HEARTBEAT_INTVL = 4500;
}