package cn.iocoder.yudao.framework.common.entity.mysql.aisle;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列与插接箱绑定关系
 * @date 2024/6/7 9:29
 */
@Schema(description = "管理后台 - 柜列与插接箱绑定关系 Response VO")
@Data
@TableName(value = "aisle_box")
public class AisleBox implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键id", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    /**
     * 柜列id
     */
    @Schema(description = "柜列id", example = "1")
    private Integer aisleId;

    /**
     *  母线的key 始端箱标识符
     */
    private String busKey;

    /**
     * 插接箱名称
     */
    private String boxKey;

    /**
     * 对应绑定母线id
     */
    private Integer aisleBarId;

    /**
     * 类型 0 插接箱 1 连接单元
     */
    private Integer boxType;

//    /**
//     * 级联地址
//     */
//    private Integer casAddr;

//    /**
//     * 母线编号
//     */
//    private Integer barId;

    /**
     * 标记位
     */
    private Integer boxIndex;

    /**
     * 输出位数量
     */
    private Integer outletNum;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
