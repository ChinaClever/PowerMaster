package cn.iocoder.yudao.module.cabinet.controller.admin.historydata;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo.*;
import cn.iocoder.yudao.module.cabinet.service.historydata.CabinetHistoryDataService;
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
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - 机柜历史数据")
@RestController
@RequestMapping("/cabinet/history-data")
@Validated
public class CabinetHistoryDataController {

    @Resource
    private CabinetHistoryDataService cabinetHistoryDataService;

    @GetMapping("/page")
    @Operation(summary = "获得机柜历史数据分页")
    public CommonResult<PageResult<Object>> getHistoryDataPage(CabinetHistoryDataPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = cabinetHistoryDataService.getHistoryDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/details")
    @Operation(summary = "获得机柜历史数据详情")
    public CommonResult<PageResult<Object>> getHistoryDataDetails(CabinetHistoryDataDetailsReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = cabinetHistoryDataService.getHistoryDataDetails(reqVO);
        return success(pageResult);
    }

    @GetMapping("/new-data/{granularity}")
    @Operation(summary = "获得机柜电力分析导航显示的插入的数据量")
    public CommonResult<Map<String, Object>> getNavNewData(@PathVariable("granularity") String granularity) throws IOException {
        Map<String, Object> map = cabinetHistoryDataService.getNavNewData(granularity);
        return success(map);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出机柜电力历史数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportHistoryDataExcel(CabinetHistoryDataPageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = cabinetHistoryDataService.getHistoryDataPage(pageReqVO).getList();
        cabinetHistoryDataService.getNewHistoryList(list);
        // 导出 Excel
        if (Objects.equals(pageReqVO.getGranularity(), "realtime")) {
            ExcelUtils.write(response, "机柜电力历史数据.xlsx", "数据", RealtimePageRespVO.class,
                    BeanUtils.toBean(list, RealtimePageRespVO.class));
        } else {
            ExcelUtils.write(response, "机柜电力历史数据.xlsx", "数据", HourAndDayPageRespVO.class,
                    BeanUtils.toBean(list, HourAndDayPageRespVO.class));
        }
    }

    @GetMapping("/details-export-excel")
    @Operation(summary = "导出机柜电力分析历史数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportDetailsHistoryDataExcel(CabinetHistoryDataDetailsReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        PageResult<Object> historyDataDetails = cabinetHistoryDataService.getHistoryDataDetails(pageReqVO);
        List<Object> list=null;
        if (historyDataDetails != null) {
            list = historyDataDetails.getList();
        }else{
            list = new ArrayList<>();
        }
        cabinetHistoryDataService.getNewDetailHistoryList(list);
        // 导出 Excel
        if (Objects.equals(pageReqVO.getGranularity(), "realtime")) {
            if(Objects.equals(pageReqVO.getAbtotal(), "a")){
                List<DetailHistoryDataExcelExportA> bean = BeanUtils.toBean(list, DetailHistoryDataExcelExportA.class);
                bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
                ExcelUtils.write(response, "机柜电力历史数据.xlsx", "数据", DetailHistoryDataExcelExportA.class,
                        bean);
            } else if (Objects.equals(pageReqVO.getAbtotal(), "b")) {
                List<DetailHistoryDataExcelExportB> bean = BeanUtils.toBean(list, DetailHistoryDataExcelExportB.class);
                bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
                ExcelUtils.write(response, "机柜电力历史数据.xlsx", "数据", DetailHistoryDataExcelExportB.class,
                        bean);
            }else{
                List<DetailHistoryDataExcelExport> bean = BeanUtils.toBean(list, DetailHistoryDataExcelExport.class);
                bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
                ExcelUtils.write(response, "机柜电力历史数据.xlsx", "数据", DetailHistoryDataExcelExport.class,
                        bean );
            }

        } else {
            if(Objects.equals(pageReqVO.getAbtotal(), "total")){
                List<HourAndDayDetailHistoryDataExcelExport> bean = BeanUtils.toBean(list, HourAndDayDetailHistoryDataExcelExport.class);
                bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
                ExcelUtils.write(response, "机柜电力历史数据.xlsx", "数据", HourAndDayDetailHistoryDataExcelExport.class,
                        bean );
            } else if (Objects.equals(pageReqVO.getAbtotal(), "a")) {
                List<HourAndDayDetailHistoryDataExcelExportA> bean = BeanUtils.toBean(list, HourAndDayDetailHistoryDataExcelExportA.class);
                bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
                ExcelUtils.write(response, "机柜电力历史数据.xlsx", "数据", HourAndDayDetailHistoryDataExcelExportA.class,
                        bean );
            }
            else{
                List<HourAndDayDetailHistoryDataExcelExportB> bean = BeanUtils.toBean(list, HourAndDayDetailHistoryDataExcelExportB.class);
                bean.stream().forEach(iter ->{iter.setLocation(pageReqVO.getNowAddress());});
                ExcelUtils.write(response, "机柜电力历史数据.xlsx", "数据", HourAndDayDetailHistoryDataExcelExportB.class,
                        bean );
            }

        }
    }

    @PostMapping("/pageEnv")
    @Operation(summary = "获得机柜历史数据分页")
    public CommonResult<PageResult<CabinetEnvResVO>> getHistoryDataPageEnv(@RequestBody CabinetHistoryDataPageReqVO pageReqVO) throws IOException {
        PageResult<CabinetEnvResVO> pageResult = cabinetHistoryDataService.getHistoryDataPageEnv(pageReqVO);
        return success(pageResult);
    }

}