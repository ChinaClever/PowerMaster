package cn.iocoder.yudao.module.alarm.job;

import cn.easyes.core.conditions.LambdaEsQueryWrapper;
import cn.iocoder.yudao.framework.common.constant.FieldConstant;
import cn.iocoder.yudao.framework.common.elasticsearch.repository.CabinetMonthPowerMapper;
import cn.iocoder.yudao.framework.common.elasticsearch.mapping.CabinetMonthPower;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetCfg;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import cn.iocoder.yudao.framework.common.enums.AlarmLevelEnums;
import cn.iocoder.yudao.framework.common.enums.AlarmStatusEnums;
import cn.iocoder.yudao.framework.common.enums.AlarmTypeEnums;
import cn.iocoder.yudao.framework.common.mapper.CabinetCfgMapper;
import cn.iocoder.yudao.framework.common.mapper.CabinetIndexMapper;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.alarm.dal.dataobject.logrecord.AlarmLogRecordDO;
import cn.iocoder.yudao.module.alarm.dal.mysql.logrecord.AlarmLogRecordMapper;
import cn.iocoder.yudao.module.alarm.service.logrecord.AlarmLogRecordService;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class CabinetMonthAlarmJob implements JobHandler {

    @Autowired
    private CabinetCfgMapper cabinetCfgMapper;

    @Autowired
    private CabinetIndexMapper cabinetIndexMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private AlarmLogRecordService alarmLogRecordService;

    @Autowired
    private AlarmLogRecordMapper alarmLogRecordMapper;

    @Autowired
    private CabinetMonthPowerMapper cabinetMonthPowerMapper;

    @Override
    public String execute(String param) throws Exception {
        Thread.sleep(1000*60*5);
        // 获取所有按月统计电量的机柜
        log.info("CabinetMonthAlarmJob execute start, current time: {}", LocalDateTime.now());
        List<CabinetCfg> cabinetCfgList = cabinetCfgMapper.selectList(new LambdaQueryWrapper<CabinetCfg>()
                .eq(CabinetCfg::getEleAlarmMonth, 1));
        for (CabinetCfg cabinetCfg : cabinetCfgList) {
            // 查询机柜的es电表数据
            Double eleLimitMonth = cabinetCfg.getEleLimitMonth();
            LambdaEsQueryWrapper<CabinetMonthPower> wrapper = new LambdaEsQueryWrapper<>();
            wrapper.eq(CabinetMonthPower::getCabinet_id, cabinetCfg.getCabinetId());
            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String nowTime = now.format(formatter);
            wrapper.lt("end_time.keyword",  nowTime);
            wrapper.gt("end_time.keyword",  now.minusMonths(1).format(formatter));
            wrapper.orderByDesc("start_time.keyword");
            wrapper.limit(1);
            CabinetMonthPower cabinetMonthPower = cabinetMonthPowerMapper.selectOne (wrapper);
            if (cabinetMonthPower != null && cabinetMonthPower.getEq_value() > eleLimitMonth) {
                // 用电超额，存入告警记录
                CabinetIndex cabinetIndex = cabinetIndexMapper.selectById(cabinetCfg.getCabinetId());
                AlarmLogRecordDO alarmRecord = new AlarmLogRecordDO();
                String alarmKey = cabinetIndex.getRoomId() + FieldConstant.SPLIT_KEY + cabinetIndex.getAisleId() + FieldConstant.SPLIT_KEY + cabinetIndex.getId();
                alarmRecord.setAlarmKey(alarmKey);
                alarmRecord.setAlarmStatus(AlarmStatusEnums.UNTREATED.getStatus());
                alarmRecord.setAlarmType(AlarmTypeEnums.CABINET_MONTH_POWER_ALARM.getType());
                // 告警位置
                String redisKey = cabinetIndex.getRoomId() + FieldConstant.SPLIT_KEY + cabinetIndex.getId();
                JSONObject cabinetJson = (JSONObject)redisTemplate.opsForValue().get(FieldConstant.REDIS_KEY_CABINET + redisKey);
                if (cabinetJson != null) {
                    String roomName = cabinetJson.get(FieldConstant.ROOM_NAME) + "";
                    String aisleName = cabinetJson.get(FieldConstant.AISLE_NAME) + "";
                    String cabinetName = cabinetJson.get(FieldConstant.CABINET_NAME) + "";
                    String location = roomName + "-" + aisleName + "-" + cabinetName;
                    alarmRecord.setAlarmPosition(location);
                } else {
                    String location = cabinetIndexMapper.selectLocationById(cabinetIndex.getId());
                    alarmRecord.setAlarmPosition(location);
                }
                // 级联关系id
                alarmRecord.setCabinetId(cabinetIndex.getId());
                alarmRecord.setRoomId(cabinetIndex.getRoomId());
                alarmRecord.setAisleId(cabinetIndex.getAisleId());
                alarmRecord.setAlarmLevel(AlarmLevelEnums.TWO.getStatus());
                // 告警描述
                DecimalFormat decimalFormat = new DecimalFormat("0.0");
                decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
                String powEqValueMonth = decimalFormat.format(cabinetMonthPower.getEq_value());
                String alarmDesc = "机柜上个月用电量超额！月用电量限制：" +  eleLimitMonth + "KWh，实际用电量：" + powEqValueMonth + "KWh";
                alarmRecord.setAlarmDesc(alarmDesc);
                alarmRecord.setStartTime(LocalDateTime.now());
                alarmLogRecordMapper.insert(alarmRecord);
            }
        }

        return null;
    }
}
