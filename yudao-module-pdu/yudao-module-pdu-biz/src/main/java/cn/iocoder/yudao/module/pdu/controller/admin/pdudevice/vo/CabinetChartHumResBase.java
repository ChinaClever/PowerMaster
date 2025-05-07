package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo;



import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CabinetChartHumResBase {
    private List<HumSeries> series = new ArrayList<>();

    private List<String> time = new ArrayList<>();

//    private List<String> happenTime = new ArrayList<>();

}
