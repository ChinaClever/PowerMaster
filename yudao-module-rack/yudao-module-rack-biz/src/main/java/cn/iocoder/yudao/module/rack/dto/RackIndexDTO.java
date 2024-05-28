package cn.iocoder.yudao.module.rack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 机架详情
 * @date 2024/5/27 13:42
 */
@Schema(description = "管理后台 - 机架详情 Response VO")
@Data
public class RackIndexDTO {

    /**
     * 机柜id
     */
    @Schema(description = "机架id", example = "1")
    private int rackId;
    /**
     * 机柜名称
     */
    @Schema(description = "机柜名称", example = "机柜123")
    private String cabinetName;

    /**
     * 机房编号
     */
    @Schema(description = "机房编号", example = "1")
    private int roomId;
    /**
     * 机房名称
     */
    @Schema(description = "机房名称", example = "机房123")
    private String roomName;

    /**
     * 机房编号
     */
    @Schema(description = "机柜编号", example = "1")
    private Integer cabinetId;


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

    /**
     * A路输出位
     */
    @Schema(description = "A路输出位", example = "[]")
    private List<Integer> outletIdA;

    /**
     * B路输出位
     */
    @Schema(description = "B路输出位", example = "[]")
    private List<Integer> outletIdB;

    /**
     * U位位置
     */
    @Schema(description = "U位位置", example = "1")
    @JsonProperty(value="uAddress")
    private int uAddress;

    /**
     * U位高度
     */
    @Schema(description = "U位高度", example = "1")
    @JsonProperty(value="uHeight")
    private int uHeight;


}
