package cn.iocoder.yudao.module.cabinet.dal.dataobject.index;

import lombok.Data;

@Data
public class PduIndex {

    private Integer id;

    private String devKey;

    private String ipAddr;

    private String cascadeAddr;
}