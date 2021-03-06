package com.example.im_chat.ui.fragment.third.invitationfunction;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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


import com.example.im_chat.entity.FileReceiveInfo;
import com.example.im_chat.entity.InvitationInfo;
import com.example.im_chat.entity.MyInfo;
import com.example.im_chat.other.JID;
import com.example.im_chat.ui.fragment.third.filefunction.FriendFileFragment;
import com.example.im_chat.utils.JDBCUtils;
import com.example.im_chat.utils.MyXMPPTCPConnectionOnLine;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.roster.Roster;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import me.yokeyword.fragmentation.SupportFragment;


/**
 *   处理好友请求的fragment
 * @auther songjihu
 * @since 2020/2/13 15:57
 */
public class FriendInvitationFragment extends SupportFragment {
    private Toolbar mToolbar;
    private RecyclerView recyclerView;//好友申请列表
    private List<InvitationInfo> InvitationList=new ArrayList<>();//list存储信息集合
    private Handler handler;//更新界面
    private InvItemAdapter  mAdapter;//配置适配器
    private String uTitles;//用户jid
    private String uTitles_name;//用户姓名
    private MyXMPPTCPConnectionOnLine connection;
    private Calendar calendar = Calendar.getInstance();//获取时间
    private InvitationInfo addItem;//被加入项
    private CountDownLatch countDownLatch = new CountDownLatch(1);//pv操作量



