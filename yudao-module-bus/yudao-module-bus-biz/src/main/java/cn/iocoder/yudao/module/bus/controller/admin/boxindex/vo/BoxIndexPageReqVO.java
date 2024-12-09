package cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 始端箱索引分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BoxIndexPageReqVO extends PageParam {

    @Schema(description = "设备识别码")
    private String devKey;

    @Schema(description = "始端箱ID列表")
    private List<String> boxDevKeyList;

    @Schema(description = "ip地址")
    private String ipAddr;

    @Schema(description = "谐波类型")
    private Integer harmonicType;

    @Schema(description = "母线地址")
    private String devAddr;

    @Schema(description = "boxId")
    private Integer boxId;

    @Schema(description = "母线编号", example = "16770")
    private Integer barId;

    @Schema(description = "运行状态 0：正常 1：预警 2：告警 3: 升级 4：故障 5：离线", example = "2")
    private List<Integer> runStatus;

    @Schema(description = "节点IP")
    private String nodeIp;

    @Schema(description = "颜色")
    private List<Integer> color;

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

    @Schema(description = "逻辑删除")
    private Integer isDeleted;

    @Schema(description = "在线")
    private List<Integer> status;

    @Schema(description = "插接箱ID列表")
    private List<Integer> boxIds;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "时间颗粒度")
    private String timeGranularity;

}