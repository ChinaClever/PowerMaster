package cn.iocoder.yudao.module.room.dal.dataobject.roomindex;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 机房索引 DO
 *
 * @author clever
 */
@TableName("room_index")
@KeySequence("room_index_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomIndexDO {

    /**
     * 主键id
     */
    @TableId
    private Integer id;
    /**
     * 机房名
     */
    private String name;
    /**
     * 是否删除
     */
    private Integer isDelete;
    /**
     * 电力容量
     */
    private Double powerCapacity;
    /**
     * 日用电告警开关 0 关 1开
     */
    private Integer eleAlarmDay;
    /**
     * 日用能限制
     */
    private Double eleLimitDay;
    /**
     * 月用电告警开关 0关 1开
     */
    private Integer eleAlarmMonth;
    /**
     * 月用能限制
     */
    private Double eleLimitMonth;
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