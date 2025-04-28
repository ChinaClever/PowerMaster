package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 湿度数据
 */

@Data
public class HumSeries {

    private String type = "line";

    private String symbol = "none";

    private String name;

    private List<Integer> data = new ArrayList<>();



}
