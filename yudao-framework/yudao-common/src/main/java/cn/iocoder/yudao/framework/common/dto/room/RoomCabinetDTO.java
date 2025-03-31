package cn.iocoder.yudao.framework.common.dto.room;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜详情
 * @date 2024/4/28 15:27
 */
@Schema(description = "管理后台 - 机柜数据 Response VO")
@Data
public class RoomCabinetDTO {

    /**
     * 机柜id
     */
    @Schema(description = "机柜id", example = "1")
    private int id;
    /**
     * 机柜名称
     */
    @Schema(description = "机柜名称", example = "机柜123")
    private String cabinetName;
    @Schema(description = "机房id")
    private Integer roomId;

    /**
     * 通道编号
     */
    @Schema(description = "通道编号")
    private int aisleId;

    /**
     * 电力容量
     */
    @Schema(description = "电力容量", example = "2")
    private float powCapacity;

    /**
     * 数据来源
     */
    @Schema(description = "数据来源 0：PDU 1：母线", example = "0")
    private int pduBox;


    /**
     * 机柜高度
     */
    @Schema(description = "机柜高度", example = "5")
    private int cabinetHeight;
    /**
     * x
     */
    @Schema(description = "x坐标", example = "xxx")
    @JsonProperty(value = "xCoordinate")
    private int xCoordinate;

    /**
     * y
     */
    @Schema(description = "y坐标", example = "yyy")
    @JsonProperty(value = "yCoordinate")
    private int yCoordinate;

    /**
     * 日用电告警开关 0禁用 1启用
     */    @Schema(description = "日用电告警开关 0禁用 1启用")
    private Boolean eleAlarmDay;

    /**
     * 日用能限制
     */    @Schema(description = "日用能限制")
    private Double eleLimitDay;

    /**
     * 月用电告警开关 0禁用 1启用
     */    @Schema(description = "月用电告警开关 0禁用 1启用")
    private Boolean eleAlarmMonth;

    /**
     * 月用能限制
     */    @Schema(description = "月用能限制")
    private Double eleLimitMonth;

    /**
     * 所属于公司
     */
    @Schema(description = "所属于公司")
    private String company;

    /**
     * 运行状态
     */
    @Schema(description = "运行状态 0：空载 1：正常 2：预警 3：告警 4:未绑定 5：离线", example = "2")
    private int runStatus;


    /**
     * 已用空间
     */
    @Schema(description = "已用空间", example = "1")
    private int usedSpace;

    /**
     * 剩余空间
     */
    @Schema(description = "剩余空间", example = "1")
    private int freeSpace;

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


    /**
     * 温度
     */
    @Schema(description = "温度")
    private double tem;

    /**
     * 负载率
     */
    @Schema(description = "负载率")
    private float loadRate;

    @Schema(description = "A")
    private String cabinetkeya;

    @Schema(description = "B")
    private String cabinetkeyb;

    /**
     * 昨日电量
     */

    @Schema(description = "昨日电量", example = "1")
    private Double yesterdayEq;


    @Schema(description = "A比例", example = "1.00")
    private BigDecimal aPow;

    @Schema(description = "B比例", example = "1.00")
    private BigDecimal bPow;

    @Schema(description = "位置")
    private Integer index;
}
