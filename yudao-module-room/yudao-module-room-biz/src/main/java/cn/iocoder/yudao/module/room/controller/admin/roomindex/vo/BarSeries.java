package cn.iocoder.yudao.module.room.controller.admin.roomindex.vo;

import lombok.Data;

@Data
public class BarSeries extends SeriesBase {

    private String type = "bar";

    private Integer barWidth = 15;

    private String label = "{ show: true, position: 'top' }";
}
