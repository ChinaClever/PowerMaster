package cn.iocoder.yudao.framework.common.dto.aisle;

import cn.hutool.json.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列新增修改实体
 * @date 2024/4/29 15:00
 */
@Schema(description = "管理后台 - 柜列母线关系新增/编辑 Request VO")
@Data
public class AisleBarDTO {

    @Schema(description = "主键id", example = "1")
    private int id;

    /**
     * 柜列id
     */
    @Schema(description = "柜列id", example = "1")
    private Integer aisleId;


    /**
     *  唯一标识
     */
    private String barKey;

    /**
     * 始端箱名称
     */
    private String busName;

    /**
     * ip地址
     */
    private String devIp;

    /**
     * 母线id
     */
    private Integer barId;

    /**
     * 级联地址
     */
    private Integer casAddr;


    /**
     * AB路
     */
    private String path;

    /**
     * 方向  左0 右1
     */
    private Integer direction;


    private List<AisleBoxDTO>  boxList;


    /**
     * 昨日电量
     */
    @Schema(description = "昨日电量", example = "1")
    private Double yesterdayEq;


}
