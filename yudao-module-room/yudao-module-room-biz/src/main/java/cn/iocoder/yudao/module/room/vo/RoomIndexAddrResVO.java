package cn.iocoder.yudao.module.room.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


/**
 * @author luowei
 * @version 1.0
 * @description: 机房详情
 * @date 2024/6/21 15:49
 */
@Data
public class RoomIndexAddrResVO {

    /**
     * 机房iD
     */
    @Schema(description = "机房iD")
    private Integer id;


    /**
     * 机房名
     */
    @Schema(description = "机房名")
    private String roomName;

    @Schema(description = "楼层")
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
     * 总功率因素
     */
    @Schema(description = "总功率因素")
    private Double powerFactor;


    /**
     * 有功功率
     */
    @Schema(description = "有功功率")
    private Double powActive;

    /**
     * 无功功率
     */
    @Schema(description = "无功功率")
    private Double powReactive;

    /**
     * 视在功率
     */
    @Schema(description = "视在功率")
    private Double powApparent;

    /**
     * 电能
     */
    @Schema(description = "电能")
    private Double eleActive;

    @Schema(description = "pue")
    private Double pue;

    @Schema(description = "负载率")
    private Double loadRate;
}
