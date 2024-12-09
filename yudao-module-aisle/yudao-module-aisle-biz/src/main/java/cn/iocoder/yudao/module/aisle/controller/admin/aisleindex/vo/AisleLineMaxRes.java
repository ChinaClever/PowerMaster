package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import lombok.Data;

@Data
public class AisleLineMaxRes extends AisleIndexRespVO{

    private Float maxPowTotal;

    private String maxPowTotalTime;

    private Float maxPowA;

    private String maxPowATime;

    private Float maxPowB;

    private String maxPowBTime;

}
