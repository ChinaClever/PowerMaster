package cn.iocoder.yudao.module.pdu.controller.admin.energyconsumption;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.pdu.controller.admin.energyconsumption.VO.*;
import cn.iocoder.yudao.module.pdu.service.energyconsumption.EnergyConsumptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - pdu能耗数据")
@RestController
@RequestMapping("/pdu/eq-data")
@Validated
public class EnergyConsumptionController {
    @Resource
    private EnergyConsumptionService energyConsumptionService;

    @GetMapping("/page")
    @Operation(summary = "获得pdu电量数据分页")
    public CommonResult<PageResult<Object>> getEQDataPage(EnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = energyConsumptionService.getEQDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出pdu能耗趋势数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportEQDataExcel(EnergyConsumptionPageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list1 = energyConsumptionService.getEQDataPage(pageReqVO).getList();
        //处理list
        List<Object>list=energyConsumptionService.getNewList(list1);
        // 导出 Excel
        if(((Map)list.get(0)).containsKey("outlet_id")){
            ExcelUtils.write(response, "pdu能耗趋势数据.xlsx", "数据", EQOutletPageRespVO.class,
                    BeanUtils.toBean(list, EQOutletPageRespVO.class));
        }
        else{
            ExcelUtils.write(response, "pdu能耗趋势数据.xlsx", "数据", EQPageRespVO.class,
                    BeanUtils.toBean(list, EQPageRespVO.class));
        }

    }

    @GetMapping("/bill-page")
    @Operation(summary = "获得pdu电费数据分页")
    public CommonResult<PageResult<Object>> getBillDataPage(EnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = energyConsumptionService.getBillDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/bill-export-excel")
    @Operation(summary = "导出pdu电费统计数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportBillDataExcel(EnergyConsumptionPageReqVO pageReqVO,
                                  HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list1 = energyConsumptionService.getBillDataPage(pageReqVO).getList();
        List<Object> list=energyConsumptionService.getNewBillList(list1);

        // 导出 Excel
        if(pageReqVO.getGranularity().equals("day")){
            if(((Map)list.get(0)).containsKey("outlet_id")){
                ExcelUtils.write(response, "pdu能耗趋势数据.xlsx", "数据", BillOutletDayPageRespVO.class,
                        BeanUtils.toBean(list, BillOutletDayPageRespVO.class));
            }
            else{
                ExcelUtils.write(response, "pdu电费统计数据.xlsx", "数据", BillDayPageRespVO.class,
                        BeanUtils.toBean(list, BillDayPageRespVO.class));
            }
        }
        else{
            if(((Map)list.get(0)).containsKey("outlet_id")){
                ExcelUtils.write(response, "pdu能耗趋势数据.xlsx", "数据", BillOutletWeekPageRespVO.class,
                        BeanUtils.toBean(list, BillOutletWeekPageRespVO.class));
            }
            else{
                ExcelUtils.write(response, "pdu电费统计数据.xlsx", "数据", BillWeekPageRespVO.class,
                        BeanUtils.toBean(list, BillWeekPageRespVO.class));
            }
        }

    }

    @GetMapping("/details")
    @Operation(summary = "获得pdu电量数据详情")
    public CommonResult<PageResult<Object>> getEQDataDetails(EnergyConsumptionPageReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = energyConsumptionService.getEQDataDetails(reqVO);
        return success(pageResult);
    }

    @GetMapping("/outlets-details")
    @Operation(summary = "获得pdu电量数据详情")
    public CommonResult<List<Object>> getOutletsEQData(EnergyConsumptionPageReqVO reqVO) throws IOException {
        List<Object> list = energyConsumptionService.getOutletsEQData(reqVO);
        return success(list);
    }
    @GetMapping("/outlets-details-excel")
    @Operation(summary = "导出pdu电量数据详情")
    @OperateLog(type = EXPORT)
    public void exportOutletsDataExcel(EnergyConsumptionPageReqVO reqVO,
                                    HttpServletResponse response) throws IOException {
        reqVO.setPageSize(10000);
        List<Object> list = energyConsumptionService.getEQDataDetails(reqVO).getList();

        if(!list.isEmpty()){
            List<Object> list1 = energyConsumptionService.getNewOutLetsList(list);
            // 导出 Excel
            ExcelUtils.write(response, "PDU能耗趋势.xlsx", "数据", OutLetsPageRespVO.class,
                    BeanUtils.toBean(list1, OutLetsPageRespVO.class));
        }
        else{
            List<OutLetsPageRespVO>list2=new ArrayList<>();
            ExcelUtils.write(response, "PDU能耗趋势.xlsx", "数据", OutLetsPageRespVO.class,list2);
        }

    }

    @GetMapping("/realtime-page")
    @Operation(summary = "获得pdu电量实时数据分页")
    public CommonResult<PageResult<Object>> getRealtimeEQDataPage(EnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = energyConsumptionService.getRealtimeEQDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/realtime-export-excel")
    @Operation(summary = "导出pdu电能记录数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportRealtimeEQDataExcel(EnergyConsumptionPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list1 = energyConsumptionService.getRealtimeEQDataPage(pageReqVO).getList();
        List<Object>list=energyConsumptionService.getNewEQList(list1);
        // 导出 Excel
        if(list.stream()
                .anyMatch(item -> item instanceof Map && ((Map<?, ?>) item).containsKey("line_id"))){
            ExcelUtils.write(response, "pdu电能记录数据.xlsx", "数据", RealtimeEQLineIdPageRespVO.class,
                    BeanUtils.toBean(list, RealtimeEQLineIdPageRespVO.class));
        }
        else if(list.stream()
                .anyMatch(item -> item instanceof Map && ((Map<?, ?>) item).containsKey("loop_id"))){
            ExcelUtils.write(response, "pdu电能记录数据.xlsx", "数据", RealtimeEQLoopIdPageRespVO.class,
                    BeanUtils.toBean(list, RealtimeEQLoopIdPageRespVO.class));
        }
        else if(list.stream()
                .anyMatch(item -> item instanceof Map && ((Map<?, ?>) item).containsKey("outlet_id"))){
            ExcelUtils.write(response, "pdu电能记录数据.xlsx", "数据", RealtimeEQOutletIdPageRespVO.class,
                    BeanUtils.toBean(list, RealtimeEQOutletIdPageRespVO.class));
        }
        else{
            ExcelUtils.write(response, "pdu电能记录数据.xlsx", "数据", RealtimeEQPageRespVO.class,
                    BeanUtils.toBean(list, RealtimeEQPageRespVO.class));
        }
    }

    @GetMapping("/new-data")
    @Operation(summary = "获得pdu能耗最近一天、一周、一月插入的数据量")
    public CommonResult<Map<String, Object>> getNewData() throws IOException {
        Map<String, Object> map = energyConsumptionService.getNewData();
        return success(map);
    }

    @GetMapping("/one-day")
    @Operation(summary = "获得pdu能耗最近一天插入的数据量")
    public CommonResult<Map<String, Object>> getOneDaySumData() throws IOException {
        Map<String, Object> map = energyConsumptionService.getOneDaySumData();
        return success(map);
    }

    @GetMapping("/bill-details")
    @Operation(summary = "获取分段电能电费")
    public CommonResult<PageResult<Object>> getSubBillDetails(EnergyConsumptionPageReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = energyConsumptionService.getSubBillDetails(reqVO);
        return success(pageResult);
    }

    @PostMapping("ele_total_realtime")
    @Operation(summary = "获取实时电量")
    public CommonResult<PageResult<EleTotalRealtimeRespVO>> getEleTotalRealtime( EleTotalRealtimeReqDTO reqDTO) throws IOException {
        PageResult<EleTotalRealtimeRespVO> list = energyConsumptionService.getEleTotalRealtime(reqDTO,true);
        return success(list);
    }

    @GetMapping("ele_total_realtimeExcel")
    @Operation(summary = "获取实时电量导出")
    public void getEleTotalRealtimeExcel( EleTotalRealtimeReqDTO reqDTO, HttpServletResponse response) throws IOException {
        PageResult<EleTotalRealtimeRespVO> list = energyConsumptionService.getEleTotalRealtime(reqDTO,false);
        ExcelUtils.write(response, "pdu实时电能记录数据.xlsx", "数据", EleTotalRealtimeRespVO.class,
                BeanUtils.toBean(list.getList(), EleTotalRealtimeRespVO.class));

    }
}
