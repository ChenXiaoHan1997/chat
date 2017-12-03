/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hhchatserver;

import com.sun.javafx.font.FontResource;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 *
 * @author Administrator
 */
public class HHServer {
    /*
    private List<HHUser> onlineUsers = new ArrayList<>();
    private HashMap<String, InetAddress> userAddressMap = new HashMap<>();
    private HashMap<String, Integer> userPortMap = new HashMap<>();
    */
    
    private HashMap<String, HHUser> usersMap = new HashMap<>();
    
    private DatagramPacket packetRcv, packetSnd;
    private DatagramSocket socket;
    private int port;
    
    private Thread udpListenerThread;
    private Thread heartbeatManagerThread;
    private UiListener uiListener;
    
    private boolean isRun = false;
    
    private static HHServer singleInstance;
    
    public static HHServer getInstance(UiListener uiListener, int port) {
        if (singleInstance == null) {
            singleInstance = new HHServer();
        }
        singleInstance.uiListener = uiListener;
        singleInstance.port = port;
        return singleInstance;
    }
    
    private HHServer() {
        
    }
        
    public void launch() {
        if (isRun) {
            uiListener.onLaunchSucceed("服务器已经启动");
            return;
        }
        if (DBUtil.init("hh_udp_server", "hhudp", "123456")) {
            uiListener.onStateChanged("成功连接到数据库");
        } else {
            uiListener.onLaunchFail("无法连接到数据库...请检查数据库是否启动");
            return;
        }
        try {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost(), port);
            socket = new DatagramSocket(inetSocketAddress);
            socket.setSoTimeout(3000);
            isRun = true;
            udpListenerThread = new Thread(new UdpListener());
            udpListenerThread.start();
            heartbeatManagerThread = new Thread(new HeartbeatManager());
            heartbeatManagerThread.start();
            //System.out.println("服务器成功启动");
            uiListener.onLaunchSucceed("服务器成功启动");
            return;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            uiListener.onLaunchFail("服务器无法启动");
            return;
        } catch (SocketException e) {
            e.printStackTrace();
            uiListener.onLaunchFail("服务器无法启动...请检查端口是否被占用");
            return;
        }
        
    }
    
    public void stop() {
        if (isRun && socket != null) {
            isRun = false;
            socket.close();
            uiListener.onStateChanged("服务器已停止运行");
        }
    }
       
    private boolean sendString(String targetPhone, String strSnd) {
        //System.out.println("sendString: " + strSnd + " to: " + );///////////////////////
        /*
        InetAddress clientAddress = userAddressMap.getOrDefault(targetPhone, null);
        int clientPort = userPortMap.getOrDefault(targetPhone, -1);
        if (clientAddress == null || clientPort == -1) {
            return false;
        }
*/
        HHUser hhUser = usersMap.get(targetPhone);
        if (hhUser == null) {
            return false;
        }
        System.out.println("sendString: " + strSnd + " to: " + hhUser.getAddress().getHostAddress() + ":" + hhUser.getPort());///////////////////////
        packetSnd = new DatagramPacket(strSnd.getBytes(), strSnd.getBytes().length, hhUser.getAddress(), hhUser.getPort());
        
        try {
            socket.send(packetSnd);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void handleString(String strRcv, InetAddress clientAddress, int clientPort) {
        // 主线程中调用
        String[] ss = strRcv.split("\\|", -1);
        if (ss.length > 0) {
            switch(ss[0]) {
                case Constant.STR_LOGIN:
                    if (ss.length >= 3) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                handleLogin(ss[1], ss[2], clientAddress, clientPort);
                            }
                        }).start();
                    }
                    break;
                case Constant.STR_REGISTER:
                    if (ss.length >= 4) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                handleRegister(ss[1], ss[2], ss[3], clientAddress, clientPort);
                            }
                        }).start();
                    }
                    break;
                case Constant.STR_LOGOUT:
                    if (ss.length >= 2) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                handleLogout(ss[1]);
                            }
                        }).start();
                    }
                    break;
                case Constant.STR_SET_NICKNAME:
                    if (ss.length >= 3) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                handleChangeNickname(ss[1], ss[2]);
                            }
                        }).start();
                    }
                    break;
                case Constant.STR_USERS:
                    if (ss.length >= 2) {
                        new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    handleUserlistReq(ss[1]);
                                }
                            }).start();
                    }
                    break;
                case Constant.STR_MESSAGE:
                    if (ss.length >= 5) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                handleMessage(ss[1], ss[2], ss[3], ss[4]);
                            }
                        }).start();
                    }
                    break;
                case Constant.STR_HEARTBEAT:
                    if (ss.length >= 2) {
                        handleHeartbeat(ss[1]);
                    }
                    break;
                default:
                    break;
            }
               
        }
    }
    
    private void handleLogin(String phone, String password, InetAddress clientAddress, int clientPort) {
        // 子线程中调用
        //System.out.println("收到来自" + clientAddress.getHostAddress() + ":" + clientPort + "的登录请求--手机号:" + phone + " 密码" + password);
        usersMap.put(phone, new HHUser(phone, "", clientAddress, clientPort, false));
        String[] nicknames = new String[1];
        String strSnd = "";
        int result = DBUtil.acknowledgeUser(phone, password, nicknames);
        switch (result) {
            case Constant.RSLT_LOGIN_OK:
                HHUser user = usersMap.get(phone);
                //System.out.println("user.getAddress()=" + user.getAddress().getHostAddress());/////////////
                
                //user.setIsOnline(true);
                //user.setNickname(nicknames[0]);

                HHUser tempUser = new HHUser(phone, nicknames[0], clientAddress, clientPort, true);
                //System.out.println("user.getNickname(): " + user.getNickname());////////////////////
                usersMap.put(phone, tempUser);
                //usersMap.put(phone, user);////////////////
                //System.out.println("tempUser.getNickname(): " + tempUser.getNickname());////////////////////
                strSnd = Constant.STR_LOGIN + "|" + "Y" + "|" + nicknames[0];
                sendString(phone, strSnd);
                
                Iterator iterator = usersMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Entry entry = (Entry) iterator.next();
                    HHUser hhUser = (HHUser) entry.getValue();
                    System.out.println("phone: " + hhUser.getPhone() + ", nickname: " + hhUser.getNickname());//////////////
                    if (hhUser.isOnline() && !entry.getKey().equals(phone)) {
                        handleUserlistReq(hhUser.getPhone());
                    }
                }
                //System.out.println("用户" + phone + "登录成功");
                uiListener.onStateChanged("用户" + phone + "上线了");
                break;
                
            case Constant.RSLT_LOGIN_WRONG_PWD:
                strSnd = Constant.STR_LOGIN + "|N|密码错误";
                sendString(phone, strSnd);
                //System.out.println("密码错误");///////////////////
                break;
                
            case Constant.RSLT_LOGIN_NO_PHONE:
                strSnd = Constant.STR_LOGIN + "|N|该手机号未注册";
                sendString(phone, strSnd);
                break;
                
            case Constant.RSLT_DB_CONN_ERROR:
                //System.out.println("无法连接到数据库");
                break;
            case Constant.RSLT_DB_SQL_ERROR:
                //System.out.println("查询数据出错");
                break;
            default:
                break;
        }
    }
    
    
    private void handleRegister(String phone, String password, String nickname, InetAddress clientAddress, int clientPort) {
        // 子线程中调用
        //System.out.println("收到来自" + clientAddress.getHostAddress() + ":" + clientPort + "的注册请求--手机号:" + phone + " 密码:" + password + " 昵称:" + nickname);
        usersMap.put(phone, new HHUser(phone, "", clientAddress, clientPort, false));
        String strSnd = "";
        int result = DBUtil.registerNewUser(phone, password, nickname);
        switch (result) {
            case Constant.RSLT_REG_OK:
                strSnd = Constant.STR_REGISTER + "|Y";
                sendString(phone, strSnd);
                //System.out.println("用户" + phone + "注册成功");
                uiListener.onStateChanged("用户" + phone + "注册成功");
                break;
            case Constant.RSLT_REG_EXIST_PHONE:
                strSnd = Constant.STR_REGISTER + "|N";
                sendString(phone, strSnd);
                break;
            case Constant.RSLT_DB_CONN_ERROR:
                //System.out.println("无法连接到数据库");
                break;
            case Constant.RSLT_DB_SQL_ERROR:
                //System.out.println("查询数据出错");
                break;
            default:
                break;
        }
    }
           
    private void handleLogout(String phone) {
        // 子线程中调用
        HHUser hhUser = usersMap.get(phone);
        if (hhUser == null) {
            return;
        }
        hhUser.setIsOnline(false);
        //System.out.println("用户" + phone + "已下线");
        uiListener.onStateChanged("用户" + phone + "下线了");
        
        Iterator iterator = usersMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            HHUser tempUser = (HHUser) entry.getValue();
            if (tempUser.isOnline()) {
                handleUserlistReq(tempUser.getPhone());
            }
        }
    }
    
    private void handleChangeNickname(String phone, String newNickname) {
        // 子线程中调用
        //System.out.println("用户" + phone + "请求修改昵称为:" + newNickname);
        int result = DBUtil.changeNickName(phone, newNickname);
        String strSnd = "";
        switch (result) {
            case Constant.RSLT_SETNAME_OK:
                strSnd = Constant.STR_SET_NICKNAME + "|Y";
                sendString(phone, strSnd);
                //System.out.println("用户" + phone + "修改昵称成功" + newNickname);
                
                HHUser user = usersMap.get(phone);
                HHUser tempUser = new HHUser(phone, newNickname, user.getAddress(), user.getPort(), true);
                usersMap.put(phone, tempUser);
                //System.out.println("usersMap.get(phone).getNickname()=" + usersMap.get(phone).getNickname());/////////////
                uiListener.onStateChanged("用户" + phone + "将昵称修改为" + newNickname);
                break;
            case Constant.RSLT_SETNAME_FAIL:
                strSnd = Constant.STR_SET_NICKNAME + "|N";
                sendString(phone, strSnd);
                //System.out.println("用户" + phone + "修改昵称失败");
                break;
            case Constant.RSLT_DB_CONN_ERROR:
                //System.out.println("无法连接到数据库");
                break;
            case Constant.RSLT_DB_SQL_ERROR:
                //System.out.println("查询数据出错");
                break;
            default:
                break;
        }
    }
    
    private void handleMessage(String fromPhone, String targetPhone, String content, String timeSnd) {
        // 子线程中调用
        //System.out.println("收到" + fromPhone + "发给" + targetPhone + "的消息--内容:" + content + " 时间" + timeSnd);
        if (fromPhone.equals(targetPhone)) {
            return;
        }
        String strSnd = Constant.STR_MESSAGE + "|" + fromPhone + "|" + content + "|" + timeSnd;
        sendString(targetPhone, strSnd);
        uiListener.onStateChanged(fromPhone + "-->" + targetPhone + "：" + content);
    }
    
    private void handleUserlistReq(String phone) {
        // 子线程中调用
        List<String> onlineUsers = new ArrayList<>();
        
        Iterator iterator = usersMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            HHUser hhUser = (HHUser) entry.getValue();
            if (hhUser.isOnline()) {
                onlineUsers.add("{\"phone\":\"" + hhUser.getPhone() + "\",\"nickname\":\"" + hhUser.getNickname() + "\",\"isOnline\":true}");
            }
        }
        
        String strSnd = Constant.STR_USERS + "|" + "[" + String.join(",", onlineUsers) + "]";
        sendString(phone, strSnd);
    }
    
    private void handleHeartbeat(String phone) {
        // 主线程中调用
        HHUser hhUser = usersMap.get(phone);
        if (hhUser == null) {
            return;
        }
        hhUser.setHeartbeatIntvl(0);
        System.out.println("收到用户" + phone + "的心跳");/////////////////////////
    }
    
    private class UdpListener implements Runnable {

        @Override
        public void run() {
            packetRcv = new DatagramPacket(new byte[1024], 1024);
            while (isRun) {
                try {
                    socket.receive(packetRcv);
                    final String strRcv = new String(packetRcv.getData(), packetRcv.getOffset(), packetRcv.getLength());
                    handleString(strRcv, packetRcv.getAddress(), packetRcv.getPort());
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
            //socket.close();
        }
    }
    
    private class HeartbeatManager implements Runnable {

        @Override
        public void run() {
            while (isRun) {
                Iterator iterator = usersMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Entry entry = (Entry) iterator.next();
                    HHUser hhUser = (HHUser) entry.getValue();
                    if (hhUser.isOnline()) {
                        hhUser.setHeartbeatIntvl(hhUser.getHeartbeatIntvl() + 1);
                        if (hhUser.isOnline() && hhUser.getHeartbeatIntvl() > Constant.TIME_MAX_HEARTBEAT_INTVL) {
                            hhUser.setIsOnline(false);
                            uiListener.onStateChanged("用户" + hhUser.getPhone() + "下线了");
                        }
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    interface UiListener {
        void onStateChanged(String detail);
        void onLaunchSucceed(String detail);
        void onLaunchFail(String detail);
    }
    
}
