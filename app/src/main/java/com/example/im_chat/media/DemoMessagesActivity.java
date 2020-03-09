package com.example.im_chat.media;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.example.im_chat.R;
import com.example.im_chat.activity.MainActivity;
import com.example.im_chat.db.DaoMaster;
import com.example.im_chat.db.DaoSession;
import com.example.im_chat.entity.ChatMessage;
import com.example.im_chat.helper.MessageTranslateBack;
import com.example.im_chat.media.data.fixtures.MessagesFixtures;
import com.example.im_chat.media.data.model.Message;
import com.example.im_chat.media.data.model.User;
import com.example.im_chat.other.JID;
import com.example.im_chat.utils.AppUtils;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.greenrobot.greendao.query.QueryBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 *   接收消息
 * @auther songjihu
 * @since 2020/2/25 15:52
 */
public abstract class DemoMessagesActivity extends AppCompatActivity
        implements MessagesListAdapter.SelectionListener,
        MessagesListAdapter.OnLoadMoreListener {
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int TOTAL_MESSAGES_COUNT = 100;

    protected final String senderId = "0";
    protected ImageLoader imageLoader;
    public static MessagesListAdapter<Message> messagesAdapter;

    private Menu menu;
    private int selectionCount;
    private String lastLoadedDate;
    private List msgs;
    private ChatMessage msg;
    private List new_msgs = new ArrayList<Message>();
    private List input_msgs = new ArrayList<Message>();
    public String accept_id;
    public String send_id;
    private DaoSession daoSession;
    static ArrayList<String> avatars = new ArrayList<String>() {
        {
            add("http://d.lanrentuku.com/down/png/1904/international_food/fried_rice.png");
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url, Object payload) {
                Picasso.with(DemoMessagesActivity.this).load(url).into(imageView);
            }
        };
        initGreenDao();
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

    //初始化时加载的适配器内容
    @Override
    protected void onStart() {
        super.onStart();

        //messagesAdapter.addToStart(MessagesFixtures.getTextMessage(), true);
        //ChatUtil ru = new ChatUtil();

        msgs= queryListByMessage();
        int size = msgs.size();
        //将数据库中最新的100条加入
        for (int j = size-1; j >=0; j--) {
            //Log.i("本地数据库大小",":"+size);
            msg= (ChatMessage) msgs.get(j);
            MessageTranslateBack helper=new MessageTranslateBack((String) msg.getMsg());
            if(new_msgs.size()==100) break;
            //好友发送的消息
            if(JID.unescapeNode(helper.getMsgTo()).equals(JID.unescapeNode(accept_id))
                    &&JID.unescapeNode(helper.getMsgFromId()).equals(JID.unescapeNode(send_id))
                    &&msg!=null){
                Log.i("本地数据库1",":"+JID.unescapeNode(helper.getMsgTo())+"__1___"+JID.unescapeNode(accept_id));
                Log.i("本地数据库2",":"+JID.unescapeNode(helper.getMsgFromId())+"___2__"+JID.unescapeNode(send_id));
                User user = new User(helper.getMsgFromId(),helper.getMsgFrom(),avatars.get(0),true);
                Message message = new Message(helper.getMsgFrom(),user,helper.getMsgContent(),helper.getMsgDate());
                new_msgs.add(message);//从最新一条开始添加
            }
            //我发送的消息
            if(JID.unescapeNode(helper.getMsgTo()).equals(JID.unescapeNode(send_id))
                    &&JID.unescapeNode(helper.getMsgFromId()).equals(JID.unescapeNode(accept_id))
                    &&msg!=null){
                Log.i("本地数据库1",":"+JID.unescapeNode(helper.getMsgTo())+"__1___"+JID.unescapeNode(send_id));
                Log.i("本地数据库2",":"+JID.unescapeNode(helper.getMsgFromId())+"___2__"+JID.unescapeNode(accept_id));
                User user = new User(helper.getMsgFromId(),helper.getMsgFrom(),avatars.get(0),true);
                Message message = new Message(helper.getMsgFrom(),user,helper.getMsgContent(),helper.getMsgDate());
                new_msgs.add(message);//从最新一条开始添加
            }
        }
        //倒序
        for(int j=new_msgs.size();j>0;j--)
        {
            if(new_msgs.get(j-1)!=null){
                input_msgs.add(new_msgs.get(j-1));
            }
            else {
                break;
            }
        }
        messagesAdapter.addToEnd(input_msgs, true);
        if(input_msgs.size()>0){
            Message t=(Message) input_msgs.get(0);
            lastLoadedDate=formatter.format(t.getCreatedAt());
        }
        else {
            lastLoadedDate=formatter.format(new Date());
        }

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
    public void onBackPressed() {
        if (selectionCount == 0) {
            super.onBackPressed();
        } else {
            messagesAdapter.unselectAllItems();
        }
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
                //ArrayList<Message> messages = MessagesFixtures.getMessages(lastLoadedDate);
                //lastLoadedDate = messages.get(messages.size() - 1).getCreatedAt();
                //messagesAdapter.addToEnd(messages, false);
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
}
