package com.example.im_chat.entity;
import android.os.Parcel;
import android.os.Parcelable;


/**
 *   好友实体
 * @auther songjihu
 * @since 2020/3/16 19:57
 */

public class Friend implements Parcelable{
    private String jid;
    private String name;
    private String online;
    private String lastmessage;

    public String getLastmessage() {
        return lastmessage;
    }

    public void setLastmessage(String lastmessage) {
        this.lastmessage = lastmessage;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public Friend() {
    }

    public Friend(String jid, String name) {
        this.jid = jid;
        this.name = name;
    }

    public Friend(String jid, String name,String lastmessage,String online) {
        this.jid = jid;
        this.name = name;
        this.online = online;
        this.lastmessage=lastmessage;
    }





    public Friend(String jid, String name,String online) {
        this.jid = jid;
        this.name = name;
        this.online = online;
    }

    protected Friend(Parcel in) {
        jid = in.readString();
        name = in.readString();

    }

    public static final Creator<Friend> CREATOR = new Creator<Friend>() {
        @Override
        public Friend createFromParcel(Parcel in) {
            return new Friend(in);
        }

        @Override
        public Friend[] newArray(int size) {
            return new Friend[size];
        }
    };

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(jid);
        dest.writeString(name);
    }
}
