package cn.iocoder.yudao.module.alarm.job;

import cn.easyes.core.conditions.LambdaEsQueryWrapper;
import cn.iocoder.yudao.framework.common.elasticsearch.mapping.RoomDayPower;
import cn.iocoder.yudao.framework.common.elasticsearch.repository.RoomDayPowerMapper;
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
import java.util.List;

@Component
public class RoomDayAlarmJob implements JobHandler {

    @Autowired
    private RoomCfgMapper roomCfgMapper;

    @Autowired
    private RoomIndexMapper  roomIndexMapper;

    @Autowired
    private AlarmLogRecordMapper alarmLogRecordMapper;

    @Autowired
    private RoomDayPowerMapper roomDayPowerMapper;

    @Override
    public String execute(String param) throws Exception {
        Thread.sleep(1000*60*5);
        // 获取所有按天统计电量的机房
        List<RoomCfg> roomCfgList = roomCfgMapper.selectList(new LambdaQueryWrapper<RoomCfg>()
                .eq(RoomCfg::getEleAlarmDay, 1));
        for (RoomCfg  roomCfg : roomCfgList) {
            // 查询机房的es电表数据
            Double eleLimitDay = roomCfg.getEleLimitDay();
            LambdaEsQueryWrapper<RoomDayPower> wrapper = new LambdaEsQueryWrapper<>();
            wrapper.eq(RoomDayPower::getRoom_id, roomCfg.getRoomId());
            wrapper.orderByDesc("start_time.keyword");
            wrapper.limit(1);
            RoomDayPower roomDayPower = roomDayPowerMapper.selectOne(wrapper);
            if (roomDayPower != null && roomDayPower.getEq_value() > eleLimitDay) {
                // 用电超额，存入告警记录
                RoomIndex roomIndex = roomIndexMapper.selectById(roomCfg.getRoomId());
                AlarmLogRecordDO alarmRecord = new AlarmLogRecordDO();
                String alarmKey = roomIndex.getId() + "";
                alarmRecord.setAlarmKey(alarmKey);
                alarmRecord.setAlarmStatus(AlarmStatusEnums.UNTREATED.getStatus());
                alarmRecord.setAlarmType(AlarmTypeEnums.ROOM_DAY_POWER_ALARM.getType());
                // 告警位置
                alarmRecord.setAlarmPosition(roomIndex.getRoomName());
                // 级联关系id
                alarmRecord.setRoomId(roomIndex.getId());
                alarmRecord.setAlarmLevel(AlarmLevelEnums.TWO.getStatus());
                // 告警描述
                DecimalFormat decimalFormat = new DecimalFormat("0.0");
                decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
                String powEqValueDay = decimalFormat.format(roomDayPower.getEq_value());
                String alarmDesc = "机房上一日用电量超额！该机房日用电量限制：" +  eleLimitDay + "KWh，实际用电量：" + powEqValueDay + "KWh";
                alarmRecord.setAlarmDesc(alarmDesc);
                alarmRecord.setStartTime(LocalDateTime.now());
                alarmLogRecordMapper.insert(alarmRecord);
            }
        }

        return null;
    }
}
