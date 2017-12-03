package com.test.hhchat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.test.hhchat.Constants;
import com.test.hhchat.HHActivity;
import com.test.hhchat.HHClient;
import com.test.hhchat.R;
import com.test.hhchat.dialog.RegisterActivity;
import com.test.hhchat.dialog.ServerPrefActivity;
import com.test.hhchat.util.SharedPrefUtil;
import com.test.hhchat.util.ThreadUtil;

import org.litepal.LitePal;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2017/11/22 0022.
 */

public class LoginActivity extends HHActivity {

    private EditText edtPhone;
    private EditText edtPassword;
    private Button btnLogin;
    private TextView tvHost;
    private TextView tvPort;
    private TextView tvForgetPassword;
    private TextView tvReg;
    private CircleImageView civSetServer;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViews();
        initViews();
        setViewListeners();
        client = HHClient.getInstance();
        client.addUiListener(myUiListener);
        LitePal.getDatabase();
    }

    @Override
    protected void onStop() {
        super.onStop();
        client.removeUiListener(myUiListener);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        client.addUiListener(myUiListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case Constants.RQCD_SERVERPREF:
                if (resultCode == RESULT_OK) {
                    String newHost = intent.getStringExtra(Constants.INTNT_HOST);
                    String newPort = intent.getStringExtra(Constants.INTN_PORT);
                    if (!TextUtils.isEmpty(newHost)) {
                        tvHost.setText(newHost);
                        SharedPrefUtil.putValue(LoginActivity.this, Constants.SP_HOST, newHost);
                    }
                    if (!TextUtils.isEmpty(newPort)) {
                        tvPort.setText(newPort);
                        SharedPrefUtil.putValue(LoginActivity.this, Constants.SP_PORT, newPort);
                    }
                }
                break;
            case Constants.RQCD_REGISTER:
                if (resultCode == RESULT_OK) {
                    String phone = intent.getStringExtra(Constants.ITNT_MY_PHONE);
                    edtPhone.setText(phone);
                    edtPassword.setText("");
                }
                break;
        }
    }

    private void findViews() {
        edtPhone = (EditText) findViewById(R.id.edt_phone);
        edtPassword = (EditText) findViewById(R.id.edt_password);
        btnLogin = (Button) findViewById(R.id.btn_login);
        tvHost = (TextView) findViewById(R.id.tv_host);
        tvPort = (TextView) findViewById(R.id.tv_port);
        tvForgetPassword = (TextView) findViewById(R.id.tv_forget_password);
        tvReg = (TextView) findViewById(R.id.tv_register);
        civSetServer = (CircleImageView) findViewById(R.id.civ_set_server);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    private void initViews() {
        String phone = SharedPrefUtil.getValue(this, Constants.SP_PHONE);
        String password = SharedPrefUtil.getValue(this, Constants.SP_PASSWORD);
        edtPhone.setText(phone);
        edtPassword.setText(password);
        if (TextUtils.isEmpty(edtPhone.getText())) {
            btnLogin.setEnabled(false);
        }
        String host = SharedPrefUtil.getValue(this, Constants.SP_HOST);
        String port = SharedPrefUtil.getValue(this, Constants.SP_PORT);
        tvHost.setText(host);
        tvPort.setText(port);
    }

    @Override
    protected void initUiListener() {
        myUiListener = new LoginUiListener();
    }

    private void setViewListeners() {
        btnLogin.setOnClickListener(myBtnClickListener);
        civSetServer.setOnClickListener(myBtnClickListener);
        tvForgetPassword.setOnClickListener(myBtnClickListener);
        tvReg.setOnClickListener(myBtnClickListener);
        edtPhone.addTextChangedListener(myTextWatcher);
    }

    private View.OnClickListener myBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_login:
                    login();
                    break;
                case R.id.tv_forget_password:
                    forgetPassword();
                    break;
                case R.id.tv_register:
                    register();
                    break;
                case R.id.civ_set_server:
                    setServerPref();
                    break;
            }
        }
    };

    private TextWatcher myTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (TextUtils.isEmpty(edtPhone.getText())) {
                btnLogin.setEnabled(false);
            } else {
                btnLogin.setEnabled(true);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {}
    };

    private void login() {
        String host = tvHost.getText().toString();
        String port = tvPort.getText().toString();
        if (TextUtils.isEmpty(host)) {
            Toast.makeText(this, "请输入服务器地址", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(port)) {
            Toast.makeText(this, "请输入端口号", Toast.LENGTH_SHORT).show();
            return;
        }
        int portNo;
        try {
            portNo = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "端口号格式不正确", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }
        String phone = edtPhone.getText().toString();
        String password = edtPassword.getText().toString();
        client.setServerHost(host);
        client.setServerPort(portNo);
        client.login(phone, password);
        btnLogin.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void register() {
        String host = tvHost.getText().toString();
        String port = tvPort.getText().toString();
        if (TextUtils.isEmpty(host)) {
            Toast.makeText(this, "请输入服务器地址", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(port)) {
            Toast.makeText(this, "请输入端口号", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int portNo = Integer.parseInt(port);
            client.setServerHost(host);
            client.setServerPort(portNo);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "端口号格式不正确", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        Intent intent = new Intent(this, RegisterActivity.class);
        startActivityForResult(intent, Constants.RQCD_REGISTER);
    }

    private void setServerPref() {
        Intent intent = new Intent(LoginActivity.this, ServerPrefActivity.class);
        String oldHost = tvHost.getText().toString();
        String oldPort = tvPort.getText().toString();
        intent.putExtra(Constants.INTNT_HOST, oldHost);
        intent.putExtra(Constants.INTN_PORT, oldPort);
        startActivityForResult(intent, Constants.RQCD_SERVERPREF);
    }

    private void forgetPassword() {

    }



    private class LoginUiListener extends HHUiListener {

        @Override
        public void onLoginSucceed(final String myNickName) {
            ThreadUtil.runInUiThread(new Runnable() {
                @Override
                public void run() {
                    btnLogin.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    toastString("登录成功");
                    String phone = edtPhone.getText().toString();
                    SharedPrefUtil.putValue(LoginActivity.this, "phone", phone);

                    client.removeUiListener(myUiListener);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra(Constants.ITNT_MY_PHONE, phone);
                    intent.putExtra(Constants.ITNT_MY_NICKNAME, myNickName);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_bottom, android.R.anim.fade_out);
                }
            });

        }

        @Override
        public void onLoginFail(final String detail) {
            ThreadUtil.runInUiThread(new Runnable() {
                @Override
                public void run() {
                    btnLogin.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    toastString(detail);
                }
            });
        }
    }
}
