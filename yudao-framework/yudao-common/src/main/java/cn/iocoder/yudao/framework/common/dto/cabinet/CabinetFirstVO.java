package cn.iocoder.yudao.framework.common.dto.cabinet;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class CabinetFirstVO {
    @Schema(description = "首位")
    private Boolean first;

    @Schema(description = "空调柜位置")
    private List<Integer> airList;

    /**
     * 机柜名称
     */
    @Schema(description = "机柜名称")
    private String cabinetName;

    /**
     * 机柜类型
     */
    @Schema(description = "机柜类型")
    private String cabinetType;

    /**
     * 机柜高度
     */
    @Schema(description = "机柜高度")
    private int cabinetHeight;

    /**
     * 电力容量
     */
    @Schema(description = "电力容量")
    private Double powCapacity;

    /**
     * 数据来源
     */
    @Schema(description = "数据来源")
    private Boolean pduBox;

    /**
     * pduip
     */
    @Schema(description = "pduip")
    private String pduIp;

    /**
     * 级联地址
     */
    @Schema(description = "级联地址")
    private Integer addr;

    /**
     * 级联数量
     */
    @Schema(description = "结束级联")
    private Integer addrNum;

    /**
     * 母线ip
     */
    @Schema(description = "母线ipa")
    private String busIpa;

    /**
     * 母线编号
     */
    @Schema(description = "母线编号a")
    private Integer busSerialNuma;

    /**
     * 插接箱地址
     */
    @Schema(description = "插接箱地址a")
    private Integer boxAddra;

    /**
     * 输出数量
     */
    @Schema(description = "输出数量a")
    private Integer outNuma;


    /**
     * 母线ip
     */
    @Schema(description = "母线ipb")
    private String busIpb;

    /**
     * 母线编号
     */
    @Schema(description = "母线编号b")
    private Integer busSerialNumb;

    /**
     * 插接箱地址
     */
    @Schema(description = "插接箱地址b")
    private Integer boxAddrb;

    /**
     * 输出数量
     */
    @Schema(description = "输出数量b")
    private Integer outNumb;


    @Schema(description = "机柜温度-前门A/B路")
    private String frontPath;

    /**
     * 传感器id
     */
    @Schema(description = "机柜温度前门-传感器id")
    private int frontSensorId;

    /**
     * 传感器类型
     */
    @Schema(description = "机柜温度前门-传感器类型")
    private int frontSensorType;

    @Schema(description = "机柜温度-后门A/B路")
    private String backPath;

    /**
     * 传感器id
     */
    @Schema(description = "机柜温度后门-传感器id")
    private int backSensorId;

    /**
     * 传感器类型
     */
    @Schema(description = "机柜温度后门-传感器类型")
    private int backSensorType;

    /**
     * 日用能告警开关
     */
    @Schema(description = "日用能告警开关")
    private Boolean eleAlarmDay;

    /**
     * 月用能告警开关
     */
    @Schema(description = "月用能告警开关")
    private Boolean eleAlarmMonth;

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

}
