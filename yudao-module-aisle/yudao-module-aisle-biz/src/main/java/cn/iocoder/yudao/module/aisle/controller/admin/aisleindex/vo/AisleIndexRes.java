package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@ExcelIgnoreUnannotated
public class AisleIndexRes extends AisleIndexRespVO {

    @Schema(description = "位置")
    private String location;

    @Schema(description = "A的负载率")
    private Double aLoadRateA;

    @Schema(description = "B的负载率")
    private Double bLoadRateA;

    @Schema(description = "C的负载率")
    private Double cLoadRateA;

    @Schema(description = "A的负载率")
    private Double aLoadRateB;

    @Schema(description = "B的负载率")
    private Double bLoadRateB;

    @Schema(description = "C的负载率")
    private Double cLoadRateB;

    @Schema(description = "A路负载率所在范围")
    private Integer colorA;

    @Schema(description = "B路负载率所在范围")
    private Integer colorB;

    @Schema(description = "运行状态")
    private Integer status;

    @Schema(description = "数据更新时间")
    private String dataUpdateTime;

}