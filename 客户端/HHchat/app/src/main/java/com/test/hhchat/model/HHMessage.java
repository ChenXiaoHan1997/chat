package com.test.hhchat.model;

import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 * Created by Administrator on 2017/11/22 0022.
 */

public class HHMessage extends DataSupport {

    private String fromPhone;
    private String targetPhone;
    private String content;
    private Date time;

    public HHMessage(String fromPhone, String targetPhone, String content, Date time) {
        this.fromPhone = fromPhone;
        this.targetPhone = targetPhone;
        this.content = content;
        this.time = time;
    }

    public String getFromPhone() {
        return fromPhone;
    }

    public void setFromPhone(String fromPhone) {
        this.fromPhone = fromPhone;
    }

    public String getTargetPhone() {
        return targetPhone;
    }

    public void setTargetPhone(String targetPhone) {
        this.targetPhone = targetPhone;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
