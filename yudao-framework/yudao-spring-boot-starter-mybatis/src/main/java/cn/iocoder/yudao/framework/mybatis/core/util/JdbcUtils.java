package cn.iocoder.yudao.framework.mybatis.core.util;

import cn.iocoder.yudao.framework.mybatis.core.object.ColumnInfo;
import com.baomidou.mybatisplus.annotation.DbType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC 工具类
 *
 * @author 芋道源码
 */
public class JdbcUtils {

    /**
     * 判断连接是否正确
     *
     * @param url      数据源连接
     * @param username 账号
     * @param password 密码
     * @return 是否正确
     */
    public static boolean isConnectionOK(String url, String username, String password) {
        try (Connection ignored = DriverManager.getConnection(url, username, password)) {
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 获得 URL 对应的 DB 类型
     *
     * @param url URL
     * @return DB 类型
     */
    public static DbType getDbType(String url) {
        String name = com.alibaba.druid.util.JdbcUtils.getDbType(url, null);
        return DbType.getDbType(name);
    }

    // 动态查询表结构（字段名+类型）
    public static List<ColumnInfo> getTableColumns(String url ,String user , String passwd , String dbName, String tableName) {
        List<ColumnInfo> columns = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, passwd);
             ResultSet rs = conn.getMetaData().getColumns(dbName, null, tableName, "%")) {
            while (rs.next()) {
                String colName = rs.getString("COLUMN_NAME");
                int colType = rs.getInt("DATA_TYPE"); // Types.XXX 常量值
                columns.add(new ColumnInfo(colName, colType));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return columns;
    }
}
