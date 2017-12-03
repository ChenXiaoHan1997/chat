package com.test.hhchat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.test.hhchat.model.HHMessage;
import com.test.hhchat.model.HHUser;
import com.test.hhchat.util.ThreadUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/11/25 0025.
 */

public class HHClient {

    private static HHClient singleInstance;

    private List<UiListener> uiListeners = new ArrayList<>();
    private Thread udpListenerThread;
    private Thread heartbeatManagerThread;

    private DatagramSocket socket;
    private DatagramPacket packetRcv, packetSnd;

    private InetAddress serverAddress;
    private String serverHost;
    private int serverPort;

    private String myPhone;

    private boolean isLogin = false;

    private HHClient() {

    }

    public static HHClient getInstance() {
        if (singleInstance == null) {
            singleInstance = new HHClient();
        }
        return singleInstance;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void addUiListener(UiListener listener) {
        if (!uiListeners.contains(listener)) {
            uiListeners.add(listener);
        }
    }

    public void removeUiListener(UiListener listener) {
        uiListeners.remove(listener);
    }

    public void login(final String phone, final String password) {
        if (isLogin) {
            return;
        }
        myPhone = phone;
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                if (!connect()) {
                    for (UiListener uiListener : uiListeners) {
                        uiListener.onLoginFail("无法连接到服务器");
                    }
                    return;
                }
                String strSnd = Constants.STR_LOGIN + "|" + phone + "|" + password;
                sendString(strSnd);

                try {
                    socket.setSoTimeout(1000);
                } catch (SocketException e) {
                    for (UiListener uiListener : uiListeners) {
                        uiListener.onLoginFail("无法连接到服务器");
                    }
                    e.printStackTrace();
                    return;
                }
                packetRcv = new DatagramPacket(new byte[1024], 1024);
                for (int i = 1; i <= Constants.TIMES_MAX_WAIT_ACK; i++) {
                    try {
                        socket.receive(packetRcv);
                        String strRcv = new String(packetRcv.getData(), packetRcv.getOffset(), packetRcv.getLength());
                        if (handleLoginString(strRcv)) {
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // 一定时间后仍未收到正确的响应
                for (UiListener listener : uiListeners) {
                    listener.onLoginFail("服务器未响应");
                }
            }
        });
    }

    public void logout() {
        if (!isLogin) {
            return;
        }
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                String strSnd = Constants.STR_LOGOUT + "|" + myPhone;
                sendString(strSnd);
                isLogin = false;
                for (UiListener listener : uiListeners) {
                    listener.onLogout();
                }
            }
        });
    }

    public void register(final String phone, final String password, final String nickname) {
        if (isLogin) {
            return;
        }
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                if (!connect()) {
                    return;
                }
                String strSnd = Constants.STR_REGISTER + "|" + phone + "|" + password + "|" + nickname;
                sendString(strSnd);

                try {
                    socket.setSoTimeout(1000);
                } catch (SocketException e) {
                    e.printStackTrace();
                    return;
                }
                packetRcv = new DatagramPacket(new byte[1024], 1024);
                for (int i = 1; i <= Constants.TIMES_MAX_WAIT_ACK; i++) {
                    try {
                        socket.receive(packetRcv);
                        String strRcv = new String(packetRcv.getData(), packetRcv.getOffset(), packetRcv.getLength());
                        if (handleRegString(strRcv)) {
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // 一定时间后仍未收到正确的响应
                for (UiListener listener : uiListeners) {
                    listener.onRegFail("服务器未响应");
                }
            }
        });
    }

    public void setNickname(final String newNickname) {
        if (!isLogin) {
            return;
        }
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                String strSnd = Constants.STR_SET_NICKNAME + "|" + myPhone + "|" + newNickname;
                sendString(strSnd);
/*
                try {
                    socket.setSoTimeout(1000);
                } catch (SocketException e) {
                    e.printStackTrace();
                    return;
                }

                packetRcv = new DatagramPacket(new byte[1024], 1024);
                for (int i = 1; i <= Constants.TIMES_MAX_WAIT_ACK; i++) {
                    try {
                        socket.receive(packetRcv);
                        String strRcv = new String(packetRcv.getData(), packetRcv.getOffset(), packetRcv.getLength());
                        Log.d("tag1", "strRcv=" + strRcv);////////////////
                        if (handleSetNicknameString(strRcv)) {
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // 一定时间后仍未收到正确的响应
                for (UiListener listener : uiListeners) {
                    listener.onSetNicknameFail("服务器未响应");
                }
                */
            }
        });
    }


    public void sendMessage(final String targetPhone, final String content) {
        if (!isLogin) {
            return;
        }
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                Date timeSnd = new Date(System.currentTimeMillis());
                String strSnd = Constants.STR_MESSAGE + "|" + myPhone + "|" + targetPhone + "|" + content + "|" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timeSnd);
                HHMessage hhMessage = new HHMessage(myPhone, targetPhone, content, timeSnd);
                if (sendString(strSnd)) {
                    for (UiListener listener : uiListeners) {
                        listener.onSendMessageSucceed(hhMessage);
                    }
                } else {
                    for (UiListener listener : uiListeners) {
                        listener.onSendMessageFail();
                    }
                }
            }
        });
    }

    public void requestUserlist() {
        if (!isLogin) {
            return;
        }
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                String strSnd = Constants.STR_USERS + "|" + myPhone;
                sendString(strSnd);
            }
        });
    }

    private void sendHeartbeat() {
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                String strSnd = Constants.STR_HEARTBEAT + "|" + myPhone;
                sendString(strSnd);
            }
        });
    }

    private boolean sendString(String strSnd) {
        // 子线程中调用
        packetSnd = new DatagramPacket(strSnd.getBytes(), strSnd.getBytes().length, serverAddress, serverPort);
        try {
            socket.send(packetSnd);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean connect() {
        // 初始化serverAddress和socket
        try {
            serverAddress = InetAddress.getByName(serverHost);
            if (socket == null) {
                socket = new DatagramSocket();
            }
            return true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        } catch (SocketException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void handleString(String strRcv) {
        // 子线程中调用
        String[] ss = strRcv.split("\\|", -1);
        if (ss.length >= 1) {
            switch(ss[0]) {
                case Constants.STR_USERS:
                    // 收到在线用户列表
                    if (ss.length >= 2) {
                        Gson gson = new Gson();
                        List<HHUser> onlineUsers = gson.fromJson(ss[1], new TypeToken<List<HHUser>>(){}.getType());
                        handleUserlist(onlineUsers);
                    }
                    break;
                case Constants.STR_MESSAGE:
                    // 收到聊天消息
                    if (ss.length >= 4) {
                        Date timeSnd = null;
                        try {
                            timeSnd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(ss[3]);
                        } catch (ParseException e) {
                            timeSnd = new Date(System.currentTimeMillis());
                            e.printStackTrace();
                        } finally {
                            HHMessage hhMessage = new HHMessage(ss[1], myPhone, ss[2], timeSnd);
                            handleMessage(hhMessage);
                        }
                    }
                    break;

                case Constants.STR_SET_NICKNAME:
                    // 收到修改昵称的结果
                    if (ss.length >= 2) {
                        if (ss[1].equals("Y")) {
                            handleSetNicknameResult(true);
                        } else {
                            handleSetNicknameResult(false);
                        }
                    }
                    break;

                default:
                    break;
            }
        }
    }

    private boolean handleLoginString(String strRcv) {
        String[] ss = strRcv.split("\\|", -1);
        if (ss.length >= 1 && ss[0].equals(Constants.STR_LOGIN)) {
            // 收到的是登录结果
            if (ss.length >= 3) {
                // 响应格式正确
                if (ss[1].equals("Y")) {
                    // 登录成功
                    isLogin = true;
                    udpListenerThread = new Thread(new UdpListener());
                    udpListenerThread.start();
                    heartbeatManagerThread = new Thread(new HeartbeatManager());
                    heartbeatManagerThread.start();
                    for (UiListener listener : uiListeners) {
                        listener.onLoginSucceed(ss[2]);
                    }
                    return true;
                } else {
                    // 登录失败
                    for (UiListener listener : uiListeners) {
                        listener.onLoginFail(ss[2]);
                    }
                    return true;
                }
            } else {
                // 响应格式不正确
                return false;
            }
        } else {
            // 收到的不是登录结果
            handleString(strRcv);
            return false;
        }
    }

    private boolean handleRegString(String strRcv) {
        String[] ss = strRcv.split("\\|", -1);
        if (ss.length >= 1 && ss[0].equals(Constants.STR_REGISTER)) {
            // 收到的是注册结果
            if (ss.length >= 2) {
                // 响应格式正确
                if (ss[1].equals("Y")) {
                    // 注册成功
                    for (UiListener listener : uiListeners) {
                        listener.onRegSucceed();
                    }
                    return true;
                } else {
                    // 注册失败
                    for (UiListener listener : uiListeners) {
                        listener.onRegFail("该手机号已注册");
                    }
                    return true;
                }
            } else {
                // 响应格式不正确
                return false;
            }
        } else {
            handleString(strRcv);
            return false;
        }
    }

    private void handleSetNicknameResult(boolean succeed) {
        if (succeed) {
            for (UiListener listener : uiListeners) {
                listener.onSetNicknameSucceed();
            }
        } else {
            for (UiListener listener : uiListeners) {
                listener.onSetNicknameFail();
            }
        }
    }

    /*
    private boolean handleSetNicknameString(String strRcv) {
        String[] ss = strRcv.split("\\|", -1);
        if (ss.length >= 1 && ss[0].equals(Constants.STR_SET_NICKNAME)) {
            // 收到的是修改昵称结果
            if (ss.length >= 2) {
                // 响应格式正确
                if (ss[1].equals("Y")) {
                    // 修改成功
                    Log.d("tag1", "修改成功");///////////////////////
                    for (UiListener listener : uiListeners) {
                        listener.onSetNicknameSucceed();
                    }
                    return true;
                } else {
                    // 修改失败
                    for (UiListener listener : uiListeners) {
                        listener.onSetNicknameFail();
                    }
                    return true;
                }
            } else {
                // 响应格式不正确
                return false;
            }
        } else {
            handleString(strRcv);
            return false;
        }
    }
    */


    private void handleMessage(HHMessage hhMessage) {
        hhMessage.save();
        for (UiListener listener : uiListeners) {
            listener.onReceiveMessage(hhMessage);
        }
    }

    private void handleUserlist(List<HHUser> onlineUsers) {
        for (UiListener listener : uiListeners) {
            listener.onReceiveUserlist(onlineUsers);
        }
    }

    private class UdpListener implements Runnable {
        @Override
        public void run() {
            packetRcv = new DatagramPacket(new byte[1024], 1024);
            try {
                socket.setSoTimeout(3000);
                while (isLogin) {
                    try {
                        socket.receive(packetRcv);
                        final String strRcv = new String(packetRcv.getData(), packetRcv.getOffset(), packetRcv.getLength());
                        ThreadUtil.runInSubThread(new Runnable() {
                            @Override
                            public void run() {
                                handleString(strRcv);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }

    private class HeartbeatManager implements Runnable {
        @Override
        public void run() {
            while (isLogin) {
                try {
                    Thread.sleep(Constants.TIME_HEARTBEAT_INTVL);
                    sendHeartbeat();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    interface UiListener {
        void onLoginSucceed(String nickname);
        void onLoginFail(String detail);
        void onRegSucceed();
        void onRegFail(String detail);
        void onLogout();
        void onSetNicknameSucceed();
        void onSetNicknameFail();
        void onReceiveUserlist(List<HHUser> hhUsers);
        void onReceiveMessage(HHMessage hhMessage);
        void onSendMessageSucceed(HHMessage hhMessage);
        void onSendMessageFail();
    }
}
