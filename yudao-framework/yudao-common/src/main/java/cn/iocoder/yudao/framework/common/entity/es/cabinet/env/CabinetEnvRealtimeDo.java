package cn.iocoder.yudao.framework.common.entity.es.cabinet.env;

import cn.iocoder.yudao.framework.common.entity.es.cabinet.CabinetBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author JIANGJINCHI
 * @version 1.0
 * @description: 机柜环境表（实时）
 * @date 2024/4/23 9:49
 */
@Data
public class CabinetEnvRealtimeDo {
    @JsonProperty("cabinet_id")
    private int cabinetId;

    @JsonProperty("create_time")
    private String createTime;

    @JsonProperty("sensor_list")
    private CabinetEnvBaseDo.sensorList sensorList;

    @Data
    public class sensorList{
        @JsonProperty("black")
        private List<CabinetEnvRealtimeSensorList> black;
        @JsonProperty("front")
        private List<CabinetEnvRealtimeSensorList> front;
    }

}
