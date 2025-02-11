package cn.iocoder.yudao.module.cabinet.controller.admin.index.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CabinetRackRspVO {

    /**
     * 机架id
     */
    private Integer id;

    /**
     * 机架名
     */
    private String name;

    /**
     * 总功率
     */
    private Double totalPower;

    /**
     * a路电流
     */
    private Double aCurrent;

    /**
     * b路电流
     */
    private Double bCurrent;
}
