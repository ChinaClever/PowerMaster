package cn.iocoder.yudao.framework.common.entity.es.room;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 机房基础数据
 * @date 2024/6/7 13:25
 */
@Data
public class RoomBaseDo {


    private int id;

    @JsonProperty("room_id")
    private int roomId;

    /**
     * 创建时间
     */
    @JsonProperty("create_time")
    private DateTime createTime;


}
