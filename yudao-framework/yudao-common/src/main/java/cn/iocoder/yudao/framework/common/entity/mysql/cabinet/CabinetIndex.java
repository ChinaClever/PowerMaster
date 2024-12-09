package cn.iocoder.yudao.framework.common.entity.mysql.cabinet;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜索引表
 * @date 2024/4/23 10:40
 */
@Data
@TableName(value = "cabinet_index")
public class CabinetIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId
    private Integer id;
    /**
     * 机房id
     */
    private Integer roomId;

    /**
     * 通道编号 0不属于任何柜列
     */
    private Integer  aisleId;

    /**
     *  柜列的位置 从1开始 0未被分配
     */
    private Integer  aisleX;

    /**
     * 机柜类型
     */
    private String cabinetType;
    /**
     * 机柜名称
     */
    private String cabinetName;


    private String  cabinetHeight;
    /**
     * 电力容量
     */
    private Double powerCapacity;

    /**
     * 禁用 0：启用 1：禁用
     */
    private Boolean isDisabled;

    /**
     * 运行状态
     */
    private Integer runStatus;

    /**
     * 负载状态
     */
    private Integer  loadStatus;

    /**
     * 数据来源 0：PDU 1：母线
     */
    private Boolean pduBox;

    /**
     * 是否删除 0未删除 1已删除
     */
    private Boolean isDeleted;
    /**
     * 创建时间
     */
//    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 最后更新时间
     */
//    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "count(*)",insertStrategy = FieldStrategy.NEVER,updateStrategy = FieldStrategy.NEVER,select = false)
    private  Integer count;
}
