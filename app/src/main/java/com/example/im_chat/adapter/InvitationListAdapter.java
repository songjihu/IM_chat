package com.example.im_chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.im_chat.R;
import com.example.im_chat.entity.Friend;
import com.example.im_chat.entity.InvitationInfo;
import com.example.im_chat.entity.MyInfo;

import java.util.ArrayList;
import java.util.List;

public class InvitationListAdapter extends BaseAdapter {

    private List<InvitationInfo> listItems= new ArrayList<>();;//数据集合
    private LayoutInflater layoutinflater;//视图容器，用来导入布局
    private Context context;
    private MyInfo myInfo=new MyInfo();


    static class ViewHolder
    {
        private TextView frommsg;
        private TextView fromtime;
        private TextView frombeizhu;
        private ImageView fromimage;
    }

    /*
     * 实例化Adapter
     */
    public InvitationListAdapter(Context context, String jid)
    {
        this.layoutinflater = LayoutInflater.from(context);
        this.context=context;
        myInfo.setUserId(jid);
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        InvitationInfo invite = listItems.get(position);
        ViewHolder holder;
        View view;
        if(convertView == null)
        {
            holder= new ViewHolder();
            //获取listitem布局文件
            view = layoutinflater.inflate(R.layout.item_invitation, null);

            //获取控件对象
            holder.frommsg = (TextView) view.findViewById(R.id.userName_inv);
            holder.fromtime = (TextView) view.findViewById(R.id.time_invi);
            holder.fromimage = (ImageView) view.findViewById(R.id.dialogAvatar_invi);
            holder.frombeizhu = (TextView) view.findViewById(R.id.userBeizhu);

            view.setTag(holder);
        }
        else
        {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        //设置图片和文字
        String msg="收到一条好友请求 来自："+invite.getFromName();
        holder.frommsg.setText(msg);
        holder.fromtime.setText(String.valueOf(invite.getFromName()));
        holder.frombeizhu.setText(String.valueOf(invite.getBeizhu()));
        return view;
    }
    public void setDatas(List<InvitationInfo> items) {
        listItems.clear();
        listItems.addAll(items);
    }

}
