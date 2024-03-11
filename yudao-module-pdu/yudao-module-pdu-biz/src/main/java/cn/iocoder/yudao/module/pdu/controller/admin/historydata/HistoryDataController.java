package cn.iocoder.yudao.module.pdu.controller.admin.historydata;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.*;
import cn.iocoder.yudao.module.pdu.dal.dataobject.historydata.HistoryDataDO;
import cn.iocoder.yudao.module.pdu.service.historydata.HistoryDataService;

@Tag(name = "管理后台 - pdu历史数据")
@RestController
@RequestMapping("/pdu/history-data")
@Validated
public class HistoryDataController {

    @Resource
    private HistoryDataService historyDataService;

    @PostMapping("/create")
    @Operation(summary = "创建pdu历史数据")
    @PreAuthorize("@ss.hasPermission('pdu:history-data:create')")
    public CommonResult<Long> createHistoryData(@Valid @RequestBody HistoryDataSaveReqVO createReqVO) {
        return success(historyDataService.createHistoryData(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新pdu历史数据")
    @PreAuthorize("@ss.hasPermission('pdu:history-data:update')")
    public CommonResult<Boolean> updateHistoryData(@Valid @RequestBody HistoryDataSaveReqVO updateReqVO) {
        historyDataService.updateHistoryData(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除pdu历史数据")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('pdu:history-data:delete')")
    public CommonResult<Boolean> deleteHistoryData(@RequestParam("id") Long id) {
        historyDataService.deleteHistoryData(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得pdu历史数据")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('pdu:history-data:query')")
    public CommonResult<HistoryDataRespVO> getHistoryData(@RequestParam("id") Long id) {
        HistoryDataDO historyData = historyDataService.getHistoryData(id);
        return success(BeanUtils.toBean(historyData, HistoryDataRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得pdu历史数据分页")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:query')")
    public CommonResult<PageResult<HistoryDataRespVO>> getHistoryDataPage(HistoryDataPageReqVO pageReqVO) {
        PageResult<HistoryDataDO> pageResult = historyDataService.getHistoryDataPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, HistoryDataRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出pdu历史数据 Excel")
    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
    @OperateLog(type = EXPORT)
    public void exportHistoryDataExcel(@Valid HistoryDataPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<HistoryDataDO> list = historyDataService.getHistoryDataPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "pdu历史数据.xls", "数据", HistoryDataRespVO.class,
                        BeanUtils.toBean(list, HistoryDataRespVO.class));
    }

}