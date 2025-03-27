package cn.iocoder.yudao.module.room.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;


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

    @Schema(description = "机房负载")
    private BigDecimal roomLoadFactor;

    @Schema(description = "PUE")
    private BigDecimal roomPue;

    @Schema(description = "前门最高湿度")
    private BigDecimal humMaxFront;

    @Schema(description = "后门最高湿度")
    private BigDecimal humMaxBlack;

    @Schema(description = "前门最高温度")
    private BigDecimal temMaxFront;

    @Schema(description = "后门最高温度")
    private BigDecimal temMaxBlack;

    @Schema(description = "前门平均湿度")
    private Double humAvgFront;

    @Schema(description = "后门平均湿度")
    private Double humAvgBlack;

    @Schema(description = "前门平均温度")
    private Double temAvgFront;

    @Schema(description = "后门平均温度")
    private Double temAvgBlack;


    private Boolean flagType;
}
