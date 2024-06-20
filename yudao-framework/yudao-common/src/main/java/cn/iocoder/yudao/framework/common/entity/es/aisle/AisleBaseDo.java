package cn.iocoder.yudao.framework.common.entity.es.aisle;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列基础数据
 * @date 2024/6/7 13:25
 */
@Data
public class AisleBaseDo {


    private int id;

    @JsonProperty("aisle_id")
    private int aisleId;

    /**
     * 创建时间
     */
    @JsonProperty("create_time")
    private DateTime createTime;


}
