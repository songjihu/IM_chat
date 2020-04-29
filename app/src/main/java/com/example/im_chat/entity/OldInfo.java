package com.example.im_chat.entity;

import com.example.im_chat.media.data.model.Dialog;

import java.util.ArrayList;
import java.util.List;

public class OldInfo {


    public String getSendId() {
        return sendId;
    }

    public void setSendId(String sendId) {
        OldInfo.sendId = sendId;
    }

    private static String sendId;//EventBusId

    public List<String> getLatestJson() {
        return latestJson;
    }

    public void setLatestJson(List<String> latestJson) {
        this.latestJson = latestJson;
    }

    private List<String> latestJson=new ArrayList<>();


}
