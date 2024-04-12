package cn.iocoder.yudao.module.pdu.controller.admin.historydata;

import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.HistoryDataPageReqVO;
import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.HistoryDataRespVO;
import cn.iocoder.yudao.module.pdu.dal.dataobject.historydata.HistoryDataDO;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
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
import cn.iocoder.yudao.module.pdu.service.historydata.HistoryDataService;

@Tag(name = "管理后台 - pdu历史数据")
@RestController
@RequestMapping("/pdu/history-data")
@Validated
public class HistoryDataController {

    @Resource
    private HistoryDataService historyDataService;


    @GetMapping("/page")
    @Operation(summary = "获得pdu历史数据分页")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:query')")
    public CommonResult<PageResult<HistoryDataDO>> getHistoryDataPage(HistoryDataPageReqVO pageReqVO) throws IOException {
        PageResult<HistoryDataDO> pageResult = historyDataService.getHistoryDataPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, HistoryDataDO.class));
    }






    @GetMapping("/export-excel")
    @Operation(summary = "导出pdu历史数据 Excel")
//    @PreAuthorize("@ss.hasPermission('pdu:history-data:export')")
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