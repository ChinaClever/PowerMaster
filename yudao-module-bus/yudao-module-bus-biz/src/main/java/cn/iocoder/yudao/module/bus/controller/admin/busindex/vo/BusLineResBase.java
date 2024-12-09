package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BusLineResBase {

    private List<SeriesBase> series = new ArrayList<>();

    private List<String> time = new ArrayList<>();
}

