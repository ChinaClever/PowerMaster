package cn.iocoder.yudao.module.alarm.job;

import cn.easyes.core.conditions.LambdaEsQueryWrapper;
import cn.iocoder.yudao.framework.common.constant.FieldConstant;
import cn.iocoder.yudao.framework.common.elasticsearch.mapping.AisleMonthPower;
import cn.iocoder.yudao.framework.common.elasticsearch.repository.AisleMonthPowerMapper;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleCfg;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.enums.AlarmLevelEnums;
import cn.iocoder.yudao.framework.common.enums.AlarmStatusEnums;
import cn.iocoder.yudao.framework.common.enums.AlarmTypeEnums;
import cn.iocoder.yudao.framework.common.mapper.AisleCfgMapper;
import cn.iocoder.yudao.framework.common.mapper.AisleIndexMapper;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.alarm.dal.dataobject.logrecord.AlarmLogRecordDO;
import cn.iocoder.yudao.module.alarm.dal.mysql.logrecord.AlarmLogRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class AisleMonthAlarmJob implements JobHandler {

    @Autowired
    private AisleCfgMapper aisleCfgMapper;

    @Autowired
    private AisleIndexMapper  aisleIndexMapper;

    @Autowired
    private AlarmLogRecordMapper alarmLogRecordMapper;

    @Autowired
    private AisleMonthPowerMapper aisleMonthPowerMapper;

    @Override
    public String execute(String param) throws Exception {
        Thread.sleep(1000*60*5);
        // 获取所有按月统计电量的柜列
        log.info("AisleMonthAlarmJob execute start, current time: {}", LocalDateTime.now());
        List<AisleCfg> aisleCfgList = aisleCfgMapper.selectList(new LambdaQueryWrapper<AisleCfg>()
                .eq(AisleCfg::getEleAlarmMonth, 1));
        for (AisleCfg aisleCfg : aisleCfgList) {
            // 查询机柜的es电表数据
            Double eleLimitMonth = aisleCfg.getEleLimitMonth();
            LambdaEsQueryWrapper<AisleMonthPower> wrapper = new LambdaEsQueryWrapper<>();
            wrapper.eq(AisleMonthPower::getAisle_id, aisleCfg.getAisleId());
            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String nowTime = now.format(formatter);
            wrapper.lt("end_time.keyword",  nowTime);
            wrapper.gt("end_time.keyword",  now.minusMonths(1).format(formatter));
            wrapper.orderByDesc("start_time.keyword");
            wrapper.limit(1);
            AisleMonthPower aisleMonthPower = aisleMonthPowerMapper.selectOne(wrapper);
            if (aisleMonthPower != null && aisleMonthPower.getEq_value() > eleLimitMonth) {
                // 用电超额，存入告警记录
                AisleIndex aisleIndex = aisleIndexMapper.selectById(aisleCfg.getAisleId());
                AlarmLogRecordDO alarmRecord = new AlarmLogRecordDO();
                String alarmKey = aisleIndex.getRoomId() + FieldConstant.SPLIT_KEY + aisleIndex.getId();
                alarmRecord.setAlarmKey(alarmKey);
                alarmRecord.setAlarmStatus(AlarmStatusEnums.UNTREATED.getStatus());
                alarmRecord.setAlarmType(AlarmTypeEnums.AISLE_MONTH_POWER_ALARM.getType());
                // 告警位置
                String location = aisleIndexMapper.selectLocationById(aisleIndex.getId());
                alarmRecord.setAlarmPosition(location);
                // 级联关系id
                alarmRecord.setRoomId(aisleIndex.getRoomId());
                alarmRecord.setAisleId(aisleIndex.getId());
                alarmRecord.setAlarmLevel(AlarmLevelEnums.TWO.getStatus());
                // 告警描述
                DecimalFormat decimalFormat = new DecimalFormat("0.0");
                decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
                String powEqValueMonth = decimalFormat.format(aisleMonthPower.getEq_value());
                String alarmDesc = "柜列上个月用电量超额！该柜列月用电量限制：" +  eleLimitMonth + "KWh，实际用电量：" + powEqValueMonth + "KWh";
                alarmRecord.setAlarmDesc(alarmDesc);
                alarmRecord.setStartTime(LocalDateTime.now());
                alarmLogRecordMapper.insert(alarmRecord);
            }
        }

        return null;
    }
}
