package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 通道列 Response VO")
@Data
@ExcelIgnoreUnannotated
public class AisleIndexRespVO {

    @Schema(description = "主键id", requiredMode = Schema.RequiredMode.REQUIRED, example = "4246")
    @ExcelProperty("主键id")
    private Integer id;

    @Schema(description = "机房id", requiredMode = Schema.RequiredMode.REQUIRED, example = "757")
    @ExcelProperty("机房id")
    private Integer roomId;

    @Schema(description = "机房名称", example = "1")
    private String roomName;


    @Schema(description = "通道名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("通道名称")
    private String name;

    @Schema(description = "数据来源")
    @ExcelProperty("数据来源")
    private Integer pduBar;

    @Schema(description = "是否删除", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否删除")
    private Integer isDelete;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "长度", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("长度")
    private Integer length;

    @Schema(description = "A路网络地址")
    private String devKeyA;

    @Schema(description = "B路网络地址")
    private String devKeyB;

    @Schema(description = "柜列类型", example = "1")
    @ExcelProperty("柜列类型")
    private String type;

    @Schema(description = "位置")
    private String location;

    @Schema(description = "状态")
    private Integer status;
}