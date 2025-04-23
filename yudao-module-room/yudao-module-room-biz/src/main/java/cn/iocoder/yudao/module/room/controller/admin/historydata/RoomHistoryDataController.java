package cn.iocoder.yudao.module.room.controller.admin.historydata;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.room.controller.admin.historydata.vo.*;
import cn.iocoder.yudao.module.room.service.historydata.RoomHistoryDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - 机房历史数据")
@RestController
@RequestMapping("/room/history-data")
@Validated
public class RoomHistoryDataController {

    @Resource
    private RoomHistoryDataService roomHistoryDataService;

    @PostMapping("/page")
    @Operation(summary = "获得机房历史数据分页")
    public CommonResult<PageResult<Object>> getHistoryDataPage(@RequestBody RoomHistoryDataPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = roomHistoryDataService.getHistoryDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/details")
    @Operation(summary = "获得机房历史数据详情")
    public CommonResult<PageResult<Object>> getHistoryDataDetails(RoomHistoryDataDetailsReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = roomHistoryDataService.getHistoryDataDetails(reqVO);
        return success(pageResult);
    }
    @GetMapping("/detailsExcel")
    @Operation(summary = "获得机房电力趋势分析数据详情导出")
    public void getHistoryDataDetailsExcel(RoomHistoryDataDetailsReqVO reqVO,HttpServletResponse response) throws IOException {
        PageResult<Object> pageResult = roomHistoryDataService.getHistoryDataDetails(reqVO);
        List<Object> list = pageResult.getList();
        if (Objects.equals("realtime",reqVO.getGranularity())) {
            if("a".equals(reqVO.getAbtotal())||"A".equals(reqVO.getAbtotal())){
                List<RoomPowerAnalysisResAVO> bean = BeanUtils.toBean(list, RoomPowerAnalysisResAVO.class);
                bean.stream().forEach(iter ->{iter.setLocation(reqVO.getNowAddress());});
                ExcelUtils.write(response, "机房电力趋势分析.xlsx", "数据", RoomPowerAnalysisResAVO.class,
                        bean);
            }else if("b".equals(reqVO.getAbtotal())||"B".equals(reqVO.getAbtotal())){
                List<RoomPowerAnalysisResBVO> bean = BeanUtils.toBean(list, RoomPowerAnalysisResBVO.class);
                bean.stream().forEach(iter ->{iter.setLocation(reqVO.getNowAddress());});
                ExcelUtils.write(response, "机房电力趋势分析.xlsx", "数据", RoomPowerAnalysisResBVO.class,
                        bean);
            }else if("total".equals(reqVO.getAbtotal())){
                List<RoomPowerAnalysisResTotalVO> bean = BeanUtils.toBean(list, RoomPowerAnalysisResTotalVO.class);
                bean.stream().forEach(iter ->{iter.setLocation(reqVO.getNowAddress());});
                ExcelUtils.write(response, "机房电力趋势分析.xlsx", "数据", RoomPowerAnalysisResTotalVO.class,
                        bean);
            }else{
                List<RoomPowerAnalysisResVO> bean = BeanUtils.toBean(list, RoomPowerAnalysisResVO.class);
                bean.stream().forEach(iter ->{iter.setLocation(reqVO.getNowAddress());});
                ExcelUtils.write(response, "机房电力趋势分析.xlsx", "数据", RoomPowerAnalysisResVO.class,
                        bean);
            }
        }else {
            if("a".equals(reqVO.getAbtotal())||"A".equals(reqVO.getAbtotal())){
                List<HourAndDayPageRespAVO> bean = BeanUtils.toBean(list, HourAndDayPageRespAVO.class);
                bean.stream().forEach(iter ->{iter.setLocation(reqVO.getNowAddress());});
                ExcelUtils.write(response, "机房电力趋势分析.xlsx", "数据", HourAndDayPageRespAVO.class,
                        bean);
            }else if("b".equals(reqVO.getAbtotal())||"B".equals(reqVO.getAbtotal())){
                List<HourAndDayPageRespBVO> bean = BeanUtils.toBean(list, HourAndDayPageRespBVO.class);
                bean.stream().forEach(iter ->{iter.setLocation(reqVO.getNowAddress());});
                ExcelUtils.write(response, "机房电力趋势分析.xlsx", "数据", HourAndDayPageRespBVO.class,
                        bean);
            }else if("total".equals(reqVO.getAbtotal())){
                List<HourAndDayPageRespTotalVO> bean = BeanUtils.toBean(list, HourAndDayPageRespTotalVO.class);
                bean.stream().forEach(iter ->{iter.setLocation(reqVO.getNowAddress());});
                ExcelUtils.write(response, "机房电力趋势分析.xlsx", "数据", HourAndDayPageRespTotalVO.class,
                        bean);
            }else {
                List<HourAndDayPageRespVO> bean = BeanUtils.toBean(list, HourAndDayPageRespVO.class);
                bean.stream().forEach(iter -> {
                    iter.setLocation(reqVO.getNowAddress());
                });
                ExcelUtils.write(response, "机房电力趋势分析.xlsx", "数据", HourAndDayPageRespVO.class,
                        bean);
            }
        }
    }
    @GetMapping("/new-data/{granularity}")
    @Operation(summary = "获得机房电力分析导航显示的插入的数据量")
    public CommonResult<Map<String, Object>> getNavNewData(@PathVariable("granularity") String granularity) throws IOException {
        Map<String, Object> map = roomHistoryDataService.getNavNewData(granularity);
        return success(map);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出机房电力历史数据 Excel")
    @OperateLog(type = EXPORT)
    public void exportHistoryDataExcel(RoomHistoryDataPageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(10000);
        List<Object> list = roomHistoryDataService.getHistoryDataPage(pageReqVO).getList();
        // 导出 Excel
        if (Objects.equals(pageReqVO.getGranularity(), "realtime")) {
            ExcelUtils.write(response, "机房电力历史数据.xlsx", "数据", RealtimePageRespVO.class,
                    BeanUtils.toBean(list, RealtimePageRespVO.class));
        } else {
            ExcelUtils.write(response, "机房电力历史数据.xlsx", "数据", HourAndDayPageRespVO.class,
                    BeanUtils.toBean(list, HourAndDayPageRespVO.class));
        }
    }

}