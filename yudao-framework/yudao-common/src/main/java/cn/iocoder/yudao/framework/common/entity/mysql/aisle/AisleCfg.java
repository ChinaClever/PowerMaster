package cn.iocoder.yudao.framework.common.entity.mysql.aisle;


import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 柜列配置")
@Data
@TableName(value = "aisle_cfg")
public class AisleCfg implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 机房id
     */
    @Schema(description = "柜列id", example = "1")
    private Integer aisleId;

    /**
     * 电力容量
     */
    @Schema(description = "电力容量", example = "0")
    private Float powerCapacity;


    /**
     * 类型
     */
    private String aisleType;

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

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
