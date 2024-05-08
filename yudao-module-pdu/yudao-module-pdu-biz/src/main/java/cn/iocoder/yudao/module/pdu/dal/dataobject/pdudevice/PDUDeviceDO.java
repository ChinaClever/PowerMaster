package cn.iocoder.yudao.module.pdu.dal.dataobject.pdudevice;

import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * PDU设备 DO
 *
 * @author 芋道源码
 */

@Data
public class PDUDeviceDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 设备唯一识别码
     */
    private int status;
    private int color;

    private Double apparentPow;

    private Double pf;

    private Double pow;

    private Double reactivePow;

    private Double ele;
    /**
     * IP地址
     */
    private String devKey;

    private String location;

    private String pduAlarm;

    private String dataUpdateTime;

    private Double Acur ;

    private Double Bcur ;

    private Double Ccur ;

    private double curUnbalance;

    private Double Avol ;

    private Double Bvol ;

    private Double Cvol ;

    private double volUnbalance;

}