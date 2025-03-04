package cn.iocoder.yudao.framework.common.entity.mysql.aisle;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列与母线绑定关系
 * @date 2024/6/7 9:29
 */
@Schema(description = "管理后台 - 柜列与母线绑定关系 Response VO")
@Data
@TableName(value = "aisle_bar")
public class AisleBar implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键id", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 柜列id
     */
    @Schema(description = "柜列id", example = "1")
    private Integer aisleId;


    /**
     *  唯一标识
     */
    private String busKey;

    private Integer boxNum;


    /**
     * AB路
     */
    private String path;

    /**
     * 方向  左0 右1
     */
    private Boolean direction;


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
