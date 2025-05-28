package cn.iocoder.yudao.framework.common.dto.aisle;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列与插接箱绑定关系
 * @date 2024/6/7 9:29
 */
@Schema(description = "管理后台 - 柜列与插接箱绑定关系 Response VO")
@Data
public class AisleBoxDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键id", example = "1")
    private int id;

    /**
     * 柜列id
     */
    @Schema(description = "柜列id", example = "1")
    private Integer aisleId;


    /**
     * 唯一标识
     */
    @Schema(description = "唯一标识")
    private String barKey;

    /**
     * 插接箱名称
     */
    @Schema(description = "插接箱名称")
    private String boxName;

    /**
     * 级联地址
     */
    @Schema(description = "级联地址")
    private Integer casAddr;

    /**
     * 对应绑定母线id
     */
    @Schema(description = "对应绑定母线id")
    private Integer aisleBarId;

    /**
     * 插接箱
     */
    @Schema(description = "插接箱key")
    private String boxKey;

    /**
     * 母线名称
     */
    @Schema(description = "母线key")
    private String busKey;

    /**
     * 类型 0 插接箱 1 连接单元
     */
    @Schema(description = "类型 0 插接箱 1 连接单元", example = "1")
    private Integer type;

    /**
     * 标记位-前端用
     */
    @Schema(description = "标记位")
    private Integer boxIndex;

    /**
     * 输出位数量
     */
    @Schema(description = "输出位数量")
    private Integer outletNum;

    /**
     * 输出位昨日电量
     */
    @Schema(description = "输出位昨日电量", example = "1")
    private Double[] yesterdayEq;


    @Schema(description = "插接箱key")
    private String devKey;
    /**
     * 相负载率
     */
    @Schema(description = "相负载率")
    private float[] lineLoadRate;
    /**
     * 相电流
     */
    @Schema(description = "相电流")
    private float[] lineCur;

    /**
     * 相电压
     */
    @Schema(description = "相电压")
    private float[] lineVol;


    /**
     * 输出位功率因素
     */
    @Schema(description = "输出位功率因素")
    private float[] powerFactor;

    /**
     * 输出位有功功率
     */
    @Schema(description = "输出位有功功率")
    private float[] powActive;


    /**
     * 输出位无功功率
     */
    @Schema(description = "输出位无功功率")
    private float[] powReactive;

    /**
     * 输出位视在功率
     */
    @Schema(description = "输出位视在功率")
    private float[] powApparent;


    /**
     * 温度
     */
    @Schema(description = "温度")
    private float[] temData;

    //    private String boxName;
    @Schema(description = "状态")
    private Integer status;

    /**
     * 插接箱系统软件版本号
     */
    @Schema(description = "插接箱系统软件版本号")
    private String boxVersion;

    /**
     * 断路器状态
     */
    @Schema(description = "断路器状态")
    private List<Integer> breakerStatus;

    /**
     * 回路
     */
    @Schema(description = "回路")
    private Integer loopNum;

    /**
     * 总功率因素
     */
    @Schema(description = "总功率因素")
    private BigDecimal powerFactorTotal;

    /**
     * 总总有功功率
     */
    @Schema(description = "总总有功功率")
    private BigDecimal powValueTotal;

    /**
     * 总视在
     */
    @Schema(description = "总视在")
    private BigDecimal powApparentTotal;

    /**
     * 总无功
     */
    @Schema(description = "总无功")
    private BigDecimal powReactiveTotal;

    /**
     * 总电压三相不平衡
     */
    @Schema(description = "总电压三相不平衡")
    private BigDecimal volUnbalance;

    /**
     * 总电流三相不平衡
     */
    @Schema(description = "总电流三相不平衡")
    private BigDecimal curUnbalance;

    /**
     * 电流谐波含量
     */
    @Schema(description = "电流谐波含量")
    private List<BigDecimal> curThd;
    /**
     * 电压谐波含量
     */
    @Schema(description = "电压谐波含量")
    private List<BigDecimal> volThd;

    /**
     * 回路电流
     */
    @Schema(description = "回路电流")
    private List<BigDecimal> curValueLoop;
}
