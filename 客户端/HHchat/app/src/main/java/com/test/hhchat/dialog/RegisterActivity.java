package com.test.hhchat.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.test.hhchat.Constants;
import com.test.hhchat.HHActivity;
import com.test.hhchat.R;
import com.test.hhchat.util.ThreadUtil;

/**
 * Created by Administrator on 2017/11/22 0022.
 */

public class RegisterActivity extends HHActivity {

    private EditText edtPhone;
    private EditText edtNickname;
    private EditText edtPassword;
    private EditText edtAckPassword;
    private Button btnReg;
    private Button btnCancel;
    private ProgressBar progressBar;

    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setWidthAndHeigth();
        findViews();
        initViews();
        setViewListeners();
        setFinishOnTouchOutside(false);
    }

    private void findViews() {
        edtPhone = (EditText) findViewById(R.id.edt_phone);
        edtNickname = (EditText) findViewById(R.id.edt_nickname);
        edtPassword = (EditText) findViewById(R.id.edt_password);
        edtAckPassword = (EditText) findViewById(R.id.edt_ackpassword);
        btnReg = (Button) findViewById(R.id.btn_reg);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    private void initViews() {
        progressBar.bringToFront();
    }

    @Override
    protected void initUiListener() {
        myUiListener = new RegisterUiListener();
    }

    private void setViewListeners() {
        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone = edtPhone.getText().toString();
                String password = edtPassword.getText().toString();
                String ackPassword = edtAckPassword.getText().toString();
                String nickname = edtNickname.getText().toString();

                if (TextUtils.isEmpty(phone)) {
                    Toast.makeText(RegisterActivity.this, "手机号不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                int len;
                len = TextUtils.getTrimmedLength(phone);
                if (len < 3 || len > 11) {
                    Toast.makeText(RegisterActivity.this, "手机号长度应为3-11个数字", Toast.LENGTH_SHORT).show();
                    return;
                }
                len = TextUtils.getTrimmedLength(password);
                if (len < 4 || len > 16) {
                    Toast.makeText(RegisterActivity.this, "密码长度应为4-16个字符", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(ackPassword)) {
                    Toast.makeText(RegisterActivity.this, "两次输入密码不一致", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(nickname)) {
                    Toast.makeText(RegisterActivity.this, "昵称不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                btnReg.setEnabled(false);
                client.register(phone, password, nickname);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
    }

    private void setWidthAndHeigth() {
        WindowManager m = getWindowManager();
        android.view.WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);
    }

    private class RegisterUiListener extends HHUiListener {

        @Override
        public void onRegSucceed() {
            ThreadUtil.runInUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                    btnReg.setEnabled(true);
                    toastString("注册成功");
                    Intent intent = new Intent();
                    intent.putExtra(Constants.ITNT_MY_PHONE, phone);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        }

        @Override
        public void onRegFail(final String detail) {
            ThreadUtil.runInUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                    btnReg.setEnabled(true);
                    toastString(detail);
                }
            });
        }
    }

}
