package com.example.im_chat.media.holder;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.example.im_chat.R;
import com.example.im_chat.db.DaoMaster;
import com.example.im_chat.db.DaoSession;
import com.example.im_chat.entity.ChatMessage;
import com.example.im_chat.entity.MyInfo;
import com.example.im_chat.entity.SendInfo;
import com.example.im_chat.helper.MessageTranslateBack;
import com.example.im_chat.helper.MessageTranslateTo;
import com.example.im_chat.media.DemoMessagesActivity;
import com.example.im_chat.media.data.model.Message;
import com.example.im_chat.media.data.model.User;
import com.example.im_chat.media.holder.holders.messages.CustomIncomingImageMessageViewHolder;
import com.example.im_chat.media.holder.holders.messages.CustomIncomingTextMessageViewHolder;
import com.example.im_chat.media.holder.holders.messages.CustomOutcomingImageMessageViewHolder;
import com.example.im_chat.media.holder.holders.messages.CustomOutcomingTextMessageViewHolder;
import com.example.im_chat.media.holder.holders.messages.IncomingVoiceMessageViewHolder;
import com.example.im_chat.media.holder.holders.messages.OutcomingVoiceMessageViewHolder;
import com.example.im_chat.other.JID;
import com.example.im_chat.ui.fragment.other.WebFragment;
import com.example.im_chat.utils.AppUtils;
import com.example.im_chat.utils.JDBCUtils;
import com.example.im_chat.utils.MyXMPPTCPConnectionOnLine;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.yokeyword.eventbusactivityscope.EventBusActivityScope;


/**
 *   聊天activity
 * @auther songjihu
 * @since 2020/2/25 14:46
 */
