package com.example.im_chat.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.example.im_chat.R;
import com.example.im_chat.base.BaseMainFragment;
import com.example.im_chat.entity.MyInfo;
import com.example.im_chat.other.TabSelectedEvent;
import com.example.im_chat.ui.fragment.first.FirstFragment;
import com.example.im_chat.ui.fragment.first.FirstHomeFragmentChat;
import com.example.im_chat.ui.fragment.second.SecondFragment;
import com.example.im_chat.ui.fragment.third.ThirdFragment;
import com.example.im_chat.ui.view.BottomBar;
import com.example.im_chat.ui.view.BottomBarTab;
import com.example.im_chat.utils.MyXMPPTCPConnection;
import com.example.im_chat.utils.MyXMPPTCPConnectionOnLine;

import org.greenrobot.eventbus.EventBus;
import java.util.ArrayList;
import java.util.List;
import me.yokeyword.eventbusactivityscope.EventBusActivityScope;
import me.yokeyword.fragmentation.SupportActivity;
import me.yokeyword.fragmentation.SupportFragment;

import static android.os.Build.TIME;

/**
 * 主界面activity
 * @auther songjihu
 * @since 2020/2/1 9:46
 */
public class MainActivity extends SupportActivity implements BaseMainFragment.OnBackToFirstListener {
    public static final int FIRST = 0;
    public static final int SECOND = 1;
    public static final int THIRD = 2;
    public static final int FOURTH = 3;
    private String user_name;
    private String user_id;
    List<String> mDatas = new ArrayList<>();
    List<String> UserDatas = new ArrayList<>();

    private SupportFragment[] mFragments = new SupportFragment[4];

    private BottomBar mBottomBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        for(int i = 1; i <= 5; i++) {
            mDatas.add("这是第"+ i + "条数据");
        }

        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);

        Bundle bundle = this.getIntent().getExtras();
        //从登陆activity的bundle中获取用户名
        user_name = bundle.getString("name");
        user_id = bundle.getString("id");
        Log.i("获取到的id值为",user_id);
        Log.i("获取到的name值为",user_name);
        MyInfo myInfo=new MyInfo();
        myInfo.setUserId(user_id);
        myInfo.setUserName(user_name);
        EventBus.getDefault().postSticky(myInfo);


        SupportFragment firstFragment = findFragment(FirstFragment.class);

        if (firstFragment == null) {
            mFragments[FIRST] = FirstFragment.newInstance();
            mFragments[SECOND] = SecondFragment.newInstance();
            mFragments[THIRD] = ThirdFragment.newInstance();

            loadMultipleRootFragment(R.id.fl_container, FIRST,
                    mFragments[FIRST],
                    mFragments[SECOND],
                    mFragments[THIRD]);
        } else {
            // 这里库已经做了Fragment恢复,所有不需要额外的处理了, 不会出现重叠问题

            // 这里我们需要拿到mFragments的引用
            mFragments[FIRST] = firstFragment;
            mFragments[SECOND] = findFragment(SecondFragment.class);
            mFragments[THIRD] = findFragment(ThirdFragment.class);
        }

        initView();
    }



    private void initView() {
        mBottomBar = (BottomBar) findViewById(R.id.bottomBar);

        mBottomBar.addItem(new BottomBarTab(this, R.drawable.ic_message))
                .addItem(new BottomBarTab(this, R.drawable.ic_friendlist))
                .addItem(new BottomBarTab(this, R.drawable.ic_me));

        mBottomBar.setOnTabSelectedListener(new BottomBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position, int prePosition) {
                showHideFragment(mFragments[position], mFragments[prePosition]);
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {
                final SupportFragment currentFragment = mFragments[position];
                int count = currentFragment.getChildFragmentManager().getBackStackEntryCount();

                // 如果不在该类别Fragment的主页,则回到主页;
                if (count > 1) {
                    if (currentFragment instanceof FirstFragment) {
                        currentFragment.popToChild(FirstHomeFragmentChat.class, false);
                    } else if (currentFragment instanceof FirstFragment) {
                        currentFragment.popToChild(FirstHomeFragmentChat.class, false);
                    }  else if (currentFragment instanceof FirstFragment) {
                        currentFragment.popToChild(FirstHomeFragmentChat.class, false);
                    }
                    return;
                }


                // 这里推荐使用EventBus来实现 -> 解耦
                if (count == 1) {
                    // 在FirstPagerFragment中接收, 因为是嵌套的孙子Fragment 所以用EventBus比较方便
                    // 主要为了交互: 重选tab 如果列表不在顶部则移动到顶部,如果已经在顶部,则刷新
                    EventBusActivityScope.getDefault(MainActivity.this).post(new TabSelectedEvent(position));
                }
            }
        });
    }

    @Override
    public void onBackPressedSupport() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            pop();
        } else {
            ActivityCompat.finishAfterTransition(this);
        }
    }

    @Override
    public void onBackToFirstFragment() {
        mBottomBar.setCurrentItem(0);
    }

    /**
     * 这里暂没实现,忽略
     */
//    @Subscribe
//    public void onHiddenBottombarEvent(boolean hidden) {
//        if (hidden) {
//            mBottomBar.hide();
//        } else {
//            mBottomBar.show();
//        }
//    }

    //双击返回键退出应用
    private long exitTime = 0;
    //重写onKeyDown方法
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //判断是否按的后退键，而且按了一次
        if(keyCode==KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0)
        {
            //获取当前的系统时间，和exitTime相减，判断两次间隔是否大于规定时间
            //exitTime没有初始值则默认为0
            //如果大于设定的时间，则弹出提示，同时把exitTime设置为当前时间
            if(System.currentTimeMillis()-exitTime>TIME)
            {
                Toast.makeText(this,"再按一次退出程序",Toast.LENGTH_LONG).show();
                exitTime= System.currentTimeMillis();
            }
            else
            {
                //如果再次按后退的时间小于规定时间，则退出,且退出登录
                MyXMPPTCPConnectionOnLine.getInstance().disconnect();
                finish();
            }
            //消费事件
            return true;
        }
        //不处理事件
        return false;
    }
}
