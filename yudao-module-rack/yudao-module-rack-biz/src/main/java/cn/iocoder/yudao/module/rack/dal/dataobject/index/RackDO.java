package cn.iocoder.yudao.module.rack.dal.dataobject.index;

import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

import java.time.LocalDateTime;

/**
 * 机架索引 DO
 *
 * @author clever
 */
@TableName("rack_index")
@KeySequence("rack_index_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RackDO  {

    /**
     * 机架id
     */
    @TableId
    private Integer id;
    /**
     * 机柜id
     */
    private Integer cabinetId;
    /**
     * 机房id
     */
//    private Integer roomId;
    /**
     * U位名称
     */
    private String rackName;

    /**
     * A路PDU输出位
     */
    private String outletIdA;
    /**
     * B路PDU输出位
     */
    private String outletIdB;
    /**
     * 所属公司
     */
    private String company;
    /**
     * U位位置
     */
    private Integer uAddress;
    /**
     * U位高度
     */
    private Integer uHeight;
    /**
     * 设备类型
     */
    private String rackType;

    /**
     * 是否删除 0未删除 1已删除
     */
    private Boolean isDelete;
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


}
