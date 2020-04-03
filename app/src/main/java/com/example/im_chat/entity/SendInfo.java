package com.example.im_chat.entity;

import com.example.im_chat.media.data.model.Dialog;

import java.util.ArrayList;
import java.util.List;

public class SendInfo {

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    private String userId;//用户id
    private String userName;//用户名
    private String friendId;//接收id
    private String friendName;//接收name
    private String msg;//消息内容
    private String type;//消息类型

}
