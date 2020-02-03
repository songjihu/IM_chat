package com.example.im_chat.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

/**
 *   聊天msg存入本地数据库
 * @auther songjihu
 * @since 2020/2/2 13:38
 */

@Entity
public class ChatMessage {
    @Id(autoincrement = true)
    private Long msgId;//消息id

    @NotNull
    private String msg;//消息


    public ChatMessage(String msg) {
        this.msg = msg;
    }


    @Generated(hash = 1574058549)
    public ChatMessage(Long msgId, @NotNull String msg) {
        this.msgId = msgId;
        this.msg = msg;
    }


    @Generated(hash = 2271208)
    public ChatMessage() {
    }


    public Long getMsgId() {
        return this.msgId;
    }


    public void setMsgId(Long msgId) {
        this.msgId = msgId;
    }


    public String getMsg() {
        return this.msg;
    }


    public void setMsg(String msg) {
        this.msg = msg;
    }




}
