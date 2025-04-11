package cn.iocoder.yudao.module.system.monitor;

import cn.iocoder.yudao.module.system.monitor.constants.BinLogConstants;
import cn.iocoder.yudao.framework.mybatis.core.object.ColumnInfo;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import com.alibaba.druid.util.StringUtils;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.EventData;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.sql.*;
import java.util.*;
import java.util.Date;

@Component
@Slf4j
public class MySQLTableMonitor {

    @Autowired
    private RedisTemplate redisTemplate;

    // 缓存表结构：表ID -> 字段列表（字段名+类型）
    private static Map<String, List<ColumnInfo>> tableSchemaCache = new HashMap<>();

    public void tableListener(BinLogConstants binLogConstants) throws Exception {

        if (StringUtils.isEmpty(binLogConstants.getTable())) {
            throw new RuntimeException("缺少请求参数");
        }

        try {
            // 获取最新的binlog信息
            String url = "jdbc:mysql://" + binLogConstants.getHost() + ":" + binLogConstants.getPort();
            Connection conn = DriverManager.getConnection(url, binLogConstants.getUsername(), binLogConstants.getPasswd());
            String[] binlogInfo = fetchLatestBinlog(conn);
            conn.close();

            BinaryLogClient client = new BinaryLogClient(binLogConstants.getHost(), binLogConstants.getPort(), binLogConstants.getUsername(), binLogConstants.getPasswd());
            client.setBinlogFilename(binlogInfo[0]);
            client.setBinlogPosition(Long.parseLong(binlogInfo[1]));

            // 步骤1：监听表结构映射事件
            client.registerEventListener(event -> {
                long tableId = -1;
                List<ColumnInfo> columns = null;
                EventData data = event.getData();
                if (data instanceof TableMapEventData) {
                    TableMapEventData tableMap = (TableMapEventData) data;
                    if (binLogConstants.getDb().equals(tableMap.getDatabase()) && binLogConstants.getTable().equals(tableMap.getTable())) {
                        if (redisTemplate.opsForValue().get(tableMap.getTable()) != null) {
                            tableSchemaCache = (Map<String, List<ColumnInfo>>) redisTemplate.opsForValue().get(tableMap.getTable());
                        } else {
                            // 查询表结构并缓存
                            String connectionUrl = "jdbc:mysql://" + binLogConstants.getHost() + ":" + binLogConstants.getPort() + "/" + binLogConstants.getDb() + "?useSSL=false";
                            columns = JdbcUtils.getTableColumns(connectionUrl,binLogConstants.getUsername(),binLogConstants.getPasswd(),tableMap.getDatabase(), tableMap.getTable());
                            tableSchemaCache.put(tableMap.getTableId()+"", columns);
                            redisTemplate.opsForValue().set(tableMap.getTable(), tableSchemaCache);
                        }
                    }
                // 步骤2：处理数据变更事件
                } else if (data instanceof WriteRowsEventData) {
                    // 插入事件
                    WriteRowsEventData writeData = (WriteRowsEventData) data;
                    tableId = writeData.getTableId();
                    columns = tableSchemaCache.get(tableId+"");
                    if (columns != null) {
                        List<Map<String, Object>> mapList = new ArrayList<>();
                        for (Serializable[] row : writeData.getRows()) {
                            Map<String, Object> rowData = parseRowData(row, columns);
                            mapList.add(rowData);
                            log.info("[INSERT] 数据内容：" + rowData);
                        }
                        redisTemplate.opsForList().rightPushAll(binLogConstants.getTable() + ":insert", mapList);
                    }
                } else if (data instanceof UpdateRowsEventData) {
                    // 更新事件
                    UpdateRowsEventData updateData = (UpdateRowsEventData) data;
                    tableId = updateData.getTableId();
                    columns = tableSchemaCache.get(tableId+"");
                    if (columns != null) {
                        List<Map<String, Object>> newMaps = new ArrayList<>();
                        List<Map<String, Object>> oldMaps = new ArrayList<>();
                        for (Map.Entry<Serializable[], Serializable[]> row : updateData.getRows()) {
                            Map<String, Object> oldData = parseRowData(row.getKey(), columns);
                            Map<String, Object> newData = parseRowData(row.getValue(), columns);
                            oldMaps.add(oldData);
                            newMaps.add(newData);
                            log.info("[UPDATE] 旧数据：" + oldData);
                            log.info("[UPDATE] 新数据：" + newData);
                        }
                        redisTemplate.opsForList().rightPushAll(binLogConstants.getTable() + ":updateNew", newMaps);
                        redisTemplate.opsForList().rightPushAll(binLogConstants.getTable() + ":updateOld", oldMaps);
                    }
                }
            });
            try {
                client.connect();
            } catch (Exception e) {
                log.error("连接失败，正在重试...");
                log.error("连接失败原因： " + e);
                Thread.sleep(5000); // 5秒后重试
                tableListener(binLogConstants);
            }

        } catch (Exception e) {
            log.error("监听异常： " + e);
        }
    }


    // 使用自定义SQL获取binlog信息（示例使用show binary log status）
    // 使用show binary log status替换SHOW MASTER STATUS(mysql8.0.22后被替换)
    public String[] fetchLatestBinlog(Connection conn) throws Exception {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SHOW BINARY LOG STATUS");
        String latestFile = "";
        long latestPosition = 0;
        if (rs.next()) {
            latestFile = rs.getString("File");
            latestPosition = rs.getLong("Position");
        }
        return new String[]{latestFile, String.valueOf(latestPosition)};
    }


    // 解析行数据为字段名-值的映射
    private Map<String, Object> parseRowData(Serializable[] row, List<ColumnInfo> columns) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < row.length; i++) {
            String columnName = columns.get(i).getName();
            Object value = convertValue(row[i], columns.get(i).getType());
            result.put(columnName, value);
        }
        return result;
    }


    // 数据类型转换（示例处理常见类型）
    private static Object convertValue(Serializable value, int sqlType) {
        if (value == null) return null;
        switch (sqlType) {
            case Types.INTEGER:
                return ((Number) value).intValue(); // Binlog中INT映射为Long
            case Types.VARCHAR:
                return value.toString();
            case Types.DATE:
                return new Date(((java.sql.Date) value).getTime());
            case Types.TIMESTAMP:
                if (value instanceof java.sql.Timestamp) {
                    return new Date(((java.sql.Timestamp) value).getTime());
                } else if (value instanceof Number) {
                    // 假设输入的时间戳为秒级，需转换为毫秒级
                    long timestamp = ((Number) value).longValue();
                    return new Date(timestamp * 1000);
                } else if (value instanceof Date) {
                    return value;
                } else {
                    throw new IllegalArgumentException("Value is not a Timestamp or Number for TIMESTAMP conversion");
                }
            default:
                return value; // 其他类型按需扩展

        }
    }

}