package cn.iocoder.yudao.module.rack.controller.admin.historydata;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rack.controller.admin.historydata.vo.RackHistoryDataDetailsReqVO;
import cn.iocoder.yudao.module.rack.controller.admin.historydata.vo.RackHistoryDataPageReqVO;
import cn.iocoder.yudao.module.rack.service.historydata.RackHistoryDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

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

    @GetMapping("/one-hour")
    @Operation(summary = "获得机架最近一小时插入的数据量")
    public CommonResult<Map<String, Object>> getOneHourSumData() throws IOException {
        Map<String, Object> map = rackHistoryDataService.getOneHourSumData();
        return success(map);
    }

}