package cn.iocoder.yudao.framework.common.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * 告警邮件接收人配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AlarmMailMapper {

    @Delete("DELETE FROM alarm_cfg_mail")
    void initCfgMailData();
}