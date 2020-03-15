package com.example.im_chat.ui.fragment.second;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Collections;
import android.os.Handler;
import android.widget.Toast;

import com.allenliu.sidebar.ISideBarSelectCallBack;
import com.allenliu.sidebar.SideBar;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.im_chat.R;
import com.example.im_chat.activity.AddFriendActivity;
import com.example.im_chat.activity.LoginActivity;
import com.example.im_chat.activity.MainActivity;
import com.example.im_chat.adapter.SecondHomeAdapter;
import com.example.im_chat.entity.Friend;
import com.example.im_chat.entity.InvitationInfo;
import com.example.im_chat.entity.MyInfo;
import com.example.im_chat.listener.OnItemClickListener;
import com.example.im_chat.media.holder.CustomHolderDialogsActivity;
import com.example.im_chat.media.holder.CustomHolderMessagesActivity;
import com.example.im_chat.other.JID;
import com.example.im_chat.utils.ChinesePinyinUtil;
import com.example.im_chat.utils.JDBCUtils;
import com.example.im_chat.utils.JDBCUtils1;
import com.example.im_chat.utils.MyXMPPTCPConnection;
import com.example.im_chat.utils.MyXMPPTCPConnectionOnLine;

import me.yokeyword.eventbusactivityscope.EventBusActivityScope;
import me.yokeyword.fragmentation.SupportFragment;


public class SecondHomeFragmentChat extends SupportFragment implements SwipeRefreshLayout.OnRefreshListener, ConnectionListener, RosterListener {
        private Toolbar mToolbar;
    private RecyclerView mRecy;
    private SwipeRefreshLayout mRefreshLayout;
    private TextView welcomeView;
    private SecondHomeAdapter mAdapter;//此项为联系人item的适配器
    private List<Friend> friendsList=new ArrayList<>();//list存储信息集合
    private List<String> friendsList_s=new ArrayList<>();//list存储信息集合(排序姓名用)
    private Handler handler;
    private MyXMPPTCPConnectionOnLine connectionOnLine;//设置在线的连接
    private Button go_add;
    private SideBar bar;
    private boolean mInAtTop = true;
    private int mScrollTotal;
    private String uTitles = new String();
    private String uTitles_name = new String();
    private final static Comparator<Object> CHINA_COMPARE = Collator.getInstance(java.util.Locale.CHINA);//排序规则
    private List<String> inputList = new ArrayList<String>();
    private List<String> inputList1 = new ArrayList<String>();
    private Roster roster;
    private MyXMPPTCPConnectionOnLine connection;

    //显示好友列表

    private int friend_number=0;



