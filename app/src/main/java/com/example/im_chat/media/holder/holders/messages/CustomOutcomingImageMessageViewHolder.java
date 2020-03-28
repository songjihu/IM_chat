package com.example.im_chat.media.holder.holders.messages;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
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
public class CustomOutcomingImageMessageViewHolder
        extends MessageHolders.OutcomingImageMessageViewHolder<Message> {

    private ImageView imageView;
    private URL myFileUrl = null;
    private Bitmap bitmap = null;
    private String url;
    private String uTitles;
    List<String> inputList = new ArrayList<String>();

    public CustomOutcomingImageMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        Log.i("初始化执行",""+inputList.size()+"次");
        //new setAvatarTask().execute(inputList);//刷新一次
    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);
        imageView = itemView.findViewById(R.id.image);
        inputList.add(message.getImageUrl());
        if(inputList.size()< 2){
            Log.i("执行",""+inputList.size()+"次");
            //new setAvatarTask().execute(inputList);//刷新一次
        }
        //imageLoader.loadImage(image, message.getImageUrl(), getPayloadForImageLoader(message));
        time.setText(message.getStatus() + " " + time.getText());
    }

    //Override this method to have ability to pass custom data in ImageLoader for loading image(not avatar).
    @Override
    protected Object getPayloadForImageLoader(Message message) {
        //For example you can pass size of placeholder before loading
        return new Pair<>(100, 100);
    }

    private class setAvatarTask extends AsyncTask<List<String>, Object, Short> {
        @Override
        protected Short doInBackground(List<String>... params) {
            try {
                Thread.sleep(2000);
                url=params[0].get(0);
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
                imageView.setImageBitmap(bitmap);
            }
        }

    }
}