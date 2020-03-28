package com.example.im_chat.ui.fragment.first;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
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
import com.example.im_chat.entity.MyInfo;
import com.example.im_chat.helper.MessageTranslateBack;
import com.example.im_chat.media.data.fixtures.DialogsFixtures;
import com.example.im_chat.media.data.model.Dialog;
import com.example.im_chat.media.data.model.Message;
import com.example.im_chat.media.data.model.User;
import com.example.im_chat.media.holder.CustomHolderDialogsActivity;
import com.example.im_chat.media.holder.CustomHolderMessagesActivity;
import com.example.im_chat.media.holder.holders.dialogs.CustomDialogViewHolder;
import com.example.im_chat.other.JID;
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
    private String uTitles_name = new String();
    private List<Dialog> unreadList=new ArrayList<>();//未读消息列表
    private String sourceUrl="http://192.168.1.109:8080/temp-rainy/user_avatar/";



    private boolean mInAtTop = true;
    private int mScrollTotal;
    private String uTitles = new String();
    private String latestJson;//最新好友消息


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
    public void onEvent(MyInfo data) {
        //接收用户jid
        uTitles=data.getUserId();
        uTitles_name=data.getUserName();
        //friendsList=data.getFriendlist();
        Log.i("shf:---",uTitles);
        //收到一条消息
        if(data.getSendId().equals("add_msg")){
            latestJson=data.getLatestJson();
            MessageTranslateBack helper=new MessageTranslateBack(latestJson);
            //获取一个好友
            User user = new User(helper.getMsgFromId(),helper.getMsgFrom(),sourceUrl+helper.getMsgFromId()+".jpg",true);
            ArrayList<User> users = new ArrayList<>();
            users.add(user);
            //ChatMessage chatMessage = new ChatMessage((String) msg.obj, 1);
            Message message = new Message(helper.getMsgFrom(),user,helper.getMsgContent(),helper.getMsgDate());
            //messageList.add(chatMessage);
            if(helper.getMsgType()!=null&&helper.getMsgType().equals("img")){
                message.setImage(new Message.Image(helper.getMsgContent()));
                message.setText("图片");
            }
            String FriendId=helper.getMsgFromId();
            String FriendName=helper.getMsgFrom();
            int unreadCount=0;
            //如果存在，则移除
            for(int i=0;i<unreadList.size();i++){
                if(FriendId.equals(unreadList.get(i).getId())){
                    unreadCount=unreadList.get(i).getUnreadCount();
                    unreadList.remove(i);
                    dialogsAdapter.deleteById(FriendId);
                    break;
                }
            }
            //加入顶部
            Log.i("加入id+++++",sourceUrl+FriendId+".jpg");
            Dialog t=new Dialog(FriendId,FriendName,sourceUrl+FriendId+".jpg",users,message,unreadCount+1);

            dialogsAdapter.addItem(0,t);
            //imageLoader.loadImage(dialogsAdapter.getItemById(FriendId).getClass().);

            unreadList.add(0,t);
        }

    }

    private static DaoSession daoSession;//配置数据库


    public void initView(View view) {
        dialogsList = (DialogsList) view.findViewById(R.id.dialogsList);//会话列表
        //mAdapter = new ThirdHomeAdapter(_mActivity);//定义item的适配器
        dialogsAdapter= new DialogsListAdapter<>(
                R.layout.item_custom_dialog_view_holder,
                CustomDialogViewHolder.class,
                imageLoader);
        //dialogsAdapter.setItems(DialogsFixtures.getDialogs());
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
        User t=(User) dialog.getUsers().get(0);
        Log.i("点击了jid",t.getId()+"姓名"+t.getName());
        int pos=dialogsAdapter.getDialogPosition(dialog);
        dialog.setUnreadCount(0);
        dialogsAdapter.updateItem(pos,dialog);
        Intent intent =new Intent(getActivity(), CustomHolderMessagesActivity.class);
        //用Bundle携带数据
        Bundle bundle=new Bundle();
        bundle.putString("jid",uTitles);
        bundle.putString("name",uTitles_name);
        bundle.putString("f_jid",t.getId());
        bundle.putString("f_name",t.getName());
        //Log.i("4523543254获取到的name值为",uuu.getUserName());
        intent.putExtras(bundle);
        startActivity(intent);

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
