package cn.iocoder.yudao.framework.common.entity.mysql.room;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 机房索引表
 * @date 2024/4/24 10:34
 */
@Schema(description = "管理后台 - 机房详情 Response VO")
@Data
@TableName(value = "room_index")
public class RoomIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "机房id", example = "2")
    @TableId(value = "id", type = IdType.AUTO)
    private int id;


    /**
     * 机房名
     */
    @Schema(description = "机房名", example = "机房2")
    private String name;

    /**
     * 是否删除
     */
    @Schema(description = "是否删除 0未删除 1已删除", example = "0")
    private int isDelete;

    /**
     * 电力容量
     */
    @Schema(description = "电力容量", example = "0")
    private float powerCapacity;
    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2024-05-07 01:00:00")
    private LocalDateTime updateTime;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-05-07 01:00:00")
    private LocalDateTime createTime;
}
