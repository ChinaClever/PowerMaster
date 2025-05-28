package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LineSeries extends  SeriesBase{

    private String type = "line";

    private String symbol = "none";

    private List<String> happenTime = new ArrayList<>();

}