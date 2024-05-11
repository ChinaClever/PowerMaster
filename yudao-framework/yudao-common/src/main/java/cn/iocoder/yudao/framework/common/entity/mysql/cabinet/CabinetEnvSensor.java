package cn.iocoder.yudao.framework.common.entity.mysql.cabinet;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜环境传感器关联表
 * @date 2024/5/10 13:47
 */
@Data
@TableName(value = "cabinet_env_sensor")
public class CabinetEnvSensor implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private int id;


    /**
     * 机柜id
     */
    private int cabinetId;

    /**
     * 机柜前后通道 1前2后
     */
    private int channel;

    /**
     * 位置 1 上 2 中 3下
     */
    private int position;

    /**
     * pdu
     */
    private char pathPdu;

    /**
     * 传感器id
     */
    private int sensorId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
