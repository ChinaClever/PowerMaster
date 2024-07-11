package cn.iocoder.yudao.module.aisle.dal.dataobject.aisleindex;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 通道列 DO
 *
 * @author clever
 */
@TableName("aisle_index")
@KeySequence("aisle_index_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AisleIndexDO {

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
     * 通道名称
     */
    private String name;
    /**
     * 数据来源
     */
    private Integer pduBar;
    /**
     * 是否删除
     */
    private Integer isDelete;
    /**
     * 长度
     */
    private Integer length;
    /**
     * 柜列类型
     */
    private String type;
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