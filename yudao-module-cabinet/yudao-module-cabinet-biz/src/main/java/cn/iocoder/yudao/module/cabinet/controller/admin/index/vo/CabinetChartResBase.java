package cn.iocoder.yudao.module.cabinet.controller.admin.index.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CabinetChartResBase {

    private List<SeriesBase> series = new ArrayList<>();

    private List<String> time = new ArrayList<>();
}