package cn.iocoder.yudao.module.aisle.controller.admin.energyconsumption;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.aisle.controller.admin.energyconsumption.VO.*;
import cn.iocoder.yudao.module.aisle.service.energyconsumption.AisleEnergyConsumptionService;
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

@Tag(name = "管理后台 - 柜列能耗数据")
@RestController
@RequestMapping("/aisle/eq-data")
@Validated
public class AisleEnergyConsumptionController {
    @Resource
    private AisleEnergyConsumptionService aisleEnergyConsumptionService;

    @GetMapping("/page")
    @Operation(summary = "获得柜列电量数据分页")
    public CommonResult<PageResult<Object>> getEQDataPage(AisleEnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = aisleEnergyConsumptionService.getEQDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出柜列能耗趋势数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportEQDataExcel(AisleEnergyConsumptionPageReqVO pageReqVO,
                                  HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = aisleEnergyConsumptionService.getEQDataPage(pageReqVO).getList();
        aisleEnergyConsumptionService.getEqExcelList(list);
        // 导出 Excel
        ExcelUtils.write(response, "柜列能耗趋势数据.xlsx", "数据", EQPageRespVO.class,
                BeanUtils.toBean(list, EQPageRespVO.class));
    }




    @GetMapping("/bill-page")
    @Operation(summary = "获得柜列电费数据分页")
    public CommonResult<PageResult<Object>> getBillDataPage(AisleEnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = aisleEnergyConsumptionService.getBillDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/bill-export-excel")
    @Operation(summary = "导出柜列电费统计数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportBillDataExcel(AisleEnergyConsumptionPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = aisleEnergyConsumptionService.getBillDataPage(pageReqVO).getList();
        aisleEnergyConsumptionService.getBillExcelList(list);
        // 导出 Excel
        ExcelUtils.write(response, "柜列电费统计数据.xlsx", "数据", BillPageRespVO.class,
                BeanUtils.toBean(list, BillPageRespVO.class));
    }

    @GetMapping("/details")
    @Operation(summary = "获得柜列电量数据详情")
    public CommonResult<PageResult<Object>> getEQDataDetails(AisleEnergyConsumptionPageReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = aisleEnergyConsumptionService.getEQDataDetails(reqVO);
        return success(pageResult);
    }

    @GetMapping("/detail-export-excel")
    @Operation(summary = "导出柜列能耗分布数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportOutletsDataExcel(AisleEnergyConsumptionPageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = aisleEnergyConsumptionService.getEQDataDetails(pageReqVO).getList();
        aisleEnergyConsumptionService.getDetailsExcelList(list);
        // 导出 Excel
        ExcelUtils.write(response, "柜列能耗趋势数据.xlsx", "数据", DetailsExportPageRespVO.class,
                BeanUtils.toBean(list, DetailsExportPageRespVO.class));
    }
    @GetMapping("/realtime-page")
    @Operation(summary = "获得柜列实时电量数据分页")
    public CommonResult<PageResult<Object>> getRealtimeEQDataPage(AisleEnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = aisleEnergyConsumptionService.getRealtimeEQDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/realtime-export-excel")
    @Operation(summary = "导出柜列电能记录数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportRealtimeEQDataExcel(AisleEnergyConsumptionPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = aisleEnergyConsumptionService.getRealtimeEQDataPage(pageReqVO).getList();
        aisleEnergyConsumptionService.getRealtimeExcelList(list);
        // 导出 Excel
        ExcelUtils.write(response, "柜列电能记录数据.xlsx", "数据", RealtimeEQPageRespVO.class,
                BeanUtils.toBean(list, RealtimeEQPageRespVO.class));
    }

    @GetMapping("/new-data")
    @Operation(summary = "获得柜列能耗最近一天、一周、一月插入的数据量")
    public CommonResult<Map<String, Object>> getNewData() throws IOException {
        Map<String, Object> map = aisleEnergyConsumptionService.getNewData();
        return success(map);
    }

    @GetMapping("/one-day")
    @Operation(summary = "获得柜列能耗最近一天插入的数据量")
    public CommonResult<Map<String, Object>> getOneDaySumData() throws IOException {
        Map<String, Object> map = aisleEnergyConsumptionService.getOneDaySumData();
        return success(map);
    }

    @GetMapping("/bill-details")
    @Operation(summary = "获取分段电能电费")
    public CommonResult<PageResult<Object>> getSubBillDetails(AisleEnergyConsumptionPageReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = aisleEnergyConsumptionService.getSubBillDetails(reqVO);
        return success(pageResult);
    }

}
