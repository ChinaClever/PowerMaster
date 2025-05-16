package cn.iocoder.yudao.module.cabinet.controller.admin.index.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LineSeries extends SeriesBase {

    private String type = "line";

    private String symbol = "none";

    private List<String> happenTime = new ArrayList<>();

    private String projectName;

    private Float maxValue;

    private Float minValue;

    private String maxTime;

    private String minTime;


}