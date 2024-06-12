package cn.iocoder.yudao.module.rack.controller.admin.energyconsumption;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rack.controller.admin.energyconsumption.VO.RackEnergyConsumptionPageReqVO;
import cn.iocoder.yudao.module.rack.service.energyconsumption.RackEnergyConsumptionService;
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

@Tag(name = "管理后台 - 机架能耗数据")
@RestController
@RequestMapping("/rack/eq-data")
@Validated
public class RackEnergyConsumptionController {
    @Resource
    private RackEnergyConsumptionService rackEnergyConsumptionService;

    @GetMapping("/page")
    @Operation(summary = "获得机架电量数据分页")
    public CommonResult<PageResult<Object>> getEQDataPage(RackEnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = rackEnergyConsumptionService.getEQDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/bill-page")
    @Operation(summary = "获得机架电费数据分页")
    public CommonResult<PageResult<Object>> getBillDataPage(RackEnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = rackEnergyConsumptionService.getBillDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/details")
    @Operation(summary = "获得机架电量数据详情")
    public CommonResult<PageResult<Object>> getEQDataDetails(RackEnergyConsumptionPageReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = rackEnergyConsumptionService.getEQDataDetails(reqVO);
        return success(pageResult);
    }

    @GetMapping("/realtime-page")
    @Operation(summary = "获得机架实时电量数据分页")
    public CommonResult<PageResult<Object>> getRealtimeEQDataPage(RackEnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = rackEnergyConsumptionService.getRealtimeEQDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/new-data")
    @Operation(summary = "获得机架能耗最近一天、一周、一月插入的数据量")
    public CommonResult<Map<String, Object>> getNewData() throws IOException {
        Map<String, Object> map = rackEnergyConsumptionService.getNewData();
        return success(map);
    }

    @GetMapping("/one-day")
    @Operation(summary = "获得机架能耗最近一天插入的数据量")
    public CommonResult<Map<String, Object>> getOneDaySumData() throws IOException {
        Map<String, Object> map = rackEnergyConsumptionService.getOneDaySumData();
        return success(map);
    }
}
