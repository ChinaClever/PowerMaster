package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LineSeries {

    private String name;

    private String type = "line";

    private String symbol = "none";

    private List<Float> data = new ArrayList<>();
}
