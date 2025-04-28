package cn.iocoder.yudao.module.alarm.controller.admin.logrecord;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
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
import cn.iocoder.yudao.module.alarm.controller.admin.logrecord.vo.*;
import cn.iocoder.yudao.module.alarm.dal.dataobject.logrecord.AlarmLogRecordDO;
import cn.iocoder.yudao.module.alarm.service.logrecord.AlarmLogRecordService;

@Tag(name = "管理后台 - 系统告警记录")
@RestController
@RequestMapping("/alarm/log-record")
@Validated
public class AlarmLogRecordController {

    @Resource
    private AlarmLogRecordService logRecordService;

    @PostMapping("/save")
    @Operation(summary = "创建系统告警记录")
    @PreAuthorize("@ss.hasPermission('alarm:log-record:save')")
    public CommonResult<Integer> saveLogRecord(@Valid @RequestBody AlarmLogRecordSaveReqVO createReqVO) {
        return success(logRecordService.saveLogRecord(createReqVO));
    }

    @PostMapping("/update")
    @Operation(summary = "更新系统告警记录")
    @PreAuthorize("@ss.hasPermission('alarm:log-record:update')")
    public CommonResult<Boolean> updateLogRecord(@RequestBody AlarmLogRecordSaveReqVO updateReqVO) {
        if (CollectionUtils.isAnyEmpty(updateReqVO.getIds()) || updateReqVO.getAlarmStatus() == null) {
            throw new IllegalArgumentException("ids 和 alarmStatus 不能为空");
        }
        logRecordService.updateLogRecord(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除系统告警记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('alarm:log-record:delete')")
    public CommonResult<Boolean> deleteLogRecord(@RequestParam("id") Integer id) {
        logRecordService.deleteLogRecord(id);
        return success(true);
    }

    @DeleteMapping("/delete-batch")
    @Operation(summary = "批量删除告警记录")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('alarm:log-record:delete')")
    public CommonResult<Boolean> deleteBatchIds(@RequestParam List<Integer> ids) {
        logRecordService.deleteBatchIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得系统告警记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('alarm:log-record:query')")
    public CommonResult<AlarmLogRecordRespVO> getLogRecord(@RequestParam("id") Integer id) {
        AlarmLogRecordDO logRecord = logRecordService.getLogRecord(id);
        return success(BeanUtils.toBean(logRecord, AlarmLogRecordRespVO.class));
    }

    @PostMapping("/page")
    @Operation(summary = "获得系统告警记录分页")
    @PreAuthorize("@ss.hasPermission('alarm:log-record:query')")
    public CommonResult<PageResult<AlarmLogRecordRespVO>> getLogRecordPage(@Valid @RequestBody AlarmLogRecordPageReqVO pageReqVO) {
        PageResult<AlarmLogRecordRespVO> pageResult = logRecordService.getLogRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AlarmLogRecordRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出系统告警记录 Excel")
    @PreAuthorize("@ss.hasPermission('alarm:log-record:export')")
    @OperateLog(type = EXPORT)
    public void exportLogRecordExcel(@Valid AlarmLogRecordPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<AlarmLogRecordRespVO> list = logRecordService.getLogRecordPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "系统告警记录.xls", "数据", AlarmLogRecordRespVO.class,
                        BeanUtils.toBean(list, AlarmLogRecordRespVO.class));
    }


    @Operation(summary = "告警等级统计")
    @GetMapping("/level/count")
    public CommonResult<AlarmLogRecordStatisticsVO> levelCount(@RequestParam(value = "roomId", required = false)@Parameter(description = "机房id") Integer roomId) {
        AlarmLogRecordStatisticsVO result = logRecordService.levelCount(roomId);
        return success(result);
    }
}