    public static SecondHomeFragmentChat newInstance() {

        Bundle args = new Bundle();

        SecondHomeFragmentChat fragment = new SecondHomeFragmentChat();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second_home, container, false);
        EventBusActivityScope.getDefault(_mActivity).register(this);
        EventBus.getDefault().register(this);
        initView(view);
        return view;
    }

    /*
    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Toast.makeText(getActivity(), event.message, Toast.LENGTH_SHORT).show();
    }
*/

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(MyInfo data) {
        //接收用户jid

        uTitles=data.getUserId();
        uTitles_name=data.getUserName();
        //friendsList=data.getFriendlist();
        Log.i("shf:---",uTitles);
    }


    private void initXMPPTCPConnection(){
        connection = MyXMPPTCPConnectionOnLine.getInstance();
        connection.addConnectionListener(this);
        roster = Roster.getInstanceFor(connection);
        roster.addRosterListener(this);

    }

    public void initView(View view) {

        mRecy = (RecyclerView) view.findViewById(R.id.recy3_2);//循环显示的多个item
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout3_2);//下拉循环布局
        mRefreshLayout.setColorSchemeResources(R.color.colorPrimary);//设置下拉刷新的颜色
        mAdapter = new SecondHomeAdapter(friendsList);//定义item的适配器
        LinearLayoutManager manager = new LinearLayoutManager(_mActivity);//设置为流布局并定义manger
        mRecy.setLayoutManager(manager);//循环显示的多个item的布局管理
        mRecy.setAdapter(mAdapter);//循环显示的多个item的适配器设置
        mRefreshLayout.setOnRefreshListener(this);//设置下拉刷新的对象

        go_add=(Button)view.findViewById(R.id.go_add_friend);
        go_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //新建一个显式意图，第一个参数为当前Activity类对象，第二个参数为你要打开的Activity类
                Intent intent =new Intent(getActivity(), AddFriendActivity.class);
                startActivity(intent);
            }
        });
        // 获取好友名单


        //点击item的事件监听，开启聊天
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Friend t=(Friend) adapter.getItem(position);
                Log.i("点击了jid",t.getJid()+"姓名"+t.getName());
                Intent intent =new Intent(getActivity(), CustomHolderMessagesActivity.class);
                //用Bundle携带数据
                Bundle bundle=new Bundle();
                bundle.putString("jid",uTitles);
                bundle.putString("name",uTitles_name);
                bundle.putString("f_jid",t.getJid());
                bundle.putString("f_name",t.getName());
                //Log.i("4523543254获取到的name值为",uuu.getUserName());
                intent.putExtras(bundle);

                startActivity(intent);
                //CustomHolderDialogsActivity.open(getActivity());


            }
        });
        //点击签到按钮的事件监听，开启新的fragment

        mRecy.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mScrollTotal += dy;
                if (mScrollTotal <= 0) {
                    mInAtTop = true;
                } else {
                    mInAtTop = false;
                }

            }
        });

        bar= (SideBar) view.findViewById(R.id.slid_bar_friend);
        bar.setOnStrSelectCallBack(new ISideBarSelectCallBack() {
            @Override
            public void onSelectStr(int index, String selectStr) {
                //Toast.makeText(getActivity(),selectStr,Toast.LENGTH_SHORT).show();
                Log.i("----------------:",selectStr);
                for(int i=0;i<friendsList.size();i++){
                    String t=friendsList.get(i).getName();
                    t= ChinesePinyinUtil.getPinYinFirstChar(t);//获取第一个字母

                    if(t.equals(selectStr)){
                        //找到选中的字符
                        Log.i("获取到字符","--"+t+"--");
                        Log.i("跳转到:::",t+"位置:::"+i);
                        mRecy.scrollToPosition(i);
                        break;
                    }

                }
            }
        });

        handler=new Handler();//创建属于主线程的handler
        //handler.post(runnableUi);
        inputList.add(uTitles);
        //实时刷新列表
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean flg = false;
                while(!flg){
                    try {
                        //serachFri(uTitles);
                        //new refreshTask().execute(inputList);
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }) .start();
        new refreshTask().execute(inputList);//刷新一次

        initXMPPTCPConnection();
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
                        String from = presence.getFrom().split("@")[0];//去掉了后缀
                        if (presence.getType().equals(Presence.Type.subscribe)) {
                            System.out.println("收到请求！请求添加好友" + from);
                        } else if (presence.getType().equals(Presence.Type.subscribed)) {//对方同意订阅
                            System.out.println("收到请求！同意订阅" + from);
                        } else if (presence.getType().equals(Presence.Type.unsubscribe)) {//取消订阅
                            System.out.println("收到请求！取消订阅" + from);
                        } else if (presence.getType().equals(Presence.Type.unsubscribed)) {//拒绝订阅
                            System.out.println("收到请求！拒绝订阅" + from);
                        } else if (presence.getType().equals(Presence.Type.unavailable)) {//用户离线
                            System.out.println("收到请求！用户离线" + from+"---");
                            inputList1.add(from);
                            new setToOffline().execute(inputList1);
                            //inputList1.clear();
                        } else if (presence.getType().equals(Presence.Type.available)) {//上线
                            System.out.println("收到请求！上线" + from+"---");
                            inputList1.add(from);
                            new setToOnline().execute(inputList1);
                            //inputList1.clear();
                        }
                    }
                }
            };
            //添加监听
            connection.addAsyncStanzaListener(packetListener, filter);
        }

    }

    @Override
    public void onRefresh() {
        mRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 获取好友名单
                //serachFri(uTitles);
                new refreshTask().execute(inputList);
            }
        }, 3000);
        mRefreshLayout.setRefreshing(false);
    }




    private class refreshTask extends AsyncTask<List<String>, Object, Short> {
        private List<Friend> addItem=new ArrayList<>();//被加入项
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
                    Log.i("dhuewiohaiu:",sql1+"---");
                    rs1=st1.executeQuery(sql1);
                    userstatus="用户离线";
                    while(rs1.next()){
                        userstatus="在线";
                    }
                    addItem.add(new Friend(
                            rs.getString("jid"),
                            rs.getString("send_name"),
                            "lastmsg",
                            "["+userstatus+"]"));
                }
                sql="SELECT * FROM `friendlist`WHERE jid = '"+ JID.unescapeNode(params[0].get(0))+"'AND accepted ='1'";//
                rs=st.executeQuery(sql);
                while(rs.next()){
                    String sql1="SELECT * FROM `userStatus`WHERE username LIKE '";//选取用户状态
                    sql1=sql1+rs.getString("fjid")+"'";
                    Log.i("4637859276457",sql1);
                    sql1=sql1.split("@")[0]+"%"+sql1.split("@")[1]+"AND `online`=1";//避开转义字符问题
                    rs1=st1.executeQuery(sql1);
                    userstatus="用户离线";
                    while(rs1.next()){
                        userstatus="在线";
                    }
                    addItem.add(new Friend(
                            rs.getString("fjid"),
                            rs.getString("accept_name"),
                            "lastmsg",
                            "["+userstatus+"]"));
                }
                JDBCUtils.close(rs,st,cn);

            } catch (SQLException e) {
                e.printStackTrace();
                return 0;
            }

            return 1;
        }

        @Override
        protected void onPostExecute(Short state) {
            switch (state){
                case 0:
                    Toast.makeText(getActivity(), "更新失败", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    //if(mAdapter.getItemCount()==0&&addItem.size()>0) mAdapter.addData(addItem.get(0));
                    if(mAdapter.getItemCount()==0) mAdapter.addData(addItem.get(0));
                    boolean found=false;
                    for(int i=0;i<addItem.size();i++){
                        for(int j=0;j<mAdapter.getItemCount();j++){
                            //不在并且>=左<=右当前则插入前边
                            found=false;
                            //如果不在则在适当位置插入
                            if(!isIn(addItem.get(i).getJid())){
                                String t=mAdapter.getItem(j).getName();
                                t= ChinesePinyinUtil.getPinYinFirstChar(t);//获取第一个字母
                                if(ChinesePinyinUtil.getPinYinFirstChar(addItem.get(i).getName()).compareTo(t)<=0){
                                    mAdapter.addData(j,addItem.get(i));
                                    found=true;
                                    break;
                                }
                            }
                            //如果在则更新
                            if(addItem.get(i).getJid().equals(mAdapter.getItem(j).getJid())&&!addItem.get(i).getOnline().equals(mAdapter.getItem(j).getOnline())){
                                mAdapter.setData(j,addItem.get(i));
                            }

                        }
                        if(!isIn(addItem.get(i).getJid())&&!found){
                            mAdapter.addData(addItem.get(i));
                        }
                    }
                    //Toast.makeText(getActivity(), "更新成功", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

    //是否已在列表中
    private boolean isIn(String str){
        for(int i=0;i<friendsList.size();i++)
        {
            if(str.equals(friendsList.get(i).getJid())){
                return true;
            }
        }
        return false;
    }

    //改变状态
    private class setToOffline extends AsyncTask<List<String>, Object, Short> {
        private String Item;//被加入项
        @Override
        protected Short doInBackground(List<String>... params) {
            Item=params[0].get(0);
            return 1;
        }

        @Override
        protected void onPostExecute(Short state) {
            for(int i=0;i<mAdapter.getItemCount();i++)
            {
                String t1=JID.unescapeNode(Item);
                String t2=mAdapter.getItem(i).getJid();
                Log.i("fgbhdseiuwgogh","---"+t1.compareTo(t2)+"---");
                if(t1.compareTo(t2)==0){
                    Log.i("找到了","greyhtreyrtey");
                    Friend tt= mAdapter.getItem(i);
                    tt.setOnline("[用户离线]");
                    mAdapter.setData(i,tt);
                    inputList1.clear();
                }
            }
        }
    }

    private class setToOnline extends AsyncTask<List<String>, Object, Short> {
        private String Item;//被加入项
        @Override
        protected Short doInBackground(List<String>... params) {
            Item=params[0].get(0);
            return 1;
        }

        @Override
        protected void onPostExecute(Short state) {
            for(int i=0;i<mAdapter.getItemCount();i++)
            {
                String t1=JID.unescapeNode(Item);
                String t2=mAdapter.getItem(i).getJid();
                Log.i("fgbhdseiuwgogh","---"+t1.compareTo(t2)+"---");
                if(t1.compareTo(t2)==0){
                    Log.i("找到了","greyhtreyrtey");
                    Friend tt= mAdapter.getItem(i);
                    tt.setOnline("[在线]");
                    mAdapter.setData(i,tt);
                    inputList1.clear();
                }
            }
        }
    }
    private void scrollToTop() {
        mRecy.smoothScrollToPosition(0);
    }

    // 构建Runnable对象，在runnable中更新界面
    Runnable  runnableUi=new  Runnable(){
        @Override
        public void run() {
            //更新界面
            //获取好友名单
            //List<Friend> friendsList = getMyFriends();
            //设置数据到适配器
            //mAdapter.setDatas(friendsList);
            mRecy.setAdapter(mAdapter);
            mRecy.scrollToPosition(mAdapter.getItemCount()-1);//此句为设置显示
        }

    };

    //TODO:打开此界面时，先更新未收到的聊天记录，加载到数据库，再将数据库中聊天记录创建在本地
    public void onReupdate()
    {

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
