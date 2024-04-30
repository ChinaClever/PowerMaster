package cn.iocoder.yudao.framework.common.entity.mysql.cabinet;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜配置表
 * @date 2024/4/28 13:53
 */
@Data
@TableName(value = "cabinet_cfg")
public class CabinetCfg implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private int id;


    /**
     * 机柜id
     */
    private int cabinetId;

    /**
     * 机柜名称
     */
    private String cabinetName;

    /**
     * 机柜高度
     */
    private int cabinetHeight;

    /**
     * 机柜类型
     */
    private String type;

    /**
     * 注释
     */
    private int xCoordinate;

    /**
     * 类名
     */
    private int yCoordinate;

    /**
     * 所属于公司
     */
    private String company;

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
