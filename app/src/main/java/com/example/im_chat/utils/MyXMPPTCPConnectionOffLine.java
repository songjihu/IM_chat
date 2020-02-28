package com.example.im_chat.utils;
import android.util.Log;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;


public class MyXMPPTCPConnectionOffLine extends XMPPTCPConnection {
    private static MyXMPPTCPConnectionOffLine connection;

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        MyXMPPTCPConnectionOffLine.username = username;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        MyXMPPTCPConnectionOffLine.password = password;
    }

    private static String username;
    private static String password;



    private MyXMPPTCPConnectionOffLine(XMPPTCPConnectionConfiguration config) {
        super(config);
    }
    public static synchronized MyXMPPTCPConnectionOffLine getInstance(){
        //初始化XMPPTCPConnection相关配置
        if(connection == null){
            XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder();
            //设置连接超时的最大时间
            builder.setConnectTimeout(10000);
            //设置登录openfire的用户名和密码
            //builder.setUsernameAndPassword(username, password);
            builder.setUsernameAndPassword("admin", "8859844007");
            //设置安全模式
            builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
            builder.setResource("Android");
            //设置服务器名称
            builder.setServiceName("123.56.163.211");
            //设置主机地址
            builder.setHost("123.56.163.211");
            //设置端口号
            builder.setPort(5222);
            //是否查看debug日志
            builder.setDebuggerEnabled(true);
            //设置为离线
            builder.setSendPresence(false);
            connection = new MyXMPPTCPConnectionOffLine(builder.build());
            connection.setPacketReplyTimeout(10000);
        }
        return connection;
    }
}
