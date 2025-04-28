package cn.iocoder.yudao.module.room.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @Schema(description = "机房x长度")
    @JsonProperty(value = "xLength")
    private int xLength;

    /**
     * 机房Y长度(单位机柜)
     */
    @Schema(description = "机房Y长度")
    @JsonProperty(value = "yLength")
    private int yLength;

    @Schema(description = "面积长度")
    private BigDecimal areaxLength;

    @Schema(description = "面积宽")
    private BigDecimal areayLength;

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

    /**
     * 是否可删除
     */
    @Schema(description = "是否可删除")
    private Boolean flagType;

    /**
     * 机房告警统计
     */
    @Schema(description = "机房告警统计")
    private Integer alarmCount;

    @Schema(description = "排序字段")
    private Integer sort;
}
