package cn.iocoder.yudao.module.pdu.controller.admin.mqconfig.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 消息队列系统配置 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MqConfigRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11740")
    @ExcelProperty("主键ID")
    private Integer id;

    @Schema(description = "ip端口", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("ip端口")
    private String ipAndPorts;

    @Schema(description = "用户名", example = "王五")
    @ExcelProperty("用户名")
    private String userName;

    @Schema(description = "密码")
    @ExcelProperty("密码")
    private String passWord;

    @Schema(description = "主题/分组")
    @ExcelProperty("主题/分组")
    private String topic;

    @Schema(description = "mq名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("mq名称")
    private String mq;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private String createTime;

}