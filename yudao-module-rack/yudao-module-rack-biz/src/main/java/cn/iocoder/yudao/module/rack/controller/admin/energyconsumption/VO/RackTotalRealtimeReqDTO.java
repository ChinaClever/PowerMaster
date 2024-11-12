package cn.iocoder.yudao.module.rack.controller.admin.energyconsumption.VO;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分页参数")
public class RackTotalRealtimeReqDTO extends PageParam {

    @Schema(description = "网络地址")
    private String location;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "时间")
    private String[] timeRange;

    private String[] ipArray;
}
