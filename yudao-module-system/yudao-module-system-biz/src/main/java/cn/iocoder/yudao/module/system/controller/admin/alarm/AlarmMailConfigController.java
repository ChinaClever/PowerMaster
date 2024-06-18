package cn.iocoder.yudao.module.system.controller.admin.alarm;


import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.mail.MailConfigPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.mail.MailConfigSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemAlarmRecord;
import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemMailAlarmConfig;
import cn.iocoder.yudao.module.system.service.alarm.MailConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 告警邮件配置")
@RestController
@RequestMapping("/system/alarm")
public class AlarmMailConfigController {

    @Resource
    private MailConfigService mailConfigService;

    @PostMapping("/mail/save")
    @Operation(summary = "保存邮件配置")
    @PreAuthorize("@ss.hasPermission('system:alarm:mail:save')")
    public CommonResult<Integer> saveMailConfig(@Valid @RequestBody MailConfigSaveReqVO saveReqVO) {
        return success(mailConfigService.saveMailConfig(saveReqVO));
    }


    @DeleteMapping("/mail/delete")
    @Operation(summary = "删除邮箱账号")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:alarm:mail:delete')")
    public CommonResult<Boolean> deleteMailAccount(@RequestParam Integer id) {
        mailConfigService.deleteMailAccount(id);
        return success(true);
    }


    @PostMapping("/mail/page")
    @Operation(summary = "获得邮箱账号分页")
    @PreAuthorize("@ss.hasPermission('system:alarm:mail:query')")
    public CommonResult<PageResult<SystemMailAlarmConfig>> getMailAccountPage(@Valid  @RequestBody MailConfigPageReqVO pageReqVO) {
        PageResult<SystemMailAlarmConfig> pageResult = mailConfigService.getMailAccountPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping({"/mail/list"})
    @Operation(summary = "获得邮箱账号列表")
    public CommonResult<List<SystemMailAlarmConfig>> getMailAccountList() {
        List<SystemMailAlarmConfig> list = mailConfigService.getMailConfig();
        return success(list);
    }

    @PostMapping("/sendMail")
    @PermitAll
    public void sendMail(@RequestBody SystemAlarmRecord record){
        mailConfigService.sendMail(record);
    }

    /**
     * 播放声音
     */
    @GetMapping("/play")
    @PermitAll
    public void playVoice(){
        mailConfigService.playAudio();

    }

}
