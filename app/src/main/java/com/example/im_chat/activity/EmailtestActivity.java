package com.example.im_chat.activity;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.im_chat.entity.UserInfo;
import com.example.im_chat.other.JID;
import com.example.im_chat.other.RandomNumber;
import com.example.im_chat.other.SendEmail;
import com.example.im_chat.R;
import com.example.im_chat.utils.MyXMPPTCPConnection;
import com.example.im_chat.utils.MyXMPPTCPConnectionOnLine;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smackx.iqregister.AccountManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static android.os.Build.TIME;


public class EmailtestActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, ConnectionListener, RosterListener {

    private EditText etInputEmail,etInputGetNum,etInputPassword,etInputPassword1;

    private long verificationCode=0;            //生成的验证码
    private String email;    //邮箱
    private int sendedflag=0; //是否已经发送过
    private MyCount mc;//倒计时
    private Button mEmailSignUpButton;
    private Button mEmailSignInButton;
    private MyXMPPTCPConnection connection;//聊天服务连接
    private MyXMPPTCPConnectionOnLine connection2;//聊天服务连接
    private Boolean isLogin = false;
    private Roster roster;
    private Calendar calendar = Calendar.getInstance();//获取时间，更替背景图片
    private int hour = calendar.get(Calendar.HOUR_OF_DAY);
    private ImageView imageView;
    private TextView textView;

    //注册时创建的连接
    private void initXMPPTCPConnection(){
        connection = MyXMPPTCPConnection.getInstance();
        connection.addConnectionListener(this);
        roster = Roster.getInstanceFor(connection);
        roster.addRosterListener(this);
    }
    //登录时创建的连接
    private void initXMPPTCPConnection2(){
        connection2 = MyXMPPTCPConnectionOnLine.getInstance();
        connection2.addConnectionListener(this);
        roster = Roster.getInstanceFor(connection2);
        roster.addRosterListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up2);
        imageView = findViewById(R.id.imageView2);
        textView = findViewById(R.id.textView2);

        init();
        initXMPPTCPConnection();

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

    private void init() {
        etInputEmail= (EditText) findViewById(R.id.etInputEmail);
        etInputGetNum= (EditText) findViewById(R.id.etInputGetNum);
        etInputPassword=(EditText) findViewById(R.id.etInputPassword);
        etInputPassword1=(EditText) findViewById(R.id.etInputPassword1);
        mEmailSignUpButton = (Button) findViewById(R.id.btGetNum);
        mEmailSignInButton = (Button) findViewById(R.id.btSubmit);
       /* mEmailSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                judgeVerificationCode();
            }
        });*/
    }


