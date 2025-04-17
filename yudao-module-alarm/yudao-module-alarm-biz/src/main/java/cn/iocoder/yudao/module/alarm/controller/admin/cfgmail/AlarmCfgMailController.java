package cn.iocoder.yudao.module.alarm.controller.admin.cfgmail;

import cn.iocoder.yudao.module.alarm.dal.dataobject.logrecord.AlarmLogRecordDO;
import org.springframework.util.CollectionUtils;
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
import cn.iocoder.yudao.module.alarm.controller.admin.cfgmail.vo.*;
import cn.iocoder.yudao.module.alarm.dal.dataobject.cfgmail.AlarmCfgMailDO;
import cn.iocoder.yudao.module.alarm.service.cfgmail.AlarmCfgMailService;

@Tag(name = "管理后台 - 告警邮件接收人配置")
@RestController
@RequestMapping("/alarm/cfg-mail")
@Validated
public class AlarmCfgMailController {

    @Resource
    private AlarmCfgMailService cfgMailService;

    @PostMapping("/save")
    @Operation(summary = "创建告警邮件接收人配置")
    @PreAuthorize("@ss.hasPermission('alarm:cfg-mail:save')")
    public CommonResult<Integer> saveCfgMail(@Valid @RequestBody AlarmCfgMailSaveReqVO saveReqVO) {
        return success(cfgMailService.saveCfgMail(saveReqVO));
    }

    @PostMapping("/batchSave")
    @Operation(summary = "批量保存邮件配置")
    @PreAuthorize("@ss.hasPermission('alarm:cfg-mail:save')")
    public CommonResult<Integer> batchSave(@Valid @RequestBody List<AlarmCfgMailSaveReqVO> saveReqVOS) {
        cfgMailService.deleteMailAll();
        if (!CollectionUtils.isEmpty(saveReqVOS)){
            cfgMailService.batchSave(saveReqVOS);
        }
        return success(saveReqVOS.size());
    }

    @PostMapping("/update")
    @Operation(summary = "更新告警邮件接收人配置")
    @PreAuthorize("@ss.hasPermission('alarm:cfg-mail:update')")
    public CommonResult<Boolean> updateCfgMail(@Valid @RequestBody AlarmCfgMailSaveReqVO updateReqVO) {
        cfgMailService.updateCfgMail(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除告警邮件接收人配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('alarm:cfg-mail:delete')")
    public CommonResult<Boolean> deleteCfgMail(@RequestParam("id") Integer id) {
        cfgMailService.deleteCfgMail(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得告警邮件接收人配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('alarm:cfg-mail:query')")
    public CommonResult<AlarmCfgMailRespVO> getCfgMail(@RequestParam("id") Integer id) {
        AlarmCfgMailDO cfgMail = cfgMailService.getCfgMail(id);
        return success(BeanUtils.toBean(cfgMail, AlarmCfgMailRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得告警邮件接收人配置分页")
    @PreAuthorize("@ss.hasPermission('alarm:cfg-mail:query')")
    public CommonResult<PageResult<AlarmCfgMailRespVO>> getCfgMailPage(@Valid AlarmCfgMailPageReqVO pageReqVO) {
        PageResult<AlarmCfgMailDO> pageResult = cfgMailService.getCfgMailPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AlarmCfgMailRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得邮箱账号列表")
    public CommonResult<List<AlarmCfgMailRespVO>> getMailAccountList() {
        List<AlarmCfgMailDO> list = cfgMailService.getMailConfig();
        return success(BeanUtils.toBean(list, AlarmCfgMailRespVO.class));
    }

    @PostMapping("/sendMail")
    @PermitAll
    public void sendMail(@RequestBody List<AlarmLogRecordDO> records){
        cfgMailService.sendMail(records);
    }

    /**
     * 播放声音
     */
    @GetMapping("/play")
    @PermitAll
    public void playVoice(){
        cfgMailService.playAudio();

    }


    @GetMapping("/export-excel")
    @Operation(summary = "导出告警邮件接收人配置 Excel")
    @PreAuthorize("@ss.hasPermission('alarm:cfg-mail:export')")
    @OperateLog(type = EXPORT)
    public void exportCfgMailExcel(@Valid AlarmCfgMailPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<AlarmCfgMailDO> list = cfgMailService.getCfgMailPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "告警邮件接收人配置.xls", "数据", AlarmCfgMailRespVO.class,
                        BeanUtils.toBean(list, AlarmCfgMailRespVO.class));
    }

}