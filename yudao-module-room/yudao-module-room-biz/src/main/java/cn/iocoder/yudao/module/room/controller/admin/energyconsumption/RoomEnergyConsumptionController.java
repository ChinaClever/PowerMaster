package cn.iocoder.yudao.module.room.controller.admin.energyconsumption;

import cn.iocoder.yudao.framework.common.entity.mysql.FileManage;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.room.controller.admin.energyconsumption.VO.*;
import cn.iocoder.yudao.module.room.service.energyconsumption.RoomEnergyConsumptionService;
import cn.iocoder.yudao.module.room.vo.RoomEleExportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - 机房能耗数据")
@RestController
@RequestMapping("/room/eq-data")
@Validated
public class RoomEnergyConsumptionController {
    @Resource
    private RoomEnergyConsumptionService roomEnergyConsumptionService;

    @GetMapping("/page")
    @Operation(summary = "获得机房电量数据分页")
    public CommonResult<PageResult<Object>> getEQDataPage(RoomEnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = roomEnergyConsumptionService.getEQDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出机房能耗趋势数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportEQDataExcel(RoomEnergyConsumptionPageReqVO pageReqVO,
                                  HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = roomEnergyConsumptionService.getEQDataPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "机房能耗趋势数据.xlsx", "数据", EQPageRespVO.class,
                BeanUtils.toBean(list, EQPageRespVO.class));
    }


    @GetMapping("/bill-page")
    @Operation(summary = "获得机房电费数据分页")
    public CommonResult<PageResult<Object>> getBillDataPage(RoomEnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = roomEnergyConsumptionService.getBillDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/bill-export-excel")
    @Operation(summary = "导出机房电费统计数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportBillDataExcel(RoomEnergyConsumptionPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = roomEnergyConsumptionService.getBillDataPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "机房电费统计数据.xlsx", "数据", BillPageRespVO.class,
                BeanUtils.toBean(list, BillPageRespVO.class));
    }

    @GetMapping("/details")
    @Operation(summary = "获得机房电量数据详情")
    public CommonResult<PageResult<Object>> getEQDataDetails(RoomEnergyConsumptionPageReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = roomEnergyConsumptionService.getEQDataDetails(reqVO);
        return success(pageResult);
    }

    @GetMapping("/detailsExcel")
    @Operation(summary = "获得机房电量数据详情")
    public void getEQDataDetailsExcel(RoomEnergyConsumptionPageReqVO reqVO, HttpServletResponse response) throws IOException {
        PageResult<Object> pageResult = roomEnergyConsumptionService.getEQDataDetails(reqVO);
        List<Object> list = pageResult.getList();
        ExcelUtils.write(response, "机房能耗排名.xlsx", "数据", OutLetsPageRespVO.class,
                BeanUtils.toBean(list, OutLetsPageRespVO.class));
    }

    @GetMapping("/realtime-page")
    @Operation(summary = "获得机房实时电量数据分页")
    public CommonResult<PageResult<Object>> getRealtimeEQDataPage(RoomEnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = roomEnergyConsumptionService.getRealtimeEQDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/realtime-export-excel")
    @Operation(summary = "导出机房电能记录数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportRealtimeEQDataExcel(RoomEnergyConsumptionPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = roomEnergyConsumptionService.getRealtimeEQDataPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "机房电能记录数据.xlsx", "数据", RealtimeEQPageRespVO.class,
                BeanUtils.toBean(list, RealtimeEQPageRespVO.class));
    }

    @GetMapping("/new-data")
    @Operation(summary = "获得机房能耗最近一天、一周、一月插入的数据量")
    public CommonResult<Map<String, Object>> getNewData() throws IOException {
        Map<String, Object> map = roomEnergyConsumptionService.getNewData();
        return success(map);
    }

    @GetMapping("/one-day")
    @Operation(summary = "获得机房能耗最近一天插入的数据量")
    public CommonResult<Map<String, Object>> getOneDaySumData() throws IOException {
        Map<String, Object> map = roomEnergyConsumptionService.getOneDaySumData();
        return success(map);
    }

    @GetMapping("/bill-details")
    @Operation(summary = "获取分段电能电费")
    public CommonResult<PageResult<Object>> getSubBillDetails(RoomEnergyConsumptionPageReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = roomEnergyConsumptionService.getSubBillDetails(reqVO);
        return success(pageResult);
    }

    @GetMapping("/exportRoomEle")
    @Operation(summary = "导出机房一天电量数据 Excel")
    public void  exportRoomEle(HttpServletResponse response) throws IOException {
        List<RoomEleExportVO> list =  roomEnergyConsumptionService.exportRoomEle();
        String name = System.currentTimeMillis() + ".xlsx";
        String path = "F:\\Temp\\"+name;
        ExcelUtils.writePath(path, "数据", RoomEleExportVO.class,
                list);
        ExcelUtils.write(response, "机房一天电量数据.xlsx", "数据", RoomEleExportVO.class,
                BeanUtils.toBean(list, RoomEleExportVO.class));
        FileManage  fileManage = new FileManage();
        fileManage.setFileName(name);
        fileManage.setFileUrl(path);
        fileManage.setSysType(1);
        roomEnergyConsumptionService.saveFileManage(fileManage);

    }

}
