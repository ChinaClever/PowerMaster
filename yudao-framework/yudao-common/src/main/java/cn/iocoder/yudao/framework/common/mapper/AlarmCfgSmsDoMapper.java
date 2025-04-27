package cn.iocoder.yudao.framework.common.mapper;

import cn.iocoder.yudao.framework.common.entity.mysql.alarm.AlarmCfgSms;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * 告警短信接收人配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AlarmCfgSmsDoMapper extends BaseMapper<AlarmCfgSms> {


    @Delete("DELETE FROM alarm_cfg_sms")
    void initCfgSmsData();
}