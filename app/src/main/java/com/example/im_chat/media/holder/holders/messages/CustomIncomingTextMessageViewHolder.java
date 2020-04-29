package com.example.im_chat.media.holder.holders.messages;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.im_chat.R;
import com.example.im_chat.media.data.model.Message;

import com.stfalcon.chatkit.messages.MessageHolders;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CustomIncomingTextMessageViewHolder
        extends MessageHolders.IncomingTextMessageViewHolder<Message> {

    //一个小点，表示是否在线
    private View onlineIndicator;
    private TextView userName;
    private ImageView imageView;
    private URL myFileUrl = null;
    private Bitmap bitmap = null;
    private String url;
    private String sourceUrl="http://192.168.1.109:8080/temp-rainy/user_avatar/";
    private String uTitles;


    //把itemView的传送数据的payload送给viewHolder
    public CustomIncomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        //在线状态为刚开始的设定
        onlineIndicator = itemView.findViewById(R.id.onlineIndicator);
        //userName = itemView.findViewById(R.id.messageName);
        imageView = itemView.findViewById(R.id.messageUserAvatar);
    }

    @Override
    public void onBind(Message message) {
        //绑定消息
        super.onBind(message);
        List<String> inputList = new ArrayList<String>();
        inputList.add(message.getUser().getId());
        //new setAvatarTask().execute(inputList);//刷新一次
        //获取发送消息的人是否在线并切换颜色
        boolean isOnline = message.getUser().isOnline();
        if (isOnline) {
            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_online);
        } else {
            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_offline);
        }

        //userName.setText(message.getUser().getName());
        //We can set click listener on view from payload
        //为来自payload的视图添加监听器
        final Payload payload = (Payload) this.payload;
        userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (payload != null && payload.avatarClickListener != null) {
                    payload.avatarClickListener.onAvatarClick();
                }
            }
        });
    }

    //嵌套下一个
    public static class Payload {
        public OnAvatarClickListener avatarClickListener;
    }

    //点击事件定义
    public interface OnAvatarClickListener {
        void onAvatarClick();
    }

    private class setAvatarTask extends AsyncTask<List<String>, Object, Short> {
        @Override
        protected Short doInBackground(List<String>... params) {
            try {
                url=sourceUrl+params[0].get(0)+".jpg";
                myFileUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
                conn.setConnectTimeout(0);
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                Thread.sleep(2000);
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
                Log.i("更新函数执行","ohohoho"+sourceUrl+params[0].get(0));
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

                imageView.setImageBitmap(bitmap);
            }
        }

    }
}
