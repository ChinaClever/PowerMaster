package cn.iocoder.yudao.framework.common.entity.mysql.room;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 机房索引表
 * @date 2024/4/24 10:34
 */
@Schema(description = "管理后台 - 机房详情 Response VO")
@Data
@TableName(value = "room_index")
public class RoomIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "机房id", example = "2")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;


    /**
     * 机房名
     */
    @Schema(description = "机房名", example = "机房2")
    private String roomName;

    /**
     * 地址（楼层）
     */
    @Schema(description = "地址（楼层）", example = "地址（楼层）")
    private String addr;

    /**
     * 显示选择
     */
    @Schema(description = "显示选择")
    private Boolean displayFlag;

    /**
     * 显示类型（0-负载率/1-pue）
     */
    @Schema(description = "显示类型（0-负载率/1-pue）")
    private Boolean displayType;

    /**
     * 是否删除
     */
    @Schema(description = "是否删除 0未删除 1已删除", example = "0")
    private int isDelete;

    /**
     * 电力容量
     */
    @Schema(description = "电力容量", example = "0")
    private float powerCapacity;


    /**
     * 空调额定功率
     */
    @Schema(description = "空调额定功率", example = "0")
    private float airPower;

    @Schema(description = "面积类型：0-地砖 1-面积")
    private Boolean areaFlag;


    /**
     * 机房x长度(单位机柜)
     */
    @JsonProperty(value="xLength")
    @TableField(value = "x_length")
    private int xLength;

    /**
     * 机房Y长度(单位机柜)
     */
    @JsonProperty(value="yLength")
    @TableField(value = "y_length")
    private int yLength;

    @TableField(value = "area_x_length")
    private BigDecimal areaxLength;

    @TableField(value = "area_y_length")
    private BigDecimal areayLength;

    @TableField(value = "sort",updateStrategy = FieldStrategy.IGNORED)
    @Schema(description = "排序字段")
    private Integer sort;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2024-05-07 01:00:00")
    private LocalDateTime updateTime;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-05-07 01:00:00")
    private LocalDateTime createTime;
}
