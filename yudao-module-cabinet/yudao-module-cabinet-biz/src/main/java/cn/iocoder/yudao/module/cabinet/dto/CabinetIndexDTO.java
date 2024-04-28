package cn.iocoder.yudao.module.cabinet.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜索引表扩展字段
 * @date 2024/4/28 15:27
 */
@Data
public class CabinetIndexDTO {

    private int id;


    /**
     * 机房编号
     */
    private Integer roomId;

    /**
     * 机柜名称
     */
    private String name;
    /**
     * 通道编号
     */
    private Integer aisleId;

    /**
     * 电力容量
     */
    private float powCapacity;

    /**
     * 运行状态
     */
    private Integer runStatus;

    /**
     * 数据来源
     */
    private Integer pduBox;

    /**
     * 机柜高度
     */
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
    private String company;
    /**
     * 总视在功率
     */
    private float apparentTotal;

    /**
     * 总有功功率
     */
    private float activeTotal;
    /**
     * a路视在功率
     */
    private float apparentA;
    /**
     * b路视在功率
     */
    private float apparentB;

    /**
     * a路有功功率
     */
    private float activeA;

    /**
     * b路有功功率
     */
    private float activeB;

    /**
     * 总电能
     */
    private double eleTotal;

    /**
     * a路电能
     */
    private double eleA;

    /**
     * b路电能
     */
    private double eleB;


    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
