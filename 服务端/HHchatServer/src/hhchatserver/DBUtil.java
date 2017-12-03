/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hhchatserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Administrator
 */
public class DBUtil {
    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement preparedStatement;
    
    private static String database;
    private static String userName;
    private static String password;
    
    private static boolean isInitialized;
    
    public static boolean init(String db, String un, String pwd) {
        database = db;
        userName = un;
        password = pwd;
        if (connect()) {
            isInitialized = true;
            //System.out.println("成功连接到数据库");
            disconnect();
            return true;
        } else {
            //System.out.println("无法连接到数据库\n请检查数据库是否启动");
            return false;
        }
    }
    
    private DBUtil() {
        
    }
    
    public static int acknowledgeUser(String phone, String password, String[] nicknames) {
        if (!isInitialized || !connect()) {
            return Constant.RSLT_DB_CONN_ERROR;
        }
        String sql = "SELECT * FROM hhchat_users WHERE phone = " + phone;
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                System.out.println("密码为:" + resultSet.getString("password"));////////////
                if (resultSet.getString("password").equals(password)) {
                    // 用户名存在且密码正确
                    nicknames[0] = resultSet.getString("nickname");
                    return Constant.RSLT_LOGIN_OK;
                } else {
                    // 密码错误
                    return Constant.RSLT_LOGIN_WRONG_PWD;   // wrong password
                }
            } else {
                // 用户名不存在
                return Constant.RSLT_LOGIN_NO_PHONE;
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            return Constant.RSLT_DB_SQL_ERROR;
        } finally {
            disconnect();
        }
    }
    
    public static int registerNewUser(String phone, String password, String nickname) {
        if (!isInitialized || !connect()) {
            return Constant.RSLT_DB_CONN_ERROR;
        }
       
        String sql = "SELECT phone FROM hhchat_users WHERE phone = " + phone;
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                // 用户名已存在
                return Constant.RSLT_REG_EXIST_PHONE;
            } else {
                sql = "insert into hhchat_users (phone,password,nickname) values(?,?,?)";
                preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
                preparedStatement.setString(1, phone);
                preparedStatement.setString(2, password);
                preparedStatement.setString(3, nickname);
                int i = preparedStatement.executeUpdate();
                preparedStatement.close();
                if (i > 0) {
                    return Constant.RSLT_REG_OK;
                } else {
                    return Constant.RSLT_DB_SQL_ERROR;
                }
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            return Constant.RSLT_DB_CONN_ERROR;
        } finally {
            disconnect();
        }
    }
    
    public static int changeNickName(String phone, String newNickname) {
        if (!isInitialized || !connect()) {
            return Constant.RSLT_DB_CONN_ERROR;
        }
        String sql = "update hhchat_users set nickname = '" + newNickname + "' where phone = '" + phone + "'";
        try {
            preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
            int i = preparedStatement.executeUpdate();
            if (i > 0) {
                System.out.println("i=" + i);////////////////
                return Constant.RSLT_SETNAME_OK;
            } else {
                return Constant.RSLT_SETNAME_FAIL;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Constant.RSLT_DB_SQL_ERROR;
        } finally {
            disconnect();
        }
    }
    
    private static boolean connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            //System.out.println("连接数据库...");
            String databaseUrl = "jdbc:mysql://localhost:3306/" + database;
            connection = DriverManager.getConnection(databaseUrl, userName, password);
            //System.out.println("连接数据库成功");
            statement = connection.createStatement();
            return true;
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
            //System.out.println("没有找到数据库驱动");
            return false;
        } catch (SQLException e) {
            //e.printStackTrace();
            //System.out.println("无法连接到数据库\n请检查数据库是否启动");
            return false;
        }
     
    }
    
    private static void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    
}
