package cn.iocoder.yudao.module.cabinet.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 母线电力负荷详情（电力负荷、功率数据） Response VO")
@Data
public class CabinetPowerLoadDetailRespVO {
    /**
     * 运行负荷 redis bus_total_data 的 pow_apparent
     */
    private BigDecimal runLoad;

    /**
     * 额定容量 即redis bus_cfg 的 cur_specs * 220 * 3
     */
    private BigDecimal ratedCapacity;

    /**
     * 负荷余量 = 额定容量 - 运行负荷
     */
    private BigDecimal reserveMargin;

    /**
     * 有功功率 redis bus_total_data 的 pow_value
     */
    private BigDecimal powActive;

    /**
     * 视在功率 redis bus_total_data 的 pow_apparent
     */
    private BigDecimal powApparent;

    /**
     * 无功功率 redis bus_total_data 的 pow_reactive
     */
    private BigDecimal powReactive;

    /**
     * 最大需量 es 始端箱总数据 小时表 近24小时最大
     */
    private BigDecimal peakDemand;

    private String peakDemandTime;
}
