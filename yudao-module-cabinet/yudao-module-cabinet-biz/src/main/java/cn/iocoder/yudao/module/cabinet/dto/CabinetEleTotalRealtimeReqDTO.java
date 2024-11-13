package cn.iocoder.yudao.module.cabinet.dto;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author: jiangjinchi
 * @time: 2024/11/8 12:04
 */
@Schema(description = "管理后台 - 机房实时能耗查询入参dto")
@Data
public class CabinetEleTotalRealtimeReqDTO extends PageParam {

    private String[] timeRange;

    private String[] roomIds;
}
