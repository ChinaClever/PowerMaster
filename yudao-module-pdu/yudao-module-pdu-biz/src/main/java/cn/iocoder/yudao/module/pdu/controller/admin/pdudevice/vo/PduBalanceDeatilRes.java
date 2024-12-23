package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;


@Schema(description = "管理后台 - 始端箱索引 Response VO")
@Data
@ExcelIgnoreUnannotated
public class PduBalanceDeatilRes {

    @Schema(description = "位置")
    private String location;

    /**
     * 设备识别码
     */
    @Schema(description = "设备识别码")
    private String devKey;

    @Schema(description = "始端箱Id")
    private Integer busId;

    @Schema(description = "A的电流")
    private List<Double> cur_value;

    @Schema(description = "B的电流")
    private List<Double> vol_value;

    @Schema(description = "电流不平衡度")
    private Double curUnbalance;

    @Schema(description = "电压不平衡度")
    private Double volUnbalance;

    @Schema(description = "负载率所在范围")
    private Integer color;

    @Schema(description = "运行状态")
    private Integer status;

    @Schema(description = "数据更新时间")
    private String dataUpdateTime;

}