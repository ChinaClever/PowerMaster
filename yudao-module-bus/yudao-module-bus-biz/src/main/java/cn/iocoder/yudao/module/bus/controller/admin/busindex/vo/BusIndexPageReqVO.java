package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

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

@Schema(description = "管理后台 - 始端箱索引分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BusIndexPageReqVO extends PageParam {

    @Schema(description = "设备识别码")
    private String devKey;

    @Schema(description = "ip地址")
    private String ipAddr;

    @Schema(description = "谐波类型")
    private Integer harmonicType;

    @Schema(description = "busId")
    private Integer busId;

    @Schema(description = "母线地址")
    private String devAddr;

    @Schema(description = "母线编号", example = "16770")
    private Integer barId;

    @Schema(description = "运行状态 0：离线 1：正常 2：告警", example = "2")
    private List<Integer> runStatus;

    @Schema(description = "节点IP")
    private String nodeIp;

    @Schema(description = "始端箱ID列表")
    private List<String> busDevKeyList;

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

    @Schema(description = "图表类型")
    private Integer lineType;

    @Schema(description = "颜色")
    private List<Integer> color;

    @Schema(description = "在线")
    private List<Integer> status;

    @Schema(description = "始端箱id列表")
    private List<Integer> busIds;

    @Schema(description = "逻辑删除")
    private Integer isDeleted;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "时间颗粒度")
    private String timeGranularity;

    @Schema(description = "负载量状态 0：空载 1：<30%  2: 30~60 3：60~90  4：>90%")
    private List<Integer> loadRateStatus;

    @Schema(description = "三相不平衡")
    private List<Integer> curUnbalanceStatus;
}