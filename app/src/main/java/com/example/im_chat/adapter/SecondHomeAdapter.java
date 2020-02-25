package com.example.im_chat.adapter;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.im_chat.R;
import com.example.im_chat.entity.Friend;
import com.example.im_chat.entity.InvitationInfo;
import com.example.im_chat.listener.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

import cn.ljp.swipemenu.SwipeMenuLayout;

/**
 *   联系人列表适配器
 * @auther songjihu
 * @since 2020/2/4 9:40
 */
public class SecondHomeAdapter extends BaseQuickAdapter<Friend, BaseViewHolder> {


    public SecondHomeAdapter(List<Friend> mShowItems) {
        super(R.layout.act_friend, mShowItems);
    }

    @Override
    protected void convert(BaseViewHolder helper, Friend item) {
        int position = helper.getLayoutPosition();
        String name = item.getName();
        String state = item.getOnline();
        ((TextView) helper.getView(R.id.dialogName_f)).setText(name);//姓名
        ((TextView) helper.getView(R.id.dialogLastMessage_f)).setText(state);//状态
    }

}
