package cn.iocoder.yudao.framework.common.entity.es.rack;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 机架基础表
 * @date 2024/4/23 9:18
 */
@Data
public class RackBaseDo {

    private int id;

    @JsonProperty("cabinet_id")
    private int cabinetId;


    @JsonProperty("rack_id")
    private int rackId;

    /**
     * 创建时间
     */
    @JsonProperty("create_time")
    private DateTime createTime;
}
