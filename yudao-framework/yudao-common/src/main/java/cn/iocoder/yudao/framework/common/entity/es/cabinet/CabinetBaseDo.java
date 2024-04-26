package cn.iocoder.yudao.framework.common.entity.es.cabinet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜基础表
 * @date 2024/4/23 9:18
 */
@Data
public class CabinetBaseDo {

    private int id;

    @JsonProperty("cabinet_id")
    private int cabinetId;

    /**
     * 创建时间
     */
    @JsonProperty("create_time")
    private String createTime;
}
