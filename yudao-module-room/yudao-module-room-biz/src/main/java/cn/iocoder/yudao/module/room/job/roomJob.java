package cn.iocoder.yudao.module.room.job;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.entity.mysql.FileManage;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.room.service.energyconsumption.RoomEnergyConsumptionService;
import cn.iocoder.yudao.module.room.vo.RoomEleExportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class roomJob implements JobHandler {
    @Autowired
    private RoomEnergyConsumptionService roomEnergyConsumptionService;

    @Override
    public String execute(String param) throws Exception {
        List<RoomEleExportVO> list = roomEnergyConsumptionService.exportRoomEle();
        LocalDate now = LocalDate.now();
        String format = LocalDateTimeUtil.format(now, "yyyy-MM-dd");
        String name = format + ".xlsx";
        String path = "F:\\Temp" + name;
        ExcelUtils.writePath(path, "数据", RoomEleExportVO.class, list);
        FileManage fileManage = new FileManage();
        fileManage.setFileName(name);
        fileManage.setFileUrl(path);
        fileManage.setSysType(1);
        roomEnergyConsumptionService.saveFileManage(fileManage);
        return String.format("执行成功！");
    }
}
