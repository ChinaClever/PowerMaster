package cn.iocoder.yudao.module.alarm.service.logrecord;

import cn.iocoder.yudao.framework.common.constant.FieldConstant;
import cn.iocoder.yudao.framework.common.constant.JobHandlerConstants;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBar;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleCfg;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BusIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetCronConfig;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.pdu.PduIndexDo;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.enums.*;
import cn.iocoder.yudao.framework.common.mapper.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.alarm.constants.DBTable;
import cn.iocoder.yudao.module.alarm.controller.admin.logrecord.vo.AlarmLogRecordPageReqVO;
import cn.iocoder.yudao.module.alarm.controller.admin.logrecord.vo.AlarmLogRecordRespVO;
import cn.iocoder.yudao.module.alarm.controller.admin.logrecord.vo.AlarmLogRecordSaveReqVO;
import cn.iocoder.yudao.module.alarm.controller.admin.logrecord.vo.AlarmLogRecordStatisticsVO;
import cn.iocoder.yudao.module.alarm.dal.dataobject.logrecord.AlarmLogRecordDO;
import cn.iocoder.yudao.module.alarm.dal.mysql.logrecord.AlarmLogRecordMapper;
import cn.iocoder.yudao.module.infra.api.job.JobApi;
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

import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    @Autowired
    private RoomIndexMapper roomIndexMapper;

    @Autowired
    private AisleCfgMapper aisleCfgMapper;

    @Autowired
    private JobApi jobApi;

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
                    .set(StringUtils.isNotEmpty(updateReqVO.getConfirmReason()), AlarmLogRecordDO::getFinishReason, updateReqVO.getConfirmReason()));
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
                .eqIfPresent(AlarmLogRecordDO::getRoomId, pageReqVO.getRoomId())
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
    public PageResult<AlarmLogRecordRespVO> getPduLogRecordPage(AlarmLogRecordPageReqVO pageReqVO) {
        Page page = new Page(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        Integer alarmLevel = AlarmLevelEnums.getStatusByName(pageReqVO.getLikeName());
        Integer alarmType = AlarmTypeEnums.getStatusByName(pageReqVO.getLikeName());
        Page<AlarmLogRecordDO> recordPageResult = new Page<>();
        if (pageReqVO.getAlarmType() == 4) {
            recordPageResult = logRecordMapper.selectPage(page, new LambdaQueryWrapperX<AlarmLogRecordDO>()
                    .inIfPresent(AlarmLogRecordDO::getAlarmStatus, pageReqVO.getAlarmStatus())
                    .eqIfPresent(AlarmLogRecordDO::getAlarmLevel, alarmLevel)
                    .eqIfPresent(AlarmLogRecordDO::getRoomId, pageReqVO.getRoomId())
                    .inIfPresent(AlarmLogRecordDO::getAlarmType, 4, 5)
                    .betweenIfPresent(AlarmLogRecordDO::getStartTime, pageReqVO.getPduStartTime(), pageReqVO.getPduFinishTime())
                    .and(StringUtils.isNotEmpty(pageReqVO.getLikeName()) && alarmLevel == null && alarmType == null, wrapper -> wrapper
                            .like(AlarmLogRecordDO::getAlarmKey, pageReqVO.getLikeName())
                            .or()
                            .like(AlarmLogRecordDO::getAlarmDesc, pageReqVO.getLikeName())
                            .or()
                            .like(AlarmLogRecordDO::getAlarmPosition, pageReqVO.getLikeName()))
                    .orderByDesc(AlarmLogRecordDO::getCreateTime));
        } else {
            recordPageResult = logRecordMapper.selectPage(page, new LambdaQueryWrapperX<AlarmLogRecordDO>()
                    .inIfPresent(AlarmLogRecordDO::getAlarmStatus, pageReqVO.getAlarmStatus())
                    .eqIfPresent(AlarmLogRecordDO::getAlarmLevel, alarmLevel)
                    .eqIfPresent(AlarmLogRecordDO::getRoomId, pageReqVO.getRoomId())
                    .inIfPresent(AlarmLogRecordDO::getAlarmType, 1, 2, 3)
                    .betweenIfPresent(AlarmLogRecordDO::getStartTime, pageReqVO.getPduStartTime(), pageReqVO.getPduFinishTime())
                    .and(StringUtils.isNotEmpty(pageReqVO.getLikeName()) && alarmLevel == null && alarmType == null, wrapper -> wrapper
                            .like(AlarmLogRecordDO::getAlarmKey, pageReqVO.getLikeName())
                            .or()
                            .like(AlarmLogRecordDO::getAlarmDesc, pageReqVO.getLikeName())
                            .or()
                            .like(AlarmLogRecordDO::getAlarmPosition, pageReqVO.getLikeName()))
                    .orderByDesc(AlarmLogRecordDO::getCreateTime));
        }
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
    public AlarmLogRecordStatisticsVO levelCount(Integer roomId) {
        return logRecordMapper.levelCount(roomId);
    }

    @Override
    public AlarmLogRecordDO getAlarmRecordById(Integer id) {
        return logRecordMapper.selectById(id);
    }

    @Override
    public Integer insertOrUpdateAlarmRecordWhenPduAlarm(List<Map<String, Object>> oldMaps, List<Map<String, Object>> newMaps) {
        Integer result = null;
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
                        String pdu_alarm = pduJson.get(FieldConstant.PDU_ALARM) == null ? "" : pduJson.get(FieldConstant.PDU_ALARM).toString();
                        alarmRecord.setAlarmDesc(pdu_alarm);
                        Object datetime = pduJson.get(FieldConstant.DATETIME);
                        if (datetime != null) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            LocalDateTime startTime = LocalDateTime.parse(datetime.toString(), formatter);
                            alarmRecord.setStartTime(startTime);
                        } else {
                            alarmRecord.setStartTime(LocalDateTime.now());
                        }
                        // 级联关系id
                        CabinetIndex cabinetIndex = cabinetIndexMapper.selectByPduKey(pduIndexDoNew.getPduKey());
                        if (cabinetIndex != null) {
                            alarmRecord.setRoomId(cabinetIndex.getRoomId());
                            alarmRecord.setAisleId(cabinetIndex.getAisleId());
                            alarmRecord.setCabinetId(cabinetIndex.getId());
                        }
                        result = logRecordMapper.insert(alarmRecord);
                    }
                } else if (alarmCodeList.contains(pduIndexDoOld.getRunStatus()) && PduStatusEnum.NORMAL.getStatus().equals(pduIndexDoNew.getRunStatus())) {
                    result = logRecordMapper.update(new LambdaUpdateWrapper<AlarmLogRecordDO>()
                            .set(AlarmLogRecordDO::getAlarmStatus, AlarmStatusEnums.FINISH.getStatus())
                            .set(AlarmLogRecordDO::getFinishTime, LocalDateTime.now())
                            .set(AlarmLogRecordDO::getFinishReason, "状态恢复正常")
                            .eq(AlarmLogRecordDO::getAlarmKey, pduIndexDoNew.getPduKey())
                            .eq(AlarmLogRecordDO::getAlarmStatus, AlarmStatusEnums.UNTREATED.getStatus()));
                }
            }
        }

        return result;
    }


    @Override
    public Integer insertOrUpdateAlarmRecordWhenBusAlarm(List<Map<String, Object>> oldMaps, List<Map<String, Object>> newMaps) {
        Integer result = null;
        if (!CollectionUtils.isEmpty(oldMaps) && !CollectionUtils.isEmpty(newMaps)) {
            ValueOperations ops = redisTemplate.opsForValue();
            List<BusIndex> busIndexListOld = BeanUtils.toBean(oldMaps, BusIndex.class);
            List<BusIndex> busIndexListNew = BeanUtils.toBean(newMaps, BusIndex.class);
            for (int i = 0; i < busIndexListOld.size(); i++) {
                BusIndex busIndexOld = busIndexListOld.get(i);
                BusIndex busIndexNew = busIndexListNew.get(i);
                List<Integer> alarmCodeList = new ArrayList<>();
                alarmCodeList.add(BusStatusEnum.OFF_LINE.getStatus());
                alarmCodeList.add(BusStatusEnum.ALARM.getStatus());
                if (alarmCodeList.contains(busIndexNew.getRunStatus()) && !busIndexOld.getRunStatus().equals(busIndexNew.getRunStatus())) {
                    AlarmLogRecordDO alarmRecord = new AlarmLogRecordDO();
                    alarmRecord.setAlarmKey(busIndexNew.getBusKey());
                    alarmRecord.setAlarmStatus(AlarmStatusEnums.UNTREATED.getStatus());
                    if (busIndexNew.getRunStatus().equals(BusStatusEnum.ALARM.getStatus())) {
                        alarmRecord.setAlarmType(AlarmTypeEnums.BUS_ALARM.getType());
                        alarmRecord.setAlarmLevel(AlarmLevelEnums.TWO.getStatus());
                    } else if (busIndexNew.getRunStatus().equals(BusStatusEnum.OFF_LINE.getStatus())) {
                        alarmRecord.setAlarmType(AlarmTypeEnums.BUS_OFF_LINE.getType());
                        alarmRecord.setAlarmLevel(AlarmLevelEnums.TWO.getStatus());
                    }
                    // 告警位置
                    String location = getLocationByBusId(busIndexNew);
                    alarmRecord.setAlarmPosition(location);
                    // 告警描述、告警开始时间
                    JSONObject busJson = (JSONObject) ops.get(FieldConstant.REDIS_KEY_BUS + busIndexNew.getBusKey());
                    if (busJson != null) {
                        String bus_alarm = busJson.get(FieldConstant.DEV_ALARM) == null ? "" : busJson.get(FieldConstant.DEV_ALARM).toString();
                        alarmRecord.setAlarmDesc(bus_alarm);
                        Object datetime = busJson.get(FieldConstant.DATETIME);
                        if (datetime != null) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            LocalDateTime startTime = LocalDateTime.parse(datetime.toString(), formatter);
                            alarmRecord.setStartTime(startTime);
                        } else {
                            alarmRecord.setStartTime(LocalDateTime.now());
                        }
                        // 级联关系id
                        AisleIndex aisleIndex = aisleIndexMapper.selectByBusKey(busIndexNew.getBusKey());
                        if (aisleIndex != null) {
                            alarmRecord.setRoomId(aisleIndex.getRoomId());
                            alarmRecord.setAisleId(aisleIndex.getId());
                        }
                        result = logRecordMapper.insert(alarmRecord);
                    }
                } else if (alarmCodeList.contains(busIndexOld.getRunStatus()) && BusStatusEnum.NORMAL.getStatus().equals(busIndexNew.getRunStatus())) {
                    result = logRecordMapper.update(new LambdaUpdateWrapper<AlarmLogRecordDO>()
                            .set(AlarmLogRecordDO::getAlarmStatus, AlarmStatusEnums.FINISH.getStatus())
                            .set(AlarmLogRecordDO::getFinishTime, LocalDateTime.now())
                            .set(AlarmLogRecordDO::getFinishReason, "状态恢复正常")
                            .eq(AlarmLogRecordDO::getAlarmKey, busIndexNew.getBusKey())
                            .eq(AlarmLogRecordDO::getAlarmStatus, AlarmStatusEnums.UNTREATED.getStatus()));
                }
            }
        }
        return result;
    }

    @Override
    public Integer insertOrUpdateAlarmRecordWhenCabinetAlarm(List<Map<String, Object>> oldMaps, List<Map<String, Object>> newMaps) {
        Integer result = null;
        if (!CollectionUtils.isEmpty(oldMaps) && !CollectionUtils.isEmpty(newMaps)) {
            ValueOperations ops = redisTemplate.opsForValue();
            List<CabinetIndex> CabinetIndexListOld = BeanUtils.toBean(oldMaps, CabinetIndex.class);
            List<CabinetIndex> CabinetIndexListNew = BeanUtils.toBean(newMaps, CabinetIndex.class);
            for (int i = 0; i < CabinetIndexListOld.size(); i++) {
                CabinetIndex cabinetIndexOld = CabinetIndexListOld.get(i);
                CabinetIndex cabinetIndexNew = CabinetIndexListNew.get(i);
                List<Integer> alarmCodeList = new ArrayList<>();
                alarmCodeList.add(CabinetStatusEnum.EARLY_WARNING.getStatus());
                alarmCodeList.add(CabinetStatusEnum.ALARM.getStatus());
                if (alarmCodeList.contains(cabinetIndexNew.getRunStatus()) && cabinetIndexOld.getRunStatus() != cabinetIndexNew.getRunStatus()) {
                    AlarmLogRecordDO alarmRecord = new AlarmLogRecordDO();
                    String alarmKey = cabinetIndexNew.getRoomId() + FieldConstant.SPLIT_KEY + cabinetIndexNew.getId();
                    alarmRecord.setAlarmKey(alarmKey);
                    alarmRecord.setAlarmStatus(AlarmStatusEnums.UNTREATED.getStatus());
                    if (cabinetIndexNew.getRunStatus() == CabinetStatusEnum.ALARM.getStatus()) {
                        alarmRecord.setAlarmType(AlarmTypeEnums.CABINET_CAPACITY_ALARM.getType());
                        alarmRecord.setAlarmLevel(AlarmLevelEnums.TWO.getStatus());
                    } else if (cabinetIndexNew.getRunStatus() == CabinetStatusEnum.EARLY_WARNING.getStatus()) {
                        alarmRecord.setAlarmType(AlarmTypeEnums.CABINET_CAPACITY_WARNING.getType());
                        alarmRecord.setAlarmLevel(AlarmLevelEnums.THREE.getStatus());
                    }

                    JSONObject cabinetJson = (JSONObject) ops.get(FieldConstant.REDIS_KEY_CABINET + alarmKey);
                    if (cabinetJson != null) {
                        // 告警描述
                        String alarmDesc = getOverCapacityAlarmDesc(cabinetJson,DBTable.CABINET_INDEX);
                        alarmRecord.setAlarmDesc(alarmDesc);
                        // 告警开始时间
                        Object datetime = cabinetJson.get(FieldConstant.DATETIME);
                        if (datetime != null) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            LocalDateTime startTime = LocalDateTime.parse(datetime.toString(), formatter);
                            alarmRecord.setStartTime(startTime);
                        } else {
                            alarmRecord.setStartTime(LocalDateTime.now());
                        }
                        // 告警位置
                        String roomName = cabinetJson.get(FieldConstant.ROOM_NAME) + "";
                        String aisleName = cabinetJson.get(FieldConstant.AISLE_NAME) + "";
                        String cabinetName = cabinetJson.get(FieldConstant.CABINET_NAME) + "";
                        String location = roomName + "-" + aisleName + "-" + cabinetName;
                        alarmRecord.setAlarmPosition(location);
                        // 级联关系id
                        CabinetIndex cabinetIndex = cabinetIndexMapper.selectById(cabinetIndexNew.getId());
                        if (cabinetIndex != null) {
                            alarmRecord.setRoomId(cabinetIndex.getRoomId());
                            alarmRecord.setAisleId(cabinetIndex.getAisleId());
                            alarmRecord.setCabinetId(cabinetIndex.getId());
                        }
                        result = logRecordMapper.insert(alarmRecord);
                    }
                } else if (alarmCodeList.contains(cabinetIndexOld.getRunStatus()) && BusStatusEnum.NORMAL.getStatus() == cabinetIndexNew.getRunStatus()) {
                    result = logRecordMapper.update(new LambdaUpdateWrapper<AlarmLogRecordDO>()
                            .set(AlarmLogRecordDO::getAlarmStatus, AlarmStatusEnums.FINISH.getStatus())
                            .set(AlarmLogRecordDO::getFinishTime, LocalDateTime.now())
                            .set(AlarmLogRecordDO::getFinishReason, "状态恢复正常")
                            .eq(AlarmLogRecordDO::getAlarmKey, cabinetIndexNew.getRoomId() + FieldConstant.SPLIT_KEY + cabinetIndexNew.getId())
                            .eq(AlarmLogRecordDO::getAlarmStatus, AlarmStatusEnums.UNTREATED.getStatus()));
                }
            }
        }
        return result;
    }


    @Override
    public Integer insertOrUpdateAlarmRecordWhenAisleAlarm(List<Map<String, Object>> oldMaps, List<Map<String, Object>> newMaps) {
        Integer result = null;
        if (!CollectionUtils.isEmpty(oldMaps) && !CollectionUtils.isEmpty(newMaps)) {
            ValueOperations ops = redisTemplate.opsForValue();
            List<AisleIndex> aisleIndexListOld = BeanUtils.toBean(oldMaps, AisleIndex.class);
            List<AisleIndex> aisleIndexListNew = BeanUtils.toBean(newMaps, AisleIndex.class);
            for (int i = 0; i < aisleIndexListOld.size(); i++) {
                AisleIndex aisleIndexOld = aisleIndexListOld.get(i);
                AisleIndex aisleIndexNew = aisleIndexListNew.get(i);
                List<Integer> alarmCodeList = new ArrayList<>();
                alarmCodeList.add(AisleStatusEnum.EARLY_WARNING.getStatus());
                alarmCodeList.add(AisleStatusEnum.ALARM.getStatus());
                if (alarmCodeList.contains(aisleIndexNew.getLoadRateStatus()) && !Objects.equals(aisleIndexOld.getLoadRateStatus(), aisleIndexNew.getLoadRateStatus())) {
                    AlarmLogRecordDO alarmRecord = new AlarmLogRecordDO();
                    String alarmKey = aisleIndexNew.getId()+"";
                    alarmRecord.setAlarmKey(alarmKey);
                    alarmRecord.setAlarmStatus(AlarmStatusEnums.UNTREATED.getStatus());
                    if (Objects.equals(aisleIndexNew.getLoadRateStatus(), AisleStatusEnum.ALARM.getStatus())) {
                        alarmRecord.setAlarmType(AlarmTypeEnums.AISLE_CAPACITY_ALARM.getType());
                        alarmRecord.setAlarmLevel(AlarmLevelEnums.TWO.getStatus());
                    } else if (Objects.equals(aisleIndexNew.getLoadRateStatus(), AisleStatusEnum.EARLY_WARNING.getStatus())) {
                        alarmRecord.setAlarmType(AlarmTypeEnums.AISLE_CAPACITY_WARNING.getType());
                        alarmRecord.setAlarmLevel(AlarmLevelEnums.THREE.getStatus());
                    }

                    JSONObject aisleJson = (JSONObject) ops.get(FieldConstant.REDIS_KEY_AISLE + alarmKey);
                    if (aisleJson != null) {
                        // 告警描述
                        String alarmDesc = getOverCapacityAlarmDesc(aisleJson,DBTable.AISLE_INDEX);
                        alarmRecord.setAlarmDesc(alarmDesc);
                        // 告警开始时间
                        Object datetime = aisleJson.get(FieldConstant.DATETIME);
                        if (datetime != null) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            LocalDateTime startTime = LocalDateTime.parse(datetime.toString(), formatter);
                            alarmRecord.setStartTime(startTime);
                        } else {
                            alarmRecord.setStartTime(LocalDateTime.now());
                        }
                        // 告警位置
                        alarmRecord.setAlarmPosition(aisleJson.getString(FieldConstant.ROOM_NAME) + FieldConstant.SPLIT_KEY + aisleIndexNew.getAisleName());
                        // 级联关系id
                        alarmRecord.setAisleId(aisleIndexNew.getId());
                        alarmRecord.setRoomId(aisleIndexNew.getRoomId());
                        result = logRecordMapper.insert(alarmRecord);
                    }
                } else if (alarmCodeList.contains(aisleIndexOld.getLoadRateStatus())
                        && (Objects.equals(RoomStatusEnum.NORMAL.getStatus(), aisleIndexNew.getLoadRateStatus())
                        || Objects.equals(RoomStatusEnum.SAFE.getStatus(), aisleIndexNew.getLoadRateStatus()))) {
                    result = logRecordMapper.update(new LambdaUpdateWrapper<AlarmLogRecordDO>()
                            .set(AlarmLogRecordDO::getAlarmStatus, AlarmStatusEnums.FINISH.getStatus())
                            .set(AlarmLogRecordDO::getFinishTime, LocalDateTime.now())
                            .set(AlarmLogRecordDO::getFinishReason, "状态恢复正常")
                            .eq(AlarmLogRecordDO::getAlarmKey, aisleIndexNew.getRoomId() + FieldConstant.SPLIT_KEY + aisleIndexNew.getId())
                            .in(AlarmLogRecordDO::getAlarmType,  AlarmTypeEnums.AISLE_CAPACITY_ALARM.getType(), AlarmTypeEnums.AISLE_CAPACITY_WARNING.getType())
                            .eq(AlarmLogRecordDO::getAlarmStatus, AlarmStatusEnums.UNTREATED.getStatus()));
                }
            }
        }
        return result;
    }
    @Override
    public Integer insertOrUpdateAlarmRecordWhenRoomAlarm(List<Map<String, Object>> oldMaps, List<Map<String, Object>> newMaps) {
        Integer result = null;
        if (!CollectionUtils.isEmpty(oldMaps) && !CollectionUtils.isEmpty(newMaps)) {
            ValueOperations ops = redisTemplate.opsForValue();
            List<RoomIndex> roomIndexListOld = BeanUtils.toBean(oldMaps, RoomIndex.class);
            List<RoomIndex> roomIndexListNew = BeanUtils.toBean(newMaps, RoomIndex.class);
            for (int i = 0; i < roomIndexListOld.size(); i++) {
                RoomIndex roomIndexOld = roomIndexListOld.get(i);
                RoomIndex roomIndexNew = roomIndexListNew.get(i);
                List<Integer> alarmCodeList = new ArrayList<>();
                alarmCodeList.add(RoomStatusEnum.EARLY_WARNING.getStatus());
                alarmCodeList.add(RoomStatusEnum.ALARM.getStatus());
                if (alarmCodeList.contains(roomIndexNew.getLoadRateStatus()) && !Objects.equals(roomIndexOld.getLoadRateStatus(), roomIndexNew.getLoadRateStatus())) {
                    AlarmLogRecordDO alarmRecord = new AlarmLogRecordDO();
                    String alarmKey = roomIndexNew.getId()+"";
                    alarmRecord.setAlarmKey(alarmKey);
                    alarmRecord.setAlarmStatus(AlarmStatusEnums.UNTREATED.getStatus());
                    if (Objects.equals(roomIndexNew.getLoadRateStatus(), RoomStatusEnum.ALARM.getStatus())) {
                        alarmRecord.setAlarmType(AlarmTypeEnums.ROOM_CAPACITY_ALARM.getType());
                        alarmRecord.setAlarmLevel(AlarmLevelEnums.TWO.getStatus());
                    } else if (Objects.equals(roomIndexNew.getLoadRateStatus(), RoomStatusEnum.EARLY_WARNING.getStatus())) {
                        alarmRecord.setAlarmType(AlarmTypeEnums.ROOM_CAPACITY_WARNING.getType());
                        alarmRecord.setAlarmLevel(AlarmLevelEnums.THREE.getStatus());
                    }

                    JSONObject roomJson = (JSONObject) ops.get(FieldConstant.REDIS_KEY_ROOM + alarmKey);
                    if (roomJson != null) {
                        // 告警描述
                        String alarmDesc = getOverCapacityAlarmDesc(roomJson,DBTable.ROOM_INDEX);
                        alarmRecord.setAlarmDesc(alarmDesc);
                        // 告警开始时间
                        Object datetime = roomJson.get(FieldConstant.DATETIME);
                        if (datetime != null) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            LocalDateTime startTime = LocalDateTime.parse(datetime.toString(), formatter);
                            alarmRecord.setStartTime(startTime);
                        } else {
                            alarmRecord.setStartTime(LocalDateTime.now());
                        }
                        // 告警位置
                        alarmRecord.setAlarmPosition(roomIndexNew.getRoomName());
                        // 级联关系id
                        alarmRecord.setRoomId(roomIndexNew.getId());
                        result = logRecordMapper.insert(alarmRecord);
                    }
                } else if (alarmCodeList.contains(roomIndexOld.getLoadRateStatus())
                        && (Objects.equals(RoomStatusEnum.NORMAL.getStatus(), roomIndexNew.getLoadRateStatus())
                            || Objects.equals(RoomStatusEnum.SAFE.getStatus(), roomIndexNew.getLoadRateStatus()))) {
                    result = logRecordMapper.update(new LambdaUpdateWrapper<AlarmLogRecordDO>()
                            .set(AlarmLogRecordDO::getAlarmStatus, AlarmStatusEnums.FINISH.getStatus())
                            .set(AlarmLogRecordDO::getFinishTime, LocalDateTime.now())
                            .set(AlarmLogRecordDO::getFinishReason, "状态恢复正常")
                            .eq(AlarmLogRecordDO::getAlarmKey, roomIndexNew.getId().toString())
                            .eq(AlarmLogRecordDO::getAlarmStatus, AlarmStatusEnums.UNTREATED.getStatus()));
                }
            }
        }
        return result;
    }


    @Override
    public void updateCabinetAlarmJob(List<Map<String, Object>> oldMaps, List<Map<String, Object>> newMaps) {
        if (!CollectionUtils.isEmpty(oldMaps) && !CollectionUtils.isEmpty(newMaps)) {
            List<CabinetCronConfig> cabinetCronConfigsOld = BeanUtils.toBean(oldMaps, CabinetCronConfig.class);
            List<CabinetCronConfig> cabinetCronConfigsNew = BeanUtils.toBean(newMaps, CabinetCronConfig.class);
            for (int i = 0; i < cabinetCronConfigsOld.size(); i++) {
                CabinetCronConfig cabinetCronConfigOld = cabinetCronConfigsOld.get(i);
                CabinetCronConfig cabinetCronConfigNew = cabinetCronConfigsNew.get(i);

                if (!cabinetCronConfigOld.getEqDayCron().equals(cabinetCronConfigNew.getEqDayCron())) {
                    // 更新每日定时任务
                    jobApi.updateCabinetJobCron(JobHandlerConstants.CABINET_DAY_ALARM_JOB, cabinetCronConfigNew.getEqDayCron());
                }
                if (!cabinetCronConfigOld.getEqMonthCron().equals(cabinetCronConfigNew.getEqMonthCron())) {
                    // 更新每月定时任务
                    jobApi.updateCabinetJobCron(JobHandlerConstants.CABINET_MONTH_ALARM_JOB, cabinetCronConfigNew.getEqMonthCron());
                }
            }
        }
    }


    public String getLocationByBusId(BusIndex busIndex) {
        String location = busIndex.getBusKey();
        //设备位置
        AisleBar aisleBar = aisleBarMapper.selectOne(new LambdaQueryWrapper<AisleBar>().eq(AisleBar::getBusKey, busIndex.getBusKey()));
        if (aisleBar != null) {
            Object aisle = redisTemplate.opsForValue().get(FieldConstant.REDIS_KEY_AISLE + aisleBar.getAisleId());
            if (aisle != null) {
                JSONObject json = JSON.parseObject(JSON.toJSONString(aisle));
                location = json.getString(FieldConstant.ROOM_NAME) + FieldConstant.SPLIT_KEY
                        + json.getString(FieldConstant.AISLE_NAME) + FieldConstant.SPLIT_KEY
                        + aisleBar.getPath() + "路";
            }
        }
        return location;
    }

    public Integer getCountByStatus(Integer status) {
        Long count = logRecordMapper.selectCount(new LambdaQueryWrapper<AlarmLogRecordDO>().eq(AlarmLogRecordDO::getAlarmStatus, status));
        return Math.toIntExact(count);
    }

    public String getOverCapacityAlarmDesc(JSONObject json,String dbName) {
        if (json == null) {
            return "";
        }

        String loadFactor = "";
        String powerCapacity = "";
        JSONObject power = null;
        if (DBTable.CABINET_INDEX.equals(dbName)) {
            loadFactor  = json.get(FieldConstant.LOAD_FACTOR) + "";
            powerCapacity = json.get(FieldConstant.POW_CAPACITY) + "";
            power  = (JSONObject) json.get(FieldConstant.CABINET_POWER);
        } else if (DBTable.AISLE_INDEX.equals(dbName)) {
            loadFactor  = json.get(FieldConstant.LOAD_FACTOR) + "";
            AisleCfg aisleCfg = aisleCfgMapper.selectOne(new LambdaQueryWrapper<AisleCfg>().eq(AisleCfg::getAisleId, json.get(FieldConstant.AISLE_KEY)));
            powerCapacity = aisleCfg.getPowerCapacity() + "";
            power  = (JSONObject) json.get(FieldConstant.AISLE_POWER);
        } else if (DBTable.ROOM_INDEX.equals(dbName)){
            loadFactor = json.get(FieldConstant.ROOM_LOAD_FACTOR) + "";
            RoomIndex roomIndex = roomIndexMapper.selectById((Serializable) json.get(FieldConstant.ROOM_KEY));
            powerCapacity = roomIndex.getPowerCapacity() + "";
            power = (JSONObject) json.get(FieldConstant.ROOM_POWER);
        }
        // 负载率处理
        DecimalFormat decimalFormat = new DecimalFormat("0.0%");
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        loadFactor = decimalFormat.format(Double.parseDouble(loadFactor) / 100);

        // 电力容量
        DecimalFormat decimalFormat1 = new DecimalFormat("0.000");
        decimalFormat1.setRoundingMode(RoundingMode.HALF_UP);
        powerCapacity = decimalFormat1.format(Double.parseDouble(powerCapacity));

        // 有功功率
        JSONObject totalData = (JSONObject) power.get(FieldConstant.TOTAL_DATA);
        String activePow = totalData.get(FieldConstant.ACTIVE_POW) + "";
        activePow = decimalFormat1.format(Double.parseDouble(activePow));

        // 描述
        String desc = "";
        if (DBTable.CABINET_INDEX.equals(dbName)) {
            desc = "当前机柜有功负载率：" + loadFactor + "，机柜额定有功容量：" + powerCapacity + "KW" + "，实际有功功率：" + activePow + "KW";
        } else if (DBTable.AISLE_INDEX.equals(dbName)) {
            desc = "当前柜列有功负载率：" + loadFactor + "，柜列额定有功容量：" + powerCapacity + "KW" + "，实际有功功率：" + activePow + "KW";
        } else {
            desc = "当前机房有功负载率：" + loadFactor + "，机房额定有功容量：" + powerCapacity + "KW" + "，实际有功功率：" + activePow + "KW";
        }
        return desc;
    }


}