package cn.iocoder.yudao.module.pdu.dal.dataobject.pdudevice;

import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * PDU设备 DO
 *
 * @author 芋道源码
 */
@TableName("pdu_device")
@KeySequence("pdu_device_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PDUDeviceDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 设备唯一识别码
     */
    private String devKey;
    /**
     * IP地址
     */
    private String ipAddr;
    /**
     * 级联地址
     */
    private Integer cascadeNum;

}