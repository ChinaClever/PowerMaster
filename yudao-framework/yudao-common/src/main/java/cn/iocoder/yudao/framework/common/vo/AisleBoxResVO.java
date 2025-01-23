package cn.iocoder.yudao.framework.common.vo;

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
 * @description: 柜列与插接箱绑定关系
 * @date 2024/6/7 9:29
 */
@Schema(description = "管理后台 - 柜列与插接箱绑定关系 Response VO")
@Data
public class AisleBoxResVO implements Serializable {

    private static final long serialVersionUID = 1L;


    private Integer id;

    private Integer roomId;

    private String roomName;
    /**
     * 柜列id
     */
    @Schema(description = "柜列id", example = "1")
    private Integer aisleId;


    /**
     *
     */
    private String boxKey;

    /**
     *
     */
    private String busKey;
    /**
     * 对应绑定母线id
     */
    private Integer aisleBarId;

    /**
     * 类型 0 插接箱 1 连接单元
     */
    private Integer boxType;

    /**
     * 标记位
     */
    private Integer boxIndex;

    /**
     * 输出位数量
     */
    private Integer outletNum;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
//    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
//    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
