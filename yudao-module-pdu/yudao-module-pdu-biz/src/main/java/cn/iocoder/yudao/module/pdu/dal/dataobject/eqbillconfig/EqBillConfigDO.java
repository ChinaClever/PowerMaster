package cn.iocoder.yudao.module.pdu.dal.dataobject.eqbillconfig;

import lombok.*;

import java.time.LocalDateTime;
import java.util.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * pdu电量计费方式配置 DO
 *
 * @author clever
 */
@TableName("pdu_eq_bill_config")
@KeySequence("pdu_eq_bill_config_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EqBillConfigDO  {

    /**
     * 主键id
     */
    @TableId
    private Integer id;
    /**
     * 电费单价
     */
    private Double bill;
    /**
     * 计费方式 1固定计费 2 分段计费
     */
    private Integer billMode;
    /**
     * 计费时间段
     */
    private String billPeriod;
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