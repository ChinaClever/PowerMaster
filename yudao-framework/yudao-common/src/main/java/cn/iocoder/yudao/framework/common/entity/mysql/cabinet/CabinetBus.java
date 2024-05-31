package cn.iocoder.yudao.framework.common.entity.mysql.cabinet;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜与母线关联表
 * @date 2024/4/23 10:30
 */
@Data
@TableName(value = "cabinet_bus")
public class CabinetBus implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    /**
     * 机柜id
     */
    private int cabinetId;
    /**
     * A路插接箱
     */
    private String devKeyA;
    /**
     * A路插接箱输出位
     */
    private int outletIdA;
    /**
     * B路插接箱
     */
    private String devKeyB;
    /**
     * B路插接箱输出位
     */
    private int outletIdB;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
