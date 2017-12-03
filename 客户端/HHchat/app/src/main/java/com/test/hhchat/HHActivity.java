package com.test.hhchat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.test.hhchat.model.HHMessage;
import com.test.hhchat.model.HHUser;

import java.util.List;

/**
 * Created by Administrator on 2017/11/25 0025.
 */

public abstract class HHActivity extends AppCompatActivity {

    protected HHClient client;
    protected HHUiListener myUiListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (client == null) {
            client = HHClient.getInstance();
        }
        initUiListener();
        client.addUiListener(myUiListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.removeUiListener(myUiListener);
    }

    protected abstract void initUiListener();

    protected abstract class HHUiListener implements HHClient.UiListener {

        protected void toastString(String strDisp) {
            Toast.makeText(HHActivity.this, strDisp, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLoginSucceed(String nickname) {

        }

        @Override
        public void onLoginFail(String detail) {

        }

        @Override
        public void onLogout() {

        }

        @Override
        public void onRegSucceed() {

        }

        @Override
        public void onRegFail(String detail) {

        }

        @Override
        public void onReceiveUserlist(List<HHUser> hhUsers) {

        }

        @Override
        public void onReceiveMessage(HHMessage hhMessage) {

        }

        @Override
        public void onSendMessageSucceed(HHMessage hhMessage) {

        }

        @Override
        public void onSendMessageFail() {

        }

        @Override
        public void onSetNicknameSucceed() {

        }

        @Override
        public void onSetNicknameFail() {

        }
    }
}
