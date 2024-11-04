package cn.iocoder.yudao.module.pdu.controller.admin.historydata;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.pdu.controller.admin.energyconsumption.VO.OutLetsPageRespVO;
import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.io.IOException;
import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

import cn.iocoder.yudao.module.pdu.service.historydata.HistoryDataService;

@Tag(name = "管理后台 - pdu历史数据")
@RestController
@RequestMapping("/pdu/history-data")
@Validated
public class HistoryDataController {

    @Resource
    private HistoryDataService historyDataService;

    @GetMapping("/type-max-value")
    @Operation(summary = "获得pdu数据参数类型的最大值")
    public CommonResult<Map> getHistoryDataTypeMaxValue() throws IOException {
        Map resultList = historyDataService.getHistoryDataTypeMaxValue();
        return success(resultList);
    }

    @GetMapping("/sensorId-max-value")
    @Operation(summary = "获得pdu环境数据传感器数量的最大值")
    public CommonResult<Map> getSensorIdMaxValue() throws IOException {
        Map resultList = historyDataService.getSensorIdMaxValue();
        return success(resultList);
    }

    @GetMapping("/page")
    @Operation(summary = "获得pdu历史数据分页")
    public CommonResult<PageResult<Object>> getHistoryDataPage(HistoryDataPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = historyDataService.getHistoryDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/details")
    @Operation(summary = "获得pdu历史数据详情")
    public CommonResult<PageResult<Object>> getHistoryDataDetails(HistoryDataDetailsReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = historyDataService.getHistoryDataDetails(reqVO);
        return success(pageResult);
    }
    //导出pdu历史数据详情
    @GetMapping("/details-export-excel")
    @Operation(summary = "导出pdu历史数据详情 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:env-history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportDetailsDataExcel(HistoryDataDetailsReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = historyDataService.getHistoryDataDetails(pageReqVO).getList();

        if(!list.isEmpty()){
            if(list.stream()
                    .anyMatch(item -> item instanceof Map && ((Map<?, ?>) item).containsKey("vol_avg_value"))) {
                List<Object> list1 = historyDataService.getNewExcelList(list, "3");
                // 导出 Excel
                ExcelUtils.write(response, "pdu历史数据详情.xlsx", "数据", HistoryDataDetailsLineExportDetailsVO.class,
                        BeanUtils.toBean(list1, HistoryDataDetailsLineExportDetailsVO.class));
            }
            else if(list.stream()
                    .anyMatch(item -> item instanceof Map && ((Map<?, ?>) item).containsKey("cur_avg_value"))) {
                List<Object> list1 = historyDataService.getNewExcelList(list, "4");
                // 导出 Excel
                ExcelUtils.write(response, "pdu历史数据详情.xlsx", "数据", HistoryDataDetailsOutletExportDetailsVO.class,
                        BeanUtils.toBean(list1, HistoryDataDetailsOutletExportDetailsVO.class));
            }
            else if (list.stream()
                    .anyMatch(item -> item instanceof Map && ((Map<?, ?>) item).containsKey("pow_apparent_avg_value"))) {
                List<Object>list1 = historyDataService.getNewExcelList(list,"2");
                // 导出 Excel
                ExcelUtils.write(response, "pdu历史数据详情.xlsx", "数据", HistoryDataDetailsExportDetailsVO.class,
                        BeanUtils.toBean(list1, HistoryDataDetailsExportDetailsVO.class));
            }
            else if(list.stream()
                    .anyMatch(item -> item instanceof Map && ((Map<?, ?>) item).containsKey("vol_value"))){
                List<Object>list1 = historyDataService.getNewExcelList(list,"1");
                // 导出 Excel
                ExcelUtils.write(response, "pdu历史数据详情.xlsx", "数据", HistoryDataDetailsLineExportVO.class,
                        BeanUtils.toBean(list1, HistoryDataDetailsLineExportVO.class));
            }
            else if(list.stream()
                    .anyMatch(item -> item instanceof Map && ((Map<?, ?>) item).containsKey("outlet_id"))){
                List<Object>list1 = historyDataService.getNewExcelList(list,"1");
                // 导出 Excel
                ExcelUtils.write(response, "pdu历史数据详情.xlsx", "数据", HistoryDataDetailsOutletExportVO.class,
                        BeanUtils.toBean(list1, HistoryDataDetailsOutletExportVO.class));
            }

            else {
                List<Object>list1 = historyDataService.getNewExcelList(list,"1");
                // 导出 Excel
                ExcelUtils.write(response, "pdu历史数据详情.xlsx", "数据", HistoryDataDetailsExportVO.class,
                        BeanUtils.toBean(list1, HistoryDataDetailsExportVO.class));
            }
        }
        else{
            List<HistoryDataDetailsExportVO>list2=new ArrayList<>();
            ExcelUtils.write(response, "pdu历史数据详情.xlsx", "数据", HistoryDataDetailsExportVO.class,list2);
        }



    }
    @GetMapping("/env-page")
    @Operation(summary = "获得pdu环境数据分页")
    public CommonResult<PageResult<Object>> getEnvDataPage(EnvDataPageReqVo pageReqVO) throws IOException {
        PageResult<Object> pageResult = historyDataService.getEnvDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/env-details")
    @Operation(summary = "获得pdu环境历史数据详情")
    public CommonResult<Map<String, Object>> getEnvDataDetails(EnvDataDetailsReqVO reqVO) throws IOException {
        Map<String, Object> pageResult = historyDataService.getEnvDataDetails(reqVO);
        return success(pageResult);
    }

    @GetMapping("/new-data/{granularity}")
    @Operation(summary = "获得pdu电力分析导航显示的插入的数据量")
    public CommonResult<Map<String, Object>> getNavNewData(@PathVariable("granularity") String granularity) throws IOException {
        Map<String, Object> map = historyDataService.getNavNewData(granularity);
        return success(map);
    }

    @GetMapping("/env-new-data")
    @Operation(summary = "查询pdu环境数据导航的新增多少条记录数据")
    public CommonResult<Map<String, Object>> getEnvNavNewData() throws IOException {
        Map<String, Object> map = historyDataService.getEnvNavNewData();
        return success(map);
    }


    @GetMapping("/export-excel")
    @Operation(summary = "导出pdu电力历史数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportHistoryDataExcel(HistoryDataPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = historyDataService.getHistoryDataPage(pageReqVO).getList();
        historyDataService.getNewHistoryDataDetails(list);

        // 导出 Excel
        if(pageReqVO.getType().equals("line")){
            if(pageReqVO.getGranularity().equals("realtime")){
                ExcelUtils.write(response, "pdu电力历史数据.xlsx", "数据", RealtimeLinePageRespVO.class,
                        BeanUtils.toBean(list, RealtimeLinePageRespVO.class));
            }
            else{
                ExcelUtils.write(response, "pdu电力历史数据.xlsx", "数据", HourAndDayLinePageRespVO.class,
                        BeanUtils.toBean(list, HourAndDayLinePageRespVO.class));
            }
        }
        else if(pageReqVO.getType().equals("loop")){
            if(pageReqVO.getGranularity().equals("realtime")){
                ExcelUtils.write(response, "pdu电力历史数据.xlsx", "数据", RealtimeLoopPageRespVO.class,
                        BeanUtils.toBean(list, RealtimeLoopPageRespVO.class));
            }
            else{
                ExcelUtils.write(response, "pdu电力历史数据.xlsx", "数据", HourAndDayLoopPageRespVO.class,
                        BeanUtils.toBean(list, HourAndDayLoopPageRespVO.class));
            }
        }
        else if(pageReqVO.getType().equals("outlet")){
            if(pageReqVO.getGranularity().equals("realtime")){
                ExcelUtils.write(response, "pdu电力历史数据.xlsx", "数据", RealtimeOutletPageRespVO.class,
                        BeanUtils.toBean(list, RealtimeOutletPageRespVO.class));
            }
            else{
                ExcelUtils.write(response, "pdu电力历史数据.xlsx", "数据", HourAndDayOutletPageRespVO.class,
                        BeanUtils.toBean(list, HourAndDayOutletPageRespVO.class));
            }
        }
        else{
            if(pageReqVO.getGranularity().equals("realtime")){
                ExcelUtils.write(response, "pdu电力历史数据.xlsx", "数据", RealtimePageRespVO.class,
                        BeanUtils.toBean(list, RealtimePageRespVO.class));
            }
            else{
                ExcelUtils.write(response, "pdu电力历史数据.xlsx", "数据", HourAndDayPageRespVO.class,
                        BeanUtils.toBean(list, HourAndDayPageRespVO.class));
            }
        }


    }

    @GetMapping("/env-export-excel")
    @Operation(summary = "导出pdu环境历史数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:env-history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportEnvHistoryDataExcel(EnvDataPageReqVo pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = historyDataService.getEnvDataPage(pageReqVO).getList();
        //对list进行处理
        historyDataService.getEnExcelList(list);
        // 导出 Excel
        if (Objects.equals(pageReqVO.getGranularity(), "realtime")) {
            ExcelUtils.write(response, "pdu环境历史数据.xlsx", "数据", EnvRealtimePageRespVO.class,
                    BeanUtils.toBean(list, EnvRealtimePageRespVO.class));
        } else {
            ExcelUtils.write(response, "pdu环境历史数据.xlsx", "数据", EnvHourAndDayPageRespVO.class,
                    BeanUtils.toBean(list, EnvHourAndDayPageRespVO.class));
        }
    }

    @GetMapping("/export-Env-excel")
    @Operation(summary = "导出pdu环境分析历史数据详情 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:env-history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportEnvDataExcel(EnvDataDetailsReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        Map<String, Object> pageResult = historyDataService.getEnvDataDetails(pageReqVO);
        List<Object> list = new ArrayList<>();
        Object object = pageResult.get("list");
        if(object instanceof List){
            List<?> collection = (List<?>) object;
            // 将集合中的元素添加到新创建的List中
            list.addAll(collection);
        }
        else {
        System.out.println("The value for key 'list' is not a List.");
    }


        //导出 Excel
        if(!list.isEmpty()){
            //对list进行处理
            historyDataService.getEnvExcelList(list);
            // 导出 Excel
            if(list.stream()
                    .anyMatch(item -> item instanceof Map && ((Map<?, ?>) item).containsKey("tem_value"))){
                ExcelUtils.write(response, "pdu历史数据详情.xlsx", "数据", HistoryEnvDataExportVO.class,
                        BeanUtils.toBean(list, HistoryEnvDataExportVO.class));
            } else if (list.stream()
                    .anyMatch(item -> item instanceof Map && ((Map<?, ?>) item).containsKey("tem_avg_value"))) {
                ExcelUtils.write(response, "pdu历史数据详情.xlsx", "数据", HistoryEnvDetailsDataExportVO.class,
                        BeanUtils.toBean(list, HistoryEnvDetailsDataExportVO.class));
            }
        }
        else{
            List<HistoryEnvDataExportVO>list1=new ArrayList<>();
            ExcelUtils.write(response, "pdu历史数据详情.xlsx", "数据", HistoryEnvDataExportVO.class,list1);
        }


    }

}