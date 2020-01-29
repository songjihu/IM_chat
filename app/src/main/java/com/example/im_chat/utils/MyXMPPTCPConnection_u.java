package com.example.im_chat.utils;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;


public class MyXMPPTCPConnection_u extends XMPPTCPConnection {
    private static MyXMPPTCPConnection_u connection;
    private MyXMPPTCPConnection_u(XMPPTCPConnectionConfiguration config) {
        super(config);
    }
    public static synchronized MyXMPPTCPConnection_u getInstance(String email,String password){
        //初始化XMPPTCPConnection相关配置
        if(connection == null){
            XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder();
            //设置连接超时的最大时间
            builder.setConnectTimeout(10000);
            //设置登录openfire的用户名和密码
            builder.setUsernameAndPassword(email, password);
            //设置安全模式
            builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
            builder.setResource("Android");
            //设置服务器名称
            builder.setServiceName("192.168.4.1");
            //设置主机地址
            builder.setHost("192.168.4.1");
            //设置端口号
            builder.setPort(5222);
            //是否查看debug日志
            builder.setDebuggerEnabled(true);
            //设置为离线
            builder.setSendPresence(false);
            connection = new MyXMPPTCPConnection_u(builder.build());
            connection.setPacketReplyTimeout(10000);
        }
        return connection;
    }
}
