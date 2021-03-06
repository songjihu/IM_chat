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
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
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
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
public class AddFriendActivity extends Activity implements ConnectionListener, RosterListener, ChatManagerListener, ChatMessageListener, View.OnClickListener {
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





    private void initXMPPTCPConnection(){
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





        //点击查询按钮，查询可以添加的好友
        btn_searchfriend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //从数据库查询，返回到数据集并加入到列表中
                String search_word=edit_addfriend.getText().toString();
                if(!TextUtils.isEmpty(search_word)){
                    //friendsList=serachFriends(search_word);
                    input=search_word;
                    //handler.post(runnableUi);
                    List<String> inputList = new ArrayList<String>();
                    inputList.add(input);
                    new searchTask().execute(inputList);//刷新一次
                    Log.i("点击事件","执行");
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
            //设置数据到适配器
            //if(friendsList.size()!=0){
                Log.i("gengxin","gengxin");
                mAdapter.setDatas(friendsList);
                recyclerView.setAdapter(mAdapter);
                recyclerView.scrollToPosition(mAdapter.getItemCount()-1);//此句为设置显示
            //}
        }
    };




    //搜索好友并设置
    private List<Friend> serachFriends(final String inputStr){
        final List<Friend> friends = new ArrayList<Friend>();//建立新的list
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean flag=false;
                    Connection cn= JDBCUtils.getConnection();
                    String sql="SELECT * FROM `user` WHERE user_name LIKE '%"+inputStr+"%' or "
                            + "jid = '"+inputStr+"'";//模糊搜索
                    Statement st=(Statement)cn.createStatement();
                    ResultSet rs=st.executeQuery(sql);
                    while(rs.next()){
                        flag=true;
                        Friend friend = new Friend(rs.getString("jid"),rs.getString("user_name"));
                        friends.add(friend);
                    }
                    if(!flag){

                        Looper.prepare();
                        Toast.makeText(AddFriendActivity.this, "未搜索到用户", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                    JDBCUtils.close(rs,st,cn);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return friends;

    }

    private class searchTask extends AsyncTask<List<String>, Object, Short> {
        @Override
        protected Short doInBackground(List<String>... params) {
            try {
                friendsList.clear();
                String inputStr = params[0].get(0);
                boolean flag=false;
                Connection cn= JDBCUtils.getConnection();
                String sql="SELECT * FROM `user` WHERE user_name LIKE '%"+inputStr+"%' or "
                        + "jid = '"+inputStr+"'";//模糊搜索
                Statement st=(Statement)cn.createStatement();
                ResultSet rs=st.executeQuery(sql);
                while(rs.next()){
                    flag=true;
                    Friend friend = new Friend(rs.getString("jid"),rs.getString("user_name"));
                    friendsList.add(friend);
                }
                if(!flag){

                    //Looper.prepare();
                    Toast.makeText(AddFriendActivity.this, "未搜索到用户", Toast.LENGTH_SHORT).show();
                    //Looper.loop();
                }
                JDBCUtils.close(rs,st,cn);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
            //获取完成后发送
            //EventBus.getDefault().postSticky(myInfo);
            return 1;
        }

        @Override
        protected void onPostExecute(Short state) {
            if(state==1){
                Log.i("更新","更新");
                handler.post(runnableUi);
            }
            if(state==0){
                Log.i("更新","faile");
                handler.post(runnableUi);
            }
        }

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


    @Override
    public void onClick(View v) {

    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {

    }

    @Override
    public void processMessage(Chat chat, Message message) {

    }
}