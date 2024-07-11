package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import lombok.Data;

import java.util.List;

@Data
public class BusHarmonicRedisRes {

    private List<Integer> times;

    private List<Float> harmonicList;

}
