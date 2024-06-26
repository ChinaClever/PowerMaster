package cn.iocoder.yudao.module.room.vo;

import cn.iocoder.yudao.framework.common.dto.aisle.AisleSaveVo;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetVo;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetEnvSensor;
import cn.iocoder.yudao.framework.common.entity.mysql.rack.RackIndex;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 机房新增修改实体
 * @date 2024/4/29 15:00
 */
@Schema(description = "管理后台 - 机房新增/编辑 Request VO")
@Data
public class RoomSaveVo {

    /**
     * 机房id
     */
    @Schema(description = "机房id", example = "2")
    private Integer id;
    /**
     * 机房名称
     */
    @Schema(description = "机房名称", example = "机房1")
    private String roomName;


    /**
     * 电力容量
     */
    @Schema(description = "电力容量", example = "5")
    private float powCapacity;


    /**
     * 是否删除
     */
    @Schema(description = "是否删除 0未删除 1已删除", example = "0")
    private Integer isDeleted;


    /**
     * 机房x长度(单位机柜)
     */
    @Schema(description = "x长度", example = "0")
    @JsonProperty(value="xLength")
    private int xLength;

    /**
     * 机房Y长度(单位机柜)
     */
    @Schema(description = "y长度", example = "0")
    @JsonProperty(value="yLength")
    private int yLength;

    @Schema(description = "柜列列表", example = "[]")
    private List<AisleSaveVo>  aisleList;

    @Schema(description = "机柜列表", example = "[]")
    private List<CabinetVo>  cabinetList;
}
