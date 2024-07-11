package cn.iocoder.yudao.module.bus.controller.admin.buspowerloaddetail.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 母线电力负荷详情（电力负荷、功率数据） Request VO")
@Data
public class BusPowerLoadDetailReqVO {
    private Long id;

    private String devKey;

    private String granularity;

}
