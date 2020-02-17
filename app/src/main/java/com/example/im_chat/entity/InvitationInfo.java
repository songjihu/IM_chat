package com.example.im_chat.entity;

public class InvitationInfo {
    private String beizhu;
    private String fromJid;
    private String fromName;
    private String fromTime;

    public InvitationInfo(String beizhu,String fromJid,String fromName,String fromTime){
        this.beizhu=beizhu;
        this.fromJid=fromJid;
        this.fromName=fromName;
        this.fromTime=fromTime;
    }

    public String getFromJid() {
        return fromJid;
    }

    public void setFromJid(String fromJid) {
        this.fromJid = fromJid;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFromTime() {
        return fromTime;
    }

    public void setFromTime(String fromTime) {
        this.fromTime = fromTime;
    }

    public String getBeizhu() {
        return beizhu;
    }

    public void setBeizhu(String beizhu) {
        this.beizhu = beizhu;
    }
}
