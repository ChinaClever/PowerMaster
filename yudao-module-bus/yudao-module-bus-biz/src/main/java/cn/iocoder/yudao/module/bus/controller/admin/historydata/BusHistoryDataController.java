package cn.iocoder.yudao.module.bus.controller.admin.historydata;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.bus.controller.admin.historydata.vo.*;
import cn.iocoder.yudao.module.bus.service.historydata.BusHistoryDataService;
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

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

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
        // 导出 Excel
        if (Objects.equals(pageReqVO.getGranularity(), "realtime")){
            ExcelUtils.write(response, "母线插接箱历史数据.xlsx", "数据", BoxRealtimePageRespVO.class,
                    BeanUtils.toBean(list, BoxRealtimePageRespVO.class));
        }else{
            ExcelUtils.write(response, "母线插接箱历史数据.xlsx", "数据", BoxHourAndDayPageRespVO.class,
                    BeanUtils.toBean(list, BoxHourAndDayPageRespVO.class));
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
            ExcelUtils.write(response, "母线始端箱历史数据.xlsx", "数据", BusRealtimePageRespVO.class,
                    BeanUtils.toBean(list, BusRealtimePageRespVO.class));
        } else {
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
        // 导出 Excel
        if (Objects.equals(pageReqVO.getGranularity(), "realtime")) {
            ExcelUtils.write(response, "插接箱环境历史数据.xlsx", "数据", BoxEnvRealtimePageRespVO.class,
                    BeanUtils.toBean(list, BoxEnvRealtimePageRespVO.class));
        } else {
            ExcelUtils.write(response, "插接箱环境历史数据.xlsx", "数据", BoxEnvHourAndDayPageRespVO.class,
                    BeanUtils.toBean(list, BoxEnvHourAndDayPageRespVO.class));
        }
    }


}