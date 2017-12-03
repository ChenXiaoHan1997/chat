package com.test.hhchat.model;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/11/22 0022.
 */

public class HHUser extends DataSupport {

    private String phone;
    private String nickname;
    private boolean isOnline;
    private int unreadMessageNum;

    public HHUser(String phone, String nickname, boolean isOnline, int unreadMessageNum) {
        this.phone = phone;
        this.nickname = nickname;
        this.isOnline = isOnline;
        this.unreadMessageNum = unreadMessageNum;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public int getUnreadMessageNum() {
        return unreadMessageNum;
    }

    public void setUnreadMessageNum(int unreadMessageNum) {
        this.unreadMessageNum = unreadMessageNum;
    }
}
