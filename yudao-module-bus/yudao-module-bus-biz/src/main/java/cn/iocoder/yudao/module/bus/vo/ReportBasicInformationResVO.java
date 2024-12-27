package cn.iocoder.yudao.module.bus.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReportBasicInformationResVO {

   private String location;

    private String devKey;
   private BigDecimal powApparent;
    /**
     * 运行状态
     */
    private int runStatus;

    private BigDecimal powerFactor;
}
