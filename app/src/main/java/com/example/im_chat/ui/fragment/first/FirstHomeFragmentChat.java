package com.example.im_chat.ui.fragment.first;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.example.im_chat.R;

import com.example.im_chat.db.DaoSession;
import com.example.im_chat.entity.ChatMessage;
import com.example.im_chat.entity.Friend;
import com.example.im_chat.helper.MessageTranslateBack;
import com.example.im_chat.media.data.fixtures.DialogsFixtures;
import com.example.im_chat.media.data.model.Dialog;
import com.example.im_chat.media.data.model.Message;
import com.example.im_chat.media.data.model.User;
import com.example.im_chat.media.holder.CustomHolderDialogsActivity;
import com.example.im_chat.media.holder.CustomHolderMessagesActivity;
import com.example.im_chat.media.holder.holders.dialogs.CustomDialogViewHolder;
import com.example.im_chat.utils.JDBCUtils;
import com.example.im_chat.utils.MyXMPPTCPConnection;
import com.example.im_chat.utils.MyXMPPTCPConnectionOnLine;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smackx.offline.OfflineMessageManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import me.yokeyword.eventbusactivityscope.EventBusActivityScope;
import me.yokeyword.fragmentation.SupportFragment;


public  class FirstHomeFragmentChat extends SupportFragment implements DialogsListAdapter.OnDialogClickListener<Dialog>{
    private RecyclerView mRecy;
    private SwipeRefreshLayout mRefreshLayout;
    //private FirstHomeAdapter mAdapter;//此项为展示待上课程item的适配器
    private List<Friend> friendsList;
    private DialogsList dialogsList;//会话列表
    private DialogsListAdapter<Dialog> dialogsAdapter;//会话适配器
    private ImageLoader imageLoader;//图片加载
    private Handler handler;
    private MyXMPPTCPConnection connection;//离线的连接
    private MyXMPPTCPConnectionOnLine connection_online;//设置在线的连接

    final CountDownLatch countDownLatch = new CountDownLatch(1);//进程结束标志
    final String team_id[] = new String[20];//加入的讨论组id
    final int team_location [] = new int[20];//加入的讨论组列的位置
    final String team_name[] = new String[20];//加入的讨论组name
    final String team_member[][] = new String[20][20];//加入的讨论组人员组成
    private String user_name;//用户名



    private boolean mInAtTop = true;
    private int mScrollTotal;
    private String uTitles = new String();

    //显示好友列表

    private int friend_number=0;

    public static void open(Context context) {
        context.startActivity(new Intent(context, CustomHolderDialogsActivity.class));
    }



    public static FirstHomeFragmentChat newInstance() {

        Bundle args = new Bundle();

        FirstHomeFragmentChat fragment = new FirstHomeFragmentChat();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_custom_holder_dialogs, container, false);
        EventBusActivityScope.getDefault(_mActivity).register(this);
        EventBus.getDefault().register(this);
        initView(view);
        return view;
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(String data) {
        //接收用户姓名
        uTitles=data;
        //Log.i("（）（）（）（）（）（）",data);
    }

    static ArrayList<String> avatars = new ArrayList<String>() {
        {
            add("http://d.lanrentuku.com/down/png/1904/international_food/fried_rice.png");
        }
    };
    private static DaoSession daoSession;//配置数据库


    public void initView(View view) {
        dialogsList = (DialogsList) view.findViewById(R.id.dialogsList);//会话列表
        //mAdapter = new ThirdHomeAdapter(_mActivity);//定义item的适配器
        dialogsAdapter= new DialogsListAdapter<>(
                R.layout.item_custom_dialog_view_holder,
                CustomDialogViewHolder.class,
                imageLoader);
        dialogsAdapter.setItems(DialogsFixtures.getDialogs());
        dialogsAdapter.setOnDialogClickListener(this);
        dialogsList.setAdapter(dialogsAdapter);
        Log.i("开启第一个Fragment","ohohohohohohohoh");
    }


    // 构建Runnable对象，在runnable中更新界面
    Runnable  runnableUi=new  Runnable(){
        @Override
        public void run() {
            //更新界面
            dialogsAdapter.setItems(DialogsFixtures.getDialogsChat(team_name,team_member,team_location[19]));
            //设置数据到适配器
            //dialogsList.setAdapter(dialogsAdapter);
            //mRecy.scrollToPosition(mAdapter.getItemCount()-1);//此句为设置显示
        }

    };



    @Override
    public void onDialogClick(Dialog dialog) {
        String temp = dialog.getId();
        int t = Integer.parseInt(temp);
        team_member[t][19]=uTitles;//存入自己是谁
        team_member[t][18]=team_id[t];//存入群组id
        team_member[t][17]=user_name;//存入自己叫啥
        EventBus.getDefault().postSticky(team_member[t]);//发送小组成员
        CustomHolderMessagesActivity.open(getActivity());

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBusActivityScope.getDefault(_mActivity).unregister(this);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }





}
