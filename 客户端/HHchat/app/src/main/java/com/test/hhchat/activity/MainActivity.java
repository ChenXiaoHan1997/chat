package com.test.hhchat.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.test.hhchat.Constants;
import com.test.hhchat.HHActivity;
import com.test.hhchat.R;
import com.test.hhchat.adapter.UserAdapter;
import com.test.hhchat.dialog.SetNicknameActivity;
import com.test.hhchat.model.HHMessage;
import com.test.hhchat.model.HHUser;
import com.test.hhchat.util.ThreadUtil;

import org.litepal.crud.DataSupport;

import java.util.List;

//TODO onReceiveMessage, 用户列表
// TODO 为什么HeaderLayout的字不能显示
public class MainActivity extends HHActivity {

    private String myPhone;
    private String myNickname;

    private List<HHUser> hhUsers;
    private UserAdapter adapter;

    private RecyclerView recyclerView;
    private DrawerLayout drawerLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NavigationView navigationView;
    private TextView tvPhone;
    private TextView tvNickname;

    private long exitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadUsers();
        findViews();
        initViews();
        setViewListeners();
        client.requestUserlist();
        myPhone = getIntent().getStringExtra(Constants.ITNT_MY_PHONE);
        myNickname = getIntent().getStringExtra(Constants.ITNT_MY_NICKNAME);
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
        client.requestUserlist();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case Constants.RQCD_SETNICKNAME:
                if (resultCode == RESULT_OK) {
                    String newNickname = intent.getStringExtra(Constants.ITNT_MY_NICKNAME);
                    if (!TextUtils.isEmpty(newNickname)) {
                        myNickname = newNickname;
                        client.requestUserlist();
                    }
                }
                drawerLayout.closeDrawers();
                break;
        }
    }

    public void startChat(HHUser targetUser) {
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.putExtra(Constants.ITNT_MY_PHONE, myPhone);
        intent.putExtra(Constants.ITNT_MY_NICKNAME, myNickname);
        intent.putExtra(Constants.ITNT_OTHER_PHONE, targetUser.getPhone());
        intent.putExtra(Constants.ITNT_OTHER_NICKNAME, targetUser.getNickname());
        startActivity(intent);
    }

    private void findViews() {
        recyclerView = (RecyclerView) findViewById(R.id.rv_user);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refrest);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);
        tvPhone = (TextView) headerView.findViewById(R.id.tv_phone);
        tvNickname = (TextView) headerView.findViewById(R.id.tv_nickname);
    }

    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.menu);
        }

        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        adapter = new UserAdapter(hhUsers);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        tvPhone.setText(myPhone);
        tvNickname.setText(myNickname);

    }

    @Override
    protected void initUiListener() {
        myUiListener = new MainUiListener();
    }

    private void setViewListeners() {
        navigationView.setNavigationItemSelectedListener(myNavigationItemListener);
        swipeRefreshLayout.setOnRefreshListener(myRefreshListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
        }
        return true;
    }

    private NavigationView.OnNavigationItemSelectedListener myNavigationItemListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_chatlog:
                    break;
                case R.id.nav_favorites:
                    break;
                case R.id.nav_set_nickname:
                    setNickname();
                    break;
                case R.id.nav_set_password:
                    break;
                case R.id.nav_check_update:
                    checkUpdate();
                    break;
            }
            return true;
        }
    };

    private SwipeRefreshLayout.OnRefreshListener myRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            client.requestUserlist();
            ThreadUtil.runInSubThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(Constants.TIME_MAX_WAIT_USERS);
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

    private void setNickname() {
        Intent intent = new Intent(MainActivity.this, SetNicknameActivity.class);
        intent.putExtra(Constants.ITNT_MY_NICKNAME, myNickname);
        startActivityForResult(intent, Constants.RQCD_SETNICKNAME);
    }

    private void checkUpdate() {
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ThreadUtil.runInUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "已经是最新版本", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loadUsers() {
        hhUsers = DataSupport.findAll(HHUser.class);
        for (HHUser hhUser : hhUsers) {
            hhUser.setOnline(false);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawers();
            } else if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, "再按一次退出当前账号", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                client.logout();
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class MainUiListener extends HHUiListener {

        @Override
        public void onReceiveMessage(HHMessage hhMessage) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification notification = new NotificationCompat.Builder(MainActivity.this)
                    .setTicker(hhMessage.getFromPhone() + ": " + hhMessage.getContent())
                    .setSmallIcon(R.drawable.hh_chat)
                    .setContentTitle(hhMessage.getFromPhone())
                    .setContentText(hhMessage.getContent())
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setVibrate(new long[] {0, 250, 100, 100})
                    .setContentIntent(PendingIntent.getActivity(MainActivity.this, 0, new Intent(), 0))
                    .build();
            manager.notify(1, notification);
        }

        @Override
        public void onReceiveUserlist(final List<HHUser> onlineUsers) {
            ThreadUtil.runInUiThread(new Runnable() {
                @Override
                public void run() {
                    for (HHUser hhUser : hhUsers) {
                        hhUser.setOnline(false);    // 先把列表中的用户全部置为离线
                    }
                    for (HHUser onlineUser : onlineUsers) {
                        boolean found = false;
                        for (HHUser hhUser : hhUsers) {
                            if (hhUser.getPhone().equals(onlineUser.getPhone())) {
                                found = true;
                                hhUser.setOnline(true); // 已经在列表中的用户，置为在线
                                hhUser.setNickname(onlineUser.getNickname());   // 重新设置昵称
                                break;
                            }
                        }
                        if (!found) {
                            hhUsers.add(onlineUser);    // 不在列表中的用户，添加到列表
                        }
                    }
                    swipeRefreshLayout.setRefreshing(false);
                    adapter.notifyDataSetChanged();

                }
            });
        }
    }
}
