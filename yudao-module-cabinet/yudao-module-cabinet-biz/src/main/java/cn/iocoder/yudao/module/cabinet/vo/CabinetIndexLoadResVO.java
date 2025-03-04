package cn.iocoder.yudao.module.cabinet.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Schema(description = "管理后台 - 始端箱索引 Response VO")
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class CabinetIndexLoadResVO {
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
    private Integer  aisleId;

    /**
     *  柜列的位置 从1开始 0未被分配
     */
    @Schema(description = "柜列的位置 从1开始 0未被分配")
    private Integer  aisleX;

    /**
     * 机柜类型
     */
    @Schema(description = "运行状态")
    private String cabinetType;

    @Schema(description = "高度")
    private Integer  cabinetHeight;
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
    private Integer  loadStatus;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    /**
     * 最后更新时间
     */
    @Schema(description = "最后更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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

    @Schema(description = "A的有功功率")
    private BigDecimal powActivea;

    @Schema(description = "B的有功功率")
    private BigDecimal powActiveb;

    @Schema(description = "B的视在功率")
    private BigDecimal powApparenta;

    @Schema(description = "B的视在功率")
    private BigDecimal powApparentb;

    @Schema(description = "负载率所在范围")
    private Integer color;

    @Schema(description = "数据更新时间")
    private String dataUpdateTime;

    @Schema(description = "负载率")
    private BigDecimal loadFactor;

    @Schema(description = "总视在功率")
    private BigDecimal   apparentTotal;

    @Schema(description = "总有功功率")
    private BigDecimal  activeTotal;

    @Schema(description = "A无功功率")
    private BigDecimal  powReactivea;
    @Schema(description = "B无功功率")
    private BigDecimal  powReactiveb;
    @Schema(description = "总无功功率")
    private BigDecimal  powReactiveTotal;
}