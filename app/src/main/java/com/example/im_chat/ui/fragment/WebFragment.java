package com.example.im_chat.ui.fragment;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import com.example.im_chat.R;
import com.example.im_chat.entity.MyInfo;
import com.example.im_chat.entity.SendInfo;
import com.example.im_chat.media.holder.CustomHolderMessagesActivity;

import org.greenrobot.eventbus.EventBus;

public class WebFragment extends Fragment {

    private WebView webView;
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 2;
    private String user_name;
    private String user_id;
    private String friend_name;
    private String friend_id;
    //private String url="http://123.56.163.211:8080/";
    private String url="http://192.168.1.109:8080/";
    private String fileCatalog="temp-rainy/";//服务器目录


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        user_name= bundle.getString("fromName");
        user_id= bundle.getString("fromId");
        friend_name= bundle.getString("toName");
        friend_id= bundle.getString("toId");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web, null);
        initView(view);
        return view;
    }

    private void initView(View view) {
        webView = view.findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        WebSettings webSettings = webView.getSettings();
        webSettings.setAllowFileAccess(true);//设置启用或禁止访问文件数据
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true); //设置是否支持JavaScript
        webSettings.setBlockNetworkImage(false);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);

        webView.loadUrl(url+"file");

        webView.setWebChromeClient(new WebChromeClient(){

            // For 3.0+ Devices (Start)
            // onActivityResult attached before constructor
            protected void openFileChooser(ValueCallback uploadMsg, String acceptType)
            {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
            }


            // For Lollipop 5.0+ Devices
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams)
            {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }

                uploadMessage = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try
                {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e)
                {
                    uploadMessage = null;
                    Toast.makeText(getActivity(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }

            //For Android 4.1 only
            protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture)
            {
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            protected void openFileChooser(ValueCallback<Uri> uploadMsg)
            {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }

        });

        //在js中调用本地java方法
        webView.addJavascriptInterface(new JsInterface(getActivity()), "AndroidWebView");

    }

    private class JsInterface {
        private Context mContext;
        public JsInterface(Context context) {
            this.mContext = context;
        }

        //在js中调用window.AndroidWebView.showInfoFromJs(name)，便会触发此方法。
        @JavascriptInterface
        public void showInfoFromJs(final String name) {
            //确定文件可用后得到返回值
            //Toast.makeText(mContext, name, Toast.LENGTH_SHORT).show();
            webView.post(new Runnable(){
                @Override
                public void run(){
                    //String str = "dfjkgnosudf b";
                    //webView.loadUrl("javascript:showInfoFromJava('" +str+ "')");
                    String msg = user_id+":"+user_name+":"+friend_id+":"+friend_name+":"+name;//组合为一条发送
                    //调用js中的函数：showInfoFromJava(msg)
                    webView.loadUrl("javascript:showInfoFromJava('" + msg + "')");
                }
            });
            SendInfo sendInfo=new SendInfo();
            sendInfo.setUserId(user_id);
            sendInfo.setUserName(user_name);
            sendInfo.setFriendId(friend_id);
            sendInfo.setFriendName(friend_name);
            sendInfo.setMsg(url+fileCatalog+name);//发送图片位置
            EventBus.getDefault().postSticky(sendInfo);

        }
    }

    //在java中调用js代码
    public void sendInfoToJs(View view) {
        String msg = user_id+":"+user_name+":"+friend_id+":"+friend_name;//组合为一条发送
        //调用js中的函数：showInfoFromJava(msg)
        webView.loadUrl("javascript:showInfoFromJava('" + msg + "')");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if (requestCode == REQUEST_SELECT_FILE)
            {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        }
        else if (requestCode == FILECHOOSER_RESULTCODE)
        {
            if (null == mUploadMessage)
                return;
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = intent == null || resultCode != CustomHolderMessagesActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
        else
            Toast.makeText(getActivity(), "Failed to Upload Image", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onPause() {
        super.onPause();
    }



}