    public static FriendInvitationFragment newInstance() {

        FriendInvitationFragment fragment = new FriendInvitationFragment();
        return fragment;
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(MyInfo data) {
        //接收用户jid
        uTitles=data.getUserId();
        uTitles_name=data.getUserName();
        //friendsList=data.getFriendlist();
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
        mToolbar = (Toolbar) view.findViewById(R.id.toolbarSettings_inv);
        recyclerView=view.findViewById(R.id.recyclerView_inv);
        handler=new Handler();//创建属于主线程的handler
        handler.post(runnableUi);
        //实时刷新列表
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean flg = false;
                while(!flg){
                    try {
                        //serachInvi(uTitles);
                        new refreshTask().execute();
                        Log.i("111111:",InvitationList.size()+"");
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }) .start();
    }

    Runnable  runnableUi=new  Runnable(){
        @Override
        public void run() {
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _mActivity.onBackPressed();
                }
            });
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
                        InvitationInfo t= (InvitationInfo) adapter.getItem(position);
                        if (t != null) {
                            attemptAccept(t.getFromJid(),uTitles,"","friend");
                            adapter.remove(position);
                        }

                    } else {
                        //Log.d("======", "点击菜单2   ");
                        InvitationInfo t= (InvitationInfo) adapter.getItem(position);
                        if (t != null) {
                            attemptReject(t.getFromJid(),uTitles,"","friend");
                            adapter.remove(position);
                        }
                    }
                }
            });
        }

    };

    Runnable runnableAdd =new  Runnable(){
        @Override
        public void run() {
            //mAdapter.setNewData(InvitationList);
            if(!isIn(addItem.getFromJid())){
                mAdapter.addData(addItem);
            }
        }
    };


    //搜索邀请列表
    private List<InvitationInfo> serachInvi(final String inputStr){
        final List<InvitationInfo> friends = new ArrayList<InvitationInfo>();//建立新的list
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i("22342432","1");
                    boolean flag=false;
                    Connection cn= JDBCUtils.getConnection();
                    String sql="SELECT * FROM `friendlist`WHERE fjid = '"+ JID.unescapeNode(inputStr)+"'AND accepted ='0'";//
                    Statement st=(Statement)cn.createStatement();
                    ResultSet rs=st.executeQuery(sql);
                    while(rs.next()){
                        flag=true;
                        if(!isIn(rs.getString("jid"))){
                            //若不在则添加
                            addItem=new InvitationInfo( rs.getString("more"),
                                                                 rs.getString("jid"),
                                                                 rs.getString("send_name"),
                                                                 rs.getString("send_time"));
                            handler.post(runnableAdd);//更新界面
                            //InvitationList.add(addItem);
                        }
                        //mAdapter.addData(rs.getString("send_name")+"(jid:"+rs.getString("jid")+")");
                        //Toast.makeText(getContext(), "add"+rs.getString("send_name"), Toast.LENGTH_SHORT).show();
                    }
                    if(!flag){
                        //Looper.prepare();
                        //Toast.makeText(getContext(), "未收到好友请求", Toast.LENGTH_SHORT).show();
                        //Looper.loop();
                    }
                    JDBCUtils.close(rs,st,cn);

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        //mAdapter.setNewData(friends);
        return friends;

    }

    private class refreshTask extends AsyncTask<List<String>, Object, Short> {
        private List<InvitationInfo> addItem=new ArrayList<>();//被加入项
        @Override
        protected Short doInBackground(List<String>... params) {
            try {
                Log.i("22342432","1");
                boolean flag=false;
                Connection cn= JDBCUtils.getConnection();
                String sql="SELECT * FROM `friendlist`WHERE fjid = '"+ JID.unescapeNode(uTitles)+"'AND accepted ='0'";//
                Statement st=(Statement)cn.createStatement();
                ResultSet rs=st.executeQuery(sql);
                while(rs.next()){
                    flag=true;
                    if(!isIn(rs.getString("jid"))){
                        //若不在则添加
                        addItem.add(new InvitationInfo( rs.getString("more"),
                                rs.getString("jid"),
                                rs.getString("send_name"),
                                rs.getString("send_time")));
                        //handler.post(runnableAdd);//更新界面
                        //InvitationList.add(addItem);
                    }
                    //mAdapter.addData(rs.getString("send_name")+"(jid:"+rs.getString("jid")+")");
                    //Toast.makeText(getContext(), "add"+rs.getString("send_name"), Toast.LENGTH_SHORT).show();
                }
                JDBCUtils.close(rs,st,cn);

            } catch (Exception e) {
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
                    Log.i("开始更新","123");
                    //if(mAdapter.getItemCount()==0&&addItem.size()>0) mAdapter.addData(addItem.get(0));
                    if(addItem.size()!=0){
                        if(mAdapter.getItemCount()==0) mAdapter.addData(addItem.get(0));
                        boolean found=false;
                        for(int i=0;i<addItem.size();i++){
                            for(int j=0;j<mAdapter.getItemCount();j++){
                                //如果不在则插入
                                if(!isIn(addItem.get(i).getFromJid())){
                                    mAdapter.addData(addItem.get(i));
                                }

                            }
                        }
                    }
                    else {
                        //Toast.makeText(getActivity(), "暂无文件", Toast.LENGTH_SHORT).show();
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
        for(int i=0;i<InvitationList.size();i++)
        {
            if(str.equals(InvitationList.get(i).getFromJid())){
                return true;
            }
        }
        return false;
    }

    //接受好友请求
    private void attemptAccept(String jid,String fjid,String nickName, String groupName) {
        List<String> List = new ArrayList<String>();
        List.add(jid);
        List.add(fjid);
        List.add(nickName);
        List.add(groupName);
        new AcceptTask().execute(List);
    }

    //拒绝好友请求
    private void attemptReject(String jid,String fjid,String nickName, String groupName) {
        List<String> List = new ArrayList<String>();
        List.add(jid);
        List.add(fjid);
        List.add(nickName);
        List.add(groupName);
        new RejectTask().execute(List);
    }

    //初始化连接
    private void initXMPPTCPConnection(){
        connection = MyXMPPTCPConnectionOnLine.getInstance();
    }



    //接受
    private class AcceptTask extends AsyncTask<List<String>, Object, Short> {
        final CountDownLatch countDownLatch = new CountDownLatch(1);//注册完成后再登录
        //此次连接登录服务器为离线状态
        @Override
        protected Short doInBackground(List<String>... params) {
            initXMPPTCPConnection();
            if(connection.isConnected()) {
                try {
                    String jid=params[0].get(0);
                    String fjid=params[0].get(1);
                    String nickName=params[0].get(2);
                    String groupName=params[0].get(3);
                    addFriend(JID.escapeNode(jid), nickName,groupName);//openfire添加好友
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH)+1;
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);
                    Connection cn= JDBCUtils.getConnection();//更新数据库表
                    Log.i("13234",jid+"==="+fjid);
                    String t=year+"-"+month+"-"+day+" "+hour+":"+minute;
                    String sql = "update friendlist set accepted ='1' ,accept_time= '"+t+"', accept_name='"+uTitles_name+ "' where jid = '"+JID.unescapeNode(jid)+"' and fjid = '"+JID.unescapeNode(fjid)+"'";
                    PreparedStatement pstm = cn.prepareStatement(sql);
                    //执行更新数据库
                    pstm.executeUpdate();
                    //关闭访问
                    pstm.close();
                    cn.close();
                    return 1;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return 2;
                }
            }
            return 2;
        }

        @Override
        protected void onPostExecute(Short state) {

            switch (state){
                case 1:
                    Toast.makeText(getContext(), "添加成功", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(getContext(), "添加失败", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }


        }
    }

    //接受
    private class RejectTask extends AsyncTask<List<String>, Object, Short> {
        final CountDownLatch countDownLatch = new CountDownLatch(1);//注册完成后再登录
        //此次连接登录服务器为离线状态
        @Override
        protected Short doInBackground(List<String>... params) {
            initXMPPTCPConnection();
            if(connection.isConnected()) {
                try {
                    String jid=params[0].get(0);
                    String fjid=params[0].get(1);
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH)+1;
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);
                    Connection cn= JDBCUtils.getConnection();//更新数据库表
                    Log.i("13234",jid+"==="+fjid);
                    String t=year+"-"+month+"-"+day+" "+hour+":"+minute;
                    String sql = "update friendlist set accepted ='2' ,accept_time= '"+t+"' where jid = '"+JID.unescapeNode(jid)+"' and fjid = '"+JID.unescapeNode(fjid)+"'";
                    PreparedStatement pstm = cn.prepareStatement(sql);
                    //执行更新数据库
                    pstm.executeUpdate();
                    //关闭访问
                    pstm.close();
                    cn.close();
                    return 1;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return 2;
                }
            }
            return 2;
        }

        @Override
        protected void onPostExecute(Short state) {

            switch (state){
                case 1:
                    Toast.makeText(getContext(), "已拒绝", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(getContext(), "拒绝失败", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }


        }
    }

    //添加好友
    private boolean addFriend(String jid, String nickName, String groupName) {
        jid=jid+"@123.56.163.211";
        if(connection.isConnected()) {
            try {
                Roster.getInstanceFor(connection).createEntry(jid, nickName, new String[]{groupName});
                return true;
            } catch (SmackException.NotLoggedInException | SmackException.NoResponseException | XMPPException.XMPPErrorException
                    | SmackException.NotConnectedException e) {
                return false;
            }
        }
        throw new NullPointerException("服务器连接失败，请先连接服务器");
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
