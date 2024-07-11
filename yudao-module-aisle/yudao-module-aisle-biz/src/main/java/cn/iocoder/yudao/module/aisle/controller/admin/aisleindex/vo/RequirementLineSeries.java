package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RequirementLineSeries extends LineSeries{

    private List<String> maxTime = new ArrayList<>();
}