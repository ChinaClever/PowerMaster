package cn.iocoder.yudao.module.bus.controller.admin.energyconsumption;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.bus.controller.admin.energyconsumption.VO.*;
import cn.iocoder.yudao.module.bus.dto.BusEleTotalRealtimeReqDTO;
import cn.iocoder.yudao.module.bus.service.busenergyconsumption.BusEnergyConsumptionService;
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

@Tag(name = "管理后台 - 始端箱能耗数据")
@RestController
@RequestMapping("/bus/eq-data")
@Validated
public class BusEnergyConsumptionController {
    @Resource
    private BusEnergyConsumptionService busEnergyConsumptionService;

    @PostMapping("/bus/page")
    @Operation(summary = "获得始端箱能耗数据分页")
    public CommonResult<PageResult<Object>> getEQDataPage(@RequestBody EnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = busEnergyConsumptionService.getEQDataPage(pageReqVO);
        return success(pageResult);
    }

    @PostMapping("/bus/export-excel")
    @Operation(summary = "导出始端箱能耗趋势数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportEQDataExcel(@RequestBody EnergyConsumptionPageReqVO pageReqVO,
                                  HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = busEnergyConsumptionService.getEQDataPage(pageReqVO).getList();

        //对list进行处理
        busEnergyConsumptionService.getNewlList(list);
        // 导出 Excel
        ExcelUtils.write(response, "始端箱能耗趋势数据.xlsx", "数据", EQPageRespVO.class,
                BeanUtils.toBean(list, EQPageRespVO.class));
    }




    @PostMapping("/bus/bill-page")
    @Operation(summary = "获得始端箱电费数据分页")
    public CommonResult<PageResult<Object>> getBillDataPage(@RequestBody EnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = busEnergyConsumptionService.getBillDataPage(pageReqVO);
        return success(pageResult);
    }

    @PostMapping("/bus/bill-export-excel")
    @Operation(summary = "导出始端箱电费统计数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportBillDataExcel(@RequestBody EnergyConsumptionPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = busEnergyConsumptionService.getBillDataPage(pageReqVO).getList();

        if(!list.isEmpty()){
            //对list进行处理
            busEnergyConsumptionService.getNewBillList(list);
            // 导出 Excel
            ExcelUtils.write(response, "始端箱电费统计数据.xlsx", "数据", BillPageRespVO.class,
                    BeanUtils.toBean(list, BillPageRespVO.class));
        }
        else{
            List<BillPageRespVO>list1=new ArrayList<>();
            ExcelUtils.write(response, "始端箱电费统计数据.xlsx", "数据", BillPageRespVO.class,list1);
        }


    }

    @GetMapping("/bus/details")
    @Operation(summary = "获得始端箱电量数据详情")
    public CommonResult<PageResult<Object>> getEQDataDetails(EnergyConsumptionPageReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = busEnergyConsumptionService.getEQDataDetails(reqVO);
        return success(pageResult);
    }

    @PostMapping("/bus/details-export-excel")
    @Operation(summary = "导出始端箱能耗排名历史数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportBuxDetailsDataExcel(@RequestBody EnergyConsumptionPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = busEnergyConsumptionService.getEQDataDetails(pageReqVO).getList();
        if(!list.isEmpty()){
            //对list进行处理
//            busEnergyConsumptionService.getNewDetailList(list);
            // 导出 Excel
            List<DetailsPageRespVO> bean = BeanUtils.toBean(list, DetailsPageRespVO.class);
            bean.stream().forEach(item -> item.setLocation(pageReqVO.getNowAddress()));
            ExcelUtils.write(response, "始端箱能耗排名历史数据.xlsx", "数据", DetailsPageRespVO.class,
                    bean);
        }
        else{
            List<DetailsPageRespVO>list1=new ArrayList<>();
            ExcelUtils.write(response, "始端箱能耗排名历史数据.xlsx", "数据", DetailsPageRespVO.class,list1);
        }
    }

    @PostMapping("/bus/realtime-page")
    @Operation(summary = "获得始端箱实时电量数据分页")
    public CommonResult<PageResult<Object>> getRealtimeEQDataPage(@RequestBody EnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = busEnergyConsumptionService.getRealtimeEQDataPage(pageReqVO);
        return success(pageResult);
    }

    @PostMapping("/bus/realtime-export-excel")
    @Operation(summary = "导出始端箱电能记录数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportRealtimeEQDataExcel(@RequestBody EnergyConsumptionPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = busEnergyConsumptionService.getRealtimeEQDataPage(pageReqVO).getList();

        if(!list.isEmpty()){
            //对list进行处理
            busEnergyConsumptionService.getNewRealtimeList(list);
            // 导出 Excel
            ExcelUtils.write(response, "始端箱电能记录数据.xlsx", "数据", RealtimeEQPageRespVO.class,
                    BeanUtils.toBean(list, RealtimeEQPageRespVO.class));
        }
        else{
            List<RealtimeEQPageRespVO>list1=new ArrayList<>();
            ExcelUtils.write(response, "插接箱电能记录数据.xlsx", "数据", RealtimeEQPageRespVO.class,list1);
        }


    }

