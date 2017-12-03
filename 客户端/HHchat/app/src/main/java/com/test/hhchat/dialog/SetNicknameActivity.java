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

import org.w3c.dom.Text;

/**
 * Created by Administrator on 2017/11/28 0028.
 */

public class SetNicknameActivity extends HHActivity {

    private EditText edtNewname;
    private Button btnOk;
    private Button btnCancel;
    private ProgressBar progressBar;

    private String newNickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setnickname);
        setWidthAndHeigth();
        findViews();
        initViews();
        setViewListeners();
        setFinishOnTouchOutside(false);
    }

    private void findViews() {
        edtNewname = (EditText) findViewById(R.id.edt_newname);
        btnOk = (Button) findViewById(R.id.btn_ok);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    private void initViews() {
        Intent intent = getIntent();
        edtNewname.setText(intent.getStringExtra(Constants.ITNT_MY_NICKNAME));
        progressBar.bringToFront();
    }

    @Override
    protected void initUiListener() {
        myUiListener = new SetNicknameUiListener();
    }

    private void setViewListeners() {
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newNickname = edtNewname.getText().toString();
                if (TextUtils.isEmpty(newNickname)) {
                    Toast.makeText(SetNicknameActivity.this, "昵称不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                btnOk.setEnabled(false);
                client.setNickname(newNickname);
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

    private class SetNicknameUiListener extends HHUiListener {
        @Override
        public void onSetNicknameSucceed() {
            ThreadUtil.runInUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                    btnOk.setEnabled(true);
                    toastString("修改成功");
                    Intent intent = new Intent();
                    intent.putExtra(Constants.ITNT_MY_NICKNAME, newNickname);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        }

        @Override
        public void onSetNicknameFail() {
            ThreadUtil.runInUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                    btnOk.setEnabled(true);
                    toastString("修改失败");
                }
            });
        }
    }
}
