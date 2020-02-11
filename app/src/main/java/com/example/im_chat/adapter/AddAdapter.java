package com.example.im_chat.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.im_chat.R;
import com.example.im_chat.activity.AddFriendActivity;
import com.example.im_chat.entity.Friend;
import com.example.im_chat.listener.OnItemClickListener;
import com.example.im_chat.other.JID;
import com.example.im_chat.utils.MyXMPPTCPConnectionOnLine;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.roster.Roster;

import java.util.ArrayList;
import java.util.List;


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


    public AddAdapter(Context context){
        mInflater = LayoutInflater.from(context);
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
                        try {
                            initXMPPTCPConnection();
                            Thread.sleep(1000);
                            String friendName = holder.jid.getText().toString();
                            friendName= JID.escapeNode(friendName);
                            boolean result = addFriend(friendName, friendName,"默认分组");
                            Message msg = new Message();
                            Bundle b = new Bundle();
                            b.putBoolean("result", result);
                            msg.setData(b);
                        } catch (Exception e) {
                            System.out.println("申请发生异常！！");
                            e.printStackTrace();
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
