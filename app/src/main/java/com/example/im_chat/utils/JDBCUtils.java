package com.example.im_chat.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class JDBCUtils {

    private static final String connectionURL = "jdbc:mysql://123.56.163.211/im_chat?useUnicode=true&characterEncoding=UTF-8";
    private static final String username = "sjh";
    private static final String password = "8859844007";

    private static ArrayList<Connection> conList = new ArrayList<Connection>();

    //静态代码块：当整个程序执行的时候，优先加载静态代码块
    static {
        for(int i =0;i<5;i++) {
            Connection con = createConnection();
            conList.add(con);
        }
    }

    public static Connection getConnection() {
        if(conList.isEmpty()==false) {
            Connection con = conList.get(0);
            conList.remove(con);
            return con;
        }else {
            return createConnection();
        }
    }


    //连接不够时会重新创建
    private static Connection createConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection(connectionURL, username, password);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }
//关闭时同时关闭3个内容
  public static void close(ResultSet rs,Statement stmt,Connection con) {
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(con);
    }
    public static void close(Statement stmt1,Statement stmt2,Connection con) {
        closeStatement(stmt1);
        closeStatement(stmt2);
        closeConnection(con);
    }


    private static void closeResultSet(ResultSet rs ) {
        try {
            if(rs!=null)rs.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private static void closeStatement(Statement stmt) {
        try {
            if(stmt!=null)
                stmt.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private static void closeConnection(Connection con) {
//        try {
//            if(con!=null)con.close();
//        } catch (SQLException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        conList.add(con);
    }
}
