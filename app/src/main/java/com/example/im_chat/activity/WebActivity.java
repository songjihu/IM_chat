package com.example.im_chat.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.example.im_chat.R;
import com.example.im_chat.entity.MyInfo;
import com.example.im_chat.entity.SendInfo;
import com.example.im_chat.other.JID;
import com.example.im_chat.ui.fragment.third.AvatarFragment;
import com.example.im_chat.ui.fragment.third.AvatarWebFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


import java.util.ArrayList;
import java.util.List;

import me.yokeyword.eventbusactivityscope.EventBusActivityScope;

/**
 *   传入url加载网页
 * @auther songjihu
 * @since 2020/3/25 20:34
 */
public class WebActivity extends Activity{

    private String uTitles;
    private String toOpenUrl;
    private AvatarWebFragment rightFragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(SendInfo data) {
        //接收消息
        if(data.getMsg()!=null&&data.getMsg().equals("update_avatar")){
            //关闭
            //onBackPressed();
            //finish();
            //onDestroy();


        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getIntent().getExtras();
        if(bundle != null){
            uTitles = JID.unescapeNode(bundle.getString("jid"));
            toOpenUrl=bundle.getString("url");
        }
        setContentView(R.layout.activity_web);
        EventBusActivityScope.getDefault(WebActivity.this).register(this);
        EventBus.getDefault().register(this);
        bundle.putString("fromId", uTitles);
        bundle.putString("url", toOpenUrl);
        rightFragment = new AvatarWebFragment();
        rightFragment.setArguments(bundle);
        fragmentManager =getFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.web_holder,rightFragment);
        //步骤三：调用commit()方法使得FragmentTransaction实例的改变生效
        transaction.commit();



    }

    @Override
    public void onDestroy() {
        fragmentManager=null;
        rightFragment=null;
        transaction=null;
        //finish();
        super.onDestroy();
        finish();
    }

}