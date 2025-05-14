package cn.iocoder.yudao.module.alarm.controller.admin.cfgprompt;

import cn.iocoder.yudao.framework.common.enums.AlarmPromptType;
import cn.iocoder.yudao.framework.common.enums.AlarmStatusEnums;
import cn.iocoder.yudao.module.alarm.service.logrecord.AlarmLogRecordService;
import cn.iocoder.yudao.module.alarm.utils.audioplayer.AudioPlayer;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.annotation.security.PermitAll;
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
import cn.iocoder.yudao.module.alarm.controller.admin.cfgprompt.vo.*;
import cn.iocoder.yudao.module.alarm.dal.dataobject.cfgprompt.AlarmCfgPromptDO;
import cn.iocoder.yudao.module.alarm.service.cfgprompt.AlarmCfgPromptService;

@Tag(name = "管理后台 - 系统告警配置")
@RestController
@RequestMapping("/alarm/cfg-prompt")
@Validated
public class AlarmCfgPromptController {

    @Resource
    private AlarmCfgPromptService cfgPromptService;

    @Resource
    private AlarmCfgPromptService alarmCfgPromptService;

    @Resource
    private AlarmLogRecordService alarmLogRecordService;

    @Resource
    private AudioPlayer audioPlayer;

    @PostMapping("/save")
    @Operation(summary = "创建系统告警配置")
    @PreAuthorize("@ss.hasPermission('alarm:cfg-prompt:save')")
    public CommonResult<Integer> saveCfgPrompt(@Valid @RequestBody AlarmCfgPromptSaveReqVO saveReqVO) {
        return success(cfgPromptService.saveCfgPrompt(saveReqVO));
    }

    @PostMapping("/update")
    @Operation(summary = "更新系统告警配置")
    @PreAuthorize("@ss.hasPermission('alarm:cfg-prompt:update')")
    public CommonResult<Boolean> updateCfgPrompt(@RequestBody AlarmCfgPromptSaveReqVO updateReqVO) {
        if (updateReqVO.getVoiceAlarm() == null || updateReqVO.getMailAlarm() == null || updateReqVO.getSmsAlarm() == null) {
            throw new RuntimeException("缺少请求参数");
        }
        cfgPromptService.updateCfgPrompt(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除系统告警配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('alarm:cfg-prompt:delete')")
    public CommonResult<Boolean> deleteCfgPrompt(@RequestParam("id") Integer id) {
        cfgPromptService.deleteCfgPrompt(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得系统告警配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('alarm:cfg-prompt:query')")
    public CommonResult<AlarmCfgPromptRespVO> getCfgPrompt(@RequestParam("id") Integer id) {
        AlarmCfgPromptDO cfgPrompt = cfgPromptService.getCfgPrompt(id);
        return success(BeanUtils.toBean(cfgPrompt, AlarmCfgPromptRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得系统告警配置分页")
    @PreAuthorize("@ss.hasPermission('alarm:cfg-prompt:query')")
    public CommonResult<PageResult<AlarmCfgPromptRespVO>> getCfgPromptPage(@Valid AlarmCfgPromptPageReqVO pageReqVO) {
        PageResult<AlarmCfgPromptDO> pageResult = cfgPromptService.getCfgPromptPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AlarmCfgPromptRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得系统告警配置列表")
    @PreAuthorize("@ss.hasPermission('alarm:cfg-prompt:query')")
    public CommonResult<AlarmPromptConfig> getCfgPromptList(@Valid AlarmCfgPromptPageReqVO pageReqVO) {
        List<AlarmCfgPromptDO> cfgPromptList = cfgPromptService.getCfgPromptList();
        AlarmPromptConfig alarmPromptConfig = new AlarmPromptConfig();
        for (AlarmCfgPromptDO alarmCfgPromptDO : cfgPromptList) {
            if (AlarmPromptType.VOICE_ALARM.getCode().equals(alarmCfgPromptDO.getPromptType())) {
                alarmPromptConfig.setVoiceAlarm(alarmCfgPromptDO.getIsEnable());
            } else if (AlarmPromptType.EMAIL_ALARM.getCode().equals(alarmCfgPromptDO.getPromptType())){
                alarmPromptConfig.setMailAlarm(alarmCfgPromptDO.getIsEnable());
            } else if (AlarmPromptType.SMS_ALARM.getCode().equals(alarmCfgPromptDO.getPromptType())) {
                alarmPromptConfig.setSmsAlarm(alarmCfgPromptDO.getIsEnable());
            }
        }
        return success(alarmPromptConfig);
    }

    /**
     * 播放声音
     */
    @GetMapping("/play")
    @PermitAll
    public void playAudio(){
        Integer voiceEnable = alarmCfgPromptService.getCfgPromptByType(AlarmPromptType.VOICE_ALARM.getCode());
        Integer count = alarmLogRecordService.getCountByStatus(AlarmStatusEnums.UNTREATED.getStatus());
        if (voiceEnable == 1 && count > 0) {
            audioPlayer.playAudio();
        }
    }

    @GetMapping("/stop")
    @PermitAll
    public void stopAudio () {
        audioPlayer.stopAudio();
    }


    @GetMapping("/export-excel")
    @Operation(summary = "导出系统告警配置 Excel")
    @PreAuthorize("@ss.hasPermission('alarm:cfg-prompt:export')")
    @OperateLog(type = EXPORT)
    public void exportCfgPromptExcel(@Valid AlarmCfgPromptPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<AlarmCfgPromptDO> list = cfgPromptService.getCfgPromptPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "系统告警配置.xls", "数据", AlarmCfgPromptRespVO.class,
                        BeanUtils.toBean(list, AlarmCfgPromptRespVO.class));
    }

}