package com.example.im_chat.media.holder.holders.messages;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.im_chat.R;
import com.example.im_chat.media.data.model.Message;
import com.stfalcon.chatkit.messages.MessageHolders;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by troy379 on 05.04.17.
 */
public class CustomIncomingImageMessageViewHolder
        extends MessageHolders.IncomingImageMessageViewHolder<Message> {

    private View onlineIndicator;
    private ImageView imageView;
    private URL myFileUrl = null;
    private Bitmap bitmap = null;
    private String url;
    private String sourceUrl="http://192.168.1.109:8080/temp-rainy/user_avatar/";
    private String uTitles;

    public CustomIncomingImageMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        onlineIndicator = itemView.findViewById(R.id.onlineIndicator);
        imageView = itemView.findViewById(R.id.messageUserAvatar);
    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);
        List<String> inputList = new ArrayList<String>();
        inputList.add(message.getUser().getId());
        //new setAvatarTask().execute(inputList);//刷新一次
        boolean isOnline = message.getUser().isOnline();
        if (isOnline) {
            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_online);
        } else {
            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_offline);
        }
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