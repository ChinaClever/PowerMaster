package cn.iocoder.yudao.module.cabinet.vo;

import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetEnvSensor;
import cn.iocoder.yudao.framework.common.entity.mysql.rack.RackIndex;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜新增修改实体
 * @date 2024/4/29 15:00
 */
@Schema(description = "管理后台 - 机柜新增/编辑 Request VO")
@Data
public class CabinetVo {

    /**
     * 机柜id
     */
    @Schema(description = "机柜id", example = "2")
    private int id;
    /**
     * 机柜名称
     */
    @Schema(description = "机柜名称", example = "机柜1")
    private String cabinetName;

    /**
     * 机房编号
     */
    @Schema(description = "机房编号", example = "2")
    private int roomId;
    /**
     * 机房名称
     */
    @Schema(description = "机房名称", example = "机房1")
    private String roomName;
    /**
     * 通道编号
     */
    @Schema(description = "通道编号", example = "2")
    private int aisleId;

    /**
     * 电力容量
     */
    @Schema(description = "电力容量", example = "5")
    private float powCapacity;

    /**
     * 数据来源
     */
    @Schema(description = "数据来源 0：PDU 1：母线", example = "0")
    private int pduBox;
    /**
     * 是否禁用
     */
    @Schema(description = "禁用 0：启用 1：禁用", example = "0")
    private Integer isDisabled;

    /**
     * 是否删除
     */
    @Schema(description = "是否删除 0未删除 1已删除", example = "0")
    private Integer isDeleted;


    /**
     * 机柜高度
     */
    @Schema(description = "机柜高度", example = "5")
    private int cabinetHeight;


    /**
     * 机柜类型
     */
    @Schema(description = "机柜类型", example = "IT机柜")
    private String type;
    /**
     * 注释
     */
    @Schema(description = "注释", example = "xxx")
    private int xCoordinate;

    /**
     * 类名
     */
    @Schema(description = "类名", example = "yyy")
    private int yCoordinate;

    /**
     * 所属于公司
     */
    @Schema(description = "所属于公司", example = "公司名")
    private String company;
    /**
     * A路IP地址
     */
    @Schema(description = "A路IP地址", example = "127.0.0.1")
    private String pduIpA;
    /**
     * A路级联编号
     */
    @Schema(description = "A路级联编号", example = "0")
    private int casIdA;
    /**
     * B路IP地址
     */
    @Schema(description = "B路IP地址", example = "127.0.0.1")
    private String pduIpB;
    /**
     * B路级联编号
     */
    @Schema(description = "B路级联编号", example = "0")
    private int casIdB;


    @Schema(description = "温度传感器绑定", example = "[]")
    private List<CabinetEnvSensor>  sensorList;

    @Schema(description = "u位绑定", example = "[]")
    private List<RackIndex>  rackList;

}
