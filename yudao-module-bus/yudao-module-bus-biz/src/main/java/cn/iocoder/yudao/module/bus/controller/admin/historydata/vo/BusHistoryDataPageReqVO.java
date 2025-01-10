package cn.iocoder.yudao.module.bus.controller.admin.historydata.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BusHistoryDataPageReqVO extends PageParam {

    private Integer pageNo;

    private Integer pageSize;

    private Integer lineId;

    private Integer loopId;

    private Integer outletId;

    private String type;

    private String granularity;

    private String[] timeRange;

    private String[] devkeys;

    private String[] busIds;

    private String[] boxIds;

    private String busName;

    private List<String> columnsToExclude;
}