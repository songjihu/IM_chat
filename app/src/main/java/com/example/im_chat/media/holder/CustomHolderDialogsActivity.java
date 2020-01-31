package com.example.im_chat.media.holder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


import com.example.im_chat.R;
import com.example.im_chat.media.DemoDialogsActivity;
import com.example.im_chat.media.data.fixtures.DialogsFixtures;
import com.example.im_chat.media.data.model.Dialog;
import com.example.im_chat.media.holder.holders.dialogs.CustomDialogViewHolder;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;


public class CustomHolderDialogsActivity extends DemoDialogsActivity {

    public static void open(Context context) {
        context.startActivity(new Intent(context, CustomHolderDialogsActivity.class));
    }

    private DialogsList dialogsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_holder_dialogs);
        //会话列表
        dialogsList = (DialogsList) findViewById(R.id.dialogsList);
        //初始化适配器并交给List
        initAdapter();


    }


    //当点击会话跳转activity到下一层，并在这里吧dialog传入
    @Override
    public void onDialogClick(Dialog dialog) {
        CustomHolderMessagesActivity.open(this);
    }

    private void initAdapter() {
        //初始化会话列表适配器
        super.dialogsAdapter = new DialogsListAdapter<>(
                R.layout.item_custom_dialog_view_holder,
                CustomDialogViewHolder.class,
                super.imageLoader);
        //设置适配器中item内容
        super.dialogsAdapter.setItems(DialogsFixtures.getDialogs());
        //设置监听器，长按或者点击的时间
        super.dialogsAdapter.setOnDialogClickListener(this);
        super.dialogsAdapter.setOnDialogLongClickListener(this);
        //将设置好的适配器配置给List
        dialogsList.setAdapter(super.dialogsAdapter);
    }
}
