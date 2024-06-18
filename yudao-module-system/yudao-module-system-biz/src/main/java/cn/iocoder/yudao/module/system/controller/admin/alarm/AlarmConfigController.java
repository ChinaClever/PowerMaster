package cn.iocoder.yudao.module.system.controller.admin.alarm;


import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.config.ConfigSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemAlarmConfig;
import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemMailAlarmConfig;
import cn.iocoder.yudao.module.system.service.alarm.AlarmConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 告警配置")
@RestController
@RequestMapping("/system/alarm/config")
public class AlarmConfigController {

    @Resource
    private AlarmConfigService alarmConfigService;

    @PostMapping("/save")
    @Operation(summary = "保存配置")
    @PreAuthorize("@ss.hasPermission('system:alarm:config:save')")
    public CommonResult<Integer> saveConfig(@Valid @RequestBody ConfigSaveReqVO saveReqVO) {
        return success(alarmConfigService.saveConfig(saveReqVO));
    }


    @GetMapping({"/get"})
    @Operation(summary = "获得配置")
    public CommonResult<SystemAlarmConfig> getConfig() {
        SystemAlarmConfig  config = alarmConfigService.getConfig();
        return success(config);
    }

}
