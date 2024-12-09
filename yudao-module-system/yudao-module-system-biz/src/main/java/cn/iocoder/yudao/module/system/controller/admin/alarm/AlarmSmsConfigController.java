package cn.iocoder.yudao.module.system.controller.admin.alarm;


import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.sms.SmsConfigPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.sms.SmsConfigSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemSmsAlarmConfig;
import cn.iocoder.yudao.module.system.service.alarm.SmsConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 告警短信配置")
@RestController
@RequestMapping("/system/alarm")
public class AlarmSmsConfigController {

    @Resource
    private SmsConfigService smsConfigService;

    @PostMapping("/sms/save")
    @Operation(summary = "保存配置")
    @PreAuthorize("@ss.hasPermission('system:alarm:sms:save')")
    public CommonResult<Integer> saveSmsConfig(@Valid @RequestBody SmsConfigSaveReqVO saveReqVO) {
        return success(smsConfigService.saveSmsConfig(saveReqVO));
    }

    @PostMapping("/sms/batchSave")
    @Operation(summary = "保存配置")
    @PreAuthorize("@ss.hasPermission('system:alarm:sms:save')")
    public CommonResult<Integer> batchSave(@Valid @RequestBody List<SmsConfigSaveReqVO> saveReqVOS) {
        if (!CollectionUtils.isEmpty(saveReqVOS)){
            saveReqVOS.forEach(smsConfigSaveReqVO -> smsConfigService.saveSmsConfig(smsConfigSaveReqVO));
        }
        return success(saveReqVOS.size());
    }


    @DeleteMapping("/sms/delete")
    @Operation(summary = "删除手机号")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:alarm:sms:delete')")
    public CommonResult<Boolean> deletePhone(@RequestParam Integer id) {
        smsConfigService.deleteSmsAccount(id);
        return success(true);
    }


    @PostMapping("/sms/page")
    @Operation(summary = "获得手机账号分页")
    @PreAuthorize("@ss.hasPermission('system:alarm:sms:query')")
    public CommonResult<PageResult<SystemSmsAlarmConfig>> getPhonetPage(@Valid  @RequestBody SmsConfigPageReqVO pageReqVO) {
        PageResult<SystemSmsAlarmConfig> pageResult = smsConfigService.getSmsAccountPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping({"/sms/list"})
    @Operation(summary = "获得手机账号列表")
    public CommonResult<List<SystemSmsAlarmConfig>> getPhoneList() {
        List<SystemSmsAlarmConfig> list = smsConfigService.getSmsConfig();
        return success(list);
    }


}
