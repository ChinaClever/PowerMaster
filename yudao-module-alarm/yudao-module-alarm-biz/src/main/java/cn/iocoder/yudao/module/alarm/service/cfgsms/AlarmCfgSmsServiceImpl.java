package cn.iocoder.yudao.module.alarm.service.cfgsms;

import cn.iocoder.yudao.framework.common.enums.AlarmLevelEnums;
import cn.iocoder.yudao.framework.common.enums.AlarmTypeEnums;
import cn.iocoder.yudao.framework.common.enums.DisableEnums;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.alarm.dal.dataobject.logrecord.AlarmLogRecordDO;
import cn.iocoder.yudao.module.system.service.sms.SmsSendService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import cn.iocoder.yudao.module.alarm.controller.admin.cfgsms.vo.*;
import cn.iocoder.yudao.module.alarm.dal.dataobject.cfgsms.AlarmCfgSmsDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.alarm.dal.mysql.cfgsms.AlarmCfgSmsMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * 告警短信接收人配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class AlarmCfgSmsServiceImpl implements AlarmCfgSmsService {

    @Resource
    private AlarmCfgSmsMapper cfgSmsMapper;

    @Resource
    SmsSendService smsSendService;

    public static final String TEMPLATE_CODE_SMS = "ALARM_SMS";

    @Override
    public Integer saveCfgSms(AlarmCfgSmsSaveReqVO saveReqVOS) {
        // 插入
        AlarmCfgSmsDO cfgSms = BeanUtils.toBean(saveReqVOS, AlarmCfgSmsDO.class);
        if (Objects.nonNull(saveReqVOS.getId())){
            //修改
            cfgSmsMapper.updateById(cfgSms);
        }else {
            //新增
            cfgSmsMapper.insert(cfgSms);
        }
        // 返回
        return cfgSms.getId();
    }

    @Override
    public void batchSave(List<AlarmCfgSmsPageReqVO> saveReqVOS) {
        if (!CollectionUtils.isEmpty(saveReqVOS)){
            List<AlarmCfgSmsDO> list = BeanUtils.toBean(saveReqVOS, AlarmCfgSmsDO.class);
            cfgSmsMapper.insertBatch(list);
        }
    }

    @Override
    public void updateCfgSms(AlarmCfgSmsSaveReqVO updateReqVO) {
        // 校验存在
        validateCfgSmsExists(updateReqVO.getId());
        // 更新
        AlarmCfgSmsDO updateObj = BeanUtils.toBean(updateReqVO, AlarmCfgSmsDO.class);
        cfgSmsMapper.updateById(updateObj);
    }

    @Override
    public void deleteCfgSms(Integer id) {
        // 校验存在
        validateCfgSmsExists(id);
        // 删除
        cfgSmsMapper.deleteById(id);
    }

    @Override
    public void deleteSmsAll() {
        cfgSmsMapper.delete(null);
    }

    private void validateCfgSmsExists(Integer id) {
        if (cfgSmsMapper.selectById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
    }

    @Override
    public AlarmCfgSmsDO getCfgSms(Integer id) {
        return cfgSmsMapper.selectById(id);
    }

    @Override
    public PageResult<AlarmCfgSmsDO> getCfgSmsPage(AlarmCfgSmsPageReqVO pageReqVO) {
        return cfgSmsMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<AlarmCfgSmsDO>()
                .likeIfPresent(AlarmCfgSmsDO::getPhoneNumber, pageReqVO.getPhoneNumber())
                .eqIfPresent(AlarmCfgSmsDO::getIsEnable, pageReqVO.getIsEnable()));
    }

    @Override
    public List<AlarmCfgSmsDO> getPhoneList() {
        return cfgSmsMapper.selectList(new LambdaUpdateWrapper<>());
    }

    @Override
    public void sendSms(AlarmLogRecordDO record) {

        List<AlarmCfgSmsDO> smsAlarmConfigs = cfgSmsMapper.selectList(new LambdaQueryWrapperX<AlarmCfgSmsDO>()
                .eq(AlarmCfgSmsDO::getIsEnable, DisableEnums.ENABLE.getStatus()));

        //报警信息
        Map<String, Object> templateParam = new HashMap<>();
        templateParam.put("position",record.getAlarmPosition());
        templateParam.put("alarmType", AlarmTypeEnums.getNameByStatus(record.getAlarmType()));
        templateParam.put("alarmLevel", AlarmLevelEnums.getNameByStatus(record.getAlarmLevel()));
        templateParam.put("alarmDesc",record.getAlarmDesc());

        if (!org.springframework.util.CollectionUtils.isEmpty(smsAlarmConfigs)){
            smsAlarmConfigs.forEach(config -> {
                String phone = config.getPhoneNumber();
                smsSendService.sendSingleSms(phone,null,null,TEMPLATE_CODE_SMS,templateParam);
            });
        }

    }



}