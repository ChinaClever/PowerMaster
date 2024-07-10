package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LineSeries extends  SeriesBase{

    private String type = "line";

    private String symbol = "none";

}
