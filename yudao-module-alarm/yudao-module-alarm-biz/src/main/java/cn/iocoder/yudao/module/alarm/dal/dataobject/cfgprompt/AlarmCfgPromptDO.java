package cn.iocoder.yudao.module.alarm.dal.dataobject.cfgprompt;

import lombok.*;

import java.io.Serializable;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 系统告警配置 DO
 *
 * @author 芋道源码
 */
@TableName("alarm_cfg_prompt")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlarmCfgPromptDO implements Serializable {

    public AlarmCfgPromptDO(Integer id, Integer isEnable, Integer promptType) {
        this.id = id;
        this.isEnable = isEnable;
        this.promptType = promptType;
    }

    private static final long serialVersionUID = 1L;
    /**
     * 主键id
     */
    @TableId
    private Integer id;
    /**
     *  0 未启用 1启用
     */
    private Integer isEnable;
    /**
     * 告警提醒方式：1声音 2邮件 3短信 4MQ
     */
    private Integer promptType;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}