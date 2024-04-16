package cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo;

import cn.hutool.core.date.DateTime;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HistoryDataPageReqVO extends PageParam {

    private Integer pageNo;

    private Integer pageSize;

    private String type;

    private String granularity;

    private String ipAddr;

    private String[] timeRange;

}