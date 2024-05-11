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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

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


//    @GetMapping("/env-page")
//    @Operation(summary = "获得机柜环境数据分页")
//    public CommonResult<PageResult<Object>> getEnvDataPage(HistoryDataPageReqVO pageReqVO) throws IOException {
//        PageResult<Object> pageResult = historyDataService.getEnvDataPage(pageReqVO);
//        return success(pageResult);
//    }
//
//    @GetMapping("/env-details")
//    @Operation(summary = "获得机柜历史数据详情")
//    public CommonResult<PageResult<Object>> getEnvDataDetails(EnvDataDetailsReqVO reqVO) throws IOException {
//        PageResult<Object> pageResult = historyDataService.getEnvDataDetails(reqVO);
//        return success(pageResult);
//    }


//    @GetMapping("/export-excel")
//    @Operation(summary = "导出pdu历史数据 Excel")
////    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
//    @OperateLog(type = EXPORT)
//    public void exportHistoryDataExcel(@Valid HistoryDataPageReqVO pageReqVO,
//              HttpServletResponse response) throws IOException {
//        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
//        List<PduHdaTotalRealtimeDO> list = historyDataService.getHistoryDataPage(pageReqVO).getList();
//        // 导出 Excel
//        ExcelUtils.write(response, "pdu历史数据.xls", "数据", HistoryDataRespVO.class,
//                        BeanUtils.toBean(list, HistoryDataRespVO.class));
//    }

}