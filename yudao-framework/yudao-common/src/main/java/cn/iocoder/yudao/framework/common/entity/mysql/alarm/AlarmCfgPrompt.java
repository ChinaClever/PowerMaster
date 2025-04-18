package cn.iocoder.yudao.framework.common.entity.mysql.alarm;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: TODO
 * @date 2025/4/18 14:02
 */
@TableName("alarm_cfg_prompt")
@Data
public class AlarmCfgPrompt implements Serializable {

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
