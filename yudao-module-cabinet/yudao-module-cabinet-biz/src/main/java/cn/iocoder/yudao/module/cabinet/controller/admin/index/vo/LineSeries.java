package cn.iocoder.yudao.module.cabinet.controller.admin.index.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LineSeries extends SeriesBase {

    private String type = "line";

    private String symbol = "circle";

    private Integer symbolSize =  4;
}