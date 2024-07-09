package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SeriesBase {

    private String name;

    private List<Float> data = new ArrayList<>();

}