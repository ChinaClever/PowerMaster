package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 通道列分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AisleIndexPageReqVO extends PageParam {

    @Schema(description = "id", example = "757")
    private Integer id;

    @Schema(description = "机房id", example = "757")
    private Integer roomId;

    @Schema(description = "通道名称", example = "芋艿")
    private String name;

    @Schema(description = "数据来源")
    private Integer pduBar;

    @Schema(description = "是否删除")
    private Integer isDelete;

    @Schema(description = "时间类型")
    private Integer timeType;

    @Schema(description = "开始时间")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime oldTime;

    @Schema(description = "结束时间")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime newTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "通道列ID列表")
    private List<Integer> aisleIds;

    @Schema(description = "长度")
    private Integer length;

    @Schema(description = "柜列类型", example = "1")
    private String type;

    @Schema(description = "时间颗粒度 插接箱配电（day/hour/today/threeDay）")
    private String timeGranularity;
}