package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RequirementLineSeries extends LineSeries{

    private List<String> maxTime = new ArrayList<>();
}
