package cn.iocoder.yudao.module.alarm.service.logrecord;

import cn.iocoder.yudao.framework.common.constant.FieldConstant;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBar;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BusIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.pdu.PduIndexDo;
import cn.iocoder.yudao.framework.common.enums.*;
import cn.iocoder.yudao.framework.common.mapper.AisleBarMapper;
import cn.iocoder.yudao.framework.common.mapper.AisleIndexMapper;
import cn.iocoder.yudao.framework.common.mapper.CabinetIndexMapper;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pdu.api.PduDeviceApi;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import cn.iocoder.yudao.module.alarm.controller.admin.logrecord.vo.*;
import cn.iocoder.yudao.module.alarm.dal.dataobject.logrecord.AlarmLogRecordDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.alarm.dal.mysql.logrecord.AlarmLogRecordMapper;

/**
 * 系统告警记录 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class AlarmLogRecordServiceImpl implements AlarmLogRecordService {

    @Autowired
    private AlarmLogRecordMapper logRecordMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PduDeviceApi pduDeviceApi;

    @Autowired
    private AisleBarMapper aisleBarMapper;

    @Autowired
    private AisleIndexMapper aisleIndexMapper;

    @Autowired
    private CabinetIndexMapper cabinetIndexMapper;

    @Override
    public Integer saveLogRecord(AlarmLogRecordSaveReqVO createReqVO) {
        // 插入
        AlarmLogRecordDO logRecord = BeanUtils.toBean(createReqVO, AlarmLogRecordDO.class);
        logRecordMapper.insert(logRecord);
        // 返回
        return logRecord.getId();
    }

    @Override
    public void updateLogRecord(AlarmLogRecordSaveReqVO updateReqVO) {
        if (Objects.nonNull(updateReqVO.getAlarmStatus())
                && updateReqVO.getAlarmStatus().equals(AlarmStatusEnums.FINISH.getStatus())) {
            logRecordMapper.update(null, new LambdaUpdateWrapper<AlarmLogRecordDO>()
                    .in(AlarmLogRecordDO::getId, updateReqVO.getIds())
                    .set(AlarmLogRecordDO::getFinishTime, new Date())
                    .set(Objects.nonNull(updateReqVO.getAlarmStatus()), AlarmLogRecordDO::getAlarmStatus, updateReqVO.getAlarmStatus())
                    .set(StringUtils.isNotEmpty(updateReqVO.getConfirmReason()),AlarmLogRecordDO::getFinishReason, updateReqVO.getConfirmReason()));
        } else {
            logRecordMapper.update(null, new LambdaUpdateWrapper<AlarmLogRecordDO>()
                    .in(AlarmLogRecordDO::getId, updateReqVO.getIds())
                    .set(StringUtils.isNotEmpty(updateReqVO.getConfirmReason()), AlarmLogRecordDO::getConfirmReason, updateReqVO.getConfirmReason())
                    .set(Objects.nonNull(updateReqVO.getAlarmStatus()), AlarmLogRecordDO::getAlarmStatus, updateReqVO.getAlarmStatus()));
        }
    }

    @Override
    public void deleteLogRecord(Integer id) {
        // 校验存在
        validateLogRecordExists(id);
        // 删除
        logRecordMapper.deleteById(id);
    }

    private void validateLogRecordExists(Integer id) {
        if (logRecordMapper.selectById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
    }

    @Override
    public void deleteBatchIds(List<Integer> ids) {

        if (!CollectionUtils.isEmpty(ids)) {
            logRecordMapper.deleteBatchIds(ids);
        }

    }

    @Override
    public AlarmLogRecordDO getLogRecord(Integer id) {
        return logRecordMapper.selectById(id);
    }

    @Override
    public PageResult<AlarmLogRecordRespVO> getLogRecordPage(AlarmLogRecordPageReqVO pageReqVO) {
        Page page = new Page(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        Integer alarmLevel = AlarmLevelEnums.getStatusByName(pageReqVO.getLikeName());
        Integer alarmType = AlarmTypeEnums.getStatusByName(pageReqVO.getLikeName());
        Page<AlarmLogRecordDO> recordPageResult = logRecordMapper.selectPage(page, new LambdaQueryWrapperX<AlarmLogRecordDO>()
                .inIfPresent(AlarmLogRecordDO::getAlarmStatus, pageReqVO.getAlarmStatus())
                .eqIfPresent(AlarmLogRecordDO::getAlarmLevel, alarmLevel)
                .eqIfPresent(AlarmLogRecordDO::getAlarmType, alarmType)
                .and(StringUtils.isNotEmpty(pageReqVO.getLikeName()) && alarmLevel == null && alarmType == null, wrapper -> wrapper
                        .like(AlarmLogRecordDO::getAlarmKey, pageReqVO.getLikeName())
                        .or()
                        .like(AlarmLogRecordDO::getAlarmDesc, pageReqVO.getLikeName())
                        .or()
                        .like(AlarmLogRecordDO::getAlarmPosition, pageReqVO.getLikeName()))
                .orderByDesc(AlarmLogRecordDO::getCreateTime));
        List<AlarmLogRecordRespVO> recordRespVOS = new ArrayList<>();
        if (Objects.nonNull(recordPageResult)) {
            List<AlarmLogRecordDO> list = recordPageResult.getRecords();
            if (!CollectionUtils.isEmpty(list)) {
                list.forEach(record -> {
                    AlarmLogRecordRespVO recordRespVO = BeanUtils.toBean(record, AlarmLogRecordRespVO.class);
                    recordRespVO.setAlarmLevelDesc(AlarmLevelEnums.getNameByStatus(record.getAlarmLevel()));
                    recordRespVO.setAlarmTypeDesc(AlarmTypeEnums.getNameByStatus(record.getAlarmType()));
                    recordRespVO.setAlarmStatusDesc(AlarmStatusEnums.getNameByStatus(record.getAlarmStatus()));
                    recordRespVOS.add(recordRespVO);
                });
            }
        }
        PageResult<AlarmLogRecordRespVO> result = new PageResult<AlarmLogRecordRespVO>().setList(recordRespVOS).setTotal(recordPageResult.getTotal());
        return result;
    }

    @Override
    public Map<Object, Object> levelCount() {
        Map<Object, Object> map = new HashMap<>();
        for (int i = 0; i < AlarmLevelEnums.values().length; i++) {
            map.put(AlarmLevelEnums.values()[i].getStatus(), 0);
        }
        LambdaQueryWrapper<AlarmLogRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AlarmLogRecordDO::getAlarmStatus, AlarmStatusEnums.UNTREATED.getStatus());
        queryWrapper.select(AlarmLogRecordDO::getAlarmLevel, AlarmLogRecordDO::getCount);
        queryWrapper.groupBy(AlarmLogRecordDO::getAlarmLevel);
        List<AlarmLogRecordDO> list = logRecordMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(list)) {
            Map<Integer, Integer> statusMap = list.stream().collect(Collectors.toMap(AlarmLogRecordDO::getAlarmLevel, AlarmLogRecordDO::getCount));
            statusMap.keySet().forEach(key -> map.put(key, statusMap.get(key)));
        }
        //获取总数
        List<AlarmLogRecordDO> records = logRecordMapper.selectList(new LambdaQueryWrapper<AlarmLogRecordDO>()
                .ne(AlarmLogRecordDO::getAlarmStatus, AlarmStatusEnums.FINISH.getStatus()));
        if (!CollectionUtils.isEmpty(records)) {
            //总报警数
            map.put("total", records.size());
            //未处理
            long untreated = records.stream()
                    .filter(t -> t.getAlarmStatus().equals(AlarmStatusEnums.UNTREATED.getStatus())).distinct().count();
            long hung = records.stream()
                    .filter(t -> t.getAlarmStatus().equals(AlarmStatusEnums.HUNG.getStatus())).distinct().count();
            long confirm = records.stream()
                    .filter(t -> t.getAlarmStatus().equals(AlarmStatusEnums.CONFIRM.getStatus())).distinct().count();
            map.put("untreated", untreated);
            map.put("hung", hung);
            map.put("confirm", confirm);

        } else {
            map.put("total", 0);
            map.put("hung", 0);
            map.put("confirm", 0);
        }
        return map;
    }

    @Override
    public AlarmLogRecordDO getAlarmRecordById(Integer id) {
        return logRecordMapper.selectById(id);
    }

    @Override
    public void insertOrUpdateAlarmRecordWhenPduAlarm (List<Map<String, Object>> oldMaps, List<Map<String, Object>> newMaps) {
        if (!CollectionUtils.isEmpty(oldMaps) && !CollectionUtils.isEmpty(newMaps)) {
            ValueOperations ops = redisTemplate.opsForValue();
            List<PduIndexDo> pduIndexDoListOld = BeanUtils.toBean(oldMaps, PduIndexDo.class);
            List<PduIndexDo> pduIndexDoListNew = BeanUtils.toBean(newMaps, PduIndexDo.class);
            for (int i = 0; i < pduIndexDoListOld.size(); i++) {
                PduIndexDo pduIndexDoOld = pduIndexDoListOld.get(i);
                PduIndexDo pduIndexDoNew = pduIndexDoListNew.get(i);
                List<Integer> alarmCodeList = new ArrayList<>();
                alarmCodeList.add(PduStatusEnum.EARLY_WARNING.getStatus());
                alarmCodeList.add(PduStatusEnum.ALARM.getStatus());
                alarmCodeList.add(PduStatusEnum.OFF_LINE.getStatus());
                if (alarmCodeList.contains(pduIndexDoNew.getRunStatus()) && !pduIndexDoOld.getRunStatus().equals(pduIndexDoNew.getRunStatus())) {
                    AlarmLogRecordDO alarmRecord = new AlarmLogRecordDO();
                    alarmRecord.setAlarmKey(pduIndexDoNew.getPduKey());
                    alarmRecord.setAlarmStatus(AlarmStatusEnums.UNTREATED.getStatus());
                    if (pduIndexDoNew.getRunStatus().equals(PduStatusEnum.EARLY_WARNING.getStatus())) {
                        alarmRecord.setAlarmType(AlarmTypeEnums.PDU_WARNING.getType());
                        alarmRecord.setAlarmLevel(AlarmLevelEnums.THREE.getStatus());
                    } else if (pduIndexDoNew.getRunStatus().equals(PduStatusEnum.ALARM.getStatus())) {
                        alarmRecord.setAlarmType(AlarmTypeEnums.PDU_ALARM.getType());
                        alarmRecord.setAlarmLevel(AlarmLevelEnums.TWO.getStatus());
                    } else if (pduIndexDoNew.getRunStatus().equals(PduStatusEnum.OFF_LINE.getStatus())) {
                        alarmRecord.setAlarmType(AlarmTypeEnums.PDU_OFF_LINE.getType());
                        alarmRecord.setAlarmLevel(AlarmLevelEnums.TWO.getStatus());
                    }
                    // 告警位置
                    Map<String, String> positionMap = pduDeviceApi.getPositionByKeys(Arrays.asList(pduIndexDoNew.getPduKey()));
                    alarmRecord.setAlarmPosition(positionMap.get(pduIndexDoNew.getPduKey()));
                    // 告警描述、告警开始时间
                    JSONObject pduJson = (JSONObject) ops.get(FieldConstant.REDIS_KEY_PDU + pduIndexDoNew.getPduKey());
                    if (pduJson != null) {
                        String pdu_alarm = pduJson.get("pdu_alarm")==null?"":pduJson.get("pdu_alarm").toString();
                        alarmRecord.setAlarmDesc(pdu_alarm);
                        Object datetime = pduJson.get("datetime");
                        if (datetime != null) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            LocalDateTime startTime = LocalDateTime.parse(datetime.toString(), formatter);
                            alarmRecord.setStartTime(startTime);
                        } else {
                            alarmRecord.setStartTime(LocalDateTime.now());
                        }
                    }
                    // 级联关系id
                    CabinetIndex cabinetIndex = cabinetIndexMapper.selectByPduKey(pduIndexDoNew.getPduKey());
                    if (cabinetIndex != null) {
                        alarmRecord.setRoomId(cabinetIndex.getRoomId());
                        alarmRecord.setAisleId(cabinetIndex.getAisleId());
                        alarmRecord.setCabinetId(cabinetIndex.getId());
                    }
                    logRecordMapper.insert(alarmRecord);
                } else if (alarmCodeList.contains(pduIndexDoOld.getRunStatus()) && PduStatusEnum.NORMAL.getStatus().equals(pduIndexDoNew.getRunStatus())) {
                    int alarmRecord = logRecordMapper.update(new LambdaUpdateWrapper<AlarmLogRecordDO>()
                            .set(AlarmLogRecordDO::getAlarmStatus, AlarmStatusEnums.FINISH.getStatus())
                            .set(AlarmLogRecordDO::getFinishTime, LocalDateTime.now())
                            .set(AlarmLogRecordDO::getFinishReason,"状态恢复正常")
                            .eq(AlarmLogRecordDO::getAlarmKey, pduIndexDoNew.getPduKey())
                            .eq(AlarmLogRecordDO::getAlarmStatus, AlarmStatusEnums.UNTREATED.getStatus()));
                }
            }
        }

    }


    @Override
    public void insertOrUpdateAlarmRecordWhenBusAlarm(List<Map<String, Object>> oldMaps, List<Map<String, Object>> newMaps) {
        if (!CollectionUtils.isEmpty(oldMaps) && !CollectionUtils.isEmpty(newMaps)) {
            ValueOperations ops = redisTemplate.opsForValue();
            List<BusIndex> busIndexListOld = BeanUtils.toBean(oldMaps, BusIndex.class);
            List<BusIndex> busIndexListNew = BeanUtils.toBean(newMaps, BusIndex.class);
            for (int i = 0; i < busIndexListOld.size(); i++) {
                BusIndex busIndexOld = busIndexListOld.get(i);
                BusIndex busIndexNew = busIndexListNew.get(i);
                List<Integer> alarmCodeList = new ArrayList<>();
                alarmCodeList.add(BusTypeEnum.OFF_LINE.getStatus());
                alarmCodeList.add(BusTypeEnum.ALARM.getStatus());
                if (alarmCodeList.contains(busIndexNew.getRunStatus()) && !busIndexOld.getRunStatus().equals(busIndexNew.getRunStatus())) {
                    AlarmLogRecordDO alarmRecord = new AlarmLogRecordDO();
                    alarmRecord.setAlarmKey(busIndexNew.getBusKey());
                    alarmRecord.setAlarmStatus(AlarmStatusEnums.UNTREATED.getStatus());
                    if (busIndexNew.getRunStatus().equals(BusTypeEnum.ALARM.getStatus())) {
                        alarmRecord.setAlarmType(AlarmTypeEnums.BUS_ALARM.getType());
                        alarmRecord.setAlarmLevel(AlarmLevelEnums.TWO.getStatus());
                    } else if (busIndexNew.getRunStatus().equals(BusTypeEnum.OFF_LINE.getStatus())) {
                        alarmRecord.setAlarmType(AlarmTypeEnums.BUS_OFF_LINE.getType());
                        alarmRecord.setAlarmLevel(AlarmLevelEnums.TWO.getStatus());
                    }
                    // 告警位置
                    String location = getLocationByBusId(busIndexNew);
                    alarmRecord.setAlarmPosition(location);
                    // 告警描述、告警开始时间
                    JSONObject busJson = (JSONObject) ops.get(FieldConstant.REDIS_KEY_BUS + busIndexNew.getBusKey());
                    if (busJson != null) {
                        String bus_alarm = busJson.get("dev_alarm")==null?"":busJson.get("dev_alarm").toString();
                        alarmRecord.setAlarmDesc(bus_alarm);
                        Object datetime = busJson.get("datetime");
                        if (datetime != null) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            LocalDateTime startTime = LocalDateTime.parse(datetime.toString(), formatter);
                            alarmRecord.setStartTime(startTime);
                        } else {
                            alarmRecord.setStartTime(LocalDateTime.now());
                        }
                    }
                    // 机房id
                    AisleIndex aisleIndex = aisleIndexMapper.selectByBusKey(busIndexNew.getBusKey());
                    if (aisleIndex != null) {
                        alarmRecord.setRoomId(aisleIndex.getRoomId());
                        alarmRecord.setAisleId(aisleIndex.getId());
                    }
                    logRecordMapper.insert(alarmRecord);
                } else if (alarmCodeList.contains(busIndexOld.getRunStatus()) && BusTypeEnum.NORMAL.getStatus().equals(busIndexNew.getRunStatus())) {
                    int alarmRecord = logRecordMapper.update(new LambdaUpdateWrapper<AlarmLogRecordDO>()
                            .set(AlarmLogRecordDO::getAlarmStatus, AlarmStatusEnums.FINISH.getStatus())
                            .set(AlarmLogRecordDO::getFinishTime, LocalDateTime.now())
                            .set(AlarmLogRecordDO::getFinishReason,"状态恢复正常")
                            .eq(AlarmLogRecordDO::getAlarmKey, busIndexNew.getBusKey())
                            .eq(AlarmLogRecordDO::getAlarmStatus, AlarmStatusEnums.UNTREATED.getStatus()));
                }
            }
        }

    }

    public String getLocationByBusId (BusIndex busIndex) {
        String location = busIndex.getBusKey();
        //设备位置
        AisleBar aisleBar = aisleBarMapper.selectOne(new LambdaQueryWrapper<AisleBar>().eq(AisleBar::getBusKey, busIndex.getBusKey()));
        if (aisleBar != null) {
            Object aisle = redisTemplate.opsForValue().get(FieldConstant.REDIS_KEY_AISLE + aisleBar.getAisleId());
            if (aisle != null) {
                JSONObject json = JSON.parseObject(JSON.toJSONString(aisle));
                location = json.getString("room_name") + FieldConstant.SPLIT_KEY
                        + json.getString("aisle_name") + FieldConstant.SPLIT_KEY
                        + aisleBar.getPath() + "路";
            }
        }
        return location;
    }


}