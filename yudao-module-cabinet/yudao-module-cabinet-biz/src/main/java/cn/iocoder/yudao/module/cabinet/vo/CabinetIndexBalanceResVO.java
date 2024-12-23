package cn.iocoder.yudao.module.cabinet.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

@Data
@Schema(description = "机柜负载状态统计")
public class CabinetIndexBalanceResVO implements Serializable {

    /**
     * 机房编号
     */
    @Schema(description = "机房编号", example = "1")
    private int roomId;
    /**
     * 机房名称
     */
    @Schema(description = "机房名称", example = "机房123")
    private String roomName;

    @Schema(description = "id")
    private Integer id;


    @Schema(description = "机柜名称")
    private String cabinetName;

    /**
     * 通道编号 0不属于任何柜列
     */
    @Schema(description = "通道编号 0不属于任何柜列")
    private Integer aisleId;

    /**
     * 柜列的位置 从1开始 0未被分配
     */
    @Schema(description = "柜列的位置 从1开始 0未被分配")
    private Integer aisleX;

    /**
     * 机柜类型
     */
    @Schema(description = "运行状态")
    private String cabinetType;

    @Schema(description = "高度")
    private Integer cabinetHeight;
    /**
     * 电力容量
     */
    @Schema(description = "电力容量")
    private Double powerCapacity;

    /**
     * 禁用 0：启用 1：禁用
     */
    @Schema(description = "禁用 0：启用 1：禁用")
    private Boolean isDisabled;

    /**
     * 运行状态
     */
    @Schema(description = "运行状态")
    private Integer runStatus;

    /**
     * 负载状态
     */
    @Schema(description = "负载状态")
    private Integer loadStatus;

    /**
     * 数据来源 0：PDU 1：母线
     */
    @Schema(description = "数据来源 0：PDU 1：母线")
    private Boolean pduBox;

    /**
     * 是否删除 0未删除 1已删除
     */
    @Schema(description = "是否删除 0未删除 1已删除")
    private Boolean isDeleted;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime createTime;
    /**
     * 最后更新时间
     */
    @Schema(description = "最后更新时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = TIME_ZONE_DEFAULT)
    private LocalDateTime updateTime;

    /**
     * 日用电告警开关 0禁用 1启用
     */
    @Schema(description = "日用电告警开关 0禁用 1启用")
    private Boolean eleAlarmDay;

    /**
     * 日用能限制
     */
    @Schema(description = "日用能限制")
    private BigDecimal eleLimitDay;

    /**
     * 月用电告警开关 0禁用 1启用
     */
    @Schema(description = "月用电告警开关 0禁用 1启用")
    private Boolean eleAlarmMonth;

    /**
     * 月用能限制
     */
    @Schema(description = "月用能限制")
    private BigDecimal eleLimitMonth;

    /**
     * x坐标
     */
    @Schema(description = "x坐标")
    @JsonProperty(value = "x_coordinate")
    private int xCoordinate;

    /**
     * y坐标
     */
    @Schema(description = "y坐标")
    @JsonProperty(value = "y_coordinate")
    private int yCoordinate;

    /**
     * 所属于公司
     */
    @Schema(description = "所属于公司")
    private String company;

    @Schema(description = "位置")
    private String location;

    @Schema(description = "A比例", example = "1.00")
    private BigDecimal aPow;

    @Schema(description = "B比例", example = "1.00")
    private BigDecimal bPow;

    @Schema(description = "A相电流", example = "1.00")
    private BigDecimal aCurValue;

    @Schema(description = "B相电流", example = "1.00")
    private BigDecimal bCurValue;

    @Schema(description = "A相电压", example = "1.00")
    private BigDecimal aVolValue;

    @Schema(description = "B相电压", example = "1.00")
    private BigDecimal bVolValue;

    @Schema(description = "A有功功率", example = "1.00")
    private BigDecimal aPowValue;

    @Schema(description = "B有功功率", example = "1.00")
    private BigDecimal bPowValue;
}
