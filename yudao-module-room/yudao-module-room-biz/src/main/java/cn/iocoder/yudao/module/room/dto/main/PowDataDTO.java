package cn.iocoder.yudao.module.room.dto.main;

import cn.iocoder.yudao.module.room.vo.RoomIndexAddrResVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


/**
 * @author luowei
 * @version 1.0
 * @description: 功率数据详情
 * @date 2024/6/21 15:49
 */
@Data
public class PowDataDTO {

    /**
     * 机房数据
     */
    @Schema(description = "机房数据")
    private List<RoomIndexAddrResVO> roomDataList;


    /**
     * 有功功率
     */
    @Schema(description = "有功功率")
    private BigDecimal totalPowActive;

    /**
     * 无功功率
     */
    @Schema(description = "无功功率")
    private BigDecimal totalPowReactive;

    /**
     * 视在功率
     */
    @Schema(description = "视在功率")
    private BigDecimal totalPowApparent;

    /**
     * 总功率因素
     */
    @Schema(description = "总功率因素")
    private BigDecimal totalPowerFactor;


}
