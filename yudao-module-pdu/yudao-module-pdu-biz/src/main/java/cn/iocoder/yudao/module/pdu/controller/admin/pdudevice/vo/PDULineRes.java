package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo;

import lombok.Data;

@Data
public class PDULineRes {

    private Long pduId;

    private String devKey;

    private Double L1MaxCur;

    private String L1MaxCurTime;

    private Double L1MaxVol;

    private String L1MaxVolTime;

    private Double L1MaxPow;

    private String L1MaxPowTime;

    private Double L2MaxCur;

    private String L2MaxCurTime;

    private Double L2MaxVol;

    private String L2MaxVolTime;

    private Double L2MaxPow;

    private String L2MaxPowTime;

    private Double L3MaxCur;

    private String L3MaxCurTime;

    private Double L3MaxVol;

    private String L3MaxVolTime;

    private Double L3MaxPow;

    private String L3MaxPowTime;

    private String Location;

    private Integer status;
}
