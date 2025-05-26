package cn.iocoder.yudao.module.alarm.monitor;

import cn.iocoder.yudao.framework.common.constant.WebsocketMessageType;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.alarm.constants.BinLogConstants;
import cn.iocoder.yudao.framework.mybatis.core.object.ColumnInfo;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.alarm.constants.DBTable;
import cn.iocoder.yudao.module.alarm.dal.dataobject.logrecord.BinlogEventHeader;
import cn.iocoder.yudao.module.alarm.service.cfgmail.AlarmCfgMailService;
import cn.iocoder.yudao.module.alarm.service.logrecord.AlarmLogRecordService;
import cn.iocoder.yudao.module.infra.api.websocket.WebSocketSenderApi;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import de.danielbechler.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class MySQLTableMonitor {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private AlarmLogRecordService alarmLogRecordService;

    @Autowired
    private AlarmCfgMailService alarmCfgMailService;

    @Autowired
    private WebSocketSenderApi webSocketSenderApi;

    // 缓存表结构：表ID -> 字段列表（字段名+类型）
    private static Map<String, List<ColumnInfo>> tableSchemaCache = new HashMap<>();
    private static Map<String, String> tableIdToName = new HashMap<>();

    public void tableListener(BinLogConstants binLogConstants) throws Exception {

        if (Collections.isEmpty(binLogConstants.getTableList())) {
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
                String tableName = "";
                List<ColumnInfo> columns = null;
                EventData data = event.getData();
                if (data instanceof TableMapEventData) {
                    TableMapEventData tableMap = (TableMapEventData) data;
                    if (binLogConstants.getDb().equals(tableMap.getDatabase()) && binLogConstants.getTableList().contains(tableMap.getTable())) {
                        // 查询表结构
                        // 不缓存表结构，避免表结构改变tableId也发生改变时，获得不到正确的数据，且性能消耗很小
                        if (tableSchemaCache.size() == 0 || tableSchemaCache.get(tableMap.getTableId()+"") == null) {
                            String connectionUrl = "jdbc:mysql://" + binLogConstants.getHost() + ":" + binLogConstants.getPort() + "/" + binLogConstants.getDb() + "?useSSL=false";
                            columns = JdbcUtils.getTableColumns(connectionUrl,binLogConstants.getUsername(),binLogConstants.getPasswd(),tableMap.getDatabase(), tableMap.getTable());
                            tableSchemaCache.put(tableMap.getTableId()+"", columns);
                        }
                        if (tableIdToName.size() == 0 || tableIdToName.get(tableMap.getTableId()+"") == null) {
                            tableIdToName.put(tableMap.getTableId()+"", tableMap.getTable());
                        }
                    }
                // 步骤2：处理数据变更事件
                } else if (data instanceof WriteRowsEventData) {
                    // 插入事件
                    WriteRowsEventData writeData = (WriteRowsEventData) data;
                    tableId = writeData.getTableId();
                    columns = tableSchemaCache.get(tableId+"");
                    tableName = tableIdToName.get(tableId+"");
                    if (columns != null && DBTable.ALARM_LOG_RECORD.equals(tableName)) {
                        if (!repeatMessage(event)) {
                            return;
                        }
                        List<Map<String, Object>> mapList = new ArrayList<>();
                        for (Serializable[] row : writeData.getRows()) {
                            Map<String, Object> rowData = parseRowData(row, columns);
                            mapList.add(rowData);
//                            log.info("[INSERT] 数据内容：" + rowData);
                        }
//                        alarmCfgMailService.pushAlarmMessage(mapList);
                    }

                } else if (data instanceof UpdateRowsEventData) {
                    // 更新事件
                    UpdateRowsEventData updateData = (UpdateRowsEventData) data;
                    tableId = updateData.getTableId();
                    columns = tableSchemaCache.get(tableId+"");
                    tableName = tableIdToName.get(tableId+"");
                    List<String> tableList = new ArrayList<>(Arrays.asList(
                            DBTable.PDU_INDEX,
                            DBTable.BUS_INDEX,
                            DBTable.CABINET_INDEX,
                            DBTable.AISLE_INDEX,
                            DBTable.ROOM_INDEX,
                            DBTable.CABINET_CRON_CONFIG,
                            DBTable.AISLE_CRON_CONFIG,
                            DBTable.ROOM_CRON_CONFIG,
                            DBTable.ALARM_LOG_RECORD
                    ));
                    if (columns != null && tableList.contains(tableName)) {
                        if (!repeatMessage(event)) {
                            return;
                        }
                        List<Map<String, Object>> newMaps = new ArrayList<>();
                        List<Map<String, Object>> oldMaps = new ArrayList<>();
                        for (Map.Entry<Serializable[], Serializable[]> row : updateData.getRows()) {
                            Map<String, Object> oldData = parseRowData(row.getKey(), columns);
                            Map<String, Object> newData = parseRowData(row.getValue(), columns);
                            if (validateData(oldData, newData)) {
                                oldMaps.add(oldData);
                                newMaps.add(newData);
//                                log.info(tableName + " [UPDATE] 旧数据：" + oldData);
//                                log.info(tableName + " [UPDATE] 新数据：" + newData);
                            }
                        }
                        Integer result = null;
                        switch (tableName) {
                            case DBTable.PDU_INDEX:
                                result = alarmLogRecordService.insertOrUpdateAlarmRecordWhenPduAlarm(oldMaps,newMaps);
                                break;
                            case DBTable.BUS_INDEX:
                                result = alarmLogRecordService.insertOrUpdateAlarmRecordWhenBusAlarm(oldMaps,newMaps);
                                break;
                            case DBTable.CABINET_INDEX:
                                result = alarmLogRecordService.insertOrUpdateAlarmRecordWhenCabinetAlarm(oldMaps,newMaps);
                                break;
                            case DBTable.AISLE_INDEX:
                                result = alarmLogRecordService.insertOrUpdateAlarmRecordWhenAisleAlarm(oldMaps,newMaps);
                                break;
                            case DBTable.ROOM_INDEX:
                                result = alarmLogRecordService.insertOrUpdateAlarmRecordWhenRoomAlarm(oldMaps,newMaps);
                                break;
                            case DBTable.CABINET_CRON_CONFIG:
                                alarmLogRecordService.updateCabinetAlarmJob(oldMaps,newMaps);
                                break;
                            case DBTable.AISLE_CRON_CONFIG:
                                alarmLogRecordService.updateAisleAlarmJob(oldMaps,newMaps);
                                break;
                            case DBTable.ROOM_CRON_CONFIG:
                                alarmLogRecordService.updateRoomAlarmJob(oldMaps,newMaps);
                                break;
                            case DBTable.ALARM_LOG_RECORD:
                                alarmLogRecordService.checkAlarmRecordChange(oldMaps,newMaps);
                                break;
                            default:
                                break;
                        }
                        // 告警记录表发生改变，向前端发送消息
                        if (result != null) {
//                            webSocketSenderApi.sendObject(UserTypeEnum.ADMIN.getValue(), WebsocketMessageType.ALARM_MESSAGE, "告警消息");
                        }
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

    public boolean repeatMessage (Event event) {
        // 记录事件，避免多服务器监听同一个表结构变化造成的重复操作
        EventHeader header = event.getHeader();
        BinlogEventHeader binlogEventHeader = BeanUtils.toBean(header, BinlogEventHeader.class);
        long timestamp = binlogEventHeader.getTimestamp();
        long nextPosition = binlogEventHeader.getNextPosition();
        String sign = "sign:" + timestamp+":"+ nextPosition;
        if (redisTemplate.opsForValue().get(sign) == null) {
            redisTemplate.opsForValue().set("sign:" + timestamp,nextPosition, 1, TimeUnit.MINUTES);
            return true;
        } else {
            return false;
        }
    }


    // 使用自定义SQL获取binlog信息（示例使用show binary log status）
    // 使用SHOW MASTER STATUS 替代 SHOW BINARY LOG STATUS。(mysql8.0.22后被替换)
    public String[] fetchLatestBinlog(Connection conn) throws Exception {
        String latestFile = "";
        long latestPosition = 0;
        Statement stmt = conn.createStatement();
        try {
            ResultSet rs = stmt.executeQuery("SHOW MASTER STATUS");
            if (rs.next()) {
                latestFile = rs.getString("File");
                latestPosition = rs.getLong("Position");
            }
        } catch (Exception e) {
            log.info("获取binlog状态异常: "+ e.getMessage());
            log.info("更换DQL语句重新获取");
            // 设置默认值或重试策略
            try {
                ResultSet rs = stmt.executeQuery("SHOW BINARY LOG STATUS");
                if (rs.next()) {
                    latestFile = rs.getString("File");
                    latestPosition = rs.getLong("Position");
                }
            } catch (Exception exception) {
                log.info("重新获取binlog异常：" + e.getMessage());
            }

        }
        return new String[]{latestFile, String.valueOf(latestPosition)};
    }


    // 解析行数据为字段名-值的映射
    private Map<String, Object> parseRowData(Serializable[] row, List<ColumnInfo> columns) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < row.length; i++) {
            String columnName = columns.get(i).getName();
            Object value = convertValue(row[i], columns.get(i).getType());
            // 过滤is_deleted字段
            if (!"is_deleted".equals(columnName)) {
                result.put(columnName, value);
            }
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

    private boolean validateData ( Map<String, Object> oldData , Map<String, Object> newData) {
        if (Objects.isNull(oldData) || Objects.isNull(newData)) {
            return false;
        }
        if (Objects.nonNull(oldData.get("run_status")) && !oldData.get("run_status").equals(newData.get("run_status"))) {
            return true;
        }
        if (Objects.nonNull(oldData.get("eq_day_cron")) && !oldData.get("eq_day_cron").equals(newData.get("eq_day_cron"))) {
            return true;
        }
        if (Objects.nonNull(oldData.get("eq_week_cron")) && !oldData.get("eq_week_cron").equals(newData.get("eq_week_cron"))) {
            return true;
        }
        if (Objects.nonNull(oldData.get("eq_month_cron")) && !oldData.get("eq_month_cron").equals(newData.get("eq_month_cron"))) {
            return true;
        }
        if (Objects.nonNull(oldData.get("load_rate_status")) && !oldData.get("load_rate_status").equals(newData.get("load_rate_status"))) {
            return true;
        }
        return false;
    }

}