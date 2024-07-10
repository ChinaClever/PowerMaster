package cn.iocoder.yudao.module.cabinet.controller.admin.index.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BarSeries extends SeriesBase {

    private String type = "bar";

    private Integer barWidth = 15;

    private String label = "{ show: true, position: 'top' }";
}
