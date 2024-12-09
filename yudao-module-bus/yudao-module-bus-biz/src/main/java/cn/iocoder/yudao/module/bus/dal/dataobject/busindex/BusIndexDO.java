package cn.iocoder.yudao.module.bus.dal.dataobject.busindex;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 始端箱索引 DO
 *
 * @author clever
 */
@TableName("bus_index")
@KeySequence("bus_index_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusIndexDO {

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
    private Integer nodeId;
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