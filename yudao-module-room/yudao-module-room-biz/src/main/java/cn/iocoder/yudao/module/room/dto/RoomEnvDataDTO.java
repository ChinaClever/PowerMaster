package cn.iocoder.yudao.module.room.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;


/**
 * @author luowei
 * @version 1.0
 * @description: 机房详情
 * @date 2024/6/21 15:49
 */
@Data
public class RoomEnvDataDTO {

    /**
     * 机房iD
     */
    private Integer id;

    /**
     * 平均温度
     */
    private float temAvg;


    /**
     * 最高温度
     */
    private  float temMax;

    /**
     * 最低温度
     */
    private  float temMin;

    /**
     * 平均湿度
     */
    private float humAvg;


    /**
     * 最高湿度
     */
    private  float humMax;

    /**
     * 最低湿度
     */
    private  float humMin;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;


}
