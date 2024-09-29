package cn.iocoder.yudao.module.cabinet.controller.admin.energyconsumption;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.cabinet.controller.admin.energyconsumption.VO.*;
import cn.iocoder.yudao.module.cabinet.service.energyconsumption.CabinetEnergyConsumptionService;
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

@Tag(name = "管理后台 - 机柜能耗数据")
@RestController
@RequestMapping("/cabinet/eq-data")
@Validated
public class CabinetEnergyConsumptionController {
    @Resource
    private CabinetEnergyConsumptionService cabinetEnergyConsumptionService;

    @GetMapping("/page")
    @Operation(summary = "获得机柜电量数据分页")
    public CommonResult<PageResult<Object>> getEQDataPage(CabinetEnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = cabinetEnergyConsumptionService.getEQDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出机柜能耗趋势数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportEQDataExcel(CabinetEnergyConsumptionPageReqVO pageReqVO,
                                  HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = cabinetEnergyConsumptionService.getEQDataPage(pageReqVO).getList();
        cabinetEnergyConsumptionService.getNewEqList(list);
        // 导出 Excel
        ExcelUtils.write(response, "机柜能耗趋势数据.xlsx", "数据", EQPageRespVO.class,
                BeanUtils.toBean(list, EQPageRespVO.class));
    }


    @GetMapping("/bill-page")
    @Operation(summary = "获得机柜电费数据分页")
    public CommonResult<PageResult<Object>> getBillDataPage(CabinetEnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = cabinetEnergyConsumptionService.getBillDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/bill-export-excel")
    @Operation(summary = "导出机柜电费统计数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportBillDataExcel(CabinetEnergyConsumptionPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = cabinetEnergyConsumptionService.getBillDataPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "机柜电费统计数据.xlsx", "数据", BillPageRespVO.class,
                BeanUtils.toBean(list, BillPageRespVO.class));
    }

    @GetMapping("/details")
    @Operation(summary = "获得机柜电量数据详情")
    public CommonResult<PageResult<Object>> getEQDataDetails(CabinetEnergyConsumptionPageReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = cabinetEnergyConsumptionService.getEQDataDetails(reqVO);
        return success(pageResult);
    }

    @GetMapping("/details-export-excel")
    @Operation(summary = "导出机柜电费统计数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportDetailsDataExcel(CabinetEnergyConsumptionPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = cabinetEnergyConsumptionService.getEQDataDetails(pageReqVO).getList();
        cabinetEnergyConsumptionService.getNewDetailsList(list);
        // 导出 Excel
        ExcelUtils.write(response, "机柜电费统计数据.xlsx", "数据", DetailsPageRespVO.class,
                BeanUtils.toBean(list, DetailsPageRespVO.class));
    }

    @GetMapping("/realtime-page")
    @Operation(summary = "获得机柜实时电量数据分页")
    public CommonResult<PageResult<Object>> getRealtimeEQDataPage(CabinetEnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = cabinetEnergyConsumptionService.getRealtimeEQDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/realtime-export-excel")
    @Operation(summary = "导出机柜电能记录数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportRealtimeEQDataExcel(CabinetEnergyConsumptionPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = cabinetEnergyConsumptionService.getRealtimeEQDataPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "机柜电能记录数据.xlsx", "数据", RealtimeEQPageRespVO.class,
                BeanUtils.toBean(list, RealtimeEQPageRespVO.class));
    }

    @GetMapping("/new-data")
    @Operation(summary = "获得机柜能耗最近一天、一周、一月插入的数据量")
    public CommonResult<Map<String, Object>> getNewData() throws IOException {
        Map<String, Object> map = cabinetEnergyConsumptionService.getNewData();
        return success(map);
    }

    @GetMapping("/one-day")
    @Operation(summary = "获得机柜能耗最近一天插入的数据量")
    public CommonResult<Map<String, Object>> getOneDaySumData() throws IOException {
        Map<String, Object> map = cabinetEnergyConsumptionService.getOneDaySumData();
        return success(map);
    }

    @GetMapping("/bill-details")
    @Operation(summary = "获取分段电能电费")
    public CommonResult<PageResult<Object>> getSubBillDetails(CabinetEnergyConsumptionPageReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = cabinetEnergyConsumptionService.getSubBillDetails(reqVO);
        return success(pageResult);
    }


}