    //点击事件
    public void btClick(View view){
        switch (view.getId()){
            case R.id.btGetNum:
                email=etInputEmail.getText().toString();
                if(sendedflag==0) {
                    sendedflag=1;
                    mc = new MyCount(60000, 1000);//计时60秒，间隔1秒
                    mc.start();
                    sendVerificationCode(email);  //发送验证码
                }
                break;
            case R.id.btSubmit:
                //Toast.makeText(getApplicationContext(),"验证失败", Toast.LENGTH_LONG).show();
                judgeVerificationCode();   //判断输入的验证码是否正确
                break;
        }
    }
    //发送验证码
    private void sendVerificationCode(final String email) {
        try {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        RandomNumber rn = new RandomNumber();
                        verificationCode = rn.getRandomNumber(6);
                        SendEmail se = new SendEmail(email);
                        se.sendHtmlEmail(verificationCode);//发送html邮件
                        Looper.prepare();
                        Toast.makeText(getApplicationContext(),"验证码已发送",Toast.LENGTH_LONG).show();
                        Looper.loop();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //判断输入的验证码是否正确
    private void judgeVerificationCode() {
        String email = etInputEmail.getText().toString();
        String password = etInputPassword.getText().toString();
        String password1 = etInputPassword1.getText().toString();
        String testcode = etInputGetNum.getText().toString();
        //attemptSignUp(email,password);//执行注册(测试)

        //创建用户类
        final UserInfo uuu = new UserInfo();
        uuu.setUserId(email);
        uuu.setUserPwd(password);

        //判断是否满足注册条件
        if(!testcode.isEmpty()&&!password.isEmpty()&&!password1.isEmpty()&&!email.isEmpty()){
            if(testcode.equals(Integer.toString((int) verificationCode))){    //验证码和输入一致
                //Toast.makeText(EmailtestActivity.this,"验证成功",Toast.LENGTH_LONG).show();
                if(password.equals(password1)){
                    attemptSignUp(email,password);//执行注册
                    //Toast.makeText(getApplicationContext(),"注册成功",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(),"密码不一致请重试",Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(getApplicationContext(),"验证失败", Toast.LENGTH_LONG).show();
            }
        }

    }

    //注册函数
    private void attemptSignUp(String email,String password) {
        //连接聊天服务器
        List<String> loginList = new ArrayList<String>();
        //loginList.add(et_account.getText().toString());
        //loginList.add(et_password.getText().toString());
        //email = (String) org.jivesoftware.smack.util.StringUtils.escapeForXML(email);
        email= JID.escapeNode(email);
        loginList.add(email);
        loginList.add(password);
        new SignUpTask().execute(loginList);
        new loginTask().execute(loginList);
    }


    //注册任务函数
    private class SignUpTask extends AsyncTask<List<String>, Object, Short> {

        final CountDownLatch countDownLatch = new CountDownLatch(1);//注册完成后再登录
        //此次连接登录服务器为离线状态
        @Override
        protected Short doInBackground(List<String>... params) {
            if(connection != null){
                try{
                    //如果没有连接openfire服务器，则连接；若已连接openfire服务器则跳过。
                    if(!connection.isConnected()){
                        connection.connect();
                    }
                    AccountManager accountManager = AccountManager.getInstance(connection);//获取账户管理对象
                    accountManager.sensitiveOperationOverInsecureConnection(true);
                    accountManager.createAccount(params[0].get(0),params[0].get(1));//创建账号即注册
                    //Log.i("_=_+_+_+_+_+_+"+params[0].get(0), params[0].get(1));
                    return 1;//注册成功
                }catch (Exception e){
                    e.printStackTrace();
                    if(getException(e).contains("XMPPError: conflict - cancel")){
                        Looper.prepare();
                        Toast.makeText(getApplicationContext(), "已经注册", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        return 2;
                    }
                    //Log.i("3452435+_+_+_+_+_+"+params[0].get(0), params[0].get(1));
                    return 2;//注册失败
                }
            }
            Log.i("+++++++++++++"+params[0].get(0), params[0].get(1));
            return 2;
        }

        @Override
        protected void onPostExecute(Short state) {
            switch (state){
                case 1:
                    Toast.makeText(getApplicationContext(), "注册成功", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(), "注册失败", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }

        }
    }

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

    private class loginTask extends AsyncTask<List<String>, Object, Short> {

        //此次连接登录服务器为离线状态
        @Override
        protected Short doInBackground(List<String>... params) {
            initXMPPTCPConnection2();
            if (connection2 != null) {
                try {
                    //如果没有连接openfire服务器，则连接；若已连接openfire服务器则跳过。
                    connection2.disconnect();
                    connection2.connect();//先退出再登录，防止重复登录
                    if (TextUtils.isEmpty(params[0].get(0))) {
                        return 0;
                    } else if (TextUtils.isEmpty(params[0].get(1))) {
                        return 1;
                    } else {
                        if (isLogin) {
                            connection2.login(params[0].get(0), params[0].get(1));
                            return 2;
                        } else {
                            if (connection2.isConnected()) {
                                connection2.login(params[0].get(0), params[0].get(1));
                                Log.i("++++++++" + params[0].get(0), params[0].get(1));
                                return 2;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getException(e).contains("Client is already logged in")) {
                        Looper.prepare();
                        Toast.makeText(getApplicationContext(), "已登录", Toast.LENGTH_SHORT).show();
                        Looper.loop();

                        return 2;
                    }
                    return 3;
                }
            }
            Log.i("+++++++++++++" + params[0].get(0), params[0].get(1));
            return 3;
        }
    }
    //计时器
    class MyCount extends CountDownTimer {
        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        @Override
        public void onFinish() {
            mEmailSignUpButton.setText("     获取验证码     ");
            sendedflag=0;
        }
        @Override
        public void onTick(long millisUntilFinished) {
            mEmailSignUpButton.setText("     还剩 "+millisUntilFinished / 1000+" 秒     ");
            //Toast.makeText(getApplicationContext(), millisUntilFinished / 1000 + "", Toast.LENGTH_LONG).show();//toast有显示时间延迟
        }
    }

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
                connection.disconnect();
                finish();
            }
            //消费事件
            return true;
        }
        //不处理事件
        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

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
