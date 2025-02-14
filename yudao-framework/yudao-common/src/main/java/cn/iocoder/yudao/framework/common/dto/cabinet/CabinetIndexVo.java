package cn.iocoder.yudao.framework.common.dto.cabinet;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜搜索实体
 * @date 2024/4/28 13:32
 */
@Schema(description = "管理后台 - 机柜列表分页 Request VO")
@Data
public class CabinetIndexVo extends PageParam {


    /**
     * 机柜id
     */
    @Schema(description = "机柜id列表", example = "[1,2,3]")
    private List<Integer> cabinetIds;

    /**
     * 机房编号
     */
    @Schema(description = "机房编号", example = "1")
    private Integer roomId;

    /**
     * 运行状态
     */
    @Schema(description = "运行状态 0：空载 1：正常 2：预警 3：告警 4:未绑定 5：离线", example = "[1,2,3]")
    private List<Integer> runStatus;

    /**
     * 负载状态
     */
    @Schema(description = "负载状态 0：0 1：0-30 2：30-60 3：60-90 4:90以上 ", example = "[1,2,3]")
    private List<Integer> loadStatus;

    /**
     * 数据来源
     */
    @Schema(description = "数据来源 0：PDU 1：母线", example = "1")
    private Integer pduBox;

    /**
     * 所属于公司
     */
    @Schema(description = "所属于公司", example = "xxxx")
    private String company;

    @Schema(description = "时间颗粒度")
    private String timeGranularity;

    private Integer startNum;

    private Integer endNum;
}
