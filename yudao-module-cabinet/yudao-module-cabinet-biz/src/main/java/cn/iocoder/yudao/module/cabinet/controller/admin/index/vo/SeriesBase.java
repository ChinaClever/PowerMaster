package cn.iocoder.yudao.module.cabinet.controller.admin.index.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SeriesBase {

    private String name;

    private List<Float> data = new ArrayList<>();

}
