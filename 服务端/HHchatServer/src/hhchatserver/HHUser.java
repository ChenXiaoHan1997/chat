/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hhchatserver;

import java.net.InetAddress;

/**
 *
 * @author Administrator
 */
public class HHUser {
    
    private String phone;
    private String nickname;
    //private String password;
    private InetAddress address;
    private int port;
    private int heartbeatIntvl = 0;
    private boolean isOnline;

    public HHUser(String phone, String nickname, InetAddress address, int port, boolean isOnline) {
        this.phone = phone;
        this.nickname = nickname;
        this.address = address;
        this.port = port;
        this.isOnline = isOnline;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickName) {
        this.nickname = nickname;
    }
/*
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
*/
    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getHeartbeatIntvl() {
        return heartbeatIntvl;
    }

    public void setHeartbeatIntvl(int heartbeatIntvl) {
        this.heartbeatIntvl = heartbeatIntvl;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }
        
}
