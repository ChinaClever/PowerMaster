package cn.iocoder.yudao.module.aisle.vo;

import cn.iocoder.yudao.framework.common.dto.aisle.AisleBarDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列新增修改实体
 * @date 2024/4/29 15:00
 */
@Schema(description = "管理后台 - 柜列新增/编辑 Request VO")
@Data
public class AisleBusSaveVo {

    /**
     * 母线数据
     */
    @Schema(description = "母线数据", example = "[]")
    private List<AisleBarDTO>  barVos;

    /**
     * 柜列id
     */
    @Schema(description = "柜列id", example = "1")
    private Integer aisleId;

}
