package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AisleEQRes extends AisleIndexRespVO {



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
    private int xCoordinate;

    /**
     * 类名
     */
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

}