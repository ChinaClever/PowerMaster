package cn.iocoder.yudao.module.cabinet.controller.admin.index.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CabinetEnvAndHumRes {


    /**
     * 主键id
     */
    @TableId
    private Integer id;
    /**
     * 机房id
     */
    @Schema(description = "机房id")
    private Integer roomId;

    /**
     * 通道编号 0不属于任何柜列
     */
    private Integer  aisleId;

    /**
     *  柜列的位置 从1开始 0未被分配
     */
    @Schema(description = "柜列的位置 从1开始 0未被分配")
    private Integer  aisleX;

    /**
     * 机柜类型
     */
    @Schema(description = "机柜类型")
    private String cabinetType;
    /**
     * 机柜名称
     */
    @Schema(description = "机柜名称")
    private String cabinetName;


    /**
     * 机柜高度
     */
    @Schema(description = "机柜高度")
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

    @Schema(description = "机房名称")
    private String roomName;

    @Schema(description = "位置")
    private String location;

    //热 平均
    @Schema(description = "热通道平均温度")
    private BigDecimal hotAverageTem;
    @Schema(description = "热通道平均湿度")
    private BigDecimal hotAverageHum;
    //冷 平均
    @Schema(description = "冷通道平均温度")
    private BigDecimal iceAverageHum;
    @Schema(description = "冷通道平均湿度")
    private BigDecimal iceAverageTem;

    //热通道温度
    @Schema(description = "热通道温度-上")
    private BigDecimal hotTopTem;
    @Schema(description = "热通道温度-中")
    private BigDecimal hotMidTem;
    @Schema(description = "热通道温度-下")
    private BigDecimal hotBomTem;

    //热通道湿度
    @Schema(description = "热通道湿度-上")
    private BigDecimal hotTopHum;
    @Schema(description = "热通道湿度-中")
    private BigDecimal hotMidHum;
    @Schema(description = "热通道湿度-下")
    private BigDecimal hotBomHum;

    //冷通道湿度
    @Schema(description = "冷通道湿度-上")
    private BigDecimal iceTopHum;
    @Schema(description = "冷通道湿度-中")
    private BigDecimal iceMidHum;
    @Schema(description = "冷通道湿度-下")
    private BigDecimal iceBomHum;

    //冷通道温度
    @Schema(description = "冷通道温度-上")
    private BigDecimal iceTopTem;
    @Schema(description = "冷通道温度-中")
    private BigDecimal iceMidTem;
    @Schema(description = "冷通道温度-下")
    private BigDecimal iceBomTem;

    //颜色
    @Schema(description = "冷通道温度颜色")
    private String iceTemColor;
    @Schema(description = "热通道温度颜色")
    private String hotTemColor;

}
