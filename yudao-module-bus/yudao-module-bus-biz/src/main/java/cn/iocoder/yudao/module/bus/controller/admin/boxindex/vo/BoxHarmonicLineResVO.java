package cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo;

import lombok.Data;

import java.util.List;

@Data
public class BoxHarmonicLineResVO {
    private String roomName;

    private String busName;

    private String boxName;

    private List<String> time;

    private List<Float> lineOne;

    private List<Float> linetwe;

    private List<Float> linethree;
}
