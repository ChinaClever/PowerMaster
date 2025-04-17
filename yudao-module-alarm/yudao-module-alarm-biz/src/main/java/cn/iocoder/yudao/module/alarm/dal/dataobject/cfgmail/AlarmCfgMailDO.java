package cn.iocoder.yudao.module.alarm.dal.dataobject.cfgmail;

import lombok.*;

import java.io.Serializable;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 告警邮件接收人配置 DO
 *
 * @author 芋道源码
 */

@Data
@TableName("alarm_cfg_mail")
@NoArgsConstructor
@AllArgsConstructor
public class AlarmCfgMailDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId
    private Integer id;
    /**
     * 是否可用 0 可用 1 不可用
     */
    private Integer isEnable;
    /**
     * 邮箱地址
     */
    private String mailAddr;
    /**
     * 描述
     */
    private String mailDesc;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}