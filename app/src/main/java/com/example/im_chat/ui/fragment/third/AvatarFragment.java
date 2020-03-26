package com.example.im_chat.ui.fragment.third;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.annotation.Nullable;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.im_chat.R;

import com.example.im_chat.activity.WebActivity;
import com.example.im_chat.entity.MyInfo;

import com.example.im_chat.entity.SendInfo;
import com.example.im_chat.other.JID;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;


import me.yokeyword.eventbusactivityscope.EventBusActivityScope;
import me.yokeyword.fragmentation.SupportFragment;

/**
 *   user头像布局
 * @auther songjihu
 * @since 2020/2/13 15:45
 */
public class AvatarFragment extends SupportFragment {

    private ImageView imageView;
    URL myFileUrl = null;
    Bitmap bitmap = null;
    String url;
    String uTitles;
    private String toOpenUrl="http://192.168.1.109:8080/user2";
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(MyInfo data) {
        //接收用户jid
        uTitles=JID.unescapeNode(data.getUserId());
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(SendInfo data) {
        //接收消息
        if(data.getMsg()!=null&&data.getMsg().equals("update_avatar")){
            //更新头像时接收
            Log.i("接收到要更新的eventbus","ohohoho");
            List<String> inputList = new ArrayList<String>();
            inputList.add("延迟更新");
            new setAvatarTask().execute(inputList);//刷新一次

        }

    }

    public static AvatarFragment newInstance() {

        Bundle args = new Bundle();

        AvatarFragment fragment = new AvatarFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_third_avatar, container, false);
        imageView=view.findViewById(R.id.third_avatar);
        EventBusActivityScope.getDefault(getActivity()).register(this);
        EventBus.getDefault().register(this);
        url="http://192.168.1.109:8080/temp-rainy/user_avatar/"+uTitles+".jpg";
        List<String> inputList = new ArrayList<String>();
        inputList.add("直接更新");
        new setAvatarTask().execute(inputList);//刷新一次
        //imageView.setImageURI(Uri.parse("http://192.168.1.109:8080/temp-rainy/avatar/1.jpg"));
        initView();
        return view;
    }

    public void initView(){
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开启设置头像网页
                Intent intent =new Intent(getActivity(), WebActivity.class);
                Bundle bundle=new Bundle();
                //传递name参数为name到下一层
                //bundle.putString("id",uuu.getUserId());
                //bundle.putString("name",uuu.getUserName());
                bundle.putString("url",toOpenUrl);
                bundle.putString("jid",uTitles);
                Log.i("4523543254获取到的name值为",uTitles);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }



    private class setAvatarTask extends AsyncTask<List<String>, Object, Short> {
        @Override
        protected Short doInBackground(List<String>... params) {
            try {
                if(params[0].get(0)!=null&&params[0].get(0).equals("延迟更新")){
                    Thread.sleep(2000);
                }
                myFileUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
                conn.setConnectTimeout(0);
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
            //获取完成后发送
            //EventBus.getDefault().postSticky(myInfo);
            return 1;
        }

        @Override
        protected void onPostExecute(Short state) {
            if(state==1){
                Log.i("更新函数执行","ohohoho");
                imageView.setImageBitmap(createCircleBitmap(bitmap));
            }
        }

    }

    private Bitmap createCircleBitmap(Bitmap resource)
    {
        //获取图片的宽度
        int width = resource.getWidth();
        Paint paint = new Paint();
        //设置抗锯齿
        paint.setAntiAlias(true);

        //创建一个与原bitmap一样宽度的正方形bitmap
        Bitmap circleBitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        //以该bitmap为低创建一块画布
        Canvas canvas = new Canvas(circleBitmap);
        //以（width/2, width/2）为圆心，width/2为半径画一个圆
        canvas.drawCircle(width/2, width/2, width/2, paint);

        //设置画笔为取交集模式
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //裁剪图片
        canvas.drawBitmap(resource, 0, 0, paint);

        return circleBitmap;
    }

    @Override
    public void onDestroy() {
        //处理内存
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
