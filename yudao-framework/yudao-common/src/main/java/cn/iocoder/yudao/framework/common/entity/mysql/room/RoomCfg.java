package cn.iocoder.yudao.framework.common.entity.mysql.room;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 机房配置表
 * @date 2024/4/28 13:53
 */
@Data
@TableName(value = "room_cfg")
public class RoomCfg implements Serializable {
    private static final long serialVersionUID = 1L;



    @TableId(value = "id", type = IdType.AUTO)
    private int id;


    /**
     * 机房id
     */
    private int roomId;


    /**
     * 机房x长度(单位机柜)
     */
    @JsonProperty(value="xLength")
    private int xLength;

    /**
     * 机房Y长度(单位机柜)
     */
    @JsonProperty(value="yLength")
    private int yLength;


    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
