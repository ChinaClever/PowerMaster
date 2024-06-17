package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Schema(description = "管理后台 - 始端箱索引 Response VO")
@Data
@ExcelIgnoreUnannotated
public class BusPFRes {

    @Schema(description = "位置")
    private String location;

    @Schema(description = "A的功率因素")
    private Double apf;


    @Schema(description = "B的功率因素")
    private Double bpf;


    @Schema(description = "C的功率因素")
    private Double cpf;

    @Schema(description = "总的功率因素")
    private Double totalPf;
    
    @Schema(description = "负载率所在范围")
    private Integer color;

    @Schema(description = "数据更新时间")
    private String dataUpdateTime;

}