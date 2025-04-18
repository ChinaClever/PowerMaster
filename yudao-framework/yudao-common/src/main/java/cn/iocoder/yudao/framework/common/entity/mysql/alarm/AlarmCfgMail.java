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
@Data
@TableName("alarm_cfg_mail")
public class AlarmCfgMail implements Serializable {

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