package com.example.im_chat.ui.fragment.first;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.Toast;


import com.example.im_chat.R;

import com.example.im_chat.activity.LoginActivity;
import com.example.im_chat.activity.MainActivity;
import com.example.im_chat.db.DaoSession;
import com.example.im_chat.entity.Friend;
import com.example.im_chat.entity.MyInfo;
import com.example.im_chat.entity.OldInfo;
import com.example.im_chat.entity.ZeroInfo;
import com.example.im_chat.helper.MessageTranslateBack;
import com.example.im_chat.listener.NotificationClickReceiver;
import com.example.im_chat.media.data.fixtures.DialogsFixtures;
import com.example.im_chat.media.data.model.Dialog;
import com.example.im_chat.media.data.model.Message;
import com.example.im_chat.media.data.model.User;
import com.example.im_chat.media.holder.CustomHolderDialogsActivity;
import com.example.im_chat.media.holder.CustomHolderMessagesActivity;
import com.example.im_chat.media.holder.holders.dialogs.CustomDialogViewHolder;
import com.example.im_chat.utils.MyXMPPTCPConnection;
import com.example.im_chat.utils.MyXMPPTCPConnectionOnLine;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import me.yokeyword.eventbusactivityscope.EventBusActivityScope;
import me.yokeyword.fragmentation.SupportFragment;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.zego.zegoavkit2.receiver.BackgroundMonitor.TAG;


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
    private List<String> oldJson =new ArrayList<>();

    private NotificationManager manager ;



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
        manager = (NotificationManager)getActivity().getSystemService(NOTIFICATION_SERVICE);
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
                sendChatMsg(getView(),"收到来自"+helper.getMsgFrom()+"的消息","图片",helper.getMsgFromId(),helper.getMsgFrom());
            }
            if(helper.getMsgType()!=null&&helper.getMsgType().equals("location")){
                message.setImage(new Message.Image(helper.getMsgContent().split("!")[2]));
                Log.i("注意","加入位置缩略图");
                message.setText("位置信息");
                sendChatMsg(getView(),"收到来自"+helper.getMsgFrom()+"的消息","位置消息",helper.getMsgFromId(),helper.getMsgFrom());
            }
            if(helper.getMsgType()!=null&&helper.getMsgType().equals("voice")){
                String t_url=helper.getMsgContent().split("!")[1];
                int t_duration=Integer.parseInt(helper.getMsgContent().split("!")[0]);
                message.setVoice(new Message.Voice(t_url,t_duration));
                message.setText("语音");
                sendChatMsg(getView(),"收到来自"+helper.getMsgFrom()+"的消息","语音",helper.getMsgFromId(),helper.getMsgFrom());
            }
            if(helper.getMsgType()==null||helper.getMsgType().equals("text")){
                sendChatMsg(getView(),"收到来自"+helper.getMsgFrom()+"的消息",helper.getMsgContent(),helper.getMsgFromId(),helper.getMsgFrom());
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

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(OldInfo data) {
        //收到一条消息
        if(data.getSendId().equals("add_msgs")){
            oldJson=data.getLatestJson();
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(ZeroInfo data) {
        //Log.i("！！","执行清零"+data.getSendId()+"size为"+unreadList.size());
        for(int i=0;i<unreadList.size();i++){
            //Log.i("!!","检索id"+unreadList.get(i).getId());
            if(data.getSendId().equals(unreadList.get(i).getId())){
                //unreadList.get(i).setUnreadCount(0);
                //Log.i("!!","清清"+unreadList.get(i).getUnreadCount());
                Dialog dialog=unreadList.get(i);
                int pos=dialogsAdapter.getDialogPosition(dialog);
                dialog.setUnreadCount(0);
                dialogsAdapter.updateItem(pos,dialog);
                break;
            }
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
        //初始化通知栏通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "chat";
            String channelName = "聊天消息";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);

            channelId = "subscribe";
            channelName = "订阅消息";
            importance = NotificationManager.IMPORTANCE_DEFAULT;
            createNotificationChannel(channelId, channelName, importance);
        }
        loadOldMessages();
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

    private void loadOldMessages(){
        String t1;
        Log.i("!!未读数量",oldJson.size()+"");
        for(int j = 0; j< oldJson.size(); j++){
            t1= oldJson.get(j);
            MessageTranslateBack helper=new MessageTranslateBack(t1);
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
                sendChatMsg(getView(),"收到来自"+helper.getMsgFrom()+"的消息","图片",helper.getMsgFromId(),helper.getMsgFrom());
            }
            if(helper.getMsgType()!=null&&helper.getMsgType().equals("location")){
                message.setImage(new Message.Image(helper.getMsgContent().split("!")[2]));
                Log.i("注意","加入位置缩略图");
                message.setText("位置信息");
                sendChatMsg(getView(),"收到来自"+helper.getMsgFrom()+"的消息","位置消息",helper.getMsgFromId(),helper.getMsgFrom());
            }
            if(helper.getMsgType()!=null&&helper.getMsgType().equals("voice")){
                String t_url=helper.getMsgContent().split("!")[1];
                int t_duration=Integer.parseInt(helper.getMsgContent().split("!")[0]);
                message.setVoice(new Message.Voice(t_url,t_duration));
                message.setText("语音");
                sendChatMsg(getView(),"收到来自"+helper.getMsgFrom()+"的消息","语音",helper.getMsgFromId(),helper.getMsgFrom());
            }
            if(helper.getMsgType()==null||helper.getMsgType().equals("text")){
                sendChatMsg(getView(),"收到来自"+helper.getMsgFrom()+"的消息",helper.getMsgContent(),helper.getMsgFromId(),helper.getMsgFrom());
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


    public void sendChatMsg(View view,String title,String text,String id,String name) {
        //配置点击事件监听
        Intent intent =new Intent (getActivity(), NotificationClickReceiver.class);
        //用Bundle携带数据

        Bundle bundle=new Bundle();
        bundle.putString("jid",uTitles);
        bundle.putString("name",uTitles_name);
        bundle.putString("f_jid",id);
        bundle.putString("f_name",name);
        //Log.i("4523543254获取到的name值为",uuu.getUserName());
        intent.putExtras(bundle);
        PendingIntent pendingIntent =PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //创建监听
        Notification notification = new NotificationCompat.Builder(getActivity(), "chat")
                .setContentTitle(title)
                .setContentText(text)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_chat)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_chat))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        manager.notify(1, notification);

    }

    public void sendSubscribeMsg(View view,String title,String text) {
        Notification notification = new NotificationCompat.Builder(getActivity(), "subscribe")
                .setContentTitle(title)
                .setContentText(text)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_chat)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_chat))
                .setAutoCancel(true)
                .build();
        manager.notify(2, notification);

    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setShowBadge(true);//允许角标通知
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(
                NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }






    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBusActivityScope.getDefault(_mActivity).unregister(this);
        EventBus.getDefault().unregister(this);
        //getActivity().unregisterReceiver(nlBroadcastReceiver );

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        //getActivity().unregisterReceiver(nlBroadcastReceiver );

    }





}
