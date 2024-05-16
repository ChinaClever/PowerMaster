package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo;

import lombok.Data;

@Data
public class PDULineRes {

    private Long pduId;

    private String devKey;

    private Double L1MaxCur;

    private Double L1MaxVol;

    private Double L1MaxPow;

    private Double L2MaxCur;

    private Double L2MaxVol;

    private Double L2MaxPow;

    private Double L3MaxCur;

    private Double L3MaxVol;

    private Double L3MaxPow;

    private String Location;
}
