package cn.iocoder.yudao.module.bus.controller.admin.historydata;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.bus.controller.admin.historydata.vo.*;
import cn.iocoder.yudao.module.bus.service.historydata.BusHistoryDataService;
import com.alibaba.excel.EasyExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.AUTH_THIRD_LOGIN_NOT_BIND;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.EXCEL_NULL;

@Tag(name = "管理后台 - 母线历史数据")
@RestController
@RequestMapping("/bus/history-data")
@Validated
public class BusHistoryDataController {

    @Resource
    private BusHistoryDataService busHistoryDataService;

    @GetMapping("/type-max-value")
    @Operation(summary = "获得母线数据参数类型的最大值")
    public CommonResult<Map> getHistoryDataTypeMaxValue(String[] boxIds) throws IOException {
        Map resultList = busHistoryDataService.getHistoryDataTypeMaxValue(boxIds);
        return success(resultList);
    }

    @GetMapping("/bus-page")
    @Operation(summary = "获得母线始端箱历史数据分页")
    public CommonResult<PageResult<Object>> getBusHistoryDataPage(BusHistoryDataPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = busHistoryDataService.getBusHistoryDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/box-page")
    @Operation(summary = "获得母线插接箱历史数据分页")
    public CommonResult<PageResult<Object>> getBoxHistoryDataPage(BusHistoryDataPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = busHistoryDataService.getBoxHistoryDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/bus-details")
    @Operation(summary = "获得母线始端箱历史数据详情")
    public CommonResult<PageResult<Object>> getBusHistoryDataDetails(BusHistoryDataDetailsReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = busHistoryDataService.getBusHistoryDataDetails(reqVO);
        return success(pageResult);
    }

    @GetMapping("/box-details")
    @Operation(summary = "获得母线插接箱历史数据详情")
    public CommonResult<PageResult<Object>> getBoxHistoryDataDetails(BusHistoryDataDetailsReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = busHistoryDataService.getBoxHistoryDataDetails(reqVO);
        return success(pageResult);
    }

    @GetMapping("/bus-new-data/{granularity}")
    @Operation(summary = "获得母线始端箱电力分析导航显示的插入的数据量")
    public CommonResult<Map<String, Object>> getBusNavNewData(@PathVariable("granularity") String granularity) throws IOException {
        Map<String, Object> map = busHistoryDataService.getBusNavNewData(granularity);
        return success(map);
    }

    @GetMapping("/box-new-data/{granularity}")
    @Operation(summary = "获得母线插接箱电力分析导航显示的插入的数据量")
    public CommonResult<Map<String, Object>> getBoxNavNewData(@PathVariable("granularity") String granularity) throws IOException {
        Map<String, Object> map = busHistoryDataService.getBoxNavNewData(granularity);
        return success(map);
    }

    @GetMapping("/box-export-excel")
    @Operation(summary = "导出母线插接箱历史数据 Excel")
//    @PreAuthorize("@ss.hasPermission('母线:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportBoxHistoryDataExcel(BusHistoryDataPageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);

        List<Object> list = busHistoryDataService.getBoxHistoryDataPage(pageReqVO).getList();
        List<String> columnsToExclude = pageReqVO.getColumnsToExclude();
        Set<String> columnsToExcludeSet = new HashSet<>(columnsToExclude);
        // 导出 Excel
        if (Objects.equals(pageReqVO.getGranularity(), "realtime")){
            //处理list
            busHistoryDataService.getNewBoxHistoryList(list);
            ExcelUtils.writeA(response, "母线插接箱历史数据.xlsx", "数据", BoxRealtimePageRespVO.class,
                    BeanUtils.toBean(list, BoxRealtimePageRespVO.class),null, columnsToExcludeSet);

        }else{
            busHistoryDataService.getNewBoxHistoryList1(list);
            ExcelUtils.writeA(response, "母线插接箱历史数据.xlsx", "数据", BoxHourAndDayPageRespVO.class,
                    BeanUtils.toBean(list, BoxHourAndDayPageRespVO.class),null, columnsToExcludeSet);
        }

    }

    @GetMapping("/bus-export-excel")
    @Operation(summary = "导出母线始端箱历史数据 Excel")
//    @PreAuthorize("@ss.hasPermission('母线:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportBusHistoryDataExcel(BusHistoryDataPageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {

        pageReqVO.setPageSize(10000);
        List<Object> list = busHistoryDataService.getBusHistoryDataPage(pageReqVO).getList();

        // 导出 Excel
        if (Objects.equals(pageReqVO.getGranularity(), "realtime")) {
            busHistoryDataService.getNewHistoryList(list);
            ExcelUtils.write(response, "母线始端箱历史数据.xlsx", "数据", BusRealtimePageRespVO.class,
                    BeanUtils.toBean(list, BusRealtimePageRespVO.class));
        } else {
            busHistoryDataService.getNewHistoryList1(list);
            ExcelUtils.write(response, "母线始端箱历史数据.xlsx", "数据", BusHourAndDayPageRespVO.class,
                    BeanUtils.toBean(list, BusHourAndDayPageRespVO.class));
        }
    }

