package cn.iocoder.yudao.module.cabinet.controller.admin.energyconsumption;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cabinet.controller.admin.energyconsumption.VO.CabinetEnergyConsumptionPageReqVO;
import cn.iocoder.yudao.module.cabinet.service.energyconsumption.CabinetEnergyConsumptionService;
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

@Tag(name = "管理后台 - 机柜能耗数据")
@RestController
@RequestMapping("/cabinet/eq-data")
@Validated
public class CabinetEnergyConsumptionController {
    @Resource
    private CabinetEnergyConsumptionService cabinetEnergyConsumptionService;

    @GetMapping("/page")
    @Operation(summary = "获得机柜电量数据分页")
    public CommonResult<PageResult<Object>> getEQDataPage(CabinetEnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = cabinetEnergyConsumptionService.getEQDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/bill-page")
    @Operation(summary = "获得机柜电费数据分页")
    public CommonResult<PageResult<Object>> getBillDataPage(CabinetEnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = cabinetEnergyConsumptionService.getBillDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/details")
    @Operation(summary = "获得机柜电量数据详情")
    public CommonResult<PageResult<Object>> getEQDataDetails(CabinetEnergyConsumptionPageReqVO reqVO) throws IOException {
        PageResult<Object> pageResult = cabinetEnergyConsumptionService.getEQDataDetails(reqVO);
        return success(pageResult);
    }

    @GetMapping("/realtime-page")
    @Operation(summary = "获得机柜实时电量数据分页")
    public CommonResult<PageResult<Object>> getRealtimeEQDataPage(CabinetEnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = cabinetEnergyConsumptionService.getRealtimeEQDataPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/new-data")
    @Operation(summary = "获得机柜能耗最近一天、一周、一月插入的数据量")
    public CommonResult<Map<String, Object>> getNewData() throws IOException {
        Map<String, Object> map = cabinetEnergyConsumptionService.getNewData();
        return success(map);
    }

    @GetMapping("/one-day")
    @Operation(summary = "获得机柜能耗最近一天插入的数据量")
    public CommonResult<Map<String, Object>> getOneDaySumData() throws IOException {
        Map<String, Object> map = cabinetEnergyConsumptionService.getOneDaySumData();
        return success(map);
    }
}
