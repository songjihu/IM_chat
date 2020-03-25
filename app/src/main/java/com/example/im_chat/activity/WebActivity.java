package com.example.im_chat.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.example.im_chat.R;
import com.example.im_chat.entity.MyInfo;
import com.example.im_chat.other.JID;
import com.example.im_chat.ui.fragment.third.AvatarWebFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


import me.yokeyword.eventbusactivityscope.EventBusActivityScope;

/**
 *   传入url加载网页
 * @auther songjihu
 * @since 2020/3/25 20:34
 */
public class WebActivity extends Activity{

    private String uTitles;
    private String toOpenUrl;


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(MyInfo data) {
        //接收用户jid
        uTitles=data.getUserId();
        //Log.i("（）（）（）（）（）（）",uTitles);
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
        AvatarWebFragment rightFragment = new AvatarWebFragment();
        rightFragment.setArguments(bundle);
        FragmentManager fragmentManager =getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.web_holder,rightFragment);
        //步骤三：调用commit()方法使得FragmentTransaction实例的改变生效
        transaction.commit();



    }



}