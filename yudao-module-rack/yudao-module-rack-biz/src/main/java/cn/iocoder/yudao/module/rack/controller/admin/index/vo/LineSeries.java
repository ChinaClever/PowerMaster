package cn.iocoder.yudao.module.rack.controller.admin.index.vo;

import lombok.Data;

@Data
public class LineSeries extends  SeriesBase{

    private String type = "line";

    private String symbol = "none";

}