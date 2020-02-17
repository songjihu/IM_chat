package com.example.im_chat.ui.fragment.third;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.im_chat.R;

import com.example.im_chat.adapter.InvItemAdapter;
import com.example.im_chat.adapter.InvitationListAdapter;


import com.example.im_chat.entity.InvitationInfo;
import com.example.im_chat.entity.MyInfo;
import com.example.im_chat.other.JID;
import com.example.im_chat.utils.JDBCUtils;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import me.yokeyword.eventbusactivityscope.EventBusActivityScope;
import me.yokeyword.fragmentation.SupportFragment;


/**
 *   处理好友请求的fragment
 * @auther songjihu
 * @since 2020/2/13 15:57
 */
public class FriendInvitationFragment extends SupportFragment {
    private Toolbar mToolbar;
    private RecyclerView recyclerView;//好友申请列表
    private List<InvitationInfo> InvitationList=new ArrayList<>();;//list存储信息集合
    private Handler handler;//更新界面
    private InvItemAdapter  mAdapter;//配置适配器
    private String uTitles;//用户jid
    private int index = 0;



    public static FriendInvitationFragment newInstance() {

        Bundle args = new Bundle();

        FriendInvitationFragment fragment = new FriendInvitationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(MyInfo data) {
        //接收用户jid
        uTitles=data.getUserId();
        Log.i("（）（）（）（）（）（）",uTitles);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_third_friend_invitation, container, false);
        EventBus.getDefault().register(this);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.toolbarSettings);

        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _mActivity.onBackPressed();
            }
        });
        recyclerView=view.findViewById(R.id.recyclerView_inv);
        LinearLayoutManager manager = new LinearLayoutManager(_mActivity);//设置为流布局并定义manger
        recyclerView.setLayoutManager(manager);//循环显示的多个item的布局管理
        //List<String> mShowItems = new ArrayList<>();
        //mShowItems.add("123");
        mAdapter = new InvItemAdapter(InvitationList);       //定义item的适配器
        recyclerView.setAdapter(mAdapter);


        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (view.getId() == R.id.tv_menu1) {
                    //Log.d("======", "点击菜单1   " + (++index));
                    //handler.post(remove);
                    //adapter.setNewData(InvitationInfo);
                    adapter.remove(position);
                } else {
                    Log.d("======", "点击菜单2   " + (++index));
                }
            }
        });
        handler=new Handler();//创建属于主线程的handler
        mAdapter.setNewData(serachInvi(uTitles));
        //mAdapter.addData("123");
        serachInvi(uTitles);
    }


    //搜索邀请列表
    private List<InvitationInfo> serachInvi(final String inputStr){
        final List<InvitationInfo> friends = new ArrayList<InvitationInfo>();//建立新的list
        final CountDownLatch countDownLatch = new CountDownLatch(1);//进程结束标志
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean flag=false;
                    Connection cn= JDBCUtils.getConnection();
                    String sql="SELECT * FROM `friendlist`WHERE fjid = '"+ JID.unescapeNode(inputStr)+"'AND accepted ='0'";//
                    Statement st=(Statement)cn.createStatement();
                    ResultSet rs=st.executeQuery(sql);
                    while(rs.next()){
                        flag=true;
                        friends.add(new InvitationInfo( rs.getString("more"),
                                                        rs.getString("jid"),
                                                        rs.getString("send_name"),
                                                        rs.getString("send_time")));
                        //mAdapter.addData(rs.getString("send_name")+"(jid:"+rs.getString("jid")+")");
                        //Toast.makeText(getContext(), "add"+rs.getString("send_name"), Toast.LENGTH_SHORT).show();
                    }
                    if(!flag){
                        Looper.prepare();
                        Toast.makeText(getContext(), "未收到好友请求", Toast.LENGTH_SHORT).show();
                        Looper.loop();
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
        //mAdapter.setNewData(friends);
        return friends;

    }

    //接受
    private void acceptInvi(final String inputStr){
        String acceptfriend="";//建立新的list
        final CountDownLatch countDownLatch = new CountDownLatch(1);//进程结束标志
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean flag=false;
                    Connection cn= JDBCUtils.getConnection();
                    String sql="SELECT * FROM `friendlist`WHERE fjid = '"+ JID.unescapeNode(inputStr)+"'AND accepted ='0'";//
                    Statement st=(Statement)cn.createStatement();
                    ResultSet rs=st.executeQuery(sql);
                    while(rs.next()){
                        flag=true;
                        //friends.add(rs.getString("send_name")+"(jid:"+rs.getString("jid")+")");
                        //mAdapter.addData(rs.getString("send_name")+"(jid:"+rs.getString("jid")+")");
                        //Toast.makeText(getContext(), "add"+rs.getString("send_name"), Toast.LENGTH_SHORT).show();
                    }
                    if(!flag){
                        Looper.prepare();
                        Toast.makeText(getContext(), "未收到好友请求", Toast.LENGTH_SHORT).show();
                        Looper.loop();
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
    }

    private void denyInvi(final String inputStr){
        final List<InvitationInfo> friends = new ArrayList<InvitationInfo>();//建立新的list
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                /*try {
                    boolean flag=false;
                    Connection cn= JDBCUtils.getConnection();
                    String sql="SELECT * FROM `friendlist` WHERE fjid = '"+inputStr+"'AND accepted ='0'";//
                    Statement st=(Statement)cn.createStatement();
                    ResultSet rs=st.executeQuery(sql);
                    while(rs.next()){
                        flag=true;
                        InvitationInfo inv = new InvitationInfo(rs.getString("jid"),rs.getString("user_name"));
                        friends.add(friend);
                    }
                    if(!flag){
                        Toast.makeText(AddFriendActivity.this, "未搜索到用户", Toast.LENGTH_SHORT).show();

                    }
                    JDBCUtils.close(rs,st,cn);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();*/
            }
        }).start();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    @Override
    public boolean onBackPressedSupport() {
        pop();
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
