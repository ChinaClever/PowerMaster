package cn.iocoder.yudao.module.alarm.dal.dataobject.cfgsms;

import lombok.*;

import java.io.Serializable;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 告警短信接收人配置 DO
 *
 * @author 芋道源码
 */
@TableName("alarm_cfg_sms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlarmCfgSmsDO implements Serializable {

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
     * 手机号
     */
    private String phoneNumber;
    /**
     * 描述
     */
    private String smsDesc;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}