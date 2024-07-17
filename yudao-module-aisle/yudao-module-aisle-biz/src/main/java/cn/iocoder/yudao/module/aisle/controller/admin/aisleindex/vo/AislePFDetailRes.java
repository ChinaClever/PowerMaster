package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import lombok.Data;

import java.util.List;

@Data
public class AislePFDetailRes {

    private List<Float> factorTotalAvgValue;

    private List<Float> factorAAvgValue;

    private List<Float> factorBAvgValue;

    private List<String> time;

}