public class CustomHolderMessagesActivity extends DemoMessagesActivity
        implements MessagesListAdapter.OnMessageLongClickListener<Message>,
        MessagesListAdapter.OnMessageViewClickListener<Message>,
        MessageInput.InputListener,
        MessageInput.AttachmentsListener, ChatManagerListener,
        ChatMessageListener, MessageHolders.ContentChecker<Message>{
    private static final byte CONTENT_TYPE_VOICE = 1;

    int f_number;
    private MyXMPPTCPConnectionOnLine connection;//连接
    private ChatManager chatManager;//会话管理
    private Chat chat;//会话
    private Chat chat1;//对自己的会话
    private static DaoSession daoSession;

    private String user_name;
    private static String user_id;
    private String friend_name;
    private String friend_id;
    private String uTitles;
    private String uTitles_name;
    private String latestJson;
    private Boolean bottom_flag=false;
    private String sourceUrl="http://192.168.1.109:8080/temp-rainy/user_avatar/";
    private URL myFileUrl = null;
    private Bitmap bitmap = null;
    private String url;
    private Message messageToUpdate;
    private Message.Image t;



    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(MyInfo data) {
        //接收用户jid
        Log.i("我也","接收到了");
        uTitles=data.getUserId();
        uTitles_name=data.getUserName();
        latestJson=data.getLatestJson();
        //Log.i("接收到----",latestJson);
        if(data.getSendId().equals("add_msg")){
            MessageTranslateBack helper=new MessageTranslateBack(latestJson);
            User user = new User(helper.getMsgFromId(),helper.getMsgFrom(),sourceUrl+helper.getMsgFromId()+".jpg",true);
            //ChatMessage chatMessage = new ChatMessage((String) msg.obj, 1);
            Message message = new Message(helper.getMsgFrom(),user,helper.getMsgContent(),helper.getMsgDate());
            //messageList.add(chatMessage);
            if((helper.getMsgFromId()).equals(friend_id)&&(helper.getMsgTo()).equals(user_id))
            {
                if(helper.getMsgType()!=null&&helper.getMsgType().equals("img")){
                    message.setImage(new Message.Image(helper.getMsgContent()));
                    //message.setVoice(new Message.Voice("http://192.168.1.109:8080/temp-rainy/test.mp3",210));
                    Log.i("注意","加入图片");
                }
                if(helper.getMsgType()!=null&&helper.getMsgType().equals("voice")){
                    String t_url=helper.getMsgContent().split("!")[1];
                    int t_duration=Integer.parseInt(helper.getMsgContent().split("!")[0]);
                    message.setVoice(new Message.Voice(t_url,t_duration));
                    Log.i("注意","加入语音");
                }
                messagesAdapter.addToStart(message,true);//加入下方列表
                //System.identityHashCode(messagesList);
                //messagesAdapter.notifyDataSetChanged();
                Log.i("1发送11111111111111111",message.getText());
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(SendInfo data) {
        //接收消息
        if(data.getMsg()!=null&&data.getMsg().equals("update_avatar")){
            //不是发送消息的不操作
        }
        else {
            Log.i("我也","接收到了！！！");
            String fromId= data.getUserId();
            String fromName=data.getUserName();
            String toId= data.getUserId();
            String toName=data.getUserName();
            String msg=data.getMsg();
            if(data.getType().equals("img")){
                sendChatMessage(msg,"img");
            }
            if(data.getType().equals("voice")){
                sendChatMessage(msg,"voice");
            }
            if(data.getType().equals("file")){
                sendChatMessage(msg,"file");
            }
        }

    }

    static ArrayList<String> avatars = new ArrayList<String>() {
        {
            add("http://d.lanrentuku.com/down/png/1904/international_food/fried_rice.png");
        }
    };
    //接受处理消息
    private static Handler handler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what){
                case 0:

                    break;
                default:
                    break;
            }
        }
    };



    private void initGreenDao() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "aserbao.db");
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    public static void open(Context context) {
        context.startActivity(new Intent(context, CustomHolderMessagesActivity.class));
    }

    //定义消息列表
    public static MessagesList messagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBusActivityScope.getDefault(this).register(this);
        EventBus.getDefault().register(this);
        Bundle bundle = this.getIntent().getExtras();
        //从登陆activity的bundle中获取用户名
        user_name = bundle.getString("name");
        user_id = JID.unescapeNode(bundle.getString("jid"));
        friend_id= JID.unescapeNode(bundle.getString("f_jid"));
        friend_name=bundle.getString("f_name");
        Log.i("4个参数","1:"+user_id+"--2:"+user_name+"--3:"+friend_id+"--4:"+friend_name);
        initGreenDao();
        setContentView(R.layout.activity_custom_holder_messages);
        //消息列表布局
        messagesList = (MessagesList) findViewById(R.id.messagesList);
        //System.identityHashCode(messagesList);
        //初始化适配器
        initAdapter();
        initChatManager();
        initChat();


        MessageInput input = (MessageInput) findViewById(R.id.input);
        input.setInputListener(this);
        input.setAttachmentsListener(this);

        //加载下方网页
        FragmentManager fragmentManager =getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        //步骤二：用add()方法加上Fragment的对象rightFragment
        //Bundle bundle = new Bundle();
        bundle.putString("fromId", user_id);
        bundle.putString("fromName", user_name);
        bundle.putString("toId", friend_id);
        bundle.putString("toName", friend_name);
        WebFragment rightFragment = new WebFragment();
        rightFragment.setArguments(bundle);
        transaction.add(R.id.messages_bottom,rightFragment);
        //步骤三：调用commit()方法使得FragmentTransaction实例的改变生效
        transaction.commit();



    }

    @Override
    public boolean hasContentFor(Message message, byte type) {
        switch (type) {
            case CONTENT_TYPE_VOICE:
                return message.getVoice() != null
                        && message.getVoice().getUrl() != null
                        && !message.getVoice().getUrl().isEmpty();
        }
        return false;
    }




    //点击发送的时间，显示输入的文字
    @Override
    public boolean onSubmit(CharSequence input) {
        //messagesAdapter.addToStart(MessagesFixtures.getTextMessage(), true);
        sendChatMessage(input.toString(),"text");
        return true;
    }


    //点击加号的事件，刷新出一个图片
    @Override
    public void onAddAttachments() {
        //messagesAdapter.addToStart(MessagesFixtures.getImageMessage(), true);
        //步骤一：添加一个FragmentTransaction的实例
        LinearLayout linearLayout=(LinearLayout)findViewById(R.id.messages_bottom);
        if(bottom_flag==false){
            if(false){
                messagesList.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int popupWidth = messagesList.getMeasuredWidth();    //  获取测量后的宽度
                int popupHeight = messagesList.getMeasuredHeight();  //获取测量后的高度
                ViewGroup.LayoutParams lp;
                lp=messagesList.getLayoutParams();
                lp.height=popupHeight-200;
                messagesList.setLayoutParams(lp);
            }
            linearLayout.setVisibility(View.VISIBLE);
            bottom_flag=true;
        }else {
            if(false){
                messagesList.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                int popupWidth = messagesList.getMeasuredWidth();    //  获取测量后的宽度
                int popupHeight = messagesList.getMeasuredHeight();  //获取测量后的高度
                ViewGroup.LayoutParams lp;
                lp=messagesList.getLayoutParams();
                lp.height=popupHeight+200;
                messagesList.setLayoutParams(lp);
            }
            linearLayout.setVisibility(View.GONE);
            bottom_flag=false;
        }
        //start(FriendInvitationFragment.newInstance());
    }

    //消息长按事件
    @Override
    public void onMessageLongClick(Message message) {
        AppUtils.showToast(this, R.string.on_log_click_message, false);
    }

    @Override
    public void onMessageViewClick(View view, Message message) {
        //点击界面，如果是音乐则播放他
        if(message.getVoice()!=null){
            //AppUtils.showToast(this, message.getText(), false);
            Log.i("!!", message.getVoice().getUrl()+"!!!"+message.getVoice().getDuration());
            //Uri myUri = Uri.parse("http://192.168.1.109:8080/temp-rainy/test.mp3"); // initialize Uri here
            Uri myUri = Uri.parse(message.getVoice().getUrl()); // initialize Uri here
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            if(mediaPlayer.isPlaying()){
                mediaPlayer.reset();
            }
            try {
                mediaPlayer.setDataSource(getApplicationContext(), myUri);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.start();
            //File remoteFile = new File(message.getText());
        }


    }
    private void initChatManager(){
        connection = MyXMPPTCPConnectionOnLine.getInstance();
        if(connection != null ){
            chatManager = ChatManager.getInstanceFor(connection);
            chatManager.addChatListener(this);
        }
    }

    private void initChat(){
        if(chatManager != null){
            //第一个参数是 用户的ID
            //第二个参数是 ChatMessageListener，我们这里传null就好了
            //群组共计x个成员，建立会话
            chat=chatManager.createChat(JID.escapeNode(friend_id)+"@123.56.163.211", null);
        }
    }

    private void initAdapter() {
        //We can pass any data to ViewHolder with payload
        //我们可以传递任何数据到ViewHolder用payload
        //定义一个 来消息 文本 的payload
        CustomIncomingTextMessageViewHolder.Payload payload = new CustomIncomingTextMessageViewHolder.Payload();
        //For example click listener
        //点击事件监听器
        payload.avatarClickListener = new CustomIncomingTextMessageViewHolder.OnAvatarClickListener() {
            @Override
            public void onAvatarClick() {
                Toast.makeText(CustomHolderMessagesActivity.this,
                        "Text message avatar clicked", Toast.LENGTH_SHORT).show();
            }
        };

        //消息holder的配置和创建
        MessageHolders holdersConfig = new MessageHolders()
                .setIncomingTextConfig(
                        CustomIncomingTextMessageViewHolder.class,
                        R.layout.item_custom_incoming_text_message,
                        payload)
                .setOutcomingTextConfig(
                        CustomOutcomingTextMessageViewHolder.class,
                        R.layout.item_custom_outcoming_text_message)
                .setIncomingImageConfig(
                        CustomIncomingImageMessageViewHolder.class,
                        R.layout.item_custom_incoming_image_message)
                .setOutcomingImageConfig(
                        CustomOutcomingImageMessageViewHolder.class,
                        R.layout.item_custom_outcoming_image_message)
                .registerContentType(
                        CONTENT_TYPE_VOICE,
                        IncomingVoiceMessageViewHolder.class,
                        R.layout.item_custom_incoming_voice_message,
                        OutcomingVoiceMessageViewHolder.class,
                        R.layout.item_custom_outcoming_voice_message,
                        this);

        //配置适配器内容，第一个参数为发送者的id，id不同则在右侧
        super.accept_id =user_id;//接收者为此用户
        super.send_id =friend_id;//发送者为好友
        super.messagesAdapter = new MessagesListAdapter<>(user_id, holdersConfig, super.imageLoader);
        //配置点击事件
        super.messagesAdapter.setOnMessageLongClickListener(this);
        //super.messagesAdapter.setLoadMoreListener(this);
        super.messagesAdapter.setOnMessageViewClickListener(this);
        //把配置好的适配器给List
        messagesList.setAdapter(super.messagesAdapter);
    }

    //发消息
    private void sendChatMessage(String msgContent,String type){
        MessageTranslateTo helper=new MessageTranslateTo(user_name,user_id,friend_id,msgContent,type);
        User user = new User(helper.getMsgFromId(),helper.getMsgFrom(),sourceUrl+helper.getMsgFromId()+".jpg",true);
        MessageTranslateBack helper1=new MessageTranslateBack(helper.getMsgJson());
        Log.i("2发送222222222222222",helper.getMsgJson());
        Message message = new Message(helper.getMsgFrom()+helper1.getMsgDate(),//id
                user,//用户
                helper.getMsgContent(),//内容
                helper1.getMsgDate());//时间
        //更新列表并加入本地数据库
        if(type.equals("img")) {
            message.setImage(new Message.Image(msgContent));
            ChatMessage chat_msg =new ChatMessage(null,(String) helper.getMsgJson());
            daoSession.insert(chat_msg);
            Log.i("数据库加入++++++",(String) helper.getMsgJson());
            messagesAdapter.addToStart(message,true);//加入下方列表
        }
        if(type.equals("voice")) {
            String t_url=helper.getMsgContent().split("!")[1];
            int t_duration=Integer.parseInt(helper.getMsgContent().split("!")[0]);
            message.setVoice(new Message.Voice(t_url,t_duration));
            ChatMessage chat_msg =new ChatMessage(null,(String) helper.getMsgJson());
            daoSession.insert(chat_msg);
            Log.i("数据库加入++++++",(String) helper.getMsgJson());
            messagesAdapter.addToStart(message,true);//加入下方列表
        }
        if(type.equals("text")) {
            ChatMessage chat_msg =new ChatMessage(null,(String) helper.getMsgJson());
            daoSession.insert(chat_msg);
            Log.i("数据库加入++++++",(String) helper.getMsgJson());
            messagesAdapter.addToStart(message,true);//加入下方列表
        }

        //额外处理

        //加入云端数据库
        if(type.equals("file")) {
            List<String> inputList = new ArrayList<String>();
            inputList.add(msgContent);//url id
            new sendFileTask().execute(inputList);
            return;
        }

        if(type.equals("img")) {
            //用一个线程去更新图片
            List<String> inputList = new ArrayList<String>();
            inputList.add(message.getImageUrl());//url id
            messageToUpdate=message;
            new setAvatarTask().execute(inputList);
        }
        /*List<String> inputList = new ArrayList<String>();
        inputList.add(message.getImageUrl());//url id
        inputList.add(helper.getMsgFrom()+helper1.getMsgDate());
         */


        //}
        if(chat!= null){
            try {
                //发送消息，参数为发送的消息内容
                chat.sendMessage(helper.getMsgJson());
                Log.i("0发送",helper.getMsgJson());
                //将所有接收到的消息，加入到数据库
                MyInfo myInfo=new MyInfo();
                myInfo.setUserId(JID.escapeNode(user_id));
                myInfo.setUserName(user_name);
                myInfo.setLatestJson(helper.getMsgJson());//加入最新消息
                myInfo.setSendId("add_msg_self:"+friend_name);
                //EventBus.getDefault().postSticky(myInfo);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        }
    }

    //监听消息接收
    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        chat.addMessageListener(this);
    }

    //接收到消息后的处理
    @Override
    public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
        //if(message.getType().equals(org.jivesoftware.smack.packet.Message.Type.chat) || message.getType().equals(org.jivesoftware.smack.packet.Message.Type.normal)){
            //if(message.getBody() != null){
                //android.os.Message msg = android.os.Message.obtain();
               // msg.what = 0;
                //msg.obj = message.getBody();
               // handler.sendMessage(msg);
           // }
       // }
    }

    private class setAvatarTask extends AsyncTask<List<String>, Object, Short> {
        @Override
        protected Short doInBackground(List<String>... params) {
            try {
                //messageToUpdate.setImage(new Message.Image(url));
                Thread.sleep(4000);
                Log.i("更新函数执行","加载图片");

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
                messagesAdapter.update(messageToUpdate.getId(),messageToUpdate);
                //imageView.setImageBitmap(bitmap);
                //textView.setText("");
            }
        }

    }

    private class sendFileTask extends AsyncTask<List<String>, Object, Short> {
        @Override
        protected Short doInBackground(List<String>... params) {
            try {
                Thread.sleep(1000);
                String friendJid = friend_id;
                String friendName = friend_name;
                Connection cn= JDBCUtils.getConnection();
                String sql = "insert into filelist (jid,fjid,send_time,accept_time,send_name,more,more_name) values (?,?,?,?,?,?,?);";
                PreparedStatement pstm = cn.prepareStatement(sql);
                //通过setString给4个问好赋值，下面的course_id，user_id，course_time，us_job_id都是已有值的变量，不要误会了
                pstm.setString(1, user_id);//发送者id
                pstm.setString(2, friend_id);//接收者id
                Calendar calendar = Calendar.getInstance();//获取时间
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH)+1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                pstm.setString(3, year+"-"+month+"-"+day+" "+hour+":"+minute);//发送时间
                pstm.setString(4, "");//接受时间
                pstm.setString(5, user_name);//发送姓名
                String file_name=params[0].get(0).split(":")[params[0].get(0).split(":").length-1];
                String file_path=params[0].get(0).split(":"+file_name)[0];
                pstm.setString(6, file_name);//发送文件名
                pstm.setString(7, file_path);//发送服务器路径
                //执行更新数据库
                pstm.executeUpdate();
                //关闭访问
                pstm.close();cn.close();
                //Toast.makeText(getBaseContext(),"文件已经发送", Toast.LENGTH_SHORT).show();
                //Looper.loop();//增加部分
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
                Log.i("文件","发送成功");
            }
        }

    }



    @Override
    public void onDestroy() {
        //处理内存
        chatManager.removeChatListener(this);
        messagesAdapter.clear();
        finish();
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i("暂停方法执行","4324352");
        //messagesAdapter.clear();
        //messagesAdapter.clear(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        //messagesAdapter.clear();
        //messagesAdapter.clear(true);
        Log.i("开始方法执行","4324352");
    }




}
