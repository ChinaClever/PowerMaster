package cn.iocoder.yudao.framework.common.dto.aisle;

import cn.hutool.json.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

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
     *  唯一标识
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
    private List<AisleBoxDTO>  boxList;


    /**
     * 昨日电量
     */
    @Schema(description = "昨日电量", example = "1")
    private Double yesterdayEq;


    private String devKey;

    /**
     * 相负载率
     */
    private float[] lineLoadRate;
    /**
     * 相电流
     */
    private  float[] lineCur;

    /**
     * 相电压
     */
    private  float[] lineVol;


    /**
     * 温度
     */
    private float[] temData;

    /**
     * 相功率因素
     */
    private float[] powerFactor;

    /**
     *  相有功功率
     */
    private  float[] powActive;


    /**
     *  相无功功率
     */
    private  float[] powReactive;

    /**
     *  相视在功率
     */
    private  float[] powApparent;
}
