package com.example.im_chat.media.data.model;

import com.stfalcon.chatkit.commons.models.IDialog;

import java.util.ArrayList;

/*
 * Created by troy379 on 04.04.17.
 */
public class Dialog implements IDialog<Message> {

    private String id;
    private String dialogPhoto;
    private String dialogName;
    private ArrayList<User> users;
    private Message lastMessage;

    public String getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(String isOnline) {
        this.isOnline = isOnline;
    }

    private String isOnline;

    private int unreadCount;

    public Dialog(String id, String name, String photo,
                  ArrayList<User> users, Message lastMessage, int unreadCount) {

        this.id = id;//群组id
        this.dialogName = name;//群名
        this.dialogPhoto = photo;//群图片
        this.users = users;//群用户list
        this.lastMessage = lastMessage;//群最后一条消息
        this.unreadCount = unreadCount;//未读消息的数量
    }

    public Dialog(String id, String name,
                  ArrayList<User> users, Message lastMessage, int unreadCount) {

        this.id = id;//群组id
        this.dialogName = name;//群名
        this.dialogPhoto = null;//群图片
        this.users = users;//群用户list
        this.lastMessage = lastMessage;//群最后一条消息
        this.unreadCount = unreadCount;//未读消息的数量
    }

    //user列表使用的dialog
    public Dialog(String id, String name,String isOnline) {

        this.id = id;//用户id
        this.dialogName = name;//用户名
        this.isOnline=isOnline;//用户是否在线

    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDialogPhoto() {
        return dialogPhoto;
    }

    @Override
    public String getDialogName() {
        return dialogName;
    }

    @Override
    public ArrayList<User> getUsers() {
        return users;
    }

    @Override
    public Message getLastMessage() {
        return lastMessage;
    }

    @Override
    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    @Override
    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
