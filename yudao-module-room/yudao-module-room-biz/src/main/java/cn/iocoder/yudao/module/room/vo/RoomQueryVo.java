package cn.iocoder.yudao.module.room.vo;

import cn.iocoder.yudao.framework.common.dto.aisle.AisleSaveVo;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetVo;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 机房查询 Request VO")
@Data
public class RoomQueryVo {
    /**
     * 机房id
     */
    @Schema(description = "机房id", example = "2")
    private Integer id;
    /**
     * 机房名称
     */
    @Schema(description = "机房名称", example = "机房1")
    private String roomName;

    /**
     * 电力容量
     */
    @Schema(description = "电力容量", example = "5")
    private float powerCapacity;

    /**
     * 空调额定功率
     */
    @Schema(description = "空调额定功率", example = "0")
    private float airPower;

    @Schema(description = "是否删除 0未删除 1已删除", example = "0")
    private int isDelete;

    /**
     * 机房x长度(单位机柜)
     */
    @Schema(description = "x长度", example = "0")
    @JsonProperty(value="xLength")
    private int xLength;

    /**
     * 机房Y长度(单位机柜)
     */
    @Schema(description = "y长度", example = "0")
    @JsonProperty(value="yLength")
    private int yLength;

    @Schema(description = "面积类型：0-地砖 1-面积")
    private Boolean areaFlag;

    @Schema(description = "面积长度")
    private BigDecimal areaxLength;

    @Schema(description = "面积宽")
    private BigDecimal areayLength;
    /**
     * 日用能告警开关
     */
    private  int eleAlarmDay;

    /**
     * 月用能告警开关
     */
    private  int eleAlarmMonth;

    /**
     * 日用能限制
     */
    private double eleLimitDay;

    /**
     * 月用能限制
     */
    private double eleLimitMonth;

}
