package com.example.im_chat.activity;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.im_chat.R;
import com.example.im_chat.adapter.AddAdapter;
import com.example.im_chat.adapter.SecondHomeAdapter;
import com.example.im_chat.entity.Friend;
import com.example.im_chat.entity.MyInfo;
import com.example.im_chat.entity.UserInfo;
import com.example.im_chat.listener.OnItemClickListener;
import com.example.im_chat.media.holder.CustomHolderDialogsActivity;
import com.example.im_chat.other.JID;
import com.example.im_chat.utils.JDBCUtils;
import com.example.im_chat.utils.JDBCUtils1;
import com.example.im_chat.utils.MyXMPPTCPConnectionOnLine;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import me.yokeyword.eventbusactivityscope.EventBusActivityScope;

/*
 *@author  Eric
 *@2015-9-7上午9:28:52
 */
public class AddFriendActivity extends Activity implements ConnectionListener, RosterListener {
    private EditText edit_addfriend;
    private Button btn_searchfriend;
    private String name,password,response,acceptAdd,alertName,alertSubName;
    private ImageView img_searchFriend,img_addFriend;
    private TextView text_searchFriend,text_response;
    private Roster roster;
    private MyXMPPTCPConnectionOnLine connection;
    private static ProgressDialog dialog;
    private RecyclerView recyclerView;//用一个recyclerView存放检索到的用户信息
    private String uTitles=new String();
    private AddAdapter mAdapter;//此项为联系人item的适配器
    private List<Friend> friendsList=new ArrayList<Friend>();;//被检索到的联系人列表
    private List<String> sList = new ArrayList<String>();//搜索参数
    private Handler handler;//更新界面
    private String input;//输入字符



    private void initXMPPTCPConnection(String name,String password){
        connection = MyXMPPTCPConnectionOnLine.getInstance();
        connection.addConnectionListener(this);
        roster = Roster.getInstanceFor(connection);
        roster.addRosterListener(this);

    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(MyInfo data) {
        //接收用户jid
        uTitles=data.getUserId();
        //Log.i("（）（）（）（）（）（）",uTitles);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addfriend);
        EventBusActivityScope.getDefault(AddFriendActivity.this).register(this);
        EventBus.getDefault().register(this);
        edit_addfriend = (EditText) findViewById(R.id.edit_addfriend);
        btn_searchfriend = (Button) findViewById(R.id.btn_searchfriend);
        recyclerView=(RecyclerView)findViewById(R.id.add_recycleView) ;//循环列表
        recyclerView.setLayoutManager(new LinearLayoutManager(AddFriendActivity.this));//设置manager
        recyclerView.setAdapter(new AddAdapter(AddFriendActivity.this,uTitles));//设置adapter
        handler=new Handler();//创建属于主线程的handler
        mAdapter = new AddAdapter(AddFriendActivity.this,uTitles);//定义item的适配器


        //name = getIntent().getStringExtra("name");
        //password = getIntent().getStringExtra("password");

        // 获取好友名单
        //friendsList = serachFriends();
        //设置数据到适配器
        //mAdapter.setDatas(friendsList);


        initXMPPTCPConnection(name,password);
        roster.setSubscriptionMode(Roster.SubscriptionMode.manual);
        //监听好友上下线模块
        if(connection!=null&&connection.isConnected()&&connection.isAuthenticated()){
            //条件过滤器
            AndFilter filter = new AndFilter(new StanzaTypeFilter(Presence.class));
            //packet监听器
            StanzaListener packetListener = new StanzaListener() {
                @Override
                public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                    if (packet instanceof Presence) {
                        Presence presence = (Presence) packet;
                        String fromId = presence.getFrom();
                        String from = presence.getFrom().split("@")[1];//去掉了后缀
                        if (presence.getType().equals(Presence.Type.subscribe)) {
                            System.out.println("收到请求！请求添加好友" + from);
                        } else if (presence.getType().equals(Presence.Type.subscribed)) {//对方同意订阅
                            System.out.println("收到请求！同意订阅" + from);
                        } else if (presence.getType().equals(Presence.Type.unsubscribe)) {//取消订阅
                            System.out.println("收到请求！取消订阅" + from);
                        } else if (presence.getType().equals(Presence.Type.unsubscribed)) {//拒绝订阅
                            System.out.println("收到请求！拒绝订阅" + from);
                        } else if (presence.getType().equals(Presence.Type.unavailable)) {//离线
                            System.out.println("收到请求！离线" + from);
                        } else if (presence.getType().equals(Presence.Type.available)) {//上线
                            System.out.println("收到请求！上线" + from);
                        }
                    }
                }
            };
            //添加监听
            connection.addAsyncStanzaListener(packetListener, filter);
        }


        //点击查询按钮，查询可以添加的好友
        btn_searchfriend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //从数据库查询，返回到数据集并加入到列表中
                String search_word=edit_addfriend.getText().toString();
                if(!TextUtils.isEmpty(search_word)){
                    //friendsList=serachFriends(search_word);
                    input=search_word;
                    handler.post(runnableUi);
                }
                else {
                    Toast.makeText(AddFriendActivity.this, "搜索不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    // 构建Runnable对象，在runnable中更新界面
    Runnable  runnableUi=new  Runnable(){
        @Override
        public void run() {
            //更新界面
            //获取好友名单
            List<Friend> friendsList = serachFriends(input);
            //设置数据到适配器
            mAdapter.setDatas(friendsList);
            recyclerView.setAdapter(mAdapter);
            recyclerView.scrollToPosition(mAdapter.getItemCount()-1);//此句为设置显示
        }
    };




    //搜索好友并设置
    private List<Friend> serachFriends(final String inputStr){
        final List<Friend> friends = new ArrayList<Friend>();//建立新的list
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean flag=false;
                    Connection cn= JDBCUtils.getConnection();
                    String sql="SELECT * FROM `user` WHERE user_name LIKE '%"+inputStr+"%'";//模糊搜索
                    Statement st=(Statement)cn.createStatement();
                    ResultSet rs=st.executeQuery(sql);
                    while(rs.next()){
                        flag=true;
                        Friend friend = new Friend(rs.getString("jid"),rs.getString("user_name"));
                        friends.add(friend);
                    }
                    if(!flag){
                        Toast.makeText(AddFriendActivity.this, "未搜索到用户", Toast.LENGTH_SHORT).show();

                    }
                    JDBCUtils.close(rs,st,cn);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
            }
        }).start();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return friends;

    }


    @Override
    public void connected(XMPPConnection connection) {

    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {

    }

    @Override
    public void connectionClosed() {

    }

    @Override
    public void connectionClosedOnError(Exception e) {

    }

    @Override
    public void reconnectionSuccessful() {

    }

    @Override
    public void reconnectingIn(int seconds) {

    }

    @Override
    public void reconnectionFailed(Exception e) {

    }

    @Override
    public void entriesAdded(Collection<String> addresses) {

    }

    @Override
    public void entriesUpdated(Collection<String> addresses) {

    }

    @Override
    public void entriesDeleted(Collection<String> addresses) {

    }

    @Override
    public void presenceChanged(Presence presence) {

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


}