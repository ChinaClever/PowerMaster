package cn.iocoder.yudao.module.pdu.controller.admin.mqconfig;

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

import cn.iocoder.yudao.module.pdu.controller.admin.mqconfig.vo.*;
import cn.iocoder.yudao.module.pdu.dal.dataobject.mqconfig.MqConfigDO;
import cn.iocoder.yudao.module.pdu.service.mqconfig.MqConfigService;

@Tag(name = "管理后台 - 消息队列系统配置")
@RestController
@RequestMapping("/sys/mq-config")
@Validated
public class MqConfigController {

    @Resource
    private MqConfigService mqConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建消息队列系统配置")
    @PreAuthorize("@ss.hasPermission('sys:mq-config:create')")
    public CommonResult<Integer> createMqConfig(@Valid @RequestBody MqConfigSaveReqVO createReqVO) {
        return success(mqConfigService.createMqConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新消息队列系统配置")
    @PreAuthorize("@ss.hasPermission('sys:mq-config:update')")
    public CommonResult<Boolean> updateMqConfig(@Valid @RequestBody MqConfigSaveReqVO updateReqVO) {
        mqConfigService.updateMqConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除消息队列系统配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sys:mq-config:delete')")
    public CommonResult<Boolean> deleteMqConfig(@RequestParam("id") Integer id) {
        mqConfigService.deleteMqConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得消息队列系统配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('sys:mq-config:query')")
    public CommonResult<MqConfigRespVO> getMqConfig(@RequestParam("id") Integer id) {
        MqConfigDO mqConfig = mqConfigService.getMqConfig(id);
        return success(BeanUtils.toBean(mqConfig, MqConfigRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得消息队列系统配置分页")
    @PreAuthorize("@ss.hasPermission('sys:mq-config:query')")
    public CommonResult<PageResult<MqConfigRespVO>> getMqConfigPage(@Valid MqConfigPageReqVO pageReqVO) {
        PageResult<MqConfigDO> pageResult = mqConfigService.getMqConfigPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, MqConfigRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出消息队列系统配置 Excel")
    @PreAuthorize("@ss.hasPermission('sys:mq-config:export')")
    @OperateLog(type = EXPORT)
    public void exportMqConfigExcel(@Valid MqConfigPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MqConfigDO> list = mqConfigService.getMqConfigPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "消息队列系统配置.xls", "数据", MqConfigRespVO.class,
                        BeanUtils.toBean(list, MqConfigRespVO.class));
    }

}