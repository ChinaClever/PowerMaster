package cn.iocoder.yudao.module.aisle.dto;

import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列列表数据
 * @date 2024/6/21 15:49
 */
@Data
public class AisleListDTO {



    private int roomId;


    /**
     * 机房名
     */
    private String roomName;
    /**
     * 电力容量
     */
    @Schema(description = "电力容量", example = "0")
    private float powerCapacity;


    /**
     * 机房x长度(单位机柜)
     */
    @JsonProperty(value="xLength")
    private int xLength;

    /**
     * 机房Y长度(单位机柜)
     */
    @JsonProperty(value="yLength")
    private int yLength;
    /**
     * 柜列数据
     */
    private List<AisleIndex>  aisleList;


}
