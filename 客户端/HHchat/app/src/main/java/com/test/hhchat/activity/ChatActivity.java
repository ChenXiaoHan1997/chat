package com.test.hhchat.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.test.hhchat.Constants;
import com.test.hhchat.HHActivity;
import com.test.hhchat.R;
import com.test.hhchat.adapter.MessageAdapter;
import com.test.hhchat.model.HHMessage;
import com.test.hhchat.model.HHUser;
import com.test.hhchat.util.ThreadUtil;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/22 0022.
 */

// TODO 收到其他联系人消息 加载聊天记录 saveUser
public class ChatActivity extends HHActivity {

    private String myPhone;
    private String myNickname;
    private String otherPhone;
    private String otherNickname;

    private List<HHMessage> hhMessages = new ArrayList<>();
    private MessageAdapter adapter;

    private Toolbar toolbar;
    private TextView tvTitle;
    private EditText edtContent;
    private Button btnSend;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        myPhone = getIntent().getStringExtra(Constants.ITNT_MY_PHONE);
        myNickname = getIntent().getStringExtra(Constants.ITNT_MY_NICKNAME);
        otherPhone = getIntent().getStringExtra(Constants.ITNT_OTHER_PHONE);
        otherNickname = getIntent().getStringExtra(Constants.ITNT_OTHER_NICKNAME);

        saveUser();
        loadChatLog();
        findViews();
        initViews();
        setViewListeners();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (toolbar != null) {
            toolbar.setTitle("");
        }
    }

    private void findViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        edtContent = (EditText) findViewById(R.id.edt_content);
        btnSend = (Button) findViewById(R.id.btn_send);
        recyclerView = (RecyclerView) findViewById(R.id.rv_message);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refrest);
    }

    private void initViews() {
        tvTitle.setText(otherNickname);
        setSupportActionBar(toolbar);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MessageAdapter(hhMessages, myPhone, myNickname, otherPhone, otherNickname);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if (adapter.getItemCount() - 1 > 0) {
            recyclerView.scrollToPosition(adapter.getItemCount()-1);
        }

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
    }

    @Override
    protected void initUiListener() {
        myUiListener = new ChatUiListener();
    }


    private void setViewListeners() {
        btnSend.setOnClickListener(myBtnClickListener);
        edtContent.addTextChangedListener(myTextWatcher);
        swipeRefreshLayout.setOnRefreshListener(myRefreshListener);
    }

    private View.OnClickListener myBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_send:
                    client.sendMessage(otherPhone, edtContent.getText().toString());
                    break;
            }
        }
    };

    private TextWatcher myTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (TextUtils.isEmpty(edtContent.getText())) {
                btnSend.setEnabled(false);
            } else {
                btnSend.setEnabled(true);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {}
    };

    private SwipeRefreshLayout.OnRefreshListener myRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            ThreadUtil.runInSubThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ThreadUtil.runInUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            });
        }
    };

    private void saveUser() {
        if (DataSupport.where("phone = ?", otherPhone).find(HHUser.class).size() == 0) {
            // 没有保存过该用户
            HHUser newUser = new HHUser(otherPhone, otherNickname, false, 0);
            newUser.save();
        }
    }

    private void loadChatLog() {
        hhMessages = DataSupport.where("fromPhone = ? and targetPhone = ? or fromPhone = ? and targetPhone = ?", myPhone, otherPhone, otherPhone, myPhone).order("time").find(HHMessage.class);
    }

    private class ChatUiListener extends HHUiListener {

        @Override
        public void onSendMessageSucceed(final HHMessage hhMessage) {
            ThreadUtil.runInUiThread(new Runnable() {
                @Override
                public void run() {
                    edtContent.setText("");
                    hhMessages.add(hhMessage);
                    adapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                    hhMessage.save();
                }
            });
        }

        @Override
        public void onSendMessageFail() {
            ThreadUtil.runInUiThread(new Runnable() {
                @Override
                public void run() {
                    toastString("发送失败");
                }
            });
        }

        @Override
        public void onReceiveMessage(final HHMessage hhMessage) {
            ThreadUtil.runInUiThread(new Runnable() {
                @Override
                public void run() {
                    if (hhMessage.getFromPhone().equals(otherPhone)) {
                        // 当前聊天页面的消息
                        hhMessages.add(hhMessage);
                        adapter.notifyDataSetChanged();
                    } else {
                        // 其他联系人的消息
                        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        Notification notification = new NotificationCompat.Builder(ChatActivity.this)
                                .setTicker(hhMessage.getFromPhone() + ": " + hhMessage.getContent())
                                .setSmallIcon(R.drawable.hh_chat)
                                .setContentTitle(hhMessage.getFromPhone())
                                .setContentText(hhMessage.getContent())
                                .setWhen(System.currentTimeMillis())
                                .setAutoCancel(true)
                                .setOngoing(false)
                                .setVibrate(new long[] {0, 250, 100, 100})
                                .setContentIntent(PendingIntent.getActivity(ChatActivity.this, 0, new Intent(), 0))
                                .build();
                        manager.notify(1, notification);
                    }
                }
            });
        }
    }

}
