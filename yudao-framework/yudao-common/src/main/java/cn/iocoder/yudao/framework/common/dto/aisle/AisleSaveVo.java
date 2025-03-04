package cn.iocoder.yudao.framework.common.dto.aisle;

import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetVo;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列新增修改实体
 * @date 2024/4/29 15:00
 */
@Schema(description = "管理后台 - 柜列新增/编辑 Request VO")
@Data
public class AisleSaveVo {

    /**
     * 柜列id
     */
    @Schema(description = "柜列id", example = "2")
    private Integer id;
    /**
     * 机房id
     */
    @Schema(description = "机房id", example = "1")
    private Integer roomId;

    /**
     * 柜列名称
     */
    @Schema(description = "柜列名称", example = "柜列1")
    private String aisleName;


    /**
     * 数据来源 0：PDU 1：母线
     */
    @Schema(description = "数据来源 0：PDU 1：母线", example = "0")
    private Boolean pduBar;


    /**
     * 电力容量
     */
    @Schema(description = "电力容量", example = "0")
    private float powerCapacity;

    /**
     * 起始x坐标
     */
    @Schema(description = "起始x坐标", example = "0")
    @JsonProperty(value="xCoordinate")
    private int xCoordinate;

    /**
     * 起始y坐标
     */
    @Schema(description = "起始y坐标", example = "0")
    @JsonProperty(value="yCoordinate")
    private int yCoordinate;

    /**
     * 柜列方向 x y
     */
    @Schema(description = "柜列方向 x y", example = "x")
    private String direction;


    /**
     * 长度
     */
    @Schema(description = "长度", example = "0")
    private Integer aisleLength;

    /**
     * 长度
     */
    @Schema(description = "长度", example = "0")
    private Integer length;

    /**
     * 类型
     */
    @Schema(description = "类型", example = "0")
    private String aisleType;

    /**
     * A路母线
     */
    private AisleBarDTO barA;
    /**
     * B路母线
     */
    private AisleBarDTO barB;

//    @Schema(description = "机柜列表", example = "[]")
//    private List<CabinetVo>  cabinetList;


    /**
     * 日用能告警开关
     */
    private  int eleAlarmDay;

    /**
     * 月用能告警开关
     */
    private  int eleAlarmMonth;

    /**
     * 日用能限制
     */
    private double eleLimitDay;

    /**
     * 月用能限制
     */
    private double eleLimitMonth;

}
