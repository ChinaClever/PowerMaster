package cn.iocoder.yudao.module.room.controller.admin.historydata.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RoomHistoryDataDetailsReqVO extends PageParam {
    private Integer roomId;

    private String granularity;

    private String[] timeRange;

}