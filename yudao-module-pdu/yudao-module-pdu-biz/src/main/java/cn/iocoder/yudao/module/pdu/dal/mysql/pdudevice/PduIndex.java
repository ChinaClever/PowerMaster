package cn.iocoder.yudao.module.pdu.dal.mysql.pdudevice;

import lombok.Data;

@Data
public class PduIndex {

    private Long id;

    private String devKey;

    private String ipAddr;

    private Integer cascadeAddr;

    private Integer isDeleted;

    private Integer runStatus;
}
