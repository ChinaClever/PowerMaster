package cn.iocoder.yudao.module.bus.dal.dataobject.boxindex;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 始端箱索引 DO
 *
 * @author clever
 */
@TableName("box_index")
@KeySequence("bus_index_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoxIndexDO {

    /**
     * id
     */
    @TableId
    private Integer id;
    /**
     * 设备识别码
     */
    private String busKey;
    /**
     * ip地址
     */
    private String ipAddr;

    /**
     * 母线编号
     */
    private Integer busId;

    /**
     * 节点IP
     */
    private String  busName;

    /**
     * 运行状态 0：离线 1：正常  2：告警
     */
    private Integer runStatus;
    /**
     * 节点IP
     */
    private Integer nodeIp;
    /**
     * 逻辑删除
     */
    private Boolean isDeleted;
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