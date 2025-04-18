package cn.iocoder.yudao.framework.common.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * 告警短信接收人配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AlarmSmsMapper {

    @Delete("DELETE FROM alarm_cfg_sms")
    void initCfgSmsData();
}