    @GetMapping("/bus/new-data")
    @Operation(summary = "获得始端箱能耗最近一天、一周、一月插入的数据量")
    public CommonResult<Map<String, Object>> getNewData() throws IOException {
        Map<String, Object> map = busEnergyConsumptionService.getNewData();
        return success(map);
    }

    @GetMapping("/bus/one-day/{timeRangeType}")
    @Operation(summary = "获得始端箱能耗最近一天插入的数据量")
    public CommonResult<Map<String, Object>> getOneDaySumData(@PathVariable("timeRangeType")String timeRangeType) throws IOException {
        Map<String, Object> map = busEnergyConsumptionService.getOneDaySumData(timeRangeType);
        return success(map);
    }

    @PostMapping("/bus/bill-details")
    @Operation(summary = "获取始端箱分段电能电费")
    public CommonResult<PageResult<Object>> getBusSubBillDetails(@RequestBody EnergyConsumptionPageReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = busEnergyConsumptionService.getBusSubBillDetails(reqVO);
        return success(pageResult);
    }


    // 插接箱
    @PostMapping("/box/page")
    @Operation(summary = "获得插接箱能耗数据分页")
    public CommonResult<PageResult<Object>> getBoxEQDataPage(@RequestBody EnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = busEnergyConsumptionService.getBoxEQDataPage(pageReqVO);
        return success(pageResult);
    }

    @PostMapping("/box/export-excel")
    @Operation(summary = "导出插接箱能耗趋势数据 Excel")
    @OperateLog(type = EXPORT)
    public void exportBoxEQDataExcel(@RequestBody EnergyConsumptionPageReqVO pageReqVO,
                                  HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = busEnergyConsumptionService.getBoxEQDataPage(pageReqVO).getList();
        busEnergyConsumptionService.getNewList(list);
        // 导出 Excel
        ExcelUtils.write(response, "插接箱能耗趋势数据.xlsx", "数据", EQPageRespVO.class,
                BeanUtils.toBean(list, EQPageRespVO.class));
    }


    @PostMapping("/box/bill-page")
    @Operation(summary = "获得插接箱电费数据分页")
    public CommonResult<PageResult<Object>> getBoxBillDataPage(@RequestBody EnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = busEnergyConsumptionService.getBoxBillDataPage(pageReqVO);
        return success(pageResult);
    }




    @GetMapping("/box/bill-export-excel")
    @Operation(summary = "导出插接箱电费统计数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportBoxBillDataExcel(EnergyConsumptionPageReqVO reqVO,
                                    HttpServletResponse response) throws IOException {
        reqVO.setPageSize(10000);
        List<Object> list = busEnergyConsumptionService.getBoxBillDataPage(reqVO).getList();

        if(!list.isEmpty()){
            //对list进行处理
            busEnergyConsumptionService.getNewDetailList(list);
            // 导出 Excel
            ExcelUtils.write(response, "插接箱电费统计数据.xlsx", "数据", BillPageRespVO.class,
                    BeanUtils.toBean(list, BillPageRespVO.class));
        }
        else{
            List<BillPageRespVO>list1=new ArrayList<>();
            ExcelUtils.write(response, "插接箱电费统计数据.xlsx", "数据", BillPageRespVO.class,list1);
        }

    }

    @GetMapping("/box/details")
    @Operation(summary = "获得插接箱电量数据详情")
    public CommonResult<PageResult<Object>> getBoxEQDataDetails(EnergyConsumptionPageReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = busEnergyConsumptionService.getBoxEQDataDetails(reqVO);
        return success(pageResult);
    }


    @GetMapping("/box/details-export-excel")
    @Operation(summary = "导出插接箱能耗排名历史数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportBoxDetailsDataExcel(EnergyConsumptionPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = busEnergyConsumptionService .getBoxEQDataDetails(pageReqVO).getList();
        if(!list.isEmpty()){
            //对list进行处理
            busEnergyConsumptionService.getNewDetailList(list);
            // 导出 Excel
            ExcelUtils.write(response, "插接箱能耗排名历史数据.xlsx", "数据", DetailsPageRespVO.class,
                    BeanUtils.toBean(list, DetailsPageRespVO.class));
        }
        else{
            List<DetailsPageRespVO>list1=new ArrayList<>();
            ExcelUtils.write(response, "插接箱能耗排名历史数据.xlsx", "数据", DetailsPageRespVO.class,list1);
        }
        }



