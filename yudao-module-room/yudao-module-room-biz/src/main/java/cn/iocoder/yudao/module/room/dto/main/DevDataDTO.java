package cn.iocoder.yudao.module.room.dto.main;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 主页面设备数据
 * @date 2024/6/21 15:49
 */
@Data
public class DevDataDTO {

    /**
     * pdu数量
     */
    private int pduNum;

     /**
     * pdu离线数
     */
    private int pduOffLine;

    /**
     * pdu在线数
     */
    private int pduOnLine;

    /**
     * 告警
     */
    @Schema(description = "pdu告警")
    private Integer pduInform;
    /**
     * 母线数量
     */
    private int barNum;
    /**
     * 始端箱数量
     */
    private int busNum;

    /**
     * 始端箱离线数
     */
    private int busOffLine;

    /**
     * 告警
     */
    @Schema(description = "bus告警")
    private Integer busInform;

    /**
     * 始端箱在线数
     */
    private int busOnLine;

    /**
     * 插接箱数量
     */
    private int boxNum;

    /**
     * 插接箱离线数
     */
    private int boxOffLine;
    /**
     * 告警
     */
    @Schema(description = "box告警")
    private Integer boxInform;
    /**
     * 插接箱在线数
     */
    private int boxOnLine;


    /**
     * 机柜数量
     */
    private int cabNum;

    /**
     * 机柜启用数  负载率>0
     */
    private int cabUse;

    /**
     * 机柜未开通数 负载率等于0
     */
    private int cabUnused;

    /**
     * 系统运行天数
     */
    private Long days;

}
