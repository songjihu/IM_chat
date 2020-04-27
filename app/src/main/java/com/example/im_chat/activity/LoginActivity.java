package com.example.im_chat.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.im_chat.db.DaoMaster;
import com.example.im_chat.db.DaoSession;
import com.example.im_chat.entity.ChatMessage;
import com.example.im_chat.entity.MyInfo;
import com.example.im_chat.entity.UserInfo;
import com.example.im_chat.other.JID;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smackx.offline.OfflineMessageManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.os.Build.TIME;

import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.im_chat.R;
import com.example.im_chat.utils.JDBCUtils;
import com.example.im_chat.utils.MyXMPPTCPConnectionOffLine;
import com.example.im_chat.utils.MyXMPPTCPConnectionOnLine;


import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LoginActivity extends Activity implements LoaderCallbacks<Cursor> , ConnectionListener, RosterListener {
    ImageView imageView;
    TextView textView;
    Calendar calendar = Calendar.getInstance();//获取时间，更替背景图片
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    public String UserName=new String();
    public static int getRequestReadContacts() {
        return REQUEST_READ_CONTACTS;
    }
    private MyXMPPTCPConnectionOnLine connection_on;//在线聊天服务连接
    private MyXMPPTCPConnectionOffLine connection_off;//离线聊天服务连接
    private Roster roster;
    private Boolean isLogin = false;
    private String name;
    private String id;
    private PrintWriter printWriter = null;
    final UserInfo uuu = new UserInfo();
    private List<String> loginList = new ArrayList<String>();

    private MyInfo myInfo;



    private void initXMPPTCPConnection_offline(String u,String p){
        connection_off = MyXMPPTCPConnectionOffLine.getInstance();
        connection_off.addConnectionListener(this);
        connection_off.disconnect();
        connection_off.instantShutdown();
        if(!connection_off.isConnected()){
            try {
                connection_off.connect();
                connection_off.login(u,p);
            } catch (SmackException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
    }

    private void initXMPPTCPConnection_online(){
        connection_on = MyXMPPTCPConnectionOnLine.getInstance();
        connection_on.addConnectionListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置为全屏显示，没有日期栏
        setContentView(R.layout.activity_login);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        //创建登录按钮及点击事件
        Button mEmailSignInButton = (Button) findViewById(R.id.sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        //创建注册按钮及点击事件
        Button mEmailSignUpButton = (Button) findViewById(R.id.sign_up_button);
        mEmailSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignUp();
            }
        });
        initXMPPTCPConnection_online();
        //按照时间切换背景
        if (hour>=5&&hour<=12) {
            imageView.setImageResource(R.drawable.good_morning_img);
            textView.setText("Morning");
        } else {
            if (hour>12&&hour<=17) {
                imageView.setImageResource(R.drawable.good_morning_img);
                textView.setText("Afternoon");
            } else {
                imageView.setImageResource(R.drawable.good_night_img);
                textView.setText("Night");
            }
        }
    }




    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        //Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        //String email = "20162430710";
        //String password = "123456";

        boolean cancel = false;
        View focusView = null;


        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            //连接聊天服务器

            //loginList.add(et_account.getText().toString());
            //loginList.add(et_password.getText().toString());
            email= JID.escapeNode(email);
            loginList.add(email);
            loginList.add(password);
            new loginTask().execute(loginList);

        }
    }

    /**
     * 获取用户昵称
     * @auther songjihu
     * @since 2020/2/1 10:18
     * @return 用户名
     */
    private class getnameTask extends AsyncTask<List<String>, Object, Short>{

        @Override
        protected Short doInBackground(List<String>... params) {
            uuu.setUserId(params[0].get(0));
            try {
                Connection cn= JDBCUtils.getConnection();
                //Class.forName("com.mysql.jdbc.Driver");
                //java.sql.Connection cn= DriverManager.getConnection("jdbc:mysql://123.56.163.211/im_chat?useUnicode=true&characterEncoding=UTF-8","sjh","8859844007");
                String sql="SELECT * FROM `user` WHERE jid = '"+JID.unescapeNode(uuu.getUserId())+"'";
                Statement st=(Statement)cn.createStatement();
                ResultSet rs=st.executeQuery(sql);
                while(rs.next()){
                    uuu.setUserName(rs.getString("user_name"));
                }
                JDBCUtils.close(rs,st,cn);
                //rs.close();cn.close();st.close();
                return 1;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Short state) {
            switch (state){
                case 0:
                    Toast.makeText(getApplicationContext(), "获取失败", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(getApplicationContext(), "获取成功", Toast.LENGTH_SHORT).show();
                    //用Bundle携带数据
                    //新建一个显式意图，第一个参数为当前Activity类对象，第二个参数为你要打开的Activity类
                    Intent intent =new Intent(LoginActivity.this,MainActivity.class);
                    //用Bundle携带数据
                    Bundle bundle=new Bundle();
                    //传递name参数为name到下一层
                    //bundle.putString("id",uuu.getUserId());
                    //bundle.putString("name",uuu.getUserName());
                    bundle.putString("id",uuu.getUserId());
                    bundle.putString("name",uuu.getUserName());
                    //Log.i("4523543254获取到的name值为",uuu.getUserName());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                    break;
                default:
                    break;
            }
        }
    }


    //初始化数据库
    private static DaoSession daoSession;//配置数据库
    private void initGreenDao() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(LoginActivity.this, "aserbao.db");
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    private class loginTask extends AsyncTask<List<String>, Object, Short>{

        //此次连接登录服务器先离线，后在线
        @Override
        protected Short doInBackground(List<String>... params) {


            if(TextUtils.isEmpty(params[0].get(0))){
                return 0;
            }else if(TextUtils.isEmpty(params[0].get(1))){
                return 1;
            }
            //从数据库读取最新的聊天记录到本地数据库，本应用中用json格式当做消息体
            initGreenDao();
            //先处理离线消息
            //MyXMPPTCPConnectionOffLine.setUsername(JID.escapeNode("admin"));
            //MyXMPPTCPConnectionOffLine.setPassword("8859844007");
            //Log.i("34865289436785267",MyXMPPTCPConnectionOffLine.getUsername()+MyXMPPTCPConnectionOffLine.getPassword());
            initXMPPTCPConnection_offline(params[0].get(0),params[0].get(1));
            OfflineMessageManager offlineMessageManager=new OfflineMessageManager(connection_off);
            List<org.jivesoftware.smack.packet.Message> messages= null;
            try {
                messages =  offlineMessageManager.getMessages();
                Log.i("离线聊天数量:",messages.size()+"");
                for(int i=0;i<messages.size();i++)
                {
                    //将所有接收到的消息，加入到数据库
                    ChatMessage chat_msg =new ChatMessage(null,messages.get(i).getBody());
                    daoSession.insert(chat_msg);
                    Log.i("offline数据库加入++++++",messages.get(i).getBody());
                    //myInfo.setUserId(uuu.getUserId());
                    //myInfo.setUserName(uuu.getUserName());
                    //myInfo.setLatestJson((String) messages.get(i).getBody());//加入最新消息
                    //myInfo.setSendId("add_msg");
                    //EventBus.getDefault().postSticky(myInfo);
                }
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }

            //结束离线状态，进入在线状态
            try {
                if(connection_off.isConnected())
                {
                    connection_off.disconnect();
                    connection_off.instantShutdown();
                }
                if(!connection_on.isConnected())
                {
                    connection_on.connect();
                }
                connection_on.login(params[0].get(0),params[0].get(1));
                return 2;
            }catch (Exception e){
                    e.printStackTrace();
                    if(getException(e).contains("Client is already logged in")){
                        Looper.prepare();
                        Toast.makeText(getApplicationContext(), "请勿重复登录", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        return 2;
                    }
                    return 3;
                }
        }

        @Override
        protected void onPostExecute(Short state) {
            switch (state){
                case 0:
                    Toast.makeText(getApplicationContext(), "请输入用户名", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(getApplicationContext(), "请输入密码", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    //activity跳转到下一层
                    Toast.makeText(getApplicationContext(), "登录成功", Toast.LENGTH_SHORT).show();
                    new getnameTask().execute(loginList);
                    //startActivity(new Intent(LoginActivity.this, FriendsActivity.class));
                    break;
                case 3:
                    isLogin = false;
                    Toast.makeText(getApplicationContext(), "登录错误", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     *
     * @auther songjihu
     * @since 2020/2/1 10:08
     * @param e :
     * @return 异常内容字符串形式
     */
    public static String getException(Exception e) {
        Writer writer = null;
        PrintWriter printWriter = null;
        try {
            writer = new StringWriter();
            printWriter = new PrintWriter(writer);
            e.printStackTrace(printWriter);
            return writer.toString();
        } finally {
            try {
                if (writer != null)
                    writer.close();
                if (printWriter != null)
                    printWriter.close();
            } catch (IOException e1) { }
        }
    }

    //启用注册Activity
    private void attemptSignUp() {
        finish();
        Intent intent =new Intent(LoginActivity.this,EmailtestActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }

    //检测登录名是否有效
    private boolean isEmailValid(String email) {
        if (null==email || "".equals(email)) return false;
//        Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配
        Pattern p =  Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//复杂匹配
        Matcher m = p.matcher(email);
        return m.matches();
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }
        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    //返回2次退出
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
                connection_off.disconnect();
                connection_on.disconnect();
                finish();
            }
            //消费事件
            return true;
        }
        //不处理事件
        return false;
    }

    //ConnectionListener
    @Override
    public void connected(XMPPConnection connection) {

    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {

    }

    @Override
    public void connectionClosed() {

    }

    @Override
    public void connectionClosedOnError(Exception e) {

    }

    @Override
    public void reconnectionSuccessful() {

    }

    @Override
    public void reconnectingIn(int seconds) {

    }

    @Override
    public void reconnectionFailed(Exception e) {

    }

    //RosterListener
    @Override
    public void entriesAdded(Collection<String> addresses) {

    }

    @Override
    public void entriesUpdated(Collection<String> addresses) {

    }

    @Override
    public void entriesDeleted(Collection<String> addresses) {

    }

    @Override
    public void presenceChanged(Presence presence) {

    }



}




