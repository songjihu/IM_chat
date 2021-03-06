package com.example.im_chat.adapter;

import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.im_chat.R;
import com.example.im_chat.entity.FileReceiveInfo;
import com.example.im_chat.entity.InvitationInfo;

import java.util.List;

import cn.ljp.swipemenu.SwipeMenuLayout;


public class FileItemAdapter extends BaseQuickAdapter<FileReceiveInfo, BaseViewHolder> {


    public FileItemAdapter(List<FileReceiveInfo> mShowItems) {
        super(R.layout.inv_item, mShowItems);
    }

    @Override
    protected void convert(BaseViewHolder helper, FileReceiveInfo item) {
        SwipeMenuLayout mSwipe = helper.getView(R.id.swipe_menu_layout);
        int position = helper.getLayoutPosition();
        mSwipe.setEnableLeftMenu(false);
        String text = "收到来自 "+item.getFromName()+" 的文件："+item.getBeizhu();
        mSwipe.setOpenChoke(false);
        ((TextView) helper.getView(R.id.tv_content)).setText(text);
        helper.addOnClickListener(R.id.tv_menu1).addOnClickListener(R.id.tv_menu2);
        ((TextView)helper.getView(R.id.tv_menu1)).setText("下载");
        ((TextView)helper.getView(R.id.tv_menu2)).setText("忽略");

    }

}
