package cn.iocoder.yudao.module.bus.controller.admin.historydata;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.bus.controller.admin.historydata.vo.BusHistoryDataDetailsReqVO;
import cn.iocoder.yudao.module.bus.controller.admin.historydata.vo.BusHistoryDataPageReqVO;
import cn.iocoder.yudao.module.bus.controller.admin.historydata.vo.BoxPageRespVO;
import cn.iocoder.yudao.module.bus.service.historydata.BusHistoryDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

//    @GetMapping("/sensorId-max-value")
//    @Operation(summary = "获得母线环境数据传感器数量的最大值")
//    public CommonResult<Map> getSensorIdMaxValue() throws IOException {
//        Map resultList = historyDataService.getSensorIdMaxValue();
//        return success(resultList);
//    }

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

    @GetMapping("/export-excel")
    @Operation(summary = "导出母线历史数据 Excel")
//    @PreAuthorize("@ss.hasPermission('母线:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportHistoryDataExcel(@Valid BusHistoryDataPageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(5000);
        List<Object> list = busHistoryDataService.getBoxHistoryDataPage(pageReqVO).getList();

        // 导出 Excel
        ExcelUtils.write(response, "母线插接箱历史数据.xls", "数据", BoxPageRespVO.class,
                BeanUtils.toBean(list, BoxPageRespVO.class));
    }

}