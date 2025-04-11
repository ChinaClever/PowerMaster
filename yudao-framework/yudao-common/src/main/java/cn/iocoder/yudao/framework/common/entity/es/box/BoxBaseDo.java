package cn.iocoder.yudao.framework.common.entity.es.box;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 插接箱数据库表
 * @date 2024/5/17 9:09
 */
@Data
public class BoxBaseDo {


    private int id;

    @JsonProperty("box_id")
    private int boxId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("create_time")
    private DateTime createTime;

}
