package com.example.im_chat.activity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.im_chat.R;
import com.example.im_chat.base.BaseMainFragment;
import com.example.im_chat.db.DaoMaster;
import com.example.im_chat.db.DaoSession;
import com.example.im_chat.entity.ChatMessage;
import com.example.im_chat.entity.Friend;
import com.example.im_chat.entity.MyInfo;
import com.example.im_chat.helper.MessageTranslateBack;
import com.example.im_chat.media.data.fixtures.MessagesFixtures;
import com.example.im_chat.media.data.model.Message;
import com.example.im_chat.media.data.model.User;
import com.example.im_chat.other.JID;
import com.example.im_chat.other.TabSelectedEvent;
import com.example.im_chat.ui.fragment.first.FirstFragment;
import com.example.im_chat.ui.fragment.first.FirstHomeFragmentChat;
import com.example.im_chat.ui.fragment.second.SecondFragment;
import com.example.im_chat.ui.fragment.third.ThirdFragment;
import com.example.im_chat.ui.view.BottomBar;
import com.example.im_chat.ui.view.BottomBarTab;
import com.example.im_chat.utils.AppUtils;
import com.example.im_chat.utils.JDBCUtils;
import com.example.im_chat.utils.JDBCUtils1;
import com.example.im_chat.utils.MyXMPPTCPConnectionOnLine;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.greendao.query.QueryBuilder;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.yokeyword.eventbusactivityscope.EventBusActivityScope;
import me.yokeyword.fragmentation.SupportActivity;
import me.yokeyword.fragmentation.SupportFragment;

import static android.os.Build.TIME;

/**
 * 主界面activity
 * @auther songjihu
 * @since 2020/2/1 9:46
 */
