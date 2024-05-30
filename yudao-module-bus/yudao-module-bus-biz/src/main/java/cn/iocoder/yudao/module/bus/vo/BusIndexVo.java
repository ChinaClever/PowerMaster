package cn.iocoder.yudao.module.bus.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 母线拓扑
 * @date 2024/5/30 14:11
 */
@Schema(description = "管理后台 - 母线拓扑 req VO")
@Data
public class BusIndexVo {

    @Schema(description = "母线id列表", example = "[]")
    private List<Integer> ids;


}
