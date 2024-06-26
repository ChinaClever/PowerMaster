package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import lombok.Data;

import java.util.List;

@Data
public class BusPFDetailRes {

    private List<Float> powerFactorAvgValueA;

    private List<Float> powerFactorAvgValueB;

    private List<Float> powerFactorAvgValueC;

    private List<String> time;

}