public class MainActivity extends SupportActivity implements BaseMainFragment.OnBackToFirstListener, MessagesListAdapter.SelectionListener,
        MessagesListAdapter.OnLoadMoreListener ,MessagesListAdapter.OnMessageLongClickListener<Message>,
        MessageInput.InputListener,
        MessageInput.AttachmentsListener, ChatManagerListener,
        ChatMessageListener {
    private String serveraddress ="@123.56.163.211";
    public Chat chat[]=new Chat[1002];//会话
    private MyXMPPTCPConnectionOnLine connection;//连接
    private ChatManager chatManager;//会话管理
    public static final int FIRST = 0;
    public static final int SECOND = 1;
    public static final int THIRD = 2;
    public static final int FOURTH = 3;
    private String user_name;
    private static String user_id;
    List<String> mDatas = new ArrayList<>();
    List<String> UserDatas = new ArrayList<>();

    private SupportFragment[] mFragments = new SupportFragment[4];

    private BottomBar mBottomBar;
    private static final int TOTAL_MESSAGES_COUNT = 100;

    protected final String senderId = "0";
    protected ImageLoader imageLoader;
    public static MessagesListAdapter<Message> messagesAdapter;

    private Menu menu;
    private int selectionCount;
    private Date lastLoadedDate;
    private List msgs;
    private ChatMessage msg;
    private List new_msgs = new ArrayList<Message>();
    private List input_msgs = new ArrayList<Message>();
    public String accept_id;
    public String send_id;
    private static DaoSession daoSession;
    public List<Friend> friendlist =new ArrayList<>();//被加入项

    private List<String> inputList = new ArrayList<String>();
    private MyInfo myInfo=new MyInfo();
    static ArrayList<String> avatars = new ArrayList<String>() {
        {
            add("http://d.lanrentuku.com/down/png/1904/international_food/fried_rice.png");
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Intent intent =new Intent(MainActivity.this,TestLoginActivity.class);
        //startActivity(intent);

        for(int i = 1; i <= 5; i++) {
            mDatas.add("这是第"+ i + "条数据");
        }

        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);

        Bundle bundle = this.getIntent().getExtras();
        //从登陆activity的bundle中获取用户名
        user_name = bundle.getString("name");
        user_id = bundle.getString("id");
        Log.i("获取到的id值为",user_id);
        Log.i("获取到的name值为",user_name);

        //获取好友列表
        inputList.add(user_id);
        new getFriendListTask().execute(inputList);//刷新一次


        myInfo.setUserId(user_id);
        myInfo.setUserName(user_name);
        myInfo.setSendId("id_name");
        EventBus.getDefault().postSticky(myInfo);

        SupportFragment firstFragment = findFragment(FirstFragment.class);

        if (firstFragment == null) {
            mFragments[FIRST] = FirstFragment.newInstance();
            mFragments[SECOND] = SecondFragment.newInstance();
            mFragments[THIRD] = ThirdFragment.newInstance();

            loadMultipleRootFragment(R.id.fl_container, FIRST,
                    mFragments[FIRST],
                    mFragments[SECOND],
                    mFragments[THIRD]);
        } else {
            // 这里库已经做了Fragment恢复,所有不需要额外的处理了, 不会出现重叠问题

            // 这里我们需要拿到mFragments的引用
            mFragments[FIRST] = firstFragment;
            mFragments[SECOND] = findFragment(SecondFragment.class);
            mFragments[THIRD] = findFragment(ThirdFragment.class);
        }

        initView();

        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url, Object payload) {
                Picasso.with(MainActivity.this).load(url).into(imageView);
            }
        };
        initGreenDao();
        //实时刷新列表
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean flg = false;
                while(!flg){
                    try {
                        //serachFri(uTitles);
                        if(!connection.isConnected()){
                            connection.connect();
                        }
                        new getFriendListTask().execute(inputList);
                        Thread.sleep(3000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }) .start();
        initChatManager();
        initChat();
    }

    private void initGreenDao() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "aserbao.db");
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }


    public DaoSession getDaoSession() {
        return daoSession;
    }


    public List queryListByMessage(){
        QueryBuilder<ChatMessage> qb = daoSession.queryBuilder(ChatMessage.class);
        List<ChatMessage> list = qb.list(); //查出当前对应的数据
        return list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.chat_actions_menu, menu);
        onSelectionChanged(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                messagesAdapter.deleteSelectedMessages();
                break;
            case R.id.action_copy:
                messagesAdapter.copySelectedMessagesText(this, getMessageStringFormatter(), true);
                AppUtils.showToast(this, R.string.copied_message, true);
                break;
        }
        return true;
    }


    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        Log.i("TAG", "onLoadMore: " + page + " " + totalItemsCount);
        if (totalItemsCount < TOTAL_MESSAGES_COUNT) {
            //loadMessages();
        }
    }

    @Override
    public void onSelectionChanged(int count) {
        this.selectionCount = count;
        menu.findItem(R.id.action_delete).setVisible(count > 0);
        menu.findItem(R.id.action_copy).setVisible(count > 0);
    }

    protected void loadMessages() {
        new Handler().postDelayed(new Runnable() { //imitation of internet connection
            @Override
            public void run() {
                ArrayList<Message> messages = MessagesFixtures.getMessages(lastLoadedDate);
                lastLoadedDate = messages.get(messages.size() - 1).getCreatedAt();
                messagesAdapter.addToEnd(messages, false);
            }
        }, 1000);
    }

    private MessagesListAdapter.Formatter<Message> getMessageStringFormatter() {
        return new MessagesListAdapter.Formatter<Message>() {
            @Override
            public String format(Message message) {
                String createdAt = new SimpleDateFormat("MMM d, EEE 'at' h:mm a", Locale.getDefault())
                        .format(message.getCreatedAt());

                String text = message.getText();
                if (text == null) text = "[attachment]";

                return String.format(Locale.getDefault(), "%s: %s (%s)",
                        message.getUser().getName(), text, createdAt);
            }
        };
    }




    private void initView() {
        mBottomBar = (BottomBar) findViewById(R.id.bottomBar);

        mBottomBar.addItem(new BottomBarTab(this, R.drawable.ic_message))
                .addItem(new BottomBarTab(this, R.drawable.ic_friendlist))
                .addItem(new BottomBarTab(this, R.drawable.ic_me));
        mBottomBar.setClickable(false);

        mBottomBar.setOnTabSelectedListener(new BottomBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position, int prePosition) {
                showHideFragment(mFragments[position], mFragments[prePosition]);
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {
                final SupportFragment currentFragment = mFragments[position];
                int count = currentFragment.getChildFragmentManager().getBackStackEntryCount();

                // 如果不在该类别Fragment的主页,则回到主页;
                if (count > 1) {
                    if (currentFragment instanceof FirstFragment) {
                        currentFragment.popToChild(FirstHomeFragmentChat.class, false);
                    } else if (currentFragment instanceof FirstFragment) {
                        currentFragment.popToChild(FirstHomeFragmentChat.class, false);
                    }  else if (currentFragment instanceof FirstFragment) {
                        currentFragment.popToChild(FirstHomeFragmentChat.class, false);
                    }
                    return;
                }


                // 这里推荐使用EventBus来实现 -> 解耦
                if (count == 1) {
                    // 在FirstPagerFragment中接收, 因为是嵌套的孙子Fragment 所以用EventBus比较方便
                    // 主要为了交互: 重选tab 如果列表不在顶部则移动到顶部,如果已经在顶部,则刷新
                    EventBusActivityScope.getDefault(MainActivity.this).post(new TabSelectedEvent(position));
                }
            }
        });


    }

    //接收到消息后的处理
    @Override
    public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
        if(message.getType().equals(org.jivesoftware.smack.packet.Message.Type.chat) || message.getType().equals(org.jivesoftware.smack.packet.Message.Type.normal)){
            if(message.getBody() != null){
                android.os.Message msg = android.os.Message.obtain();
                msg.what = 0;
                msg.obj = message.getBody();
                handler.sendMessage(msg);
                Log.i("接收到一条消息","123");
            }
        }
    }
    //接受处理消息
    private Handler handler = new Handler(){

        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what){
                case 0:
                    MessageTranslateBack helper=new MessageTranslateBack((String) msg.obj);
                    User user = new User(helper.getMsgFromId(),helper.getMsgFrom(),avatars.get(0),true);
                    //ChatMessage chatMessage = new ChatMessage((String) msg.obj, 1);
                    Message message = new Message(helper.getMsgFrom(),user,helper.getMsgContent(),helper.getMsgDate());
                    //messageList.add(chatMessage);
                    if((helper.getMsgFromId()).equals(JID.unescapeNode(user_id)))
                    {
                        //啥也不干
                    }else{
                        //messagesAdapter.addToStart(message,true);//加入下方列表
                        //System.identityHashCode(messagesList);
                        //messagesAdapter.notifyDataSetChanged();
                        //Log.i("1发送11111111111111111",message.getText());
                    }
                    //将所有接收到的消息，加入到数据库
                    ChatMessage chat_msg =new ChatMessage(null,(String) msg.obj);
                    daoSession.insert(chat_msg);
                    Log.i("主界面数据库加入++++++",(String) msg.obj);
                    myInfo.setUserId(user_id);
                    myInfo.setUserName(user_name);
                    myInfo.setLatestJson((String) msg.obj);//加入最新消息
                    myInfo.setSendId("add_msg");
                    EventBus.getDefault().postSticky(myInfo);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onBackPressedSupport() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            pop();
        } else {
            ActivityCompat.finishAfterTransition(this);
        }
    }

    @Override
    public void onBackToFirstFragment() {
        mBottomBar.setCurrentItem(0);
    }

    /**
     * 这里暂没实现,忽略
     */
