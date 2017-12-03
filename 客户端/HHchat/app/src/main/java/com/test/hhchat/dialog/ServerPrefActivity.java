package com.test.hhchat.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.test.hhchat.Constants;
import com.test.hhchat.R;

/**
 * Created by Administrator on 2017/11/22 0022.
 */

public class ServerPrefActivity extends AppCompatActivity {

    private EditText edtHost;
    private EditText edtPort;
    private Button btnOk;
    private Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serverpref);
        setWidthAndHeigth();
        findViews();
        initViews();
        setViewListeners();
        setFinishOnTouchOutside(false);
    }

    private void findViews() {
        edtHost = (EditText) findViewById(R.id.edt_host);
        edtPort = (EditText) findViewById(R.id.edt_port);
        btnOk = (Button) findViewById(R.id.btn_ok);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
    }

    private void initViews() {
        Intent intent = getIntent();
        edtHost.setText(intent.getStringExtra(Constants.INTNT_HOST));
        edtPort.setText(intent.getStringExtra(Constants.INTN_PORT));
    }

    private void setViewListeners() {
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String host = edtHost.getText().toString();
                String port = edtPort.getText().toString();
                Intent intent = new Intent();
                intent.putExtra(Constants.INTNT_HOST, host);
                intent.putExtra(Constants.INTN_PORT, port);
                setResult(RESULT_OK, intent);
                finish();
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
}
