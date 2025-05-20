package cn.iocoder.yudao.module.room.job;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.room.service.energyconsumption.RoomEnergyConsumptionService;
import cn.iocoder.yudao.module.room.vo.RoomEleExportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

//@Component
public class roomJob implements JobHandler {
    @Autowired
    private RoomEnergyConsumptionService roomEnergyConsumptionService;

    @Value("${basePath}")
    private String basePath;

    @Value("${ip}")
    private String ip;

    @Override
    public String execute(String param) throws Exception {
        List<RoomEleExportVO> list = roomEnergyConsumptionService.exportRoomEle();

        return null;
    }
}
