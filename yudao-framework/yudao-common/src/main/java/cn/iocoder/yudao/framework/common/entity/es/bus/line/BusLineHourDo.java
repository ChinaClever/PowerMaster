package cn.iocoder.yudao.framework.common.entity.es.bus.line;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 始端箱相历史数据表（按小时算））
 * @date 2024/5/21 14:02
 */
@Data
public class BusLineHourDo extends BusLineBaseDo {

    /**
     * 电流谐波含量
     */
    @JsonProperty("cur_thd")
    private float[] curThd;

    /**
     * 电压谐波含量
     */
    @JsonProperty("vol_thd")
    private float[]  volThd;
}
