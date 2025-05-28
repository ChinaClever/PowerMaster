package cn.iocoder.yudao.framework.common.entity.mysql.room;


import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 机房配置")
@Data
@TableName(value = "room_cfg")
public class RoomCfg implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "机房配置id", example = "2")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "关联机房id", example = "2")
    private Integer roomId;

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

    /**
     * 是否删除
     */
    @Schema(description = "是否删除 0未删除 1已删除", example = "0")
    private Integer isDelete;

    /**
     * 日用能告警开关
     */
    private  Integer eleAlarmDay;

    /**
     * 月用能告警开关
     */
    private  Integer eleAlarmMonth;

    /**
     * 日用能限制
     */
    private Double eleLimitDay;

    /**
     * 月用能限制
     */
    private Double eleLimitMonth;

}
