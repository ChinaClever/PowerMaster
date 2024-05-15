package cn.iocoder.yudao.module.cabinet.dto;

import cn.iocoder.yudao.module.cabinet.enums.CabinetChannelEnum;
import cn.iocoder.yudao.module.cabinet.enums.CabinetPduEnum;
import cn.iocoder.yudao.module.cabinet.enums.CabinetPositionEnum;
import cn.iocoder.yudao.module.cabinet.enums.SensorTypeEnum;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜环境传感器关联
 * @date 2024/5/10 13:47
 */
@Data
public class CabinetEnvSensorDTO {


    @Schema(description = "id", example = "1")
    private int id;


    /**
     * 机柜id
     */
    @Schema(description = "机柜id", example = "1")
    private int cabinetId;

    /**
     * 机柜前后通道 1前2后
     */
    @Schema(description = "机柜前后通道 1前2后", example = "1")
    private int channel;

    /**
     * 位置 1 上 2 中 3下
     */
    @Schema(description = "位置", example = "1")
    private int position;

    /**
     * pdu
     */
    @Schema(description = "pdu", example = "A")
    private String pathPdu;

    /**
     * 传感器类型
     */
    @Schema(description = "传感器类型", example = "{}")
    private int sensorType;

    /**
     * 传感器id
     */
    @Schema(description = "传感器id", example = "1")
    private int sensorId;

}
