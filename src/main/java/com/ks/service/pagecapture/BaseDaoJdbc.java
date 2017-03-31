package com.ks.service.pagecapture; /**
 * Created by Administrator on 2017/3/18.
 */

import java.sql.*;

public class BaseDaoJdbc {

    /**
     * 创建数据库连接
     *
     * @return
     */
    public static Connection getConn() {
        Connection conn = null;
        try {
            Class.forName(Config.driver);
            String url = Config.url;
            conn = DriverManager.getConnection(url, Config.username, Config.password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * 关闭数据库连接
     *
     * @param conn
     * @param ps
     * @param rs
     */
    public static void closeConn(Connection conn, PreparedStatement ps, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeConn(Connection conn, PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 数据库操作conn设置不自动提交，需再catch中执行此方法
     *
     * @param conn
     */
    public static void rollbackConn(Connection conn) {
        try {
            if (!conn.isClosed()) {
                conn.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
