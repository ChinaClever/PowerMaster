package cn.iocoder.yudao.module.alarm.job;

import cn.easyes.core.conditions.LambdaEsQueryWrapper;
import cn.iocoder.yudao.framework.common.elasticsearch.mapping.RoomDayPower;
import cn.iocoder.yudao.framework.common.elasticsearch.mapping.RoomMonthPower;
import cn.iocoder.yudao.framework.common.elasticsearch.repository.RoomDayPowerMapper;
import cn.iocoder.yudao.framework.common.elasticsearch.repository.RoomMonthPowerMapper;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomCfg;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.enums.AlarmLevelEnums;
import cn.iocoder.yudao.framework.common.enums.AlarmStatusEnums;
import cn.iocoder.yudao.framework.common.enums.AlarmTypeEnums;
import cn.iocoder.yudao.framework.common.mapper.RoomCfgMapper;
import cn.iocoder.yudao.framework.common.mapper.RoomIndexMapper;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.alarm.dal.dataobject.logrecord.AlarmLogRecordDO;
import cn.iocoder.yudao.module.alarm.dal.mysql.logrecord.AlarmLogRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class RoomMonthAlarmJob implements JobHandler {

    @Autowired
    private RoomCfgMapper roomCfgMapper;

    @Autowired
    private RoomIndexMapper  roomIndexMapper;

    @Autowired
    private AlarmLogRecordMapper alarmLogRecordMapper;

    @Autowired
    private RoomMonthPowerMapper roomMonthPowerMapper;

    @Override
    public String execute(String param) throws Exception {
        Thread.sleep(1000*60*5);
        // 获取所有按天统计电量的机房
        List<RoomCfg> roomCfgList = roomCfgMapper.selectList(new LambdaQueryWrapper<RoomCfg>()
                .eq(RoomCfg::getEleAlarmMonth, 1));
        for (RoomCfg  roomCfg : roomCfgList) {
            // 查询机房的es电表数据
            Double eleLimitMonth = roomCfg.getEleLimitMonth();
            LambdaEsQueryWrapper<RoomMonthPower> wrapper = new LambdaEsQueryWrapper<>();
            wrapper.eq(RoomMonthPower::getRoom_id, roomCfg.getRoomId());
            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String nowTime = now.format(formatter);
            wrapper.lt("end_time.keyword",  nowTime);
            wrapper.gt("end_time.keyword",  now.minusMonths(1).format(formatter));
            wrapper.orderByDesc("start_time.keyword");
            wrapper.limit(1);
            RoomMonthPower roomMonthPower = roomMonthPowerMapper.selectOne(wrapper);
            if (roomMonthPower != null && roomMonthPower.getEq_value() > eleLimitMonth) {
                // 用电超额，存入告警记录
                RoomIndex roomIndex = roomIndexMapper.selectById(roomCfg.getRoomId());
                AlarmLogRecordDO alarmRecord = new AlarmLogRecordDO();
                String alarmKey = roomIndex.getId() + "";
                alarmRecord.setAlarmKey(alarmKey);
                alarmRecord.setAlarmStatus(AlarmStatusEnums.UNTREATED.getStatus());
                alarmRecord.setAlarmType(AlarmTypeEnums.ROOM_MONTH_POWER_ALARM.getType());
                // 告警位置
                alarmRecord.setAlarmPosition(roomIndex.getRoomName());
                // 级联关系id
                alarmRecord.setRoomId(roomIndex.getId());
                alarmRecord.setAlarmLevel(AlarmLevelEnums.TWO.getStatus());
                // 告警描述
                DecimalFormat decimalFormat = new DecimalFormat("0.0");
                decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
                String powEqValueMonth = decimalFormat.format(roomMonthPower.getEq_value());
                String alarmDesc = "机房上个月用电量超额！该机房月用电量限制：" +  eleLimitMonth + "KWh，实际用电量：" + powEqValueMonth + "KWh";
                alarmRecord.setAlarmDesc(alarmDesc);
                alarmRecord.setStartTime(LocalDateTime.now());
                alarmLogRecordMapper.insert(alarmRecord);
            }
        }

        return null;
    }
}
