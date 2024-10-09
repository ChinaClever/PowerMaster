package cn.iocoder.yudao.module.aisle.controller.admin.historydata;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.aisle.controller.admin.historydata.vo.*;
import cn.iocoder.yudao.module.aisle.service.historydata.AisleHistoryDataService;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - 柜列历史数据")
@RestController
@RequestMapping("/aisle/history-data")
@Validated
public class AisleHistoryDataController {

    @Resource
    private AisleHistoryDataService aisleHistoryDataService;

    @GetMapping("/page")
    @Operation(summary = "获得柜列历史数据分页")
    public CommonResult<PageResult<Object>> getHistoryDataPage(AisleHistoryDataPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = aisleHistoryDataService.getHistoryDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/details")
    @Operation(summary = "获得柜列历史数据详情")
    public CommonResult<PageResult<Object>> getHistoryDataDetails(AisleHistoryDataDetailsReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = aisleHistoryDataService.getHistoryDataDetails(reqVO);
        return success(pageResult);
    }
    @GetMapping("/details-export-excel")
    @Operation(summary = "导出柜列电力分析历史数据 Excel")
    @OperateLog(type = EXPORT)
    public void exportDetaHistoryDataExcel(AisleHistoryDataDetailsReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = aisleHistoryDataService.getHistoryDataDetails(pageReqVO).getList();
        aisleHistoryDataService.getNewDetailsExcelList(list);

        // 导出 Excel
        if (Objects.equals(pageReqVO.getGranularity(), "realtime")) {
            if(Objects.equals(pageReqVO.getAbtotal(), "a")){
                ExcelUtils.write(response, "机柜列电力分析历史数据.xlsx", "数据", DetailHistoryDataExcelExportA.class,
                        BeanUtils.toBean(list, DetailHistoryDataExcelExportA.class));
            } else if (Objects.equals(pageReqVO.getAbtotal(), "b")) {
                ExcelUtils.write(response, "机柜列电力分析历史数据.xlsx", "数据", DetailHistoryDataExcelExportB.class,
                        BeanUtils.toBean(list, DetailHistoryDataExcelExportB.class));
            }else{
                ExcelUtils.write(response, "机柜列电力分析历史数据.xlsx", "数据", DetailHistoryDataExcelExport.class,
                        BeanUtils.toBean(list, DetailHistoryDataExcelExport.class));
            }

        } else {
            if(Objects.equals(pageReqVO.getAbtotal(), "total")){
                ExcelUtils.write(response, "机柜列电力分析历史数据.xlsx", "数据", HourAndDayDetailHistoryDataExcelExport.class,
                        BeanUtils.toBean(list, HourAndDayDetailHistoryDataExcelExport.class));
            } else if (Objects.equals(pageReqVO.getAbtotal(), "a")) {
                ExcelUtils.write(response, "机柜列电力分析历史数据.xlsx", "数据", HourAndDayDetailHistoryDataExcelExportA.class,
                        BeanUtils.toBean(list, HourAndDayDetailHistoryDataExcelExportA.class));
            }
            else{
                ExcelUtils.write(response, "机柜列电力分析历史数据.xlsx", "数据", HourAndDayDetailHistoryDataExcelExportB.class,
                        BeanUtils.toBean(list, HourAndDayDetailHistoryDataExcelExportB.class));
            }

        }
    }

    @GetMapping("/new-data/{granularity}")
    @Operation(summary = "获得柜列电力分析导航显示的插入的数据量")
    public CommonResult<Map<String, Object>> getNavNewData(@PathVariable("granularity") String granularity) throws IOException {
        Map<String, Object> map = aisleHistoryDataService.getNavNewData(granularity);
        return success(map);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出柜列电力历史数据 Excel")
    @OperateLog(type = EXPORT)
    public void exportHistoryDataExcel(AisleHistoryDataPageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = aisleHistoryDataService.getHistoryDataPage(pageReqVO).getList();

        aisleHistoryDataService.getNewExcelList(list);

        // 导出 Excel
        if (Objects.equals(pageReqVO.getGranularity(), "realtime")) {
            ExcelUtils.write(response, "柜列电力历史数据.xlsx", "数据", RealtimePageRespVO.class,
                    BeanUtils.toBean(list, RealtimePageRespVO.class));
        } else {
            ExcelUtils.write(response, "柜列电力历史数据.xlsx", "数据", HourAndDayPageRespVO.class,
                    BeanUtils.toBean(list, HourAndDayPageRespVO.class));
        }
    }

}