package com.example.im_chat.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

/**
 *   未读消息数量本地数据库
 * @auther songjihu
 * @since 2020/3/15 19:50
 */

@Entity
public class DialogMessage {
    @Id(autoincrement = true)
    private Long msgId;//聊天好友id

    @NotNull
    private String msg_count;//未读数量

    @Generated(hash = 779196925)
    public DialogMessage(Long msgId, @NotNull String msg_count) {
        this.msgId = msgId;
        this.msg_count = msg_count;
    }

    @Generated(hash = 147023049)
    public DialogMessage() {
    }

    public Long getMsgId() {
        return this.msgId;
    }

    public void setMsgId(Long msgId) {
        this.msgId = msgId;
    }

    public String getMsg_count() {
        return this.msg_count;
    }

    public void setMsg_count(String msg_count) {
        this.msg_count = msg_count;
    }

    


}
