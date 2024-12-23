package cn.iocoder.yudao.module.bus.dto;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 母线数据
 * @date 2024/5/30 14:40
 */
@Schema(description = "管理后台 - 母线拓扑 Response VO")
@Data
public class BusDataDTO {


    @Schema(description = "始端箱名称", example = "bus-1")
    private String busName;

    @Schema(description = "母线ip", example = "")
    private String devIp;

    /**
     * AB路
     */
    private String path;

    /**
     * 方向  左0 右1
     */
    private Boolean direction;


    @Schema(description = "母线key", example = "")
    private String barKey;

    @Schema(description = "始端箱数据", example = "{}")
    private JSONObject busData;
    @Schema(description = "插接箱数据", example = "[{}]")
    private List<JSONObject> boxDataList;

}
