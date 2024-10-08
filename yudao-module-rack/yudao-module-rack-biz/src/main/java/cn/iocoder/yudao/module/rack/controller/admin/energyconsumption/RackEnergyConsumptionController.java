package cn.iocoder.yudao.module.rack.controller.admin.energyconsumption;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.rack.controller.admin.energyconsumption.VO.*;
import cn.iocoder.yudao.module.rack.service.energyconsumption.RackEnergyConsumptionService;
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

@Tag(name = "管理后台 - 机架能耗数据")
@RestController
@RequestMapping("/rack/eq-data")
@Validated
public class RackEnergyConsumptionController {
    @Resource
    private RackEnergyConsumptionService rackEnergyConsumptionService;

    @GetMapping("/page")
    @Operation(summary = "获得机架电量数据分页")
    public CommonResult<PageResult<Object>> getEQDataPage(RackEnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = rackEnergyConsumptionService.getEQDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出机架能耗趋势数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportEQDataExcel(RackEnergyConsumptionPageReqVO pageReqVO,
                                  HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = rackEnergyConsumptionService.getEQDataPage(pageReqVO).getList();
        rackEnergyConsumptionService.getNewEqList(list);
        // 导出 Excel
        ExcelUtils.write(response, "机架能耗趋势数据.xlsx", "数据", EQPageRespVO.class,
                BeanUtils.toBean(list, EQPageRespVO.class));
    }

    @GetMapping("/bill-page")
    @Operation(summary = "获得机架电费数据分页")
    public CommonResult<PageResult<Object>> getBillDataPage(RackEnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = rackEnergyConsumptionService.getBillDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/bill-export-excel")
    @Operation(summary = "导出机架电费统计数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportBillDataExcel(RackEnergyConsumptionPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = rackEnergyConsumptionService.getBillDataPage(pageReqVO).getList();
        rackEnergyConsumptionService.getNewBillList(list);
        // 导出 Excel
        ExcelUtils.write(response, "机架电费统计数据.xlsx", "数据", BillPageRespVO.class,
                BeanUtils.toBean(list, BillPageRespVO.class));
    }

    @GetMapping("/details")
    @Operation(summary = "获得机架电量数据详情")
    public CommonResult<PageResult<Object>> getEQDataDetails(RackEnergyConsumptionPageReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = rackEnergyConsumptionService.getEQDataDetails(reqVO);
        return success(pageResult);
    }

    @GetMapping("/realtime-page")
    @Operation(summary = "获得机架实时电量数据分页")
    public CommonResult<PageResult<Object>> getRealtimeEQDataPage(RackEnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = rackEnergyConsumptionService.getRealtimeEQDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/realtime-export-excel")
    @Operation(summary = "导出机架电能记录数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportRealtimeEQDataExcel(RackEnergyConsumptionPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = rackEnergyConsumptionService.getRealtimeEQDataPage(pageReqVO).getList();
        rackEnergyConsumptionService.getNewRealTimeList(list);
        // 导出 Excel
        ExcelUtils.write(response, "机架电能记录数据.xlsx", "数据", RealtimeEQPageRespVO.class,
                BeanUtils.toBean(list, RealtimeEQPageRespVO.class));
    }

    @GetMapping("/new-data")
    @Operation(summary = "获得机架能耗最近一天、一周、一月插入的数据量")
    public CommonResult<Map<String, Object>> getNewData() throws IOException {
        Map<String, Object> map = rackEnergyConsumptionService.getNewData();
        return success(map);
    }

    @GetMapping("/one-day")
    @Operation(summary = "获得机架能耗最近一天插入的数据量")
    public CommonResult<Map<String, Object>> getOneDaySumData() throws IOException {
        Map<String, Object> map = rackEnergyConsumptionService.getOneDaySumData();
        return success(map);
    }

    @GetMapping("/bill-details")
    @Operation(summary = "获取分段电能电费")
    public CommonResult<PageResult<Object>> getSubBillDetails(RackEnergyConsumptionPageReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = rackEnergyConsumptionService.getSubBillDetails(reqVO);
        return success(pageResult);
    }


    @GetMapping("/outlets-export-excel")
    @Operation(summary = "导出机架能耗排名数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportOutletsEQDataExcel(RackEnergyConsumptionPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = rackEnergyConsumptionService.getEQDataDetails(pageReqVO).getList();
        rackEnergyConsumptionService.getNewOutletsList(list);
        // 导出 Excel
        ExcelUtils.write(response, "机架能耗排名数据.xlsx", "数据", RackEnergyExportPageVO.class,
                BeanUtils.toBean(list, RackEnergyExportPageVO.class));
    }
}
