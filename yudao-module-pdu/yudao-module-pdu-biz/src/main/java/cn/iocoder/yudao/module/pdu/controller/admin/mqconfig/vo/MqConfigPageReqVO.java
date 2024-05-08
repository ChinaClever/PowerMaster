package cn.iocoder.yudao.module.pdu.controller.admin.mqconfig.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 消息队列系统配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MqConfigPageReqVO extends PageParam {

    @Schema(description = "ip端口")
    private String ipAndPorts;

    @Schema(description = "用户名", example = "王五")
    private String userName;

    @Schema(description = "密码")
    private String passWord;

    @Schema(description = "主题/分组")
    private String topic;

    @Schema(description = "mq名称")
    private String mq;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private String[] createTime;

}