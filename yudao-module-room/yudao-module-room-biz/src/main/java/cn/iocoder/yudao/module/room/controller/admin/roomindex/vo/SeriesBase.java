package cn.iocoder.yudao.module.room.controller.admin.roomindex.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SeriesBase {

    private String name;

    private List data = new ArrayList<>();

}