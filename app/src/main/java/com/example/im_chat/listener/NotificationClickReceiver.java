package com.example.im_chat.listener;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.im_chat.activity.LoginActivity;
import com.example.im_chat.entity.ZeroInfo;
import com.example.im_chat.media.holder.CustomHolderMessagesActivity;

import org.greenrobot.eventbus.EventBus;


public class NotificationClickReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("TAG", "userClick:我被点击啦！！！ ");
        if(intent.getExtras()!=null){
            Log.i("!!","intent不为空"+intent.getExtras().get("name"));
        }else {
            Log.i("!!","intent为空");
        }
        ZeroInfo zeroInfo=new ZeroInfo();
        String t=intent.getExtras().getString("f_jid");
        zeroInfo.setSendId(t);
        EventBus.getDefault().postSticky(zeroInfo);
        Intent newIntent = new Intent(context, CustomHolderMessagesActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //用Bundle携带数据
        Bundle bundle=intent.getExtras();
        //Log.i("4523543254获取到的name值为",uuu.getUserName());
        newIntent.putExtras(bundle);
        context.startActivity(newIntent);
    }

}