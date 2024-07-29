package cn.iocoder.yudao.module.system.controller.admin.alarm;


import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.record.AlarmRecordPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.record.AlarmRecordRespVO;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.record.AlarmRecordSaveReqVO;
import cn.iocoder.yudao.module.system.service.alarm.AlarmRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 告警记录")
@RestController
@RequestMapping("/system/alarm/record")
public class AlarmRecordController {

    @Resource
    private AlarmRecordService alarmRecordService;

    @PostMapping("/save")
    @Operation(summary = "保存告警记录操作")
    @PreAuthorize("@ss.hasPermission('system:alarm:record:save')")
    public CommonResult<Integer> saveRecord(@Valid @RequestBody AlarmRecordSaveReqVO saveReqVO) {
        return success(alarmRecordService.updateRecord(saveReqVO));
    }


    @DeleteMapping("/delete")
    @Operation(summary = "删除记录")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('system:alarm:record:delete')")
    public CommonResult<Boolean> deleteRecord(@RequestParam List<Integer> ids) {
        alarmRecordService.deleteRecord(ids);
        return success(true);
    }


    @PostMapping("/page")
    @Operation(summary = "获得记录分页")
    @PreAuthorize("@ss.hasPermission('system:alarm:record:query')")
    public CommonResult<PageResult<AlarmRecordRespVO>> getRecordPage(@Valid @RequestBody AlarmRecordPageReqVO pageReqVO) {
        PageResult<AlarmRecordRespVO> pageResult = alarmRecordService.getRecordPage(pageReqVO);
        return success(pageResult);
    }

    @Operation(summary = "告警等级统计")
    @GetMapping("/level/count")
    public CommonResult<Map<Object,Object>> levelCount() {
        Map<Object,Object> result = alarmRecordService.levelCount();
        return success(result);
    }

}