    @PostMapping("/box/realtime-page")
    @Operation(summary = "获得插接箱实时电量数据分页")
    public CommonResult<PageResult<Object>> getBoxRealtimeEQDataPage(@RequestBody EnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = busEnergyConsumptionService.getBoxRealtimeEQDataPage(pageReqVO);
        return success(pageResult);
    }

    @PostMapping("/box/realtime-export-excel")
    @Operation(summary = "导出插接箱电能记录数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportBoxRealtimeEQDataExcel(@RequestBody EnergyConsumptionPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = busEnergyConsumptionService.getBoxRealtimeEQDataPage(pageReqVO).getList();
        if(!list.isEmpty()){
            //对list进行处理
            busEnergyConsumptionService.getNewRealtimeList(list);
            // 导出 Excel
            ExcelUtils.write(response, "插接箱电能记录数据.xlsx", "数据", RealtimeEQPageRespVO.class,
                    BeanUtils.toBean(list, RealtimeEQPageRespVO.class));
        }
        else{
            List<RealtimeEQPageRespVO>list1=new ArrayList<>();
            ExcelUtils.write(response, "插接箱电能记录数据.xlsx", "数据", RealtimeEQPageRespVO.class,list1);
        }

;
    }

    @GetMapping("/box/new-data")
    @Operation(summary = "获得插接箱能耗最近一天、一周、一月插入的数据量")
    public CommonResult<Map<String, Object>> getBoxNewData() throws IOException {
        Map<String, Object> map = busEnergyConsumptionService.getBoxNewData();
        return success(map);
    }

    @GetMapping("/box/one-day/{timeRangeType}")
    @Operation(summary = "获得插接箱能耗最近一天插入的数据量")
    public CommonResult<Map<String, Object>> getBoxOneDaySumData(@PathVariable("timeRangeType")String timeRangeType) throws IOException {
        Map<String, Object> map = busEnergyConsumptionService.getBoxOneDaySumData(timeRangeType);
        return success(map);
    }

    @PostMapping("/box/bill-details")
    @Operation(summary = "获取插接箱分段电能电费")
    public CommonResult<PageResult<Object>> getBoxSubBillDetails(@RequestBody EnergyConsumptionPageReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = busEnergyConsumptionService.getBoxSubBillDetails(reqVO);
        return success(pageResult);
    }

    @PostMapping("bus/eleTotalRealtime")
    @Operation(summary = "获取实时能耗")
    public CommonResult<PageResult<BusEleTotalRealtimeResVO>> getBusEleTotalRealtime(@RequestBody BusEleTotalRealtimeReqDTO reqDTO) throws IOException {
        PageResult<BusEleTotalRealtimeResVO> list = busEnergyConsumptionService.getBusEleTotalRealtime(reqDTO, true);
        return success(list);
    }

    @PostMapping("bus/eleTotalRealtimeExcel")
    @Operation(summary = "获取实时能耗")
    public void getBusEleTotalRealtimeExcel(@RequestBody BusEleTotalRealtimeReqDTO reqDTO, HttpServletResponse response) throws IOException {
        PageResult<BusEleTotalRealtimeResVO> list = busEnergyConsumptionService.getBusEleTotalRealtime(reqDTO, false);
        ExcelUtils.write(response, "始端箱实时电能记录数据.xlsx", "数据", BusEleTotalRealtimeResVO.class,
                BeanUtils.toBean(list.getList(), BusEleTotalRealtimeResVO.class));
    }



    @PostMapping("box/eleTotalRealtime")
    @Operation(summary = "box获取实时能耗")
    public CommonResult<PageResult<BusEleTotalRealtimeResVO>> getBoxEleTotalRealtime(@RequestBody BusEleTotalRealtimeReqDTO reqDTO) throws IOException {
        PageResult<BusEleTotalRealtimeResVO> list = busEnergyConsumptionService.getBoxEleTotalRealtime(reqDTO, true);
        return success(list);
    }

    @PostMapping("box/eleTotalRealtimeExcel")
    @Operation(summary = "box获取实时能耗")
    public void getBoxEleTotalRealtimeExcel(@RequestBody BusEleTotalRealtimeReqDTO reqDTO, HttpServletResponse response) throws IOException {
        PageResult<BusEleTotalRealtimeResVO> list = busEnergyConsumptionService.getBoxEleTotalRealtime(reqDTO, false);
        ExcelUtils.write(response, "插接箱实时电能记录数据.xlsx", "数据", BusEleTotalRealtimeResVO.class,
                BeanUtils.toBean(list.getList(), BusEleTotalRealtimeResVO.class));
    }

}
