package cn.iocoder.yudao.framework.common.entity.es.cabinet.env;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CabinetEnvHourSensorList {
    @JsonProperty("sensorId")
    private Integer sensorId;
    @JsonProperty("tem_avg_value")
    private BigDecimal temAvgValue;
    @JsonProperty("tem_max_value")
    private BigDecimal temMaxValue;
    @JsonProperty("tem_min_value")
    private BigDecimal temMinValue;
    @JsonProperty("hum_avg_value")
    private BigDecimal humAvgValue;
    @JsonProperty("hum_max_value")
    private BigDecimal humMaxValue;
    @JsonProperty("hum_min_value")
    private BigDecimal humMinValue;
    @JsonProperty("max_time")
    private LocalDateTime maxTime;
    @JsonProperty("min_time")
    private LocalDateTime minTime;

}
