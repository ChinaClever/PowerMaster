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
 * @date 2025/4/18 14:01
 */
@TableName("alarm_cfg_sms")
@Data
public class AlarmCfgSms implements Serializable {

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
