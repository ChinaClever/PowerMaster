package cn.iocoder.yudao.module.room.controller.admin.roomindex.vo;

import lombok.Data;

import java.util.List;

@Data
public class LineSeries extends  SeriesBase{

    private String type = "line";

    private String symbol = "none";

    private List<String> happenTime;

}