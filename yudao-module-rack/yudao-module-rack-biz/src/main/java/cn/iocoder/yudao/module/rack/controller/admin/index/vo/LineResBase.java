package cn.iocoder.yudao.module.rack.controller.admin.index.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LineResBase {

    private List<SeriesBase> series = new ArrayList<>();

    private List<String> time = new ArrayList<>();
}