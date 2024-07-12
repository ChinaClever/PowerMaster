package cn.iocoder.yudao.framework.common.dto.aisle;

import cn.hutool.json.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列与插接箱绑定关系
 * @date 2024/6/7 9:29
 */
@Schema(description = "管理后台 - 柜列与插接箱绑定关系 Response VO")
@Data
public class AisleBoxDTO implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 插接箱名称
     */
    private String boxName;

    /**
     * 对应绑定母线id
     */
    private Integer aisleBarId;

    /**
     * 类型 0 插接箱 1 连接单元
     */
    private Integer type;

    /**
     * 标记位-前端用
     */
    private Integer boxIndex;

    /**
     * 输出位昨日电量
     */
    @Schema(description = "昨日电量", example = "1")
    private Double[] yesterdayEq;
}
