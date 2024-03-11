package cn.iocoder.yudao.module.pdu.dal.dataobject.historydata;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * pdu历史数据 DO
 *
 * @author 芋道源码
 */
@TableName("pdu_history_data")
@KeySequence("pdu_history_data_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
//@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryDataDO{

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * pdu编号
     */
    private Long pduId;
    /**
     * 数据类型	
     */
    private String type;
    /**
     * topic
     */
    private String topic;
    /**
     * indexes
     */
    private Long indexes;
    /**
     * 值
     */
    private Double value;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}