//    @Subscribe
//    public void onHiddenBottombarEvent(boolean hidden) {
//        if (hidden) {
//            mBottomBar.hide();
//        } else {
//            mBottomBar.show();
//        }
//    }

    //双击返回键退出应用
    private long exitTime = 0;
    //重写onKeyDown方法
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //判断是否按的后退键，而且按了一次
        if(keyCode==KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0)
        {
            //获取当前的系统时间，和exitTime相减，判断两次间隔是否大于规定时间
            //exitTime没有初始值则默认为0
            //如果大于设定的时间，则弹出提示，同时把exitTime设置为当前时间
            if(System.currentTimeMillis()-exitTime>TIME)
            {
                Toast.makeText(this,"再按一次退出程序",Toast.LENGTH_LONG).show();
                exitTime= System.currentTimeMillis();
            }
            else
            {
                //如果再次按后退的时间小于规定时间，则退出,且退出登录
                MyXMPPTCPConnectionOnLine.getInstance().disconnect();
                finish();
            }
            //消费事件
            return true;
        }
        //不处理事件
        return false;
    }

    @Override
    public void onAddAttachments() {

    }

    @Override
    public boolean onSubmit(CharSequence input) {
        return false;
    }

    @Override
    public void onMessageLongClick(Message message) {

    }


    private class getFriendListTask extends AsyncTask<List<String>, Object, Short> {
        @Override
        protected Short doInBackground(List<String>... params) {
            try {
                Log.i("22342432","1");
                Connection cn= JDBCUtils.getConnection();
                Connection cn1= JDBCUtils1.getConnection();
                String sql="SELECT * FROM `friendlist`WHERE fjid = '"+ JID.unescapeNode(params[0].get(0))+"'AND accepted ='1'";//
                Statement st=(Statement)cn.createStatement();
                Statement st1=(Statement)cn1.createStatement();
                ResultSet rs=st.executeQuery(sql);
                ResultSet rs1;
                String userstatus;
                while(rs.next()){
                    String sql1="SELECT * FROM `userStatus`WHERE username LIKE '";//选取用户状态
                    sql1=sql1+rs.getString("jid")+"'";
                    sql1=sql1.split("@")[0]+"%"+sql1.split("@")[1]+"AND `online`=1";//避开转义字符问题
                   // Log.i("dhuewiohaiu:",sql1+"---");
                    rs1=st1.executeQuery(sql1);
                    userstatus="用户离线";
                    while(rs1.next()){
                        userstatus="在线";
                    }
                    if(!isIn(rs.getString("jid"))){
                        friendlist.add(new Friend(
                                rs.getString("jid"),
                                rs.getString("send_name"),
                                "lastmsg",
                                "["+userstatus+"]"));
                    }
                }
                sql="SELECT * FROM `friendlist`WHERE jid = '"+ JID.unescapeNode(params[0].get(0))+"'AND accepted ='1'";//
                rs=st.executeQuery(sql);
                while(rs.next()){
                    String sql1="SELECT * FROM `userStatus`WHERE username LIKE '";//选取用户状态
                    sql1=sql1+rs.getString("fjid")+"'";
                    //Log.i("4637859276457",sql1);
                    sql1=sql1.split("@")[0]+"%"+sql1.split("@")[1]+"AND `online`=1";//避开转义字符问题
                    rs1=st1.executeQuery(sql1);
                    userstatus="用户离线";
                    while(rs1.next()){
                        userstatus="在线";
                    }
                    if(!isIn(rs.getString("fjid"))){
                        friendlist.add(new Friend(
                                rs.getString("fjid"),
                                rs.getString("accept_name"),
                                "lastmsg",
                                "["+userstatus+"]"));
                    }
                }
                JDBCUtils.close(rs,st,cn);

            } catch (SQLException e) {
                e.printStackTrace();
                return 0;
            }
            //获取完成后发送
            //EventBus.getDefault().postSticky(myInfo);
            return 1;
        }

        @Override
        protected void onPostExecute(Short state) {}

    }

    //是否已在列表中不在则创建聊天
    private boolean isIn(String str){
        for(int i=0;i<friendlist.size();i++)
        {
            if(str.equals(friendlist.get(i).getJid())){
                return true;
            }
        }
        chat[friendlist.size()]=chatManager.createChat(JID.escapeNode(str)+ serveraddress, null);
        return false;
    }

    //监听消息接收
    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        //chat.addMessageListener(this);
        chat.addMessageListener(this);
        ChatManagerListener ls=this;
    }
    ChatManagerListener ls=this;
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
            Friend t;
            for (int i=0;i<friendlist.size();i++)
            {
                t=friendlist.get(i);
                chat[i]=chatManager.createChat(JID.escapeNode(t.getJid())+ serveraddress, null);
                Log.i("创建聊天",JID.escapeNode(t.getJid())+ serveraddress+"---");
            }

        }
    }

    @Override
    public void onPause() {
        Log.i("4753689207854","暂停方法执行");
        super.onPause();  // Always call the superclass method first
        // 自定义资源释放
        //friendlist.clear();
        for(int i=0;i<friendlist.size();i++){
            //chat[i].removeMessageListener(this);
        }
    }

    @Override
    public void onResume() {
        Log.i("4753689207854","恢复方法执行");
        super.onResume();  // Always call the superclass method first
        // 初始化上一步被释放的资源
        for(int i=0;i<friendlist.size();i++){
            //chat[i].addMessageListener(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("4753689207854", "停止方法执行");
        // 初始化上一步被释放的资源
        for (int i = 0; i < friendlist.size(); i++) {
            //chat[i].addMessageListener(this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i("4753689207854", "开始方法执行");
        //super.onResume();  // Always call the superclass method first
        // 初始化上一步被释放的资源
        for (int i = 0; i < friendlist.size(); i++) {
            //chat[i].addMessageListener(this);
        }
    }


}
