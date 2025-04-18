package cn.iocoder.yudao.framework.common.mapper;


import cn.iocoder.yudao.framework.common.entity.mysql.alarm.AlarmCfgMail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * 告警邮件接收人配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AlarmCfgMailDoMapper extends BaseMapper<AlarmCfgMail> {


    @Delete("DELETE FROM alarm_cfg_mail")
    void initCfgMailData();
}