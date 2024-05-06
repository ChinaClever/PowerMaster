package cn.iocoder.yudao.module.cabinet.dto;

import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜详情
 * @date 2024/4/28 15:27
 */
@Data
public class CabinetDTO {

    /**
     * 机柜id
     */
    private int id;
    /**
     * 机柜名称
     */
    private String cabinetName;

    /**
     * 机房编号
     */
    private int roomId;
    /**
     * 机房名称
     */
    private String roomName;
    /**
     * 通道编号
     */
    private int aisleId;

    /**
     * 电力容量
     */
    private float powCapacity;

    /**
     * 数据来源
     */
    private int pduBox;
    /**
     * 是否禁用
     */
    private Integer isDisabled;

    /**
     * 是否删除
     */
    private Integer isDeleted;


    /**
     * 机柜高度
     */
    private int cabinetHeight;


    /**
     * 机柜类型
     */
    private String type;
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
     * A路IP地址
     */
    private String pduIpA;
    /**
     * A路级联编号
     */
    private int casIdA;
    /**
     * B路IP地址
     */
    private String pduIpB;
    /**
     * B路级联编号
     */
    private int casIdB;
    /**
     * 运行状态
     */
    private int runStatus;

}
