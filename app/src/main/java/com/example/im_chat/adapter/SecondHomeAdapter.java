package com.example.im_chat.adapter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.im_chat.R;
import com.example.im_chat.entity.Friend;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *   联系人列表适配器
 * @auther songjihu
 * @since 2020/2/4 9:40
 */
public class SecondHomeAdapter extends BaseQuickAdapter<Friend, BaseViewHolder> {


    private String sourceUrl="http://192.168.1.109:8080/temp-rainy/user_avatar/";
    private URL myFileUrl = null;
    private Bitmap bitmap = null;
    private String url;
    private ImageView imageView;
    private String uTitles;


    public SecondHomeAdapter(List<Friend> mShowItems) {
        super(R.layout.act_friend, mShowItems);
    }

    @Override
    protected void convert(BaseViewHolder helper, Friend item) {
        int position = helper.getLayoutPosition();
        String name = item.getName();
        String state = item.getOnline();
        Bitmap bm=item.getAvatar();
        uTitles = item.getJid();
        imageView=helper.getView(R.id.dialogAvatar_f);

        List<String> inputList = new ArrayList<String>();
        inputList.add("直接更新");
        //new setAvatarTask().execute(inputList);//刷新一次
        ((ImageView) helper.getView(R.id.dialogAvatar_f)).setImageBitmap(bm);//好友头像
        ((TextView) helper.getView(R.id.dialogName_f)).setText(name);//姓名
        ((TextView) helper.getView(R.id.dialogLastMessage_f)).setText(state);//状态
        Resources resources = mContext.getResources();
        Drawable drawable_online = resources.getDrawable(R.drawable.bubble_online);
        Drawable drawable_offline = resources.getDrawable(R.drawable.bubble_offline);
        if(state.contains("在线")){
            //Bitmap bitmap1 = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.bubble_online);
            (helper.getView(R.id.dialogBubble_f)).setBackground(drawable_online);//设置上线图标
        }else {
            //Bitmap bitmap1 = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.bubble_offline);
            (helper.getView(R.id.dialogBubble_f)).setBackground(drawable_offline);//设置下线图标
        }
    }

    private class setAvatarTask extends AsyncTask<List<String>, Object, Short> {
        @Override
        protected Short doInBackground(List<String>... params) {
            try {
                if(params[0].get(0)!=null&&params[0].get(0).equals("延迟更新")){
                    Thread.sleep(2000);
                }
                url=sourceUrl+uTitles+".jpg";
                myFileUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
                conn.setConnectTimeout(0);
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                Thread.sleep(2000);
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

}
