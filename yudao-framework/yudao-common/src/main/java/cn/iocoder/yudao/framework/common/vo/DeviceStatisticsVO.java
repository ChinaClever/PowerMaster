package cn.iocoder.yudao.framework.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DeviceStatisticsVO {

    /**
     * pdu数量
     */
    @Schema(description = "pdu数量")
    private Integer pduNum;

    /**
     * pdu离线数
     */
    @Schema(description = "pdu离线数")
    private Integer pduOffLine;

    @Schema(description = "pdu告警")
    private Integer pduInform;

    /**
     * pdu在线数
     */
    @Schema(description = "pdu在线数")
    private Integer pduOnLine;

    /**
     * 母线数量
     */
    @Schema(description = "母线数量")
    private Integer barNum;

    /**
     * 始端箱数量
     */
    @Schema(description = "始端箱数量")
    private Integer busNum;

    /**
     * 告警
     */
    @Schema(description = "始端箱告警")
    private Integer busInform;

    /**
     * 始端箱离线数
     */
    @Schema(description = "始端箱离线数")
    private Integer busOffLine;

    /**
     * 始端箱在线数
     */
    @Schema(description = "始端箱在线数")
    private Integer busOnLine;

    /**
     * 插接箱数量
     */
    @Schema(description = "插接箱数量")
    private Integer boxNum;

    /**
     * 告警
     */
    @Schema(description = "插接箱告警")
    private Integer boxInform;

    /**
     * 插接箱离线数
     */
    @Schema(description = "插接箱离线数")
    private Integer boxOffLine;

    /**
     * 插接箱在线数
     */
    @Schema(description = "插接箱在线数")
    private Integer boxOnLine;


    /**
     * 机柜数量
     */
    @Schema(description = "机柜数量")
    private Integer cabNum;

    /**
     * 机柜启用数  负载率>0
     */
    @Schema(description = "机柜启用数")
    private Integer cabUse;

    /**
     * 机柜未开通数 负载率等于0
     */
    @Schema(description = "机柜未开通数")
    private Integer cabUnused;

}
