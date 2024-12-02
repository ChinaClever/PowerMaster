package cn.iocoder.yudao.framework.common.dto.cabinet;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @version 1.0
 * @description: 机柜配电 状态统计
 */

@Schema(description = "管理后台 - 机柜配电/状态统计 Request VO")
@Data
public class CabinetRunStatusVo {

    @Schema(description = "运行状态 0：空载 ")
    private Integer sumNoload;

    @Schema(description = "运行状态 1：正常")
    private Integer sumNormal;
    @Schema(description = "运行状态 2：预警" )
    private Integer sumEarly;
    @Schema(description = "运行状态 3：告警")
    private Integer sumInform;

    @Schema(description = "运行状态  4:未绑定 ")
    private Integer sumDidnot;

    @Schema(description = "运行状态  5：离线")
    private Integer sumOffline;

}
