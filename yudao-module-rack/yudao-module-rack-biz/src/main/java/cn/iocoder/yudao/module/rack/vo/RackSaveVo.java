package cn.iocoder.yudao.module.rack.vo;

import cn.iocoder.yudao.framework.common.entity.mysql.rack.RackIndex;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 机架保存
 * @date 2024/5/27 14:28
 */
@Schema(description = "管理后台 - 机架保存 Request VO")
@Data
public class RackSaveVo {

    /**
     * 机柜编号
     */
    @Schema(description = "机柜编号", example = "1")
    private int cabinetId;

    /**
     * 机房编号
     */
    @Schema(description = "机房编号", example = "1")
    private Integer roomId;



    @Schema(description = "保存机架列表", example = "[]")
    private List<RackIndex> racks;

}
