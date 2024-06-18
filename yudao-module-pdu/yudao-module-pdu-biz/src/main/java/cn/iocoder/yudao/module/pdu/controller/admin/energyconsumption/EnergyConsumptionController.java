package cn.iocoder.yudao.module.pdu.controller.admin.energyconsumption;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pdu.controller.admin.energyconsumption.VO.EnergyConsumptionPageReqVO;
import cn.iocoder.yudao.module.pdu.service.energyconsumption.EnergyConsumptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - pdu能耗数据")
@RestController
@RequestMapping("/pdu/eq-data")
@Validated
public class EnergyConsumptionController {
    @Resource
    private EnergyConsumptionService energyConsumptionService;

    @GetMapping("/page")
    @Operation(summary = "获得pdu电量数据分页")
    public CommonResult<PageResult<Object>> getEQDataPage(EnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = energyConsumptionService.getEQDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/bill-page")
    @Operation(summary = "获得pdu电费数据分页")
    public CommonResult<PageResult<Object>> getBillDataPage(EnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = energyConsumptionService.getBillDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/details")
    @Operation(summary = "获得pdu电量数据详情")
    public CommonResult<PageResult<Object>> getEQDataDetails(EnergyConsumptionPageReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = energyConsumptionService.getEQDataDetails(reqVO);
        return success(pageResult);
    }

    @GetMapping("/outlets-details")
    @Operation(summary = "获得pdu电量数据详情")
    public CommonResult<List<Object>> getOutletsEQData(EnergyConsumptionPageReqVO reqVO) throws IOException {
        List<Object> list = energyConsumptionService.getOutletsEQData(reqVO);
        return success(list);
    }

    @GetMapping("/realtime-page")
    @Operation(summary = "获得pdu电量实时数据分页")
    public CommonResult<PageResult<Object>> getRealtimeEQDataPage(EnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = energyConsumptionService.getRealtimeEQDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/new-data")
    @Operation(summary = "获得pdu能耗最近一天、一周、一月插入的数据量")
    public CommonResult<Map<String, Object>> getNewData() throws IOException {
        Map<String, Object> map = energyConsumptionService.getNewData();
        return success(map);
    }

    @GetMapping("/one-day")
    @Operation(summary = "获得pdu能耗最近一天插入的数据量")
    public CommonResult<Map<String, Object>> getOneDaySumData() throws IOException {
        Map<String, Object> map = energyConsumptionService.getOneDaySumData();
        return success(map);
    }
}
