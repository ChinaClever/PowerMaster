package cn.iocoder.yudao.framework.common.entity.mysql.aisle;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 通道索引
 * @date 2024/5/6 8:52
 */
@Schema(description = "管理后台 - 柜列详情 Response VO")
@Data
@TableName(value = "aisle_index")
public class AisleIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "柜列id", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 机房id
     */
    @Schema(description = "机房id", example = "1")
    private Integer roomId;

    /**
     * 柜列名称
     */
    @Schema(description = "柜列名称", example = "柜列1")
    private String aisleName;

    /**
     * 数据来源 0：PDU 1：母线
     */
    @Schema(description = "数据来源 0：PDU 1：母线", example = "0")
    private Boolean pduBar;

    /**
     * 是否删除
     */
    @Schema(description = "是否删除 0未删除 1已删除", example = "0")
    private Integer isDelete;


    /**
     * 长度
     */
    private Integer aisleLength;




    /**
     * 起始x坐标
     */
    @JsonProperty(value="xCoordinate")
    private int xCoordinate;

    /**
     * 起始y坐标
     */
    @JsonProperty(value="yCoordinate")
    private int yCoordinate;

    /**
     * 柜列方向 x y
     */
    private String direction;


//    private Double powerCapacity;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2024-05-07 01:00:00")
//    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-05-07 01:00:00")
//    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;


}
