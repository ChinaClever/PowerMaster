package cn.iocoder.yudao.framework.common.entity.mysql.aisle;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 通道索引
 * @date 2024/5/6 8:52
 */
@Data
@TableName(value = "aisle_index")
public class AisleIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    /**
     * 机房id
     */
    private Integer roomId;

    /**
     * 柜列名称
     */
    private String name;

    /**
     * 数据来源 0：PDU 1：母线
     */
    private Integer pduBar;

    /**
     * 是否删除
     */
    private Integer isDelete;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
