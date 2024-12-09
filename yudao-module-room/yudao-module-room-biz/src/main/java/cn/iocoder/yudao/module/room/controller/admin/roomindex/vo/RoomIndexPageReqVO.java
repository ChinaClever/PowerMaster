package cn.iocoder.yudao.module.room.controller.admin.roomindex.vo;

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

@Schema(description = "管理后台 - 机房索引分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RoomIndexPageReqVO extends PageParam {

    @Schema(description = "id", example = "757")
    private Integer id;

    @Schema(description = "id", example = "757")
    private List<Integer> roomIds;

    @Schema(description = "机房名", example = "芋艿")
    private String name;

    @Schema(description = "是否删除")
    private Integer isDelete;

    @Schema(description = "电力容量")
    private Double powerCapacity;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

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

    @Schema(description = "时间类型")
    private Integer timeType;

    @Schema(description = "日用电告警开关 0 关 1开")
    private Integer eleAlarmDay;

    @Schema(description = "日用能限制")
    private Double eleLimitDay;

    @Schema(description = "月用电告警开关 0关 1开")
    private Integer eleAlarmMonth;

    @Schema(description = "月用能限制")
    private Double eleLimitMonth;

}