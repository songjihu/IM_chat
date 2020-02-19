package com.example.im_chat.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.im_chat.R;
import com.example.im_chat.activity.AddFriendActivity;
import com.example.im_chat.entity.Friend;
import com.example.im_chat.entity.MyInfo;
import com.example.im_chat.listener.OnItemClickListener;
import com.example.im_chat.other.JID;
import com.example.im_chat.utils.JDBCUtils;
import com.example.im_chat.utils.MyXMPPTCPConnection;
import com.example.im_chat.utils.MyXMPPTCPConnectionOnLine;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.roster.Roster;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.yokeyword.eventbusactivityscope.EventBusActivityScope;


/**
 *   搜索好友列表适配器
 * @auther songjihu
 * @since 2020/2/7 13:33
 */
public class AddAdapter extends RecyclerView.Adapter <AddAdapter.VH> {
    //context
    private Context mContext;
    //展示的数据
    private List<Friend> mItems = new ArrayList<>();
    private Context context;
    private MyXMPPTCPConnectionOnLine connection;
    private LayoutInflater mInflater;
    private OnItemClickListener mClickListener;
    private MyInfo myInfo=new MyInfo();
    private Calendar calendar = Calendar.getInstance();//获取时间



    public AddAdapter(Context context,String jid){
        mInflater = LayoutInflater.from(context);
        this.context=context;
        myInfo.setUserId(jid);
    }



    //onCreateViewHolder方法创建一个viewHolder，viewholder可以理解为一条数据的展示布局，这里我们自定义类LinearViewHolder创建一个只有TextView的item
    //这里我们需要创建每条布局使用的layout：layout_linear_item
    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        //return new LinearViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_add_friend,parent,false));
        //item的布局形式
        View view = mInflater.inflate(R.layout.item_add_friend, parent, false);
        final AddAdapter.VH holder = new AddAdapter.VH(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();//增加部分
                        try {
                            initXMPPTCPConnection();
                            Thread.sleep(1000);
                            String friendJid = holder.jid.getText().toString();
                            String friendName = holder.name.getText().toString();
                            friendJid= JID.escapeNode(friendJid);
                            addFriend(friendJid, "","friend");
                            Connection cn= JDBCUtils.getConnection();
                            String sql = "insert into friendlist (jid,fjid,send_time,accept_time,send_name,more) values (?,?,?,?,?,?);";
                            PreparedStatement pstm = cn.prepareStatement(sql);
                            //通过setString给4个问好赋值，下面的course_id，user_id，course_time，us_job_id都是已有值的变量，不要误会了
                            pstm.setString(1, JID.unescapeNode(myInfo.getUserId()));//申请人id
                            pstm.setString(2, JID.unescapeNode(friendJid));//被申请人id
                            int year = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH)+1;
                            int day = calendar.get(Calendar.DAY_OF_MONTH);
                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            int minute = calendar.get(Calendar.MINUTE);
                            pstm.setString(3, year+"-"+month+"-"+day+" "+hour+":"+minute);//发送时间
                            pstm.setString(4, "");//接受时间
                            pstm.setString(5, myInfo.getUserName());//发送姓名
                            pstm.setString(6, "备注");//发送备注
                            //执行更新数据库
                            pstm.executeUpdate();
                            //关闭访问
                            pstm.close();cn.close();
                            Toast.makeText(context,"申请已经发送", Toast.LENGTH_SHORT).show();
                            Looper.loop();//增加部分
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context,"申请发生异常", Toast.LENGTH_SHORT).show();
                            Looper.loop();//增加部分
                            //System.out.println("申请发生异常！！");

                        }
                    }
                });
                //启动线程和实例化handler
                thread.start();
            }
        });

        return holder;
    }


    //onBindViewHolder方法为item的UI绑定展示数据，
    @Override
    public void onBindViewHolder(VH holder, int position) {
        Friend item = mItems.get(position);

        // 把每个图片视图设置不同的Transition名称, 防止在一个视图内有多个相同的名称, 在变换的时候造成混乱
        // Fragment支持多个View进行变换, 使用适配器时, 需要加以区分
        ViewCompat.setTransitionName(holder.name, String.valueOf(position) + "1");
        ViewCompat.setTransitionName(holder.jid, String.valueOf(position) + "2");

        //配置用户名等参数
        holder.name.setText(item.getName());
        holder.jid.setText(item.getJid());
    }

    /**
     *   清空之前的列表并加入新的
     * @auther songjihu
     * @since 2020/2/8 13:31
     * @param items :
     * @return null
     */
    public void setDatas(List<Friend> items) {
        mItems.clear();
        mItems.addAll(items);
    }

    public class VH extends RecyclerView.ViewHolder {
        public TextView name;//昵称
        public TextView jid;//jid


        public VH(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_Friend_name);
            jid = (TextView) itemView.findViewById(R.id.text_Friend_jid);
        }
    }

    //item的总个数
    @Override
    public int getItemCount() {
        return mItems.size();
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

    //初始化连接
    private void initXMPPTCPConnection(){
        connection = MyXMPPTCPConnectionOnLine.getInstance();
    }

}
