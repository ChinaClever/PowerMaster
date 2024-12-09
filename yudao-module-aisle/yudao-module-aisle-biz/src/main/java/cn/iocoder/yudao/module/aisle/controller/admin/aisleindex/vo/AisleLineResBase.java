package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AisleLineResBase {

    private List<SeriesBase> series = new ArrayList<>();

    private List<String> time = new ArrayList<>();
}