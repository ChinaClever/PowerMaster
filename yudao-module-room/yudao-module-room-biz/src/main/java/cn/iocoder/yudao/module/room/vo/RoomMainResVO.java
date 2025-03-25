package cn.iocoder.yudao.module.room.vo;

import cn.iocoder.yudao.framework.common.dto.room.RoomCabinetDTO;
import cn.iocoder.yudao.framework.common.dto.room.AisleDataDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RoomMainResVO {

    @Schema(description = "机房id", example = "2")
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
     * 电力容量
     */
    @Schema(description = "电力容量", example = "0")
    private float powerCapacity;


    /**
     * 空调额定功率
     */
    @Schema(description = "空调额定功率", example = "0")
    private float airPower;

    /**
     * 机房x长度(单位机柜)
     */
    @Schema(description = "机房x长度")
    @JsonProperty(value = "x_length")
    private int xLength;

    /**
     * 机房Y长度(单位机柜)
     */
    @Schema(description = "机房Y长度")
    @JsonProperty(value = "y_length")
    private int yLength;

    /**
     * 日用能告警开关
     */
    @Schema(description = "日用能告警开关")
    private int eleAlarmDay;

    /**
     * 月用能告警开关
     */
    @Schema(description = "月用能告警开关")
    private int eleAlarmMonth;

    /**
     * 日用能限制
     */
    @Schema(description = "日用能限制")
    private double eleLimitDay;

    /**
     * 月用能限制
     */
    @Schema(description = "月用能限制")
    private double eleLimitMonth;


    /**
     * 总功率因素
     */
    @Schema(description = "总功率因素")
    private float powerFactor;


    /**
     * 有功功率
     */
    @Schema(description = "有功功率")
    private float powActive;

    /**
     * 无功功率
     */
    @Schema(description = "无功功率")
    private float powReactive;



    /**
     * 视在功率
     */
    @Schema(description = "视在功率")
    private float powApparent;

    @Schema(description = "柜列数据")
    private List<AisleDataDTO> aisleList;

    /**
     * 机柜数据
     */
    @Schema(description = "机柜数据")
    private List<RoomCabinetDTO> cabinetList;


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

}
