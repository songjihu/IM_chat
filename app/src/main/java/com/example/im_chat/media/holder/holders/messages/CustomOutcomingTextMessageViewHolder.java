package com.example.im_chat.media.holder.holders.messages;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;
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

public class CustomOutcomingTextMessageViewHolder
        extends MessageHolders.OutcomingTextMessageViewHolder<Message> {

    private ImageView imageView;
    private TextView textView;
    private URL myFileUrl = null;
    private Bitmap bitmap = null;
    private String url;
    private String uTitles;


    public CustomOutcomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        imageView = itemView.findViewById(R.id.image);
        textView = itemView.findViewById(R.id.messageText);
    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);
        if(message.getText().contains("jpg")){
            List<String> inputList = new ArrayList<String>();
            inputList.add(message.getText());
            //new setAvatarTask().execute(inputList);//刷新一次
        }
        time.setText(message.getStatus() + " " + time.getText());
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
                //textView.setText("");
            }
        }

    }
}
