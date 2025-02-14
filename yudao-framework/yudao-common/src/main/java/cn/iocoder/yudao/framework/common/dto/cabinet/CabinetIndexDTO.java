package cn.iocoder.yudao.framework.common.dto.cabinet;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜索引表扩展字段
 * @date 2024/4/28 15:27
 */
@Schema(description = "管理后台 - 机柜用能 Response VO")
@Data
public class CabinetIndexDTO {

    @Schema(description = "机柜id", example = "1")
    private int id;

    @Schema(description = "位置")
    private String location;
    /**
     * 机房编号
     */
    @Schema(description = "机房编号", example = "1")
    private Integer roomId;

    /**
     * 机柜名称
     */
    @Schema(description = "机柜名称", example = "xxx")
    private String name;

    /**
     * 机房名称
     */
    @Schema(description = "机房名称", example = "xxx")
    private String roomName;
    /**
     * 通道编号
     */
    @Schema(description = "通道编号", example = "1")
    private Integer aisleId;

    /**
     * 电力容量
     */
    @Schema(description = "电力容量", example = "1")
    private float powCapacity;

    /**
     * 运行状态
     */
    @Schema(description = "运行状态", example = "1")
    private Integer runStatus;

    /**
     * 负载状态
     */
    @Schema(description = "负载状态", example = "1")
    private Integer loadStatus;


    /**
     * 数据来源
     */
    @Schema(description = "数据来源", example = "1")
    private Integer pduBox;

    /**
     * 机柜高度
     */
    @Schema(description = "机柜高度", example = "1")
    private int cabinetHeight;


    /**
     * 注释
     */
    @JsonProperty(value="xCoordinate")
    private int xCoordinate;

    /**
     * 类名
     */
    @JsonProperty(value="yCoordinate")
    private int yCoordinate;

    /**
     * 所属于公司
     */
    @Schema(description = "所属于公司", example = "xx")
    private String company;

    /**
     * 昨日电量
     */
    @Schema(description = "昨日电量", example = "1")
    private Double yesterdayEq;

    /**
     * 上周电量
     */
    @Schema(description = "上周电量", example = "1")
    private Double lastWeekEq;

    /**
     * 上月电量
     */
    @Schema(description = "上月电量", example = "1")
    private Double lastMonthEq;


    /**
     * 已用空间
     */
    @Schema(description = "已用空间", example = "1")
    private int usedSpace;
    @Schema(description = "已用空间占比")
    private BigDecimal usedSpaceRate;

    /**
     * 设备总数
     */
    @Schema(description = "设备总数", example = "1")
    private int rackNum;

    /**
     * 剩余空间
     */
    @Schema(description = "剩余空间", example = "1")
    private int freeSpace;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
