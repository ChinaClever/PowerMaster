package cn.iocoder.yudao.framework.common.entity.mysql.cabinet;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜与PDU关联表
 * @date 2024/4/23 10:30
 */
@Data
@TableName(value = "cabinet_pdu")
public class CabinetPdu implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    /**
     * 机柜id
     */
    private int cabinetId;
    /**
     * A路IP地址
     */
    private String pduIpA;
    /**
     * A路级联编号
     */
    private int casIdA;
    /**
     * B路IP地址
     */
    private String pduIpB;
    /**
     * B路级联编号
     */
    private int casIdB;
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
