package com.example.im_chat.entity;

import com.example.im_chat.media.data.model.Dialog;

import java.util.ArrayList;
import java.util.List;

public class MyInfo {
    private static String userId;//用户id
    private static String userPwd;//用户密码
    private static String rpwd;
    private static String userName;//用户名
    private static String isOnline;//是否在线
    private static String userType;//用户类别
    private static List<Friend> friendlist =new ArrayList<>();//被加入项
    private List<Dialog> unreadList;//未读消息列表

    public List<Dialog> getUnreadList() {
        return unreadList;
    }

    public void setUnreadList(List<Dialog> unreadList) {
        this.unreadList = unreadList;
    }


    public String getSendId() {
        return sendId;
    }

    public void setSendId(String sendId) {
        MyInfo.sendId = sendId;
    }

    private static String sendId;//EventBusId

    public String getLatestJson() {
        return latestJson;
    }

    public void setLatestJson(String latestJson) {
        this.latestJson = latestJson;
    }

    private String latestJson;

    public static List<Friend> getFriendlist() {
        return friendlist;
    }

    public static void setFriendlist(List<Friend> friendlist) {
        MyInfo.friendlist = friendlist;
    }


    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public String getRpwd() {
        return rpwd;
    }

    public void setRpwd(String rpwd) {
        this.rpwd = rpwd;
    }

    public String getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(String isOnline) {
        this.isOnline = isOnline;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
