package cn.iocoder.yudao.module.bus.controller.admin.buspowerloaddetail.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 母线电力负荷详情（电力负荷、功率数据） Response VO")
@Data
public class BusPowerLoadDetailRespVO {
    /**
     * 运行负荷 redis bus_total_data 的 pow_apparent
     */
    private Double runLoad;

    /**
     * 额定容量 即redis bus_cfg 的 cur_specs * 220 * 3
     */
    private Double ratedCapacity;

    /**
     * 负荷余量 = 额定容量 - 运行负荷
     */
    private Double reserveMargin;

    /**
     * 有功功率 redis bus_total_data 的 pow_value
     */
    private Double powActive;

    /**
     * 无功功率 redis bus_total_data 的 pow_reactive
     */
    private Double powReactive;

    /**
     * 最大需量 es 始端箱总数据 小时表 近24小时最大
     */
    private Double peakDemand;

    private String peakDemandTime;
}