    @GetMapping("/bus-env-page")
    @Operation(summary = "获得始端箱环境数据分页")
    public CommonResult<PageResult<Object>> getEnvDataPage(BusHistoryDataPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = busHistoryDataService.getBusEnvDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/bus-env-details")
    @Operation(summary = "获得始端箱环境历史数据详情")
    public CommonResult<PageResult<Object>> getEnvDataDetails(BusHistoryDataDetailsReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = busHistoryDataService.getBusEnvDataDetails(reqVO);
        return success(pageResult);
    }

    @GetMapping("/bus-env-new-data")
    @Operation(summary = "查询始端箱环境数据导航的新增多少条记录数据")
    public CommonResult<Map<String, Object>> getEnvNavNewData() throws IOException {
        Map<String, Object> map = busHistoryDataService.getBusEnvNavNewData();
        return success(map);
    }

    @GetMapping("/bus-env-export-excel")
    @Operation(summary = "导出母线始端箱环境历史数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:env-history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportEnvHistoryDataExcel(BusHistoryDataPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = busHistoryDataService.getBusEnvDataPage(pageReqVO).getList();
        busHistoryDataService.getNewEnvHistoryList(list);
        // 导出 Excel
        if (Objects.equals(pageReqVO.getGranularity(), "realtime")) {
            ExcelUtils.write(response, "始端箱环境历史数据.xlsx", "数据", BusEnvRealtimePageRespVO.class,
                    BeanUtils.toBean(list, BusEnvRealtimePageRespVO.class));
        } else {
            ExcelUtils.write(response, "始端箱环境历史数据.xlsx", "数据", BusEnvHourAndDayPageRespVO.class,
                    BeanUtils.toBean(list, BusEnvHourAndDayPageRespVO.class));
        }
    }

    @GetMapping("/box-env-page")
    @Operation(summary = "获得插接箱环境数据分页")
    public CommonResult<PageResult<Object>> getBoxEnvDataPage(BusHistoryDataPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = busHistoryDataService.getBoxEnvDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/box-env-details")
    @Operation(summary = "获得插接箱环境历史数据详情")
    public CommonResult<PageResult<Object>> getBoxEnvDataDetails(BusHistoryDataDetailsReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = busHistoryDataService.getBoxEnvDataDetails(reqVO);
        return success(pageResult);
    }

    @GetMapping("/box-env-new-data")
    @Operation(summary = "查询插接箱环境数据导航的新增多少条记录数据")
    public CommonResult<Map<String, Object>> getBoxEnvNavNewData() throws IOException {
        Map<String, Object> map = busHistoryDataService.getBoxEnvNavNewData();
        return success(map);
    }

    @GetMapping("/box-env-export-excel")
    @Operation(summary = "导出母线插接箱环境历史数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:env-history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportBoxEnvHistoryDataExcel(BusHistoryDataPageReqVO pageReqVO,
                                             HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = busHistoryDataService.getBoxEnvDataPage(pageReqVO).getList();
        busHistoryDataService.getNewEnvHistoryList(list);
        // 导出 Excel
        if (Objects.equals(pageReqVO.getGranularity(), "realtime")) {
            ExcelUtils.write(response, "插接箱环境历史数据.xlsx", "数据", BoxEnvRealtimePageRespVO.class,
                    BeanUtils.toBean(list, BoxEnvRealtimePageRespVO.class));
        } else {
            ExcelUtils.write(response, "插接箱环境历史数据.xlsx", "数据", BoxEnvHourAndDayPageRespVO.class,
                    BeanUtils.toBean(list, BoxEnvHourAndDayPageRespVO.class));
        }
    }

    @GetMapping("/bus-tem-export-excel")
    @Operation(summary = "导出母线始端箱温度分析环境历史数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:env-history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportBusTemHistoryDataExcel(BusHistoryDataDetailsReqVO pageReqVO,
                                             HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        PageResult<Object> busEnvDataDetails = busHistoryDataService.getBusEnvDataDetails(pageReqVO);
        if (Objects.isNull(busEnvDataDetails)){
            throw exception(EXCEL_NULL);
        }
        List<Object> list = busEnvDataDetails.getList();
        busHistoryDataService.getNewTemHistoryList(list);
        // 导出 Excel
        if (Objects.equals(pageReqVO.getGranularity(), "realtime")) {
            List<BusTemHistoryDataVO> bean = BeanUtils.toBean(list, BusTemHistoryDataVO.class);
            bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
            ExcelUtils.write(response, "始端箱温度分析.xlsx", "数据", BusTemHistoryDataVO.class,
                    bean);
        } else {
            List<BusHourAndDayTemHistoryDataVO> bean = BeanUtils.toBean(list, BusHourAndDayTemHistoryDataVO.class);
            bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
            ExcelUtils.write(response, "始端箱温度分析.xlsx", "数据", BusHourAndDayTemHistoryDataVO.class,
                    bean);
        }
    }

    @GetMapping("/box-tem-export-excel")
    @Operation(summary = "导出母线插接箱温度分析环境历史数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:env-history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportBoxTemHistoryDataExcel(BusHistoryDataDetailsReqVO pageReqVO,
                                             HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = busHistoryDataService.getBoxEnvDataDetails(pageReqVO).getList();

        busHistoryDataService.getNewTemHistoryList(list);
        // 导出 Excel
        if (Objects.equals(pageReqVO.getGranularity(), "realtime")) {
            List<BusTemHistoryDataVO> bean = BeanUtils.toBean(list, BusTemHistoryDataVO.class);
            bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
            ExcelUtils.write(response, "插接箱温度分析.xlsx", "数据", BusTemHistoryDataVO.class,
                    bean);
        } else {
            List<BusHourAndDayTemHistoryDataVO> bean = BeanUtils.toBean(list, BusHourAndDayTemHistoryDataVO.class);
            bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
            ExcelUtils.write(response, "插接箱温度分析.xlsx", "数据", BusHourAndDayTemHistoryDataVO.class,
                    bean);
        }
    }





    @GetMapping("/bus-export-detail-excel")
    @Operation(summary = "导出母线始端箱电力分析 Excel")
//    @PreAuthorize("@ss.hasPermission('母线:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportBusDetailHistoryDataExcel(BusHistoryDataDetailsReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {

        pageReqVO.setPageSize(10000);
        List<Object> list = busHistoryDataService.getBusHistoryDataDetails(pageReqVO).getList();

        busHistoryDataService.getNewDetailHistoryList(list);
       if(((Map)list.get(0)).containsKey("line_id")){
           // 导出 Excel
           if (Objects.equals(pageReqVO.getGranularity(), "realtime")) {
               List<BusExportLineDetailVO> bean = BeanUtils.toBean(list, BusExportLineDetailVO.class);
               bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
               ExcelUtils.write(response, "母线始端箱电力分析.xlsx", "数据", BusExportLineDetailVO.class,
                       bean);
           } else {
               List<BusHourAndDayLineExportDetailVO> bean = BeanUtils.toBean(list, BusHourAndDayLineExportDetailVO.class);
               bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
               ExcelUtils.write(response, "母线始端箱电力分析.xlsx", "数据", BusHourAndDayLineExportDetailVO.class,
                       bean);
           }
       }
       else{
           // 导出 Excel
           if (Objects.equals(pageReqVO.getGranularity(), "realtime")) {
               List<BusExportDetailVO> bean = BeanUtils.toBean(list, BusExportDetailVO.class);
               bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
               ExcelUtils.write(response, "母线始端箱电力分析.xlsx", "数据", BusExportDetailVO.class,
                       bean);
           } else {
               List<BusHourAndDayExportDetailVO> bean = BeanUtils.toBean(list, BusHourAndDayExportDetailVO.class);
               bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
               ExcelUtils.write(response, "母线始端箱电力分析.xlsx", "数据", BusHourAndDayExportDetailVO.class,
                       bean);
           }
       }
    }
    @GetMapping("/box-export-detail-excel")
    @Operation(summary = "导出母线插接箱电力分析 Excel")
//    @PreAuthorize("@ss.hasPermission('母线:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportBoxDetailHistoryDataExcel(BusHistoryDataDetailsReqVO pageReqVO,
                                                HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = busHistoryDataService.getBoxHistoryDataDetails(pageReqVO).getList();

        busHistoryDataService.getNewBoxDetailHistoryList(list);
        if(((Map)list.get(0)).containsKey("line_id")){
            // 导出 Excel
            if (Objects.equals(pageReqVO.getGranularity(), "realtime")) {
                List<BoxExportLineDetailVO> bean = BeanUtils.toBean(list, BoxExportLineDetailVO.class);
                bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
                ExcelUtils.write(response, "母线插接箱电力分析.xlsx", "数据", BoxExportLineDetailVO.class,
                        bean );
            } else {
                List<BoxHourAndDayLineExportDetailVO> bean = BeanUtils.toBean(list, BoxHourAndDayLineExportDetailVO.class);
                bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
                ExcelUtils.write(response, "母线插接箱电力分析.xlsx", "数据", BoxHourAndDayLineExportDetailVO.class,
                        bean);
            }
        }else if(((Map)list.get(0)).containsKey("loop_id")){
            // 导出 Excel
            if (Objects.equals(pageReqVO.getGranularity(), "realtime")) {
                List<BoxExportLoopDetailVO> bean = BeanUtils.toBean(list, BoxExportLoopDetailVO.class);
                bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
                ExcelUtils.write(response, "母线插接箱电力分析.xlsx", "数据", BoxExportLoopDetailVO.class,
                        bean);
            } else {
                List<BoxHourAndDayLoopExportDetailVO> bean = BeanUtils.toBean(list, BoxHourAndDayLoopExportDetailVO.class);
                bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
                ExcelUtils.write(response, "母线插接箱电力分析.xlsx", "数据", BoxHourAndDayLoopExportDetailVO.class,
                        bean);
            }
        } else if(((Map)list.get(0)).containsKey("outlet_id")){
            // 导出 Excel
            if (Objects.equals(pageReqVO.getGranularity(), "realtime")) {
                List<BoxExportOutletDetailVO> bean = BeanUtils.toBean(list, BoxExportOutletDetailVO.class);
                bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
                ExcelUtils.write(response, "母线插接箱电力分析.xlsx", "数据", BoxExportOutletDetailVO.class,
                        bean );
            } else {
                List<BoxHourAndDayOutletExportDetailVO> bean = BeanUtils.toBean(list, BoxHourAndDayOutletExportDetailVO.class);
                bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
                ExcelUtils.write(response, "母线插接箱电力分析.xlsx", "数据", BoxHourAndDayOutletExportDetailVO.class,
                        bean);
            }
        }
        else{
            // 导出 Excel
            if (Objects.equals(pageReqVO.getGranularity(), "realtime")) {
                List<BoxExportDetailVO> bean = BeanUtils.toBean(list, BoxExportDetailVO.class);
                bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
                ExcelUtils.write(response, "母线插接箱电力分析.xlsx", "数据", BoxExportDetailVO.class,
                        bean);
            } else {
                List<BoxHourAndDayExportDetailVO> bean = BeanUtils.toBean(list, BoxHourAndDayExportDetailVO.class);
                bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
                ExcelUtils.write(response, "母线插接箱电力分析.xlsx", "数据", BoxHourAndDayExportDetailVO.class,
                        bean);
            }
        }
    }


}