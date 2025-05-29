package cn.iocoder.yudao.framework.common.dto.aisle;

import cn.iocoder.yudao.framework.common.entity.mysql.bus.BusIndex;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列新增修改实体
 * @date 2024/4/29 15:00
 */
@Schema(description = "管理后台 - 柜列母线关系新增/编辑 Request VO")
@Data
public class AisleBarDTO {

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
    @Schema(description = "唯一标识", example = "1")
    private String barKey;

    /**
     * 始端箱名称
     */
    @Schema(description = "始端箱名称", example = "1")
    private String busName;

    /**
     * ip地址
     */
    @Schema(description = "ip地址", example = "1")
    private String devIp;

    /**
     * 母线id
     */
    @Schema(description = "母线id", example = "1")
    private Integer barId;

    /**
     * 级联地址
     */
    @Schema(description = "级联地址", example = "1")
    private Integer casAddr;


    /**
     * AB路
     */
    @Schema(description = "AB路", example = "1")
    private String path;

    /**
     * 方向  左0 右1
     */
    @Schema(description = "方向  左0 右1", example = "1")
    private Integer direction;

    @Schema(description = "插接箱", example = "1")
    private List<AisleBoxDTO> boxList;


    /**
     * 昨日电量
     */
    @Schema(description = "昨日电量", example = "1")
    private Double yesterdayEq;

    @Schema(description = "母线key")
    private String devKey;

    /**
     * 相负载率
     */
    @Schema(description = "相负载率")
    private Double[] lineLoadRate;
    /**
     * 相电流
     */
    @Schema(description = "相电流")
    private Double[] lineCur;

    /**
     * 相电压
     */
    @Schema(description = "相电压")
    private Double[] lineVol;


    /**
     * 温度
     */
    @Schema(description = "温度")
    private Double[] temData;

    /**
     * 相功率因素
     */
    @Schema(description = "相功率因素")
    private Double[] powerFactor;

    /**
     * 相有功功率
     */
    @Schema(description = "相有功功率")
    private Double[] powActive;


    /**
     * 相无功功率
     */
    @Schema(description = "相无功功率")
    private Double[] powReactive;

    /**
     * 相视在功率
     */
    @Schema(description = "相视在功率")
    private Double[] powApparent;

    @Schema(description = "母线")
    private BusIndex busIndex;

    /**
     * 始端箱系统软件版本号
     */
    @Schema(description = "始端箱系统软件版本号")
    private String busVersion;

    /**
     * 运行状态
     */
    @Schema(description = "运行状态")
    private Integer status;

    /**
     * 断路器状态,1：合闸   2：分闸   3：跳闸
     */
    @Schema(description = "断路器状态,1：合闸   2：分闸   3：跳闸")
    private Integer breakerStatus;

    /**
     * 防雷状态,1：工作正常   2：损坏
     */
    @Schema(description = "防雷状态,1：工作正常   2：损坏")
    private Integer lspStatus;

    /**
     * 剩余电流
     */
    @Schema(description = "剩余电流")
    private Integer curResidualValueTotal;

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
     * 频率值
     */
    @Schema(description = "频率值")
    private BigDecimal hzValue;

    /**
     * 线电压
     */
    @Schema(description = "线电压")
    private List<BigDecimal> volLineValue;

    /**
     * 电压谐波含量
     */
    @Schema(description = "电压谐波含量")
    private List<BigDecimal> volThd;
    /**
     * 电流谐波含量
     */
    @Schema(description = "电流谐波含量")
    private List<BigDecimal> curThd;
}
