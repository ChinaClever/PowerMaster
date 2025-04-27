package cn.iocoder.yudao.module.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


/**
 * @author luowei
 * @version 1.0
 * @description: 机房详情
 * @date 2024/6/21 15:49
 */
@Data
public class RoomPowDataDTO {

    /**
     * 机房iD
     */
    private Integer id;


    /**
     * 机房名
     */
    private String name;

    /**
     * 总功率因素
     */
    private float powerFactor;


    /**
     * 有功功率
     */
    private  float powActive;

    /**
     * 无功功率
     */
    private  float powReactive;

    /**
     * 视在功率
     */
    private  float powApparent;

    /**
     * 显示类型（0-负载率/1-pue）
     */
    @Schema(description = "显示类型（0-负载率/1-pue）")
    private Boolean displayType;

    @Schema(description = "机房负载")
    private BigDecimal roomLoadFactor;

    @Schema(description = "PUE")
    private BigDecimal roomPue;
}
