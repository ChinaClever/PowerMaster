package cn.iocoder.yudao.module.system.service.alarm;

import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import cn.iocoder.yudao.framework.common.enums.AlarmLevelEnums;
import cn.iocoder.yudao.framework.common.enums.AlarmStatusEnums;
import cn.iocoder.yudao.framework.common.enums.AlarmTypeEnums;
import cn.iocoder.yudao.framework.common.enums.DeviceTypeEnums;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.record.AlarmRecordPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.record.AlarmRecordRespVO;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.record.AlarmRecordSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemAlarmConfig;
import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemAlarmRecord;
import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemMailAlarmConfig;
import cn.iocoder.yudao.module.system.dal.mysql.alarm.SysAlarmRecordMapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.*;

/**
 * 邮箱配置 Service 实现类
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@Service
@Validated
@Slf4j
public class AlarmRecordServiceImpl implements AlarmRecordService {

    @Resource
    private SysAlarmRecordMapper alarmRecordMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public Integer updateRecord(AlarmRecordSaveReqVO updateReqVO) {

        if (Objects.nonNull(updateReqVO.getStatus())
        && updateReqVO.getStatus().equals(AlarmStatusEnums.FINISH.getStatus())){
            //结束操作只能在设备是离线时操作
            SystemAlarmRecord record = alarmRecordMapper.selectById(updateReqVO.getId());
            if (Objects.nonNull(record)){
                String devKey = record.getDevKey();
                if (record.getDevType().equals(DeviceTypeEnums.PDU.getType())){
                    ValueOperations ops = redisTemplate.opsForValue();
                    String redisKey = REDIS_KEY_PDU + devKey;
                    Object pdu = ops.get(redisKey);
                    if (Objects.isNull(pdu)){
                        //设备离线
                        alarmRecordMapper.update(null,new LambdaUpdateWrapper<SystemAlarmRecord>()
                                .eq(SystemAlarmRecord::getId,updateReqVO.getId())
                                .set(StringUtils.isNotEmpty(updateReqVO.getConfirmReason()),SystemAlarmRecord::getConfirmReason,updateReqVO.getConfirmReason())
                                .set(Objects.nonNull(updateReqVO.getStatus()),SystemAlarmRecord::getStatus,updateReqVO.getStatus()));
                    }
                }else if (record.getDevType().equals(DeviceTypeEnums.BUS.getType())){
                    ValueOperations ops = redisTemplate.opsForValue();
                    String redisKey = REDIS_KEY_BUS + devKey;
                    Object data = ops.get(redisKey);
                    if (Objects.isNull(data)){
                        //设备离线
                        alarmRecordMapper.update(null,new LambdaUpdateWrapper<SystemAlarmRecord>()
                                .eq(SystemAlarmRecord::getId,updateReqVO.getId())
                                .set(StringUtils.isNotEmpty(updateReqVO.getConfirmReason()),SystemAlarmRecord::getConfirmReason,updateReqVO.getConfirmReason())
                                .set(Objects.nonNull(updateReqVO.getStatus()),SystemAlarmRecord::getStatus,updateReqVO.getStatus()));
                    }
                }else if (record.getDevType().equals(DeviceTypeEnums.BOX.getType())){
                    ValueOperations ops = redisTemplate.opsForValue();
                    String redisKey = REDIS_KEY_BOX + devKey;
                    Object data = ops.get(redisKey);
                    if (Objects.isNull(data)){
                        //设备离线
                        alarmRecordMapper.update(null,new LambdaUpdateWrapper<SystemAlarmRecord>()
                                .eq(SystemAlarmRecord::getId,updateReqVO.getId())
                                .set(StringUtils.isNotEmpty(updateReqVO.getConfirmReason()),SystemAlarmRecord::getConfirmReason,updateReqVO.getConfirmReason())
                                .set(Objects.nonNull(updateReqVO.getStatus()),SystemAlarmRecord::getStatus,updateReqVO.getStatus()));
                    }
                }
            }
        }else {
            alarmRecordMapper.update(null,new LambdaUpdateWrapper<SystemAlarmRecord>()
                    .eq(SystemAlarmRecord::getId,updateReqVO.getId())
                    .set(StringUtils.isNotEmpty(updateReqVO.getConfirmReason()),SystemAlarmRecord::getConfirmReason,updateReqVO.getConfirmReason())
                    .set(Objects.nonNull(updateReqVO.getStatus()),SystemAlarmRecord::getStatus,updateReqVO.getStatus()));
        }
        return updateReqVO.getId();
    }

    @Override
    public void deleteRecord(List<Integer> ids) {

        if (!CollectionUtils.isEmpty(ids)){
            alarmRecordMapper.deleteBatchIds(ids);
        }

    }

    @Override
    public PageResult<AlarmRecordRespVO> getRecordPage(AlarmRecordPageReqVO pageReqVO) {
        PageResult<SystemAlarmRecord> recordPageResult = alarmRecordMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<SystemAlarmRecord>()
                .likeIfPresent(SystemAlarmRecord::getDevKey, pageReqVO.getDevKey())
                .likeIfPresent(SystemAlarmRecord::getDevName, pageReqVO.getDevName())
                .likeIfPresent(SystemAlarmRecord::getDevPosition, pageReqVO.getDevPosition())
                .eqIfPresent(SystemAlarmRecord::getAlarmLevel , pageReqVO.getAlarmLevel())
                .eqIfPresent(SystemAlarmRecord::getAlarmType , pageReqVO.getAlarmType())
                .eqIfPresent(SystemAlarmRecord::getDevType , pageReqVO.getDevType())
                .inIfPresent(SystemAlarmRecord::getStatus,pageReqVO.getStatus())
                .and(StringUtils.isNotEmpty(pageReqVO.getLikeName()),wrapper ->wrapper
                        .like(SystemAlarmRecord::getDevKey, pageReqVO.getLikeName())
                        .or()
                        .like(SystemAlarmRecord::getDevName, pageReqVO.getLikeName())
                        .or()
                        .like(SystemAlarmRecord::getDevPosition, pageReqVO.getLikeName()))
                .orderByDesc(SystemAlarmRecord::getCreateTime));
        List<AlarmRecordRespVO> recordRespVOS = new ArrayList<>();
        if (Objects.nonNull(recordPageResult)){
              List<SystemAlarmRecord> list = recordPageResult.getList();
              if (!CollectionUtils.isEmpty(list)){
                  list.forEach(record ->{
                      AlarmRecordRespVO recordRespVO = BeanUtils.toBean(record, AlarmRecordRespVO.class);
                      recordRespVO.setAlarmLevelDesc(AlarmLevelEnums.getNameByStatus(record.getAlarmLevel()));
                      recordRespVO.setAlarmTypeDesc(AlarmTypeEnums.getNameByStatus(record.getAlarmType()));
                      recordRespVO.setDevTypeDesc(DeviceTypeEnums.getNameByStatus(record.getDevType()));
                      recordRespVO.setStatusDesc(AlarmStatusEnums.getNameByStatus(record.getStatus()));
                      recordRespVOS.add(recordRespVO);
                  });
              }
        }
        PageResult<AlarmRecordRespVO> result = new PageResult<AlarmRecordRespVO>().setList(recordRespVOS).setTotal(recordPageResult.getTotal());
        return  result;
    }

    @Override
    public Map<Integer, Integer> levelCount() {
        Map<Integer,Integer> map = new HashMap<>();
        for (int i = 0; i < AlarmLevelEnums.values().length; i++) {
            map.put(AlarmLevelEnums.values()[i].getStatus(),0);
        }
        LambdaQueryWrapper<SystemAlarmRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemAlarmRecord::getStatus,AlarmStatusEnums.UNTREATED.getStatus());
        queryWrapper.select(SystemAlarmRecord::getAlarmLevel,SystemAlarmRecord::getCount);
        queryWrapper.groupBy(SystemAlarmRecord::getAlarmLevel);
        List<SystemAlarmRecord> list = alarmRecordMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(list)){
            Map<Integer,Integer>  statusMap = list.stream().collect(Collectors.toMap(SystemAlarmRecord::getAlarmLevel,SystemAlarmRecord::getCount));
            statusMap.keySet().forEach(key -> map.put(key,statusMap.get(key)));
        }
        return map;
    }
}
