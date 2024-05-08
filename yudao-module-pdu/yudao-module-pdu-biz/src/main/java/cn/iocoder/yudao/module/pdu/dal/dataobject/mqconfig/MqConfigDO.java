package cn.iocoder.yudao.module.pdu.dal.dataobject.mqconfig;

import lombok.*;

import java.time.LocalDateTime;
import java.util.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 消息队列系统配置 DO
 *
 * @author 芋道源码
 */
@TableName("sys_mq_config")
@KeySequence("sys_mq_config_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MqConfigDO  {

    /**
     * 主键ID
     */
    @TableId
    private Integer id;
    /**
     * ip端口
     */
    private String ipAndPorts;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 密码
     */
    private String passWord;
    /**
     * 主题/分组
     */
    private String topic;
    /**
     * mq名称
     */
    private String mq;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}