package cn.iocoder.yudao.framework.common.entity.es.cabinet.env;

import cn.iocoder.yudao.framework.common.entity.es.cabinet.CabinetBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author JIANGJINCHI
 * @version 1.0
 * @description: 机柜环境基础表
 * @date 2024/4/23 10:19
 */
@Data
public class CabinetEnvBaseDo {

    @JsonProperty("cabinet_id")
    private int cabinetId;

    @JsonProperty("create_time")
    private String createTime;

    @JsonProperty("sensor_list")
    private sensorList sensorList;

    @Data
    public class sensorList{
        @JsonProperty("black")
        private List<CabinetEnvRealtimeSensorList> black;
        @JsonProperty("front")
        private List<CabinetEnvRealtimeSensorList> front;
    }
}
