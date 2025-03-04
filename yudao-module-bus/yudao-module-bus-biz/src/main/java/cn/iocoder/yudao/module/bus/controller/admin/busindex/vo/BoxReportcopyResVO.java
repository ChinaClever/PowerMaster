package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import lombok.Data;

@Data
public class BoxReportcopyResVO {

    private String boxName;

    private String devKey;

    private Integer runStatus;

    private Double powApparent;

    private Double powActiveOne;

    private Double powActiveTwo;

    private Double powActiveThree;
}
