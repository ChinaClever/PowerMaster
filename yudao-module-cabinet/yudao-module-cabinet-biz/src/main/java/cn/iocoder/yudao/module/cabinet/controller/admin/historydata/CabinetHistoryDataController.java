package cn.iocoder.yudao.module.cabinet.controller.admin.historydata;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo.CabinetHistoryDataDetailsReqVO;
import cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo.CabinetHistoryDataPageReqVO;
import cn.iocoder.yudao.module.cabinet.service.historydata.CabinetHistoryDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

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

}