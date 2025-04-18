package cn.iocoder.yudao.framework.common.mapper;

import cn.iocoder.yudao.framework.common.entity.mysql.alarm.AlarmCfgPrompt;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统告警配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AlarmCfgPromptDoMapper extends BaseMapper<AlarmCfgPrompt> {

    @Delete("DELETE FROM alarm_cfg_prompt")
    void initCfgPromptData();
}