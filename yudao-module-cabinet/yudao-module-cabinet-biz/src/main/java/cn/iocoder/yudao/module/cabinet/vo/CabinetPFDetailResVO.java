package cn.iocoder.yudao.module.cabinet.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CabinetPFDetailResVO {

    private List<Float> powerFactorAvgValueA;

    private List<Float> powerFactorAvgValueB;

    private List<Float> powerFactorAvgValueTotal;

    private List<String> time;

}
