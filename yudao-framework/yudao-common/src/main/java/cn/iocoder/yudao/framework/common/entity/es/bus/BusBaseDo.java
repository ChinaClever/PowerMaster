package cn.iocoder.yudao.framework.common.entity.es.bus;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 始端箱基础类
 * @date 2024/5/17 8:54
 */
@Data
public class BusBaseDo {


    private int id;

    @JsonProperty("bus_id")
    private int busId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("create_time")
    private DateTime createTime;
}
