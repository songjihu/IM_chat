package com.example.im_chat.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 消息打包规则
 * 要传入来源去向和内容
 */

public class MessageTranslateTo {
    private String msgFrom;//消息发送者
    private String msgFromId;//消息发送者
    private String msgTo;//消息接收群组id
    private String msgDate;//消息发送时间
    private String msgContent;//消息内容
    private String msgJson;//转换结果
    private String msgType;//消息类型



    public MessageTranslateTo(String msgFrom, String msgFromId, String msgTo, String msgContent,String msgType){
        /*Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH,0);
        calendar.add(Calendar.MINUTE, 0);*/
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        this.msgFrom = msgFrom;
        this.msgFromId = msgFromId;
        this.msgTo = msgTo;
        this.msgDate = formatter.format(currentTime);
        this.msgContent = msgContent;
        this.msgType=msgType;
        this.msgJson = "{\"msgFrom\":\""+this.msgFrom+"\"," +
                        "\"msgFromId\":\""+this.msgFromId+"\"," +
                        "\"msgTo\":\""+this.msgTo+"\"," +
                        "\"msgDate\":\""+this.msgDate+"\"," +
                        "\"msgType\":\""+this.msgType+"\"," +
                        "\"msgContent\":\""+this.msgContent+"\"}";

    }

    public void setMsgFrom(String msgFrom) {
        this.msgFrom = msgFrom;
    }

    public void setMsgTo(String msgTo) {
        this.msgTo = msgTo;
    }

    public void setMsgDate(String msgDate) {
        this.msgDate = msgDate;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    public void setMsgJson(String msgJson) {
        this.msgJson = msgJson;
    }

    public void setMsgFromId(String msgFromId) {
        this.msgFromId = msgFromId;
    }

    public String getMsgFromId() {
        return msgFromId;
    }

    public String getMsgFrom() {
        return msgFrom;
    }

    public String getMsgTo() {
        return msgTo;
    }

    public String getMsgDate() {
        return msgDate;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public String getMsgJson() {
        return msgJson;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }


}
