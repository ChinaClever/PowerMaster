package cn.iocoder.yudao.module.cabinet.vo;

import cn.iocoder.yudao.framework.common.vo.CabineIndexCfgVO;
import com.baomidou.mybatisplus.annotation.TableId;
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
public class CabinetEnergyStatisticsResVO{
    /**
     * 主键id
     */
    @TableId
    private Integer id;
    /**
     * 机房id
     */
    private Integer roomId;

    /**
     * 通道编号 0不属于任何柜列
     */
    private Integer  aisleId;

    /**
     *  柜列的位置 从1开始 0未被分配
     */
    private Integer  aisleX;

    /**
     * 机柜类型
     */
    private String cabinetType;
    /**
     * 机柜名称
     */
    private String cabinetName;


    private Integer  cabinetHeight;
    /**
     * 电力容量
     */
    private Double powerCapacity;

    /**
     * 禁用 0：启用 1：禁用
     */
    private Boolean isDisabled;

    /**
     * 运行状态
     */
    private Integer runStatus;

    /**
     * 负载状态
     */
    private Integer  loadStatus;

    /**
     * 数据来源 0：PDU 1：母线
     */
    private Boolean pduBox;

    /**
     * 是否删除 0未删除 1已删除
     */
    private Boolean isDeleted;
    /**
     * 创建时间
     */
//    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 最后更新时间
     */
//    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 日用电告警开关 0禁用 1启用
     */
    private Boolean eleAlarmDay;

    /**
     * 日用能限制
     */
    private BigDecimal eleLimitDay;

    /**
     * 月用电告警开关 0禁用 1启用
     */
    private Boolean eleAlarmMonth;

    /**
     * 月用能限制
     */
    private BigDecimal eleLimitMonth;

    /**
     * x坐标
     */
    @JsonProperty(value = "x_coordinate")
    private int xCoordinate;

    /**
     * y坐标
     */
    @JsonProperty(value = "y_coordinate")
    private int yCoordinate;

    /**
     * 所属于公司
     */
    private String company;


    /**
     * 机房名称
     */
    @Schema(description = "机房名称", example = "xxx")
    private String roomName;

    /**
     * 昨日电量
     */
    @Schema(description = "昨日电量", example = "1")
    private BigDecimal yesterdayEq;

    /**
     * 上周电量
     */
    @Schema(description = "上周电量", example = "1")
    private BigDecimal lastWeekEq;

    /**
     * 上月电量
     */
    @Schema(description = "上月电量", example = "1")
    private BigDecimal lastMonthEq;

    @Schema(description = "位置")
    private String location;

}