package cn.iocoder.yudao.module.rack.controller.admin.historydata;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.rack.controller.admin.historydata.vo.*;
import cn.iocoder.yudao.module.rack.service.historydata.RackHistoryDataService;
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

@Tag(name = "管理后台 - 机架历史数据")
@RestController
@RequestMapping("/rack/history-data")
@Validated
public class RackHistoryDataController {

    @Resource
    private RackHistoryDataService rackHistoryDataService;

    @GetMapping("/page")
    @Operation(summary = "获得机架历史数据分页")
    public CommonResult<PageResult<Object>> getHistoryDataPage(RackHistoryDataPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = rackHistoryDataService.getHistoryDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/details")
    @Operation(summary = "获得机架历史数据详情")
    public CommonResult<PageResult<Object>> getHistoryDataDetails(RackHistoryDataDetailsReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = rackHistoryDataService.getHistoryDataDetails(reqVO);
        return success(pageResult);
    }

    @GetMapping("/detailsExcel")
    @Operation(summary = "获得机架历史数据详情")
    public void getHistoryDataDetailsExcel(RackHistoryDataDetailsReqVO reqVO, HttpServletResponse response) throws IOException {
        PageResult<Object> pageResult = rackHistoryDataService.getHistoryDataDetails(reqVO);
        if (Objects.equals("realtime", reqVO.getGranularity())) {
            List<RackDetailsExcelVO> bean = BeanUtils.toBean(pageResult.getList(), RackDetailsExcelVO.class);
            bean.stream().forEach(item -> {item.setLocation(reqVO.getNowAddress());});
            ExcelUtils.write(response, "机架电力分析数据.xlsx", "数据", RackDetailsExcelVO.class,
                    bean);
        } else {
            List<HistoryDataDetailsExportDetailsVO> bean = BeanUtils.toBean(pageResult.getList(), HistoryDataDetailsExportDetailsVO.class);
            bean.stream().forEach(item -> {item.setLocation(reqVO.getNowAddress());});
            ExcelUtils.write(response, "机架电力分析数据.xlsx", "数据", HistoryDataDetailsExportDetailsVO.class,
                    bean);
        }
    }

    @GetMapping("/new-data/{granularity}")
    @Operation(summary = "获得机架电力分析导航显示的插入的数据量")
    public CommonResult<Map<String, Object>> getNavNewData(@PathVariable("granularity") String granularity) throws IOException {
        Map<String, Object> map = rackHistoryDataService.getNavNewData(granularity);
        return success(map);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出机架电力历史数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportHistoryDataExcel(RackHistoryDataPageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = rackHistoryDataService.getHistoryDataPage(pageReqVO).getList();
        rackHistoryDataService.getNewList(list);
        // 导出 Excel
        if (Objects.equals(pageReqVO.getGranularity(), "realtime")) {
            ExcelUtils.write(response, "机架电力历史数据.xlsx", "数据", RealtimePageRespVO.class,
                    BeanUtils.toBean(list, RealtimePageRespVO.class));
        } else {
            ExcelUtils.write(response, "机架电力历史数据.xlsx", "数据", HourAndDayPageRespVO.class,
                    BeanUtils.toBean(list, HourAndDayPageRespVO.class));
        }
    }

}