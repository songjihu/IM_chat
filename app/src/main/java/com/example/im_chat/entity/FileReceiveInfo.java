package com.example.im_chat.entity;

public class FileReceiveInfo {
    private String beizhu;
    private String fromJid;
    private String fromName;
    private String fromTime;
    private String file_path;

    public FileReceiveInfo(String beizhu, String fromJid, String fromName, String fromTime, String file_path){
        this.beizhu=beizhu;
        this.fromJid=fromJid;
        this.fromName=fromName;
        this.fromTime=fromTime;
        this.file_path=file_path;
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

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

}
