package cn.iocoder.yudao.module.alarm.controller.admin.cfgsms;

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
import cn.iocoder.yudao.module.alarm.controller.admin.cfgsms.vo.*;
import cn.iocoder.yudao.module.alarm.dal.dataobject.cfgsms.AlarmCfgSmsDO;
import cn.iocoder.yudao.module.alarm.service.cfgsms.AlarmCfgSmsService;

@Tag(name = "管理后台 - 告警短信接收人配置")
@RestController
@RequestMapping("/alarm/cfg-sms")
@Validated
public class AlarmCfgSmsController {

    @Resource
    private AlarmCfgSmsService cfgSmsService;

    @PostMapping("/save")
    @Operation(summary = "创建告警短信接收人配置")
    @PreAuthorize("@ss.hasPermission('alarm:cfg-sms:save')")
    public CommonResult<Integer> saveCfgSms(@Valid @RequestBody AlarmCfgSmsSaveReqVO saveReqVOS) {
        return success(cfgSmsService.saveCfgSms(saveReqVOS));
    }

    @PostMapping("/batchSave")
    @Operation(summary = "保存告警短信接收人配置")
    @PreAuthorize("@ss.hasPermission('alarm:cfg-sms:save')")
    public CommonResult<Integer> batchSave(@Valid @RequestBody List<AlarmCfgSmsPageReqVO> saveReqVOS) {
        cfgSmsService.deleteSmsAll();
        if (!CollectionUtils.isEmpty(saveReqVOS)){
            cfgSmsService.batchSave(saveReqVOS);
        }
        return success(saveReqVOS.size());
    }

    @PostMapping("/update")
    @Operation(summary = "更新告警短信接收人配置")
    @PreAuthorize("@ss.hasPermission('alarm:cfg-sms:update')")
    public CommonResult<Boolean> updateCfgSms(@Valid @RequestBody AlarmCfgSmsSaveReqVO updateReqVO) {
        cfgSmsService.updateCfgSms(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除告警短信接收人手机号")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('alarm:cfg-sms:delete')")
    public CommonResult<Boolean> deleteCfgSms(@RequestParam("id") Integer id) {
        cfgSmsService.deleteCfgSms(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得告警短信接收人配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('alarm:cfg-sms:query')")
    public CommonResult<AlarmCfgSmsRespVO> getCfgSms(@RequestParam("id") Integer id) {
        AlarmCfgSmsDO cfgSms = cfgSmsService.getCfgSms(id);
        return success(BeanUtils.toBean(cfgSms, AlarmCfgSmsRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得告警短信接收人配置分页")
    @PreAuthorize("@ss.hasPermission('alarm:cfg-sms:query')")
    public CommonResult<PageResult<AlarmCfgSmsRespVO>> getCfgSmsPage(@Valid AlarmCfgSmsPageReqVO pageReqVO) {
        PageResult<AlarmCfgSmsDO> pageResult = cfgSmsService.getCfgSmsPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AlarmCfgSmsRespVO.class));
    }


    @GetMapping({"/list"})
    @Operation(summary = "获得手机账号列表")
    public CommonResult<List<AlarmCfgSmsRespVO>> getPhoneList() {
        List<AlarmCfgSmsDO> list = cfgSmsService.getPhoneList();
        return success(BeanUtils.toBean(list, AlarmCfgSmsRespVO.class));
    }

    @PostMapping("/sendSms")
    @PermitAll
    public void sendSms(@RequestBody AlarmLogRecordDO record){
        cfgSmsService.sendSms(record);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出告警短信接收人配置 Excel")
    @PreAuthorize("@ss.hasPermission('alarm:cfg-sms:export')")
    @OperateLog(type = EXPORT)
    public void exportCfgSmsExcel(@Valid AlarmCfgSmsPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<AlarmCfgSmsDO> list = cfgSmsService.getCfgSmsPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "告警短信接收人配置.xls", "数据", AlarmCfgSmsRespVO.class,
                        BeanUtils.toBean(list, AlarmCfgSmsRespVO.class));
    }




}