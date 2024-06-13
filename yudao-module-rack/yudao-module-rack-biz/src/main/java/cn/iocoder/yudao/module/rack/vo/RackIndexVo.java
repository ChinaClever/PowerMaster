package cn.iocoder.yudao.module.rack.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 机架搜索实体
 * @date 2024/4/28 13:32
 */
@Schema(description = "管理后台 - 机架列表分页 Request VO")
@Data
public class RackIndexVo extends PageParam {


    /**
     * 机柜id
     */
    @Schema(description = "机架id列表", example = "[1,2,3]")
    private List<Integer> rackIds;

    /**
     * 机柜编号
     */
    @Schema(description = "机柜编号", example = "1")
    private List<Integer> cabinetIds;

    /**
     * 机房编号
     */
    @Schema(description = "机房编号", example = "1")
    private Integer roomId;


    /**
     * 所属于公司
     */
    @Schema(description = "所属于公司", example = "xxxx")
    private String company;

    /**
     * 机架名称
     */
    @Schema(description = "机架名称", example = "xxxx")
    private String rackName;


    /**
     * 机架类型
     */
    @Schema(description = "机架类型", example = "xxxx")
    private String type;

}
