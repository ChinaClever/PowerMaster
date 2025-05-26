package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo;

import cn.iocoder.yudao.module.pdu.dal.dataobject.pdudevice.PDUDeviceDO;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PDULineRes extends PDUDeviceDO {

    private Integer pduId;

    private String devKey;

    private BigDecimal L1MaxCur;

    private String L1MaxCurTime;

    private BigDecimal L1MaxVol;

    private String L1MaxVolTime;

    private BigDecimal L1MaxPow;

    private String L1MaxPowTime;

    private BigDecimal L2MaxCur;

    private String L2MaxCurTime;

    private BigDecimal L2MaxVol;

    private String L2MaxVolTime;

    private BigDecimal L2MaxPow;

    private String L2MaxPowTime;

    private BigDecimal L3MaxCur;

    private String L3MaxCurTime;

    private BigDecimal L3MaxVol;

    private String L3MaxVolTime;

    private BigDecimal L3MaxPow;

    private String L3MaxPowTime;

    private String Location;

    private Integer status;
}
