package cn.iocoder.yudao.module.room.dto;

import cn.iocoder.yudao.framework.common.dto.aisle.AisleCabinetDTO;
import cn.iocoder.yudao.framework.common.dto.aisle.AisleDetailDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;


/**
 * @author luowei
 * @version 1.0
 * @description: 机房详情
 * @date 2024/6/21 15:49
 */
@Data
public class RoomDetailDTO {

    /**
     * 机房iD
     */
    private Integer id;


    /**
     * 机房名称
     */
    private String roomName;

    /**
     * 电量容量
     */
    private float powerCapacity;


    /**
     * 机房x长度(单位机柜)
     */
    @JsonProperty(value="xLength")
    private int xLength;

    /**
     * 机房Y长度(单位机柜)
     */
    @JsonProperty(value="yLength")
    private int yLength;

    //柜列数据
    List<AisleDetailDTO> aisleList;


    /**
     * 机柜数据
     */
    private List<AisleCabinetDTO> cabinetList;

    /**
     * 日用能告警开关
     */
    private  int eleAlarmDay;

    /**
     * 月用能告警开关
     */
    private  int eleAlarmMonth;

    /**
     * 日用能限制
     */
    private double eleLimitDay;

    /**
     * 月用能限制
     */
    private double eleLimitMonth;


}
