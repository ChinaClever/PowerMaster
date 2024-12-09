package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BarSeries extends SeriesBase {

    private String type = "bar";

    private Integer barWidth = 15;

    private String label = "{ show: true, position: 'top' }";
}