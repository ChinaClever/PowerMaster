package cn.iocoder.yudao.module.cabinet.dal.dataobject.index;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 机柜索引 DO
 *
 * @author 芋道源码
 */
@TableName("cabinet_index")
@KeySequence("cabinet_index_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexDO  {

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
     * 机柜名称
     */
    private String cabinetName;
    /**
     * 通道编号
     */
    private Integer aisleId;
    /**
     * 电力容量
     */
    private Double powerCapacity;
    /**
     * 数据来源 0：PDU 1：母线
     */
    private Integer pduBox;
    /**
     * 禁用 0：启用 1：禁用
     */
    private Integer isDisabled;
    /**
     * 是否删除 0未删除 1已删除
     */
    private Integer isDeleted;
    /**
     * 运行状态
     */
    private Integer runStatus;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 最